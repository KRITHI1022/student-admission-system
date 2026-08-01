package com.kirithika.studentadmission.service.interfaces;

import com.kirithika.studentadmission.dto.request.LoginRequest;
import com.kirithika.studentadmission.dto.request.RegisterRequest;
import com.kirithika.studentadmission.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}