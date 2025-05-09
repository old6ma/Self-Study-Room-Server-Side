package com.studyroom.service;

import com.studyroom.model.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Primary
@Service
public class CompositeUserDetailsService implements UserDetailsService {

    private final AdminService adminService;
    private final StudentService studentService;

    public CompositeUserDetailsService(@Lazy AdminService adminService,@Lazy StudentService studentService) {
        this.adminService = adminService;
        this.studentService = studentService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return adminService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            return studentService.loadUserByUsername(username);
        }
    }
}