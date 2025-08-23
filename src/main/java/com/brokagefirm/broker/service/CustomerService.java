package com.brokagefirm.broker.service;

import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.service.dto.BrokerCustomerDto;
import com.brokagefirm.broker.service.dto.RoleDto;

import java.util.Optional;

public interface CustomerService {
    RoleDto createRole(RoleDto role) throws BrokerGenericException;

    void addRoleToCustomer(String username, String roleName) throws BrokerGenericException;

    BrokerCustomerDto getCustomerInfo(String username);

    public Optional<String> getCurrentLoggedInUsername();

    public BrokerCustomerDto getCurrentLoggedInCustomer();

    public void validateCustomerIdIfItsTheCurrentCustomer(Long customerId) throws BrokerGenericException;
}
