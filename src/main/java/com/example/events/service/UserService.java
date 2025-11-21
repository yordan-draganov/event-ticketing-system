package com.example.events.service;

import com.example.events.DTO.AuthResponse;
import com.example.events.DTO.LoginRequest;
import com.example.events.DTO.SignupRequest;
import com.example.events.DTO.UserDTO;
import com.example.events.exception.*;
import com.example.events.model.User;
import com.example.events.model.UserRole;
import com.example.events.repository.UserRepository;
import com.example.events.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Pattern email_pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTokenBlacklistService tokenBlacklistService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RedisTokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!email_pattern.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

    }

    public AuthResponse signUp(SignupRequest request) {
        validateEmail(request.getEmail());

        validatePassword(request.getPassword());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserExistsException("Email already registered");
        }

        if (userRepository.existsByName(request.getName())) {
            throw new UserExistsException("User with name '" + request.getName() + "' already exists");
        }

        UserRole role = request.getRole() != null ? request.getRole() : UserRole.user;

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByName(request.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found with name: " + request.getName()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

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
                .message("Login successful. Welcome " + user.getName())
                .build();
    }

    public String deleteUser(HttpServletRequest request) {
        String userName = (String) request.getAttribute("userName");

        if (userName == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
        }

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

        validatePassword(newPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
        }

        return "Password changed successfully. Please login again with your new password.";
    }

    public AuthResponse changeName(String newName, HttpServletRequest request) {
        User user = getAuthenticatedUser(request);

        if (user.getName().equals(newName)) {
            throw new IllegalArgumentException("New name is the same as current name");
        }

        if (userRepository.existsByName(newName)) {
            throw new UserExistsException("Name '" + newName + "' is already taken");
        }

        user.setName(newName);
        userRepository.save(user);

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
                .message("Name changed successfully to: " + newName)
                .build();
    }

    public String getUserRole(String name) {
        User user = userRepository.findByName(name.trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with name: " + name));

        return user.getRole().toString();
    }

    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        String userName = (String) request.getAttribute("userName");

        if (userName == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        return userRepository.findByName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}