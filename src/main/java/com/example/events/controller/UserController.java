package com.example.events.controller;

import com.example.events.DTO.*;
import com.example.events.exception.UserExistsException;
import com.example.events.exception.UserNotFoundException;
import com.example.events.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignupRequest request) throws UserExistsException {
        AuthResponse response = userService.signUp(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) throws UserNotFoundException {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteUser(HttpServletRequest request) throws UserNotFoundException {
        String message = userService.deleteUser(request);
        return ResponseEntity.ok(message);
    }


    @PatchMapping("/pass")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) throws UserNotFoundException {
        String message = userService.changePassword(request.getNewPassword(), httpRequest);
        return ResponseEntity.ok(message);
    }


    @PatchMapping("/name")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthResponse> changeName(@RequestBody ChangeNameRequest request,
                                                   HttpServletRequest httpRequest)
            throws UserExistsException, UserNotFoundException {
        AuthResponse response = userService.changeName(request.getNewName(), httpRequest);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/role/{name}")
    public ResponseEntity<String> getUserRole(@PathVariable String name) throws UserNotFoundException {
        String role = userService.getUserRole(name);
        return ResponseEntity.ok(role);
    }


    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) throws UserNotFoundException {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }


    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser(HttpServletRequest request) throws UserNotFoundException {
        String userName = (String) request.getAttribute("userName");
        if (userName == null) {
            throw new UserNotFoundException("User not authenticated");
        }

        String userIdStr = (String) request.getAttribute("userId");
        UUID userId = UUID.fromString(userIdStr);
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}