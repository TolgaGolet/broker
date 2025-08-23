package com.brokagefirm.broker.api;

import com.brokagefirm.broker.api.request.AddRoleToCustomerRequest;
import com.brokagefirm.broker.api.request.RoleCreateRequest;
import com.brokagefirm.broker.api.response.RoleCreateResponse;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.mapper.CustomerServiceMapper;
import com.brokagefirm.broker.service.CustomerService;
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
@RequestMapping("api/v1/customer")
@SecurityRequirement(name = "BearerAuth")
public class CustomerApi {
    private final CustomerService customerService;
    private final CustomerServiceMapper customerServiceMapper;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/role/create")
    public ResponseEntity<RoleCreateResponse> createRole(@RequestBody @Validated RoleCreateRequest roleCreateRequest) throws BrokerGenericException {
        return ResponseEntity.ok(customerServiceMapper.toRoleResponse(customerService.createRole(customerServiceMapper.toRoleDto(roleCreateRequest))));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/role/add-role-to-customer")
    public ResponseEntity<Void> addRoleToCustomer(@RequestBody @Validated AddRoleToCustomerRequest addRoleToCustomerRequest) throws BrokerGenericException {
        customerService.addRoleToCustomer(addRoleToCustomerRequest.getUsername(), addRoleToCustomerRequest.getRoleName());
        return ResponseEntity.ok().build();
    }
}