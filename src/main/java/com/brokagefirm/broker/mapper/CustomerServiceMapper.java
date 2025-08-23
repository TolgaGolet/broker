package com.brokagefirm.broker.mapper;

import com.brokagefirm.broker.api.request.RoleCreateRequest;
import com.brokagefirm.broker.api.response.RoleCreateResponse;
import com.brokagefirm.broker.entity.BrokerCustomer;
import com.brokagefirm.broker.entity.Role;
import com.brokagefirm.broker.service.dto.BrokerCustomerDto;
import com.brokagefirm.broker.service.dto.RoleDto;
import org.mapstruct.Mapper;

import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(componentModel = "spring", unmappedTargetPolicy = IGNORE)
public interface CustomerServiceMapper {
    BrokerCustomerDto toBrokerCustomerDto(BrokerCustomer brokerCustomer);

    RoleDto toRoleDto(Role role);

    RoleDto toRoleDto(RoleCreateRequest role);

    Role toRoleEntity(RoleDto roleDto);

    RoleCreateResponse toRoleResponse(RoleDto roleDto);
}
