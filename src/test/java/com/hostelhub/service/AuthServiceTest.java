package com.hostelhub.service;

import com.hostelhub.dto.LoginRequest;
import com.hostelhub.dto.LoginResponse;
import com.hostelhub.dto.RegisterRequest;
import com.hostelhub.dto.UserResponse;
import com.hostelhub.exception.UserAlreadyExistsException;
import com.hostelhub.model.Role;
import com.hostelhub.model.User;
import com.hostelhub.repository.ComplaintRepository;
import com.hostelhub.repository.GroupMembershipRepository;
import com.hostelhub.repository.HostelGroupRepository;
import com.hostelhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private HostelGroupRepository groupRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
        membershipRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest("Rohan Sharma", "rohan@example.com", "Password@123");

        UserResponse response = authService.register(request);

        assertNotNull(response.getId());
        assertEquals("Rohan Sharma", response.getName());
        assertEquals("rohan@example.com", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
        assertNotNull(response.getCreatedAt());

        // Verify password is hashed in database and NOT plain text
        User savedUser = userRepository.findByEmail("rohan@example.com").orElseThrow();
        assertNotEquals("Password@123", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("Password@123", savedUser.getPassword()));
    }

    @Test
    void testRegisterUser_DuplicateEmail() {
        RegisterRequest request = new RegisterRequest("Rohan Sharma", "rohan@example.com", "Password@123");
        authService.register(request);

        RegisterRequest duplicateRequest = new RegisterRequest("Other Name", "rohan@example.com", "DifferentPass");
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(duplicateRequest));
    }

    @Test
    void testLoginUser_Success() {
        RegisterRequest registerReq = new RegisterRequest("Rohan Sharma", "rohan@example.com", "Password@123");
        authService.register(registerReq);

        LoginRequest loginReq = new LoginRequest("rohan@example.com", "Password@123");
        LoginResponse response = authService.login(loginReq);

        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("rohan@example.com", response.getUser().getEmail());
        assertEquals(Role.STUDENT, response.getUser().getRole());
    }

    @Test
    void testLoginUser_InvalidPassword() {
        RegisterRequest registerReq = new RegisterRequest("Rohan Sharma", "rohan@example.com", "Password@123");
        authService.register(registerReq);

        LoginRequest loginReq = new LoginRequest("rohan@example.com", "WrongPassword");
        assertThrows(BadCredentialsException.class, () -> authService.login(loginReq));
    }
}
