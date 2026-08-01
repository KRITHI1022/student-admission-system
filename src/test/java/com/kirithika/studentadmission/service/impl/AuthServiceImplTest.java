package com.kirithika.studentadmission.service.impl;

import com.kirithika.studentadmission.dto.request.RegisterRequest;
import com.kirithika.studentadmission.dto.response.AuthResponse;
import com.kirithika.studentadmission.entity.User;
import com.kirithika.studentadmission.enums.Role;
import com.kirithika.studentadmission.exception.ResourceAlreadyExistsException;
import com.kirithika.studentadmission.repository.StudentRepository;
import com.kirithika.studentadmission.repository.UserRepository;
import com.kirithika.studentadmission.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .phoneNumber("9999999999")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Test Address")
                .city("Test City")
                .state("Test State")
                .country("India")
                .build();
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {

        when(userRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.of(new User()));

        assertThrows(ResourceAlreadyExistsException.class,
                () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldCreateUserAndReturnToken_whenEmailIsNew() {

        User savedUser = User.builder()
                .id(1L)
                .email(registerRequest.getEmail())
                .role(Role.STUDENT)
                .build();

        when(userRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name()))
                .thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(response.getRole()).isEqualTo(Role.STUDENT);

        verify(userRepository, times(1)).save(any(User.class));
        verify(studentRepository, times(1)).save(any());
    }
}