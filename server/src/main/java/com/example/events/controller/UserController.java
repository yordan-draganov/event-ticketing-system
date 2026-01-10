package com.example.events.controller;

import com.example.events.DTO.*;
import com.example.events.exception.UserNotFoundException;
import com.example.events.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import com.example.events.service.RedisTokenBlacklistService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "Users", description = "User authentication and management endpoints")
public class UserController {

    private final UserService userService;
    private final RedisTokenBlacklistService tokenBlacklistService;

    public UserController(UserService userService, RedisTokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/signup")
    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = userService.signUp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout user", description = "Invalidate current JWT token", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
            return ResponseEntity.ok("Logout successful. Token has been invalidated.");
        }

        return ResponseEntity.badRequest().body("No token found to invalidate.");
    }

    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete user account", description = "Delete current user's account", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<String> deleteUser(HttpServletRequest request) {
        String message = userService.deleteUser(request);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/pass")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", description = "Update user password", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                 HttpServletRequest httpRequest) {
        String message = userService.changePassword(
                request.getOldPassword(),
                request.getNewPassword(),
                httpRequest
        );
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/name")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change name", description = "Update user display name", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<AuthResponse> changeName(@Valid @RequestBody ChangeNameRequest request,
                                                   HttpServletRequest httpRequest) {
        AuthResponse response = userService.changeName(request.getNewName(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{name}")
    @Operation(summary = "Get user role", description = "Retrieve user role by username")
    public ResponseEntity<String> getUserRole(@PathVariable String name) {
        String role = userService.getUserRole(name);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by UUID", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve all users (Admin only)", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user", description = "Get authenticated user's profile", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<UserDTO> getCurrentUser(HttpServletRequest request) {
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