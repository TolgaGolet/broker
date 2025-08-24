package com.brokagefirm.broker.security.service;

import com.brokagefirm.broker.entity.BrokerCustomer;
import com.brokagefirm.broker.entity.CustomerToken;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.exception.enums.GenericExceptionMessages;
import com.brokagefirm.broker.repository.BrokerCustomerRepository;
import com.brokagefirm.broker.repository.CustomerTokenRepository;
import com.brokagefirm.broker.security.api.request.AuthenticationRequest;
import com.brokagefirm.broker.security.api.request.RegisterRequest;
import com.brokagefirm.broker.security.api.response.AuthenticationResponse;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.brokagefirm.broker.entity.audit.AuditAwareImpl.SYSTEM_USER;

@Profile("!disabled-security")
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final BrokerCustomerRepository brokerCustomerRepository;
    private final CustomerTokenRepository customerTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private static final Integer MAX_USER_TOKEN_COUNT = 5;

    public AuthenticationResponse register(RegisterRequest request) throws BrokerGenericException {
        if (brokerCustomerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BrokerGenericException(GenericExceptionMessages.USERNAME_ALREADY_EXISTS.getMessage());
        }
        if (request.getUsername().equalsIgnoreCase(SYSTEM_USER)) {
            throw new BrokerGenericException(GenericExceptionMessages.SYSTEM_USERNAME_NOT_ALLOWED.getMessage());
        }
        var user = BrokerCustomer.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        var savedUser = brokerCustomerRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public BrokerCustomer authenticateUser(String username, String password) throws BrokerGenericException {
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
        BrokerCustomer user = brokerCustomerRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(GenericExceptionMessages.CUSTOMER_NOT_FOUND.getMessage()));
        return user;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws BrokerGenericException {
        BrokerCustomer user = authenticateUser(request.getUsername(), request.getPassword());
        return generateTokensAndBuildResponse(user);
    }

    private AuthenticationResponse generateTokensAndBuildResponse(BrokerCustomer user) {
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        customerTokenRepository.deleteOlderTokensOfCustomerByCount(user.getId(), MAX_USER_TOKEN_COUNT);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveUserToken(BrokerCustomer user, String jwtToken) {
        var token = CustomerToken.builder()
                .customer(user)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        customerTokenRepository.save(token);
    }

    private void revokeAllUserTokens(BrokerCustomer user) {
        var validUserTokens = customerTokenRepository.findAllValidCustomerTokensByCustomer(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        customerTokenRepository.saveAll(validUserTokens);
    }

    public AuthenticationResponse refreshToken(String refreshToken) throws BrokerGenericException {
        final String username;
        if (refreshToken == null) {
            throw new BrokerGenericException(GenericExceptionMessages.REFRESH_TOKEN_MISSING.getMessage());
        }
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BrokerGenericException(GenericExceptionMessages.JWT_EXPIRED.getMessage());
        }
        if (username == null) {
            throw new BrokerGenericException(GenericExceptionMessages.JWT_SUBJECT_MISSING.getMessage());
        }
        var user = brokerCustomerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(GenericExceptionMessages.CUSTOMER_NOT_FOUND.getMessage()));
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BrokerGenericException(GenericExceptionMessages.JWT_NOT_VALID.getMessage());
        }
        var accessToken = jwtService.generateToken(user);
        refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        customerTokenRepository.deleteOlderTokensOfCustomerByCount(user.getId(), MAX_USER_TOKEN_COUNT);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
