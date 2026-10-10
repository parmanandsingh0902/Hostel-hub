package com.hostelhub.service;

import com.hostelhub.dto.LoginRequest;
import com.hostelhub.dto.LoginResponse;
import com.hostelhub.dto.RegisterRequest;
import com.hostelhub.dto.UserResponse;
import com.hostelhub.exception.UserAlreadyExistsException;
import com.hostelhub.model.Role;
import com.hostelhub.model.User;
import com.hostelhub.repository.UserRepository;
import com.hostelhub.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public UserResponse register(RegisterRequest request) {
        return registerWithRole(request, Role.STUDENT);
    }

    public UserResponse registerWithRole(RegisterRequest request, Role role) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role != null ? role : Role.STUDENT);

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        UserResponse userResponse = mapToUserResponse(user);

        return new LoginResponse(token, "Bearer", userResponse);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
