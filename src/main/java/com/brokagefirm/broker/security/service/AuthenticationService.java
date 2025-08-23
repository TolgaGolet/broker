package com.brokagefirm.broker.security.service;

import com.brokagefirm.broker.entity.BrokerUser;
import com.brokagefirm.broker.entity.UserToken;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.exception.enums.GenericExceptionMessages;
import com.brokagefirm.broker.repository.BrokerUserRepository;
import com.brokagefirm.broker.repository.UserTokenRepository;
import com.brokagefirm.broker.security.api.request.AuthenticationRequest;
import com.brokagefirm.broker.security.api.request.RegisterRequest;
import com.brokagefirm.broker.security.api.response.AuthenticationResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.brokagefirm.broker.entity.audit.AuditAwareImpl.SYSTEM_USER;
import static com.brokagefirm.broker.security.SecurityParams.BEARER_PREFIX;

@Profile("!disabled-security")
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final BrokerUserRepository brokerUserRepository;
    private final UserTokenRepository userTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private static final Integer MAX_USER_TOKEN_COUNT = 5;

    public AuthenticationResponse register(RegisterRequest request) throws BrokerGenericException {
        if (brokerUserRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BrokerGenericException(GenericExceptionMessages.USERNAME_ALREADY_EXISTS.getMessage());
        }
        if (request.getUsername().equalsIgnoreCase(SYSTEM_USER)) {
            throw new BrokerGenericException(GenericExceptionMessages.SYSTEM_USERNAME_NOT_ALLOWED.getMessage());
        }
        var user = BrokerUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        var savedUser = brokerUserRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .build();
    }

    public BrokerUser authenticateUser(String username, String password) throws BrokerGenericException {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            password
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BrokerGenericException(GenericExceptionMessages.BAD_USERNAME_OR_PASSWORD.getMessage());
        }
        BrokerUser user = brokerUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(GenericExceptionMessages.USER_NOT_FOUND.getMessage()));
        return user;
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws BrokerGenericException {
        BrokerUser user = authenticateUser(request.getUsername(), request.getPassword());
        return generateTokensAndBuildResponse(user);
    }

    private AuthenticationResponse generateTokensAndBuildResponse(BrokerUser user) {
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        userTokenRepository.deleteOlderTokensOfUserByCount(user.getId(), MAX_USER_TOKEN_COUNT);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveUserToken(BrokerUser user, String jwtToken) {
        var token = UserToken.builder()
                .user(user)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        userTokenRepository.save(token);
    }

    private void revokeAllUserTokens(BrokerUser user) {
        var validUserTokens = userTokenRepository.findAllValidUserTokensByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        userTokenRepository.saveAll(validUserTokens);
    }

    @Transactional
    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws BrokerGenericException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String username;
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new BrokerGenericException(GenericExceptionMessages.AUTHORIZATION_HEADER_MISSING.getMessage());
        }
        refreshToken = authHeader.substring(7);
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BrokerGenericException(GenericExceptionMessages.JWT_EXPIRED.getMessage());
        }
        if (username == null) {
            throw new BrokerGenericException(GenericExceptionMessages.JWT_SUBJECT_MISSING.getMessage());
        }
        var user = brokerUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(GenericExceptionMessages.USER_NOT_FOUND.getMessage()));
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BrokerGenericException(GenericExceptionMessages.JWT_NOT_VALID.getMessage());
        }
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        userTokenRepository.deleteOlderTokensOfUserByCount(user.getId(), MAX_USER_TOKEN_COUNT);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
