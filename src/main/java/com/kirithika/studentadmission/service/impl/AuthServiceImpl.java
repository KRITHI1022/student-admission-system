package com.kirithika.studentadmission.service.impl;

import com.kirithika.studentadmission.dto.request.LoginRequest;
import com.kirithika.studentadmission.dto.request.RegisterRequest;
import com.kirithika.studentadmission.dto.response.AuthResponse;
import com.kirithika.studentadmission.entity.Student;
import com.kirithika.studentadmission.entity.User;
import com.kirithika.studentadmission.enums.Role;
import com.kirithika.studentadmission.exception.*;
import com.kirithika.studentadmission.repository.StudentRepository;
import com.kirithika.studentadmission.repository.UserRepository;
import com.kirithika.studentadmission.security.JwtUtil;
import com.kirithika.studentadmission.service.interfaces.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           StudentRepository studentRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        Student student = Student.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .user(savedUser)
                .build();

        studentRepository.save(student);

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(request.getFullName())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String fullName = studentRepository.findByUserId(user.getId())
                .map(Student::getFullName)
                .orElse("Admin");

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(fullName)
                .role(user.getRole())
                .build();
    }
}