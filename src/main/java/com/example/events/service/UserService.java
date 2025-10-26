package com.example.events.service;

import com.example.events.DTO.UserDTO;
import com.example.events.exception.UserAlreadyLoggedOutException;
import com.example.events.exception.UserExistsException;
import com.example.events.exception.UserNotFoundException;
import com.example.events.model.User;
import com.example.events.model.UserRole;
import com.example.events.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void signUp(User user) throws UserExistsException {
        if (userRepository.existsByName(user.getName())) {
            throw new UserExistsException("User with name " + user.getName() + " already exists");
        }

        if (user.getRole() == null) {
            user.setRole(UserRole.user);
        }

        userRepository.save(user);
    }

    public ResponseEntity<String> login(User user, HttpServletRequest request) throws UserNotFoundException {
        User existingUser = userRepository.findByName(user.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found with name: " + user.getName()));

        if (!existingUser.getPassword().equals(user.getPassword())) {
            throw new UserNotFoundException("Invalid credentials");
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("userId", existingUser.getId().toString());
        session.setAttribute("userName", existingUser.getName());
        session.setAttribute("userRole", existingUser.getRole().toString());

        return ResponseEntity.ok("Login successful. Welcome " + existingUser.getName());
    }

    public ResponseEntity<String> logout(HttpServletRequest request) throws UserAlreadyLoggedOutException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UserAlreadyLoggedOutException("User is already logged out");
        }

        session.invalidate();
        return ResponseEntity.ok("Logout successful");
    }

    public ResponseEntity<String> delete(String name, HttpServletRequest request)
            throws UserAlreadyLoggedOutException, UserNotFoundException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new UserAlreadyLoggedOutException("You must be logged in to delete account");
        }

        User user = userRepository.findByName(name.trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with name: " + name));

        userRepository.delete(user);
        session.invalidate();

        return ResponseEntity.ok("User deleted successfully");
    }

    public ResponseEntity<String> changePassword(String password, HttpServletRequest request)
            throws UserAlreadyLoggedOutException {

        User user = getLoggedInUser(request);
        user.setPassword(password.trim());
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

    public ResponseEntity<String> changeName(String name, HttpServletRequest request)
            throws UserAlreadyLoggedOutException, UserExistsException {

        User user = getLoggedInUser(request);

        if (userRepository.existsByName(name.trim()) && !user.getName().equals(name.trim())) {
            throw new UserExistsException("Name " + name + " is already taken");
        }

        user.setName(name.trim());
        userRepository.save(user);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("userName", name.trim());
        }

        return ResponseEntity.ok("Name changed successfully to: " + name);
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

    private User getLoggedInUser(HttpServletRequest request) throws UserAlreadyLoggedOutException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            throw new UserAlreadyLoggedOutException("You must be logged in to perform this action");
        }

        String userId = (String) session.getAttribute("userId");
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Session user not found"));
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