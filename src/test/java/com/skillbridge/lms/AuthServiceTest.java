package com.skillbridge.lms;

import com.skillbridge.lms.dto.*;
import com.skillbridge.lms.entity.Role;
import com.skillbridge.lms.entity.User;
import com.skillbridge.lms.exception.ApiException;
import com.skillbridge.lms.exception.DuplicateResourceException;
import com.skillbridge.lms.repository.UserRepository;
import com.skillbridge.lms.security.JwtTokenProvider;
import com.skillbridge.lms.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setName("Aarav Patel");
        validRegisterRequest.setEmail("aarav@example.edu");
        validRegisterRequest.setPassword("Secret123");
        validRegisterRequest.setConfirmPassword("Secret123");
    }

    @Test
    void register_ShouldSaveUserWithBCryptAndReturnJwt_WhenValid() {
        when(userRepository.existsByEmailIgnoreCase("aarav@example.edu")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("$2a$10$hashedPasswordValue");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(validRegisterRequest);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("Aarav Patel", response.getUser().getName());
        assertEquals("aarav@example.edu", response.getUser().getEmail());
        assertEquals("LEARNER", response.getUser().getRole());
        verify(passwordEncoder).encode("Secret123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowApiException_WhenPasswordsDoNotMatch() {
        validRegisterRequest.setConfirmPassword("Different123");

        ApiException ex = assertThrows(ApiException.class, () -> authService.register(validRegisterRequest));
        assertTrue(ex.getMessage().contains("Passwords do not match"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowDuplicateResourceException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmailIgnoreCase("aarav@example.edu")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(validRegisterRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        User storedUser = new User("Aarav Patel", "aarav@example.edu", "$2a$10$hashed", Role.LEARNER);
        storedUser.setId(10L);

        when(userRepository.findByEmailIgnoreCase("aarav@example.edu")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("Secret123", "$2a$10$hashed")).thenReturn(true);
        when(jwtTokenProvider.generateToken(storedUser)).thenReturn("valid-login-jwt");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("aarav@example.edu");
        loginRequest.setPassword("Secret123");

        AuthResponse response = authService.login(loginRequest);

        assertEquals("valid-login-jwt", response.getToken());
        assertEquals("aarav@example.edu", response.getUser().getEmail());
    }

    @Test
    void login_ShouldThrowBadCredentialsException_WhenPasswordIsIncorrect() {
        User storedUser = new User("Aarav Patel", "aarav@example.edu", "$2a$10$hashed", Role.LEARNER);
        when(userRepository.findByEmailIgnoreCase("aarav@example.edu")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("WrongPass", "$2a$10$hashed")).thenReturn(false);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("aarav@example.edu");
        loginRequest.setPassword("WrongPass");

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
