package com.brokagefirm.broker.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.brokagefirm.broker.entity.BrokerCustomer.*;

@Data
public class AddRoleToCustomerRequest {
    @NotNull
    @Size(min = USERNAME_MIN_LENGTH, max = USERNAME_MAX_LENGTH)
    @Pattern(regexp = USERNAME_REGULAR_EXPRESSION)
    private String username;
    @NotNull
    @Size(min = 3, max = 15)
    private String roleName;
}
