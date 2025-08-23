package com.brokagefirm.broker;

import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.repository.BrokerUserRepository;
import com.brokagefirm.broker.repository.RoleRepository;
import com.brokagefirm.broker.security.api.request.RegisterRequest;
import com.brokagefirm.broker.security.api.response.AuthenticationResponse;
import com.brokagefirm.broker.security.service.AuthenticationService;
import com.brokagefirm.broker.service.UserService;
import com.brokagefirm.broker.service.dto.RoleDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// TODO: Important: This class is used to create an admin user and role when the application starts for testing purposes only. Delete this class afterwards.
@Component
@RequiredArgsConstructor
public class AdminUserInitializer {
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final BrokerUserRepository brokerUserRepository;
    private final RoleRepository roleRepository;

    @PostConstruct
    public void init() throws BrokerGenericException {
        AuthenticationResponse authenticationResponse = null;

        // Create ADMIN role if it doesn't exist
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            RoleDto adminRole = new RoleDto();
            adminRole.setName("ADMIN");
            userService.createRole(adminRole);
        }

        // Create admin user if it doesn't exist
        if (brokerUserRepository.findByUsername("admin").isEmpty()) {
            authenticationResponse = authenticationService.register(RegisterRequest.builder().username("admin").password("password").build());
        }

        // Add ADMIN role to admin user if it doesn't have it
        if (authenticationResponse != null && userService.getUserInfo("admin").getRoles().stream().noneMatch(r -> "ADMIN".equals(r.getName()))) {
            userService.addRoleToUser("admin", "ADMIN");
        }
    }
}
