package com.brokagefirm.broker.mapper;

import com.brokagefirm.broker.api.request.RoleCreateRequest;
import com.brokagefirm.broker.api.response.RoleCreateResponse;
import com.brokagefirm.broker.entity.BrokerUser;
import com.brokagefirm.broker.entity.Role;
import com.brokagefirm.broker.service.dto.BrokerUserDto;
import com.brokagefirm.broker.service.dto.RoleDto;
import org.mapstruct.Mapper;

import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(componentModel = "spring", unmappedTargetPolicy = IGNORE)
public interface UserServiceMapper {
    BrokerUserDto toBrokerUserDto(BrokerUser brokerUser);

    RoleDto toRoleDto(Role role);

    RoleDto toRoleDto(RoleCreateRequest role);

    Role toRoleEntity(RoleDto roleDto);

    RoleCreateResponse toRoleResponse(RoleDto roleDto);
}
