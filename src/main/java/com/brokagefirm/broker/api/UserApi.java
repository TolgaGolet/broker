package com.brokagefirm.broker.api;

import com.brokagefirm.broker.api.request.AddRoleToUserRequest;
import com.brokagefirm.broker.api.request.RoleCreateRequest;
import com.brokagefirm.broker.api.response.RoleCreateResponse;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.mapper.UserServiceMapper;
import com.brokagefirm.broker.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user")
@SecurityRequirement(name = "BearerAuth")
public class UserApi {
    private final UserService userService;
    private final UserServiceMapper userServiceMapper;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/role/create")
    public ResponseEntity<RoleCreateResponse> createRole(@RequestBody @Validated RoleCreateRequest roleCreateRequest) throws BrokerGenericException {
        return ResponseEntity.ok(userServiceMapper.toRoleResponse(userService.createRole(userServiceMapper.toRoleDto(roleCreateRequest))));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/role/add-role-to-user")
    public ResponseEntity<Void> addRoleToUser(@RequestBody @Validated AddRoleToUserRequest addRoleToUserRequest) throws BrokerGenericException {
        userService.addRoleToUser(addRoleToUserRequest.getUsername(), addRoleToUserRequest.getRoleName());
        return ResponseEntity.ok().build();
    }
}