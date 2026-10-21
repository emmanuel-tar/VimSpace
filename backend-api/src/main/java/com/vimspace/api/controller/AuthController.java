package com.vimspace.api.controller;

import com.vimspace.api.service.AuthService;
import com.vimspace.contract.CreateUserRequest;
import com.vimspace.contract.LoginRequest;
import com.vimspace.contract.LoginResponse;
import com.vimspace.contract.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return authService.authenticate(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(authService.createUser(req));
    }

    @PostMapping("/users/{id}/toggle")
    public ResponseEntity<UserDTO> toggleUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(authService.toggleUserActive(id));
    }
}
