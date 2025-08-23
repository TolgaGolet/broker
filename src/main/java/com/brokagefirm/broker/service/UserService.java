package com.brokagefirm.broker.service;

import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.service.dto.BrokerUserDto;
import com.brokagefirm.broker.service.dto.RoleDto;

import java.util.Optional;

public interface UserService {
    RoleDto createRole(RoleDto role) throws BrokerGenericException;

    void addRoleToUser(String username, String roleName) throws BrokerGenericException;

    BrokerUserDto getUserInfo(String username);

    public Optional<String> getCurrentLoggedInUsername();

    public BrokerUserDto getCurrentLoggedInUser();

    public void validateUserIdIfItsTheCurrentUser(Long userId) throws BrokerGenericException;
}
