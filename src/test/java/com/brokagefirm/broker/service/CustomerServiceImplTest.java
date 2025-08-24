package com.brokagefirm.broker.service;

import com.brokagefirm.broker.entity.BrokerCustomer;
import com.brokagefirm.broker.entity.Role;
import com.brokagefirm.broker.exception.BrokerGenericException;
import com.brokagefirm.broker.mapper.CustomerServiceMapper;
import com.brokagefirm.broker.repository.BrokerCustomerRepository;
import com.brokagefirm.broker.repository.RoleRepository;
import com.brokagefirm.broker.service.dto.BrokerCustomerDto;
import com.brokagefirm.broker.service.dto.RoleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    @Mock
    private CustomerServiceMapper mapper;
    @Mock
    private BrokerCustomerRepository brokerCustomerRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRole_whenExists_throws() {
        RoleDto dto = new RoleDto();
        dto.setName("ADMIN");
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(new Role()));
        assertThrows(BrokerGenericException.class, () -> customerService.createRole(dto));
        verify(roleRepository, never()).save(any());
    }

    @Test
    void createRole_whenNotExists_savesAndReturns() throws Exception {
        RoleDto dto = new RoleDto();
        dto.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(new Role());
        when(mapper.toRoleEntity(dto)).thenReturn(new Role());
        when(mapper.toRoleDto(any(Role.class))).thenReturn(dto);

        RoleDto res = customerService.createRole(dto);
        assertEquals("USER", res.getName());
    }

    @Test
    void addRoleToCustomer_whenCustomerMissing_throws() {
        when(brokerCustomerRepository.findByUsername("u")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> customerService.addRoleToCustomer("u", "ADMIN"));
    }

    @Test
    void addRoleToCustomer_whenRoleMissing_throws() {
        BrokerCustomer bc = BrokerCustomer.builder().username("u").build();
        when(brokerCustomerRepository.findByUsername("u")).thenReturn(Optional.of(bc));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        assertThrows(BrokerGenericException.class, () -> customerService.addRoleToCustomer("u", "ADMIN"));
    }

    @Test
    void addRoleToCustomer_success_addsAndSaves() throws Exception {
        BrokerCustomer bc = new BrokerCustomer();
        bc.setUsername("u");
        Role role = new Role(); role.setName("ADMIN");
        when(brokerCustomerRepository.findByUsername("u")).thenReturn(Optional.of(bc));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));

        customerService.addRoleToCustomer("u", "ADMIN");
        assertTrue(bc.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName())));
        verify(brokerCustomerRepository).save(bc);
    }

    @Test
    void getCustomerInfo_whenMissing_throws() {
        when(brokerCustomerRepository.findByUsername("u")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> customerService.getCustomerInfo("u"));
    }

    @Test
    void getCustomerInfo_success_maps() {
        BrokerCustomer bc = BrokerCustomer.builder().username("u").build();
        when(brokerCustomerRepository.findByUsername("u")).thenReturn(Optional.of(bc));
        BrokerCustomerDto dto = new BrokerCustomerDto(); dto.setUsername("u");
        when(mapper.toBrokerCustomerDto(bc)).thenReturn(dto);

        BrokerCustomerDto res = customerService.getCustomerInfo("u");
        assertEquals("u", res.getUsername());
    }

    @Test
    void getCurrentLoggedInUsername_whenNone_returnsEmpty() {
        assertTrue(customerService.getCurrentLoggedInUsername().isEmpty());
    }

    @Test
    void getCurrentLoggedInUsername_whenAnonymous_returnsEmpty() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("anonymousUser", "n", "ROLE_ANON"));
        assertTrue(customerService.getCurrentLoggedInUsername().isEmpty());
    }

    @Test
    void getCurrentLoggedInUsername_whenAuthenticated_returnsName() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("john", "n", "ROLE_USER"));
        assertEquals("john", customerService.getCurrentLoggedInUsername().orElse(null));
    }

    @Test
    void getCurrentLoggedInCustomer_whenMissing_throws() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("john", "n", "ROLE_USER"));
        when(brokerCustomerRepository.findByUsername("john")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, customerService::getCurrentLoggedInCustomer);
    }

    @Test
    void getCurrentLoggedInCustomer_success_maps() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("john", "n", "ROLE_USER"));
        BrokerCustomer bc = BrokerCustomer.builder().username("john").build();
        when(brokerCustomerRepository.findByUsername("john")).thenReturn(Optional.of(bc));
        BrokerCustomerDto dto = new BrokerCustomerDto(); dto.setUsername("john"); dto.setId(1L);
        when(mapper.toBrokerCustomerDto(bc)).thenReturn(dto);

        BrokerCustomerDto res = customerService.getCurrentLoggedInCustomer();
        assertEquals(1L, res.getId());
    }

    @Test
    void validateCustomerIdIfItsTheCurrentCustomer_whenNull_throws() {
        assertThrows(BrokerGenericException.class, () -> customerService.validateCustomerIdIfItsTheCurrentCustomer(null));
    }

    @Test
    void validateCustomerIdIfItsTheCurrentCustomer_whenAdmin_allowsAny() throws Exception {
        BrokerCustomerDto dto = new BrokerCustomerDto();
        dto.setId(99L);
        Role admin = new Role(); admin.setName("ADMIN");
        dto.getRoles().add(admin);
        when(brokerCustomerRepository.findByUsername("admin"))
                .thenReturn(Optional.of(BrokerCustomer.builder().username("admin").build()));
        when(mapper.toBrokerCustomerDto(any(BrokerCustomer.class))).thenReturn(dto);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("admin", "n", "ROLE_ADMIN"));

        customerService.validateCustomerIdIfItsTheCurrentCustomer(1L);
    }

    @Test
    void validateCustomerIdIfItsTheCurrentCustomer_whenNonAdminAndDifferent_throws() {
        BrokerCustomerDto dto = new BrokerCustomerDto();
        dto.setId(2L);
        when(brokerCustomerRepository.findByUsername("john"))
                .thenReturn(Optional.of(BrokerCustomer.builder().username("john").build()));
        when(mapper.toBrokerCustomerDto(any(BrokerCustomer.class))).thenReturn(dto);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("john", "n", "ROLE_USER"));

        assertThrows(BrokerGenericException.class, () -> customerService.validateCustomerIdIfItsTheCurrentCustomer(1L));
    }

    @Test
    void isCustomerExists_delegatesToRepo() throws Exception {
        when(brokerCustomerRepository.existsById(5L)).thenReturn(true);
        assertTrue(customerService.isCustomerExists(5L));
    }
}


