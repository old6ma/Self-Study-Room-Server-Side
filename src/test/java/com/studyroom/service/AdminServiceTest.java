package com.studyroom.service;

import com.studyroom.model.Admin;
import com.studyroom.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = new Admin();
        testAdmin.setUsername("admin");
        testAdmin.setPassword("encodedPassword");
    }

    @Test
    void init_ShouldCreateDefaultAdmin_WhenNoAdminExists() {
        // 模拟没有管理员的情况
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // 执行初始化
        adminService.init();

        // 验证保存操作被调用
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void init_ShouldNotCreateAdmin_WhenAdminAlreadyExists() {
        // 模拟已有管理员的情况
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));

        // 执行初始化
        adminService.init();

        // 验证没有保存新管理员
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenAdminExists() {
        // 模拟管理员存在
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));

        // 执行测试
        UserDetails userDetails = adminService.loadUserByUsername("admin");

        // 验证结果
        assertEquals("admin", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenAdminNotExists() {
        // 模拟管理员不存在
        when(adminRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // 验证抛出异常
        assertThrows(UsernameNotFoundException.class, () -> {
            adminService.loadUserByUsername("unknown");
        });
    }

    @Test
    void findByUsername_ShouldReturnAdmin_WhenAdminExists() {
        // 模拟管理员存在
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));

        // 执行测试
        Admin result = adminService.findByUsername("admin");

        // 验证结果
        assertEquals("admin", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
    }

    @Test
    void findByUsername_ShouldThrowException_WhenAdminNotExists() {
        // 模拟管理员不存在
        when(adminRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // 验证抛出异常
        assertThrows(UsernameNotFoundException.class, () -> {
            adminService.findByUsername("unknown");
        });
    }
}