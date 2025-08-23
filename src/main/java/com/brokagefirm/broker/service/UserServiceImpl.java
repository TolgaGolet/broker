package com.brokagefirm.broker.service;

import com.brokagefirm.broker.entity.BrokerUser;
import com.brokagefirm.broker.entity.Role;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.exception.enums.GenericExceptionMessages;
import com.brokagefirm.broker.mapper.UserServiceMapper;
import com.brokagefirm.broker.repository.BrokerUserRepository;
import com.brokagefirm.broker.repository.RoleRepository;
import com.brokagefirm.broker.service.dto.BrokerUserDto;
import com.brokagefirm.broker.service.dto.RoleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserServiceMapper mapper;
    private final BrokerUserRepository brokerUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RoleDto createRole(RoleDto role) throws BrokerGenericException {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new BrokerGenericException(GenericExceptionMessages.ROLE_NAME_ALREADY_EXISTS.getMessage());
        }
        return mapper.toRoleDto(roleRepository.save(mapper.toRoleEntity(role)));
    }

    @Override
    public void addRoleToUser(String username, String roleName) throws BrokerGenericException {
        BrokerUser brokerUser = brokerUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(GenericExceptionMessages.USER_NOT_FOUND.getMessage()));
        Role role = roleRepository.findByName(roleName).orElseThrow(() -> new BrokerGenericException(GenericExceptionMessages.ROLE_NOT_FOUND.getMessage()));
        brokerUser.getRoles().add(role);
        brokerUserRepository.save(brokerUser);
    }

    @Override
    public BrokerUserDto getUserInfo(String username) {
        BrokerUser brokerUser = brokerUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(GenericExceptionMessages.USER_NOT_FOUND.getMessage()));
        return mapper.toBrokerUserDto(brokerUser);
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
    public BrokerUserDto getCurrentLoggedInUser() {
        String username = getCurrentLoggedInUsername().orElseThrow(() -> new UsernameNotFoundException(GenericExceptionMessages.USER_NOT_FOUND.getMessage()));
        return getUserInfo(username);
    }

    @Override
    public void validateUserIdIfItsTheCurrentUser(Long userId) throws BrokerGenericException {
        if (userId == null) {
            throw new BrokerGenericException(GenericExceptionMessages.USER_ID_CANT_BE_NULL.getMessage());
        }
        if (!userId.equals(getCurrentLoggedInUser().getId())) {
            throw new BrokerGenericException(GenericExceptionMessages.NOT_AUTHORIZED_TO_PERFORM.getMessage());
        }
    }
}
