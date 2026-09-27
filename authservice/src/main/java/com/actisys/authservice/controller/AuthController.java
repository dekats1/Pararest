package com.actisys.authservice.controller;

import com.actisys.authservice.dto.JwtResponse;
import com.actisys.authservice.dto.LoginRequest;
import com.actisys.authservice.dto.RegisterRequest;
import com.actisys.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public Mono<ResponseEntity<JwtResponse>> register(@RequestBody RegisterRequest request) {
        return Mono.fromCallable(() -> authService.register(request))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<JwtResponse>> login(@RequestBody LoginRequest request) {
        return Mono.fromCallable(() -> authService.login(request))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }

    @GetMapping("/refresh")
    public Mono<ResponseEntity<JwtResponse>> refresh(@RequestHeader("X-User-Id") String userId) {
        return Mono.fromCallable(() -> authService.refreshToken(UUID.fromString(userId)))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }
}
