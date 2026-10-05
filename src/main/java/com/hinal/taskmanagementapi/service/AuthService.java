package com.hinal.taskmanagementapi.service;

import com.hinal.taskmanagementapi.dto.RegisterRequest;
import com.hinal.taskmanagementapi.dto.UserResponse;
import com.hinal.taskmanagementapi.dto.AuthResponse;
import com.hinal.taskmanagementapi.entity.User;
import com.hinal.taskmanagementapi.exception.UsernameAlreadyExistsException;
import com.hinal.taskmanagementapi.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser.getId(), savedUser.getUsername());
    }

    public AuthResponse login(String username, String password) {
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, password));
        return new AuthResponse("Login successful");
    }
}
