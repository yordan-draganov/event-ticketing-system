package com.example.events.service;

import com.example.events.DTO.AuthResponse;
import com.example.events.DTO.LoginRequest;
import com.example.events.DTO.SignupRequest;
import com.example.events.DTO.UserDTO;
import com.example.events.exception.UserExistsException;
import com.example.events.exception.UserNotFoundException;
import com.example.events.model.User;
import com.example.events.model.UserRole;
import com.example.events.repository.UserRepository;
import com.example.events.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public AuthResponse signUp(SignupRequest request) throws UserExistsException {
        if (userRepository.existsByName(request.getName())) {
            throw new UserExistsException("User with name " + request.getName() + " already exists");
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

    public AuthResponse login(LoginRequest request) throws UserNotFoundException {
        User user = userRepository.findByName(request.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found with name: " + request.getName()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserNotFoundException("Invalid credentials");
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

    public String deleteUser(HttpServletRequest request) throws UserNotFoundException {
        String userName = (String) request.getAttribute("userName");

        if (userName == null) {
            throw new UserNotFoundException("User not authenticated");
        }

        User user = userRepository.findByName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.delete(user);
        return "User deleted successfully";
    }

    public String changePassword(String newPassword, HttpServletRequest request)
            throws UserNotFoundException {

        User user = getAuthenticatedUser(request);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password changed successfully";
    }

    public AuthResponse changeName(String newName, HttpServletRequest request)
            throws UserExistsException, UserNotFoundException {

        User user = getAuthenticatedUser(request);

        if (userRepository.existsByName(newName) && !user.getName().equals(newName)) {
            throw new UserExistsException("Name " + newName + " is already taken");
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

    public String getUserRole(String name) throws UserNotFoundException {
        User user = userRepository.findByName(name.trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with name: " + name));

        return user.getRole().toString();
    }

    public UserDTO getUserById(UUID id) throws UserNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private User getAuthenticatedUser(HttpServletRequest request) throws UserNotFoundException {
        String userName = (String) request.getAttribute("userName");

        if (userName == null) {
            throw new UserNotFoundException("User not authenticated");
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
