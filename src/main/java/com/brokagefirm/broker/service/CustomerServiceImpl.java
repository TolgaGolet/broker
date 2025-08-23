package com.brokagefirm.broker.service;

import com.brokagefirm.broker.entity.BrokerCustomer;
import com.brokagefirm.broker.entity.Role;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.exception.enums.GenericExceptionMessages;
import com.brokagefirm.broker.mapper.CustomerServiceMapper;
import com.brokagefirm.broker.repository.BrokerCustomerRepository;
import com.brokagefirm.broker.repository.RoleRepository;
import com.brokagefirm.broker.service.dto.BrokerCustomerDto;
import com.brokagefirm.broker.service.dto.RoleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {
    private final CustomerServiceMapper mapper;
    private final BrokerCustomerRepository brokerCustomerRepository;
    private final RoleRepository roleRepository;

    @Override
    public RoleDto createRole(RoleDto role) throws BrokerGenericException {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new BrokerGenericException(GenericExceptionMessages.ROLE_NAME_ALREADY_EXISTS.getMessage());
        }
        return mapper.toRoleDto(roleRepository.save(mapper.toRoleEntity(role)));
    }

    @Override
    public void addRoleToCustomer(String username, String roleName) throws BrokerGenericException {
        BrokerCustomer brokerCustomer = brokerCustomerRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(GenericExceptionMessages.CUSTOMER_NOT_FOUND.getMessage()));
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.ROLE_NOT_FOUND.getMessage()));
        brokerCustomer.getRoles().add(role);
        brokerCustomerRepository.save(brokerCustomer);
    }

    @Override
    public BrokerCustomerDto getCustomerInfo(String username) {
        BrokerCustomer brokerCustomer = brokerCustomerRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(GenericExceptionMessages.CUSTOMER_NOT_FOUND.getMessage()));
        return mapper.toBrokerCustomerDto(brokerCustomer);
    }

    @Override
    public Optional<String> getCurrentLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return !Objects.equals(authentication.getName(), "anonymousUser") ? Optional.ofNullable(authentication.getName()) : Optional.empty();
    }

    @Override
    public BrokerCustomerDto getCurrentLoggedInCustomer() {
        String username = getCurrentLoggedInUsername().orElseThrow(() -> new UsernameNotFoundException(GenericExceptionMessages.CUSTOMER_NOT_FOUND.getMessage()));
        return getCustomerInfo(username);
    }

    @Override
    public void validateCustomerIdIfItsTheCurrentCustomer(Long customerId) throws BrokerGenericException {
        if (customerId == null) {
            throw new BrokerGenericException(GenericExceptionMessages.CUSTOMER_ID_CANT_BE_NULL.getMessage());
        }
        BrokerCustomerDto currentUser = getCurrentLoggedInCustomer();
        if (currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            return;
        }
        if (!customerId.equals(currentUser.getId())) {
            throw new BrokerGenericException(GenericExceptionMessages.NOT_AUTHORIZED_TO_PERFORM.getMessage());
        }
    }
}
