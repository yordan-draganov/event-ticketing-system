package com.example.events.service;

import com.example.events.DTO.AuthResponse;
import com.example.events.DTO.LoginRequest;
import com.example.events.DTO.SignupRequest;
import com.example.events.DTO.UserDTO;
import com.example.events.exception.*;
import com.example.events.mapper.UserMapper;
import com.example.events.model.User;
import com.example.events.model.UserRole;
import com.example.events.repository.UserRepository;
import com.example.events.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTokenBlacklistService tokenBlacklistService;
    private final UserMapper userMapper;

    public AuthResponse signUp(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserExistsException("Email already registered");
        }

        if (userRepository.existsByName(request.getName())) {
            throw new UserExistsException("User with name '" + request.getName() + "' already exists");
        }

        request.setRole(UserRole.user);

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser, "User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByName(request.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found with name: " + request.getName()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return buildAuthResponse(user, "Login successful. Welcome " + user.getName());
    }

    public String deleteUser(HttpServletRequest request) {
        String userName = (String) request.getAttribute("userName");

        if (userName == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        blacklistCurrentToken(request);

        User user = userRepository.findByName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
        return "User deleted successfully";
    }

    public String changePassword(String oldPassword, String newPassword, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ValidationException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        blacklistCurrentToken(request);

        return "Password changed successfully. Please login again with your new password.";
    }

    public AuthResponse changeName(String newName, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);

        if (user.getName().equals(newName)) {
            throw new ValidationException("New name is the same as current name");
        }

        if (userRepository.existsByName(newName)) {
            throw new UserExistsException("Name '" + newName + "' is already taken");
        }

        user.setName(newName);
        userRepository.save(user);

        return buildAuthResponse(user, "Name changed successfully to: " + newName);
    }

    public String getUserRole(String name) {
        User user = userRepository.findByName(name.trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with name: " + name));

        return user.getRole().toString();
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null
                || tokenBlacklistService.isTokenBlacklisted(refreshToken)
                || !jwtUtil.validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        UUID userId = jwtUtil.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return buildAuthResponse(user, "Access token refreshed successfully");
    }

    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        return userMapper.toDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getName(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .message(message)
                .build();
    }

    private void blacklistCurrentToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
        }
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        String userName = (String) request.getAttribute("userName");

        if (userName == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        return userRepository.findByName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
