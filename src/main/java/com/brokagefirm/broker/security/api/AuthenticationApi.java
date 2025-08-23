package com.brokagefirm.broker.security.api;

import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.security.api.request.AuthenticationRequest;
import com.brokagefirm.broker.security.api.request.RegisterRequest;
import com.brokagefirm.broker.security.api.response.AuthenticationResponse;
import com.brokagefirm.broker.security.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("!disabled-security")
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthenticationApi {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Validated RegisterRequest request) throws BrokerGenericException {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Validated AuthenticationRequest request) throws BrokerGenericException {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) throws BrokerGenericException {
        return ResponseEntity.ok(authenticationService.refreshToken(request, response));
    }
}
