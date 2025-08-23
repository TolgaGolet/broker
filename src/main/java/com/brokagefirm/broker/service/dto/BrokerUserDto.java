package com.brokagefirm.broker.service.dto;

import com.brokagefirm.broker.entity.Role;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class BrokerUserDto {
    private Long id;
    private String username;
    private String password;
    private Set<Role> roles = new HashSet<>();
}
