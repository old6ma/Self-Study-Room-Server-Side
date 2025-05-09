package com.studyroom.service;

import com.studyroom.model.Student;
import com.studyroom.repository.StudentRepository;
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
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setUsername("student");
        testStudent.setPassword("encodedPassword");
        testStudent.setName("Test Student");
        testStudent.setStudentId("202500001");
    }

    @Test
    void init_ShouldCreateDefaultStudent_WhenNoStudentExists() {
        // 模拟没有学生账户的情况
        when(studentRepository.findByUsername("student")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // 执行初始化
        studentService.init();

        // 验证保存操作被调用
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void init_ShouldNotCreateStudent_WhenStudentAlreadyExists() {
        // 模拟已有学生账户的情况
        when(studentRepository.findByUsername("student")).thenReturn(Optional.of(testStudent));

        // 执行初始化
        studentService.init();

        // 验证没有保存新学生
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenStudentExists() {
        // 模拟学生存在
        when(studentRepository.findByUsername("student")).thenReturn(Optional.of(testStudent));

        // 执行测试
        UserDetails userDetails = studentService.loadUserByUsername("student");

        // 验证结果
        assertEquals("student", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().iterator().next().getAuthority().equals("ROLE_STUDENT"));
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenStudentNotExists() {
        // 模拟学生不存在
        when(studentRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // 验证抛出异常
        assertThrows(UsernameNotFoundException.class, () -> {
            studentService.loadUserByUsername("unknown");
        });
    }

    @Test
    void findByUsername_ShouldReturnStudent_WhenStudentExists() {
        // 模拟学生存在
        when(studentRepository.findByUsername("student")).thenReturn(Optional.of(testStudent));

        // 执行测试
        Student result = studentService.findByUsername("student");

        // 验证结果
        assertEquals("student", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
    }

    @Test
    void findByUsername_ShouldThrowException_WhenStudentNotExists() {
        // 模拟学生不存在
        when(studentRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // 验证抛出异常
        assertThrows(UsernameNotFoundException.class, () -> {
            studentService.findByUsername("unknown");
        });
    }
}