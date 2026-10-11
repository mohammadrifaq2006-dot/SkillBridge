package com.skillbridge.lms.service;

import com.skillbridge.lms.dto.*;
import com.skillbridge.lms.entity.Role;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.exception.ApiException;
import com.skillbridge.lms.exception.DuplicateResourceException;
import com.skillbridge.lms.exception.ResourceNotFoundException;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("Passwords do not match.", HttpStatus.BAD_REQUEST);
        }

        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("An account with this email is already registered.");
        }

        User user = new User(
                request.getName().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                Role.LEARNER
        );

        User savedUser = userRepository.save(user);
        log.info("Registered new learner user ID={} email='{}'", savedUser.getId(), savedUser.getEmail());

        String token = jwtTokenProvider.generateToken(savedUser);
        return new AuthResponse(token, UserResponse.fromEntity(savedUser), "Registration successful.");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        String token = jwtTokenProvider.generateToken(user);
        log.info("User logged in ID={} email='{}' role={}", user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, UserResponse.fromEntity(user), "Login successful.");
    }

    @Transactional
    public String resetPassword(PasswordResetRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("New password and confirm password do not match.", HttpStatus.BAD_REQUEST);
        }

        String normalizedEmail = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No registered account found for the provided email."));

        if (!user.getName().trim().equalsIgnoreCase(request.getName().trim())) {
            throw new ApiException("Account verification failed: full name does not match the registered email.", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset completed for user ID={} email='{}'", user.getId(), user.getEmail());
        return "Your password has been updated in the database. You can now log in with your new password.";
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));
        return UserResponse.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }
}
