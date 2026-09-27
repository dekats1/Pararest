package com.actisys.authservice.service;


import com.actisys.authservice.dto.JwtResponse;
import com.actisys.authservice.dto.LoginRequest;
import com.actisys.authservice.dto.RegisterRequest;

import java.util.UUID;

public interface AuthService {
    JwtResponse register(RegisterRequest request);

    JwtResponse login(LoginRequest request);

    JwtResponse refreshToken(UUID id);
}
