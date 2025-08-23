package com.brokagefirm.broker.security.api;

import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.security.api.request.AuthenticationRequest;
import com.brokagefirm.broker.security.api.request.RegisterRequest;
import com.brokagefirm.broker.security.api.response.AuthenticationResponse;
import com.brokagefirm.broker.security.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Profile("!disabled-security")
@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationApi {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Validated RegisterRequest request) throws BrokerGenericException {
        AuthenticationResponse response = authenticationService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Validated AuthenticationRequest request) throws BrokerGenericException {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestParam String refreshToken) throws BrokerGenericException {
        return ResponseEntity.ok(authenticationService.refreshToken(refreshToken));
    }
}
