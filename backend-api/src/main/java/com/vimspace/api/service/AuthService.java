package com.vimspace.api.service;

import com.vimspace.api.entity.UserEntity;
import com.vimspace.api.repository.UserRepository;
import com.vimspace.contract.CreateUserRequest;
import com.vimspace.contract.LoginRequest;
import com.vimspace.contract.LoginResponse;
import com.vimspace.contract.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<LoginResponse> authenticate(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return Optional.empty();
        }

        return userRepository.findByUsername(request.getUsername().trim())
                .filter(u -> Boolean.TRUE.equals(u.getActive()))
                .filter(u -> u.getPasswordHash().equals(request.getPassword().trim()))
                .map(u -> new LoginResponse(
                        UUID.randomUUID().toString(),
                        u.getUsername(),
                        u.getRole()
                ));
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserDTO(u.getId(), u.getUsername(), u.getRole(), u.getActive()))
                .collect(Collectors.toList());
    }

    public UserDTO createUser(CreateUserRequest req) {
        if (userRepository.findByUsername(req.getUsername().trim()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + req.getUsername());
        }
        UserEntity user = new UserEntity(req.getUsername().trim(), req.getPassword().trim(), req.getRole().trim());
        UserEntity saved = userRepository.save(user);
        return new UserDTO(saved.getId(), saved.getUsername(), saved.getRole(), saved.getActive());
    }

    public UserDTO toggleUserActive(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        UserEntity saved = userRepository.save(user);
        return new UserDTO(saved.getId(), saved.getUsername(), saved.getRole(), saved.getActive());
    }
}
