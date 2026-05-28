package com.example.events.controller;

import com.example.events.DTO.*;
import com.example.events.exception.UserNotFoundException;
import com.example.events.exception.UnauthorizedException;
import com.example.events.security.JwtUtil;
import com.example.events.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import com.example.events.service.RedisTokenBlacklistService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User authentication and management endpoints")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RedisTokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpirationMs;

    @PostMapping("/signup")
    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignupRequest request, 
                                               HttpServletResponse response) {
        AuthResponse authResponse = userService.signUp(request);
        setAuthCookies(response, authResponse);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletResponse response) {
        AuthResponse authResponse = userService.login(request);
        setAuthCookies(response, authResponse);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Issue a new short-lived access token from the HttpOnly refresh token cookie")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookieValue(request, "refresh_token");
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh token is missing");
        }

        AuthResponse authResponse = userService.refreshAccessToken(refreshToken);
        setAccessTokenCookie(response, authResponse.getToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout user", description = "Invalidate current JWT token", 
               security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = extractAccessToken(request);

        if (token != null) {
            tokenBlacklistService.blacklistToken(token);
        }

        String refreshToken = extractCookieValue(request, "refresh_token");
        if (refreshToken != null) {
            tokenBlacklistService.blacklistToken(refreshToken);
        }
        
        clearAuthCookies(response);
        
        return ResponseEntity.ok("Logout successful. Token has been invalidated.");
    }

    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete user account", description = "Delete current user's account", 
               security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<String> deleteUser(HttpServletRequest request, HttpServletResponse response) {
        String message = userService.deleteUser(request);
        blacklistRefreshToken(request);
        clearAuthCookies(response);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/pass")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", description = "Update user password", 
               security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {
        String message = userService.changePassword(
                request.getOldPassword(),
                request.getNewPassword(),
                httpRequest
        );
        blacklistRefreshToken(httpRequest);
        clearAuthCookies(httpResponse);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/name")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change name", description = "Update user display name", 
               security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<AuthResponse> changeName(@Valid @RequestBody ChangeNameRequest request,
                                                   HttpServletRequest httpRequest,
                                                   HttpServletResponse httpResponse) {
        AuthResponse authResponse = userService.changeName(request.getNewName(), httpRequest);
        blacklistRefreshToken(httpRequest);
        setAuthCookies(httpResponse, authResponse);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/role/{name}")
    @Operation(summary = "Get user role", description = "Retrieve user role by username")
    public ResponseEntity<String> getUserRole(@PathVariable String name) {
        String role = userService.getUserRole(name);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by UUID", 
               security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve all users (Admin only)", 
               security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user", description = "Get authenticated user's profile", 
               security = @SecurityRequirement(name = "Bearer Authentication"))
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

    private void setAuthCookies(HttpServletResponse response, AuthResponse authResponse) {
        setAccessTokenCookie(response, authResponse.getToken());
        String refreshToken = jwtUtil.generateRefreshToken(
                authResponse.getUserId(),
                authResponse.getName(),
                authResponse.getRole().name()
        );
        setRefreshTokenCookie(response, refreshToken);
    }

    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = buildCookie("access_token", token, seconds(accessTokenExpirationMs));
        response.addCookie(cookie);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = buildCookie("refresh_token", token, seconds(refreshTokenExpirationMs));
        response.addCookie(cookie);
    }

    private Cookie buildCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    private void clearAuthCookies(HttpServletResponse response) {
        response.addCookie(buildCookie("access_token", null, 0));
        response.addCookie(buildCookie("refresh_token", null, 0));
        response.addCookie(buildCookie("auth_token", null, 0));
    }

    private int seconds(Long milliseconds) {
        if (milliseconds == null) {
            return 0;
        }
        return Math.toIntExact(milliseconds / 1000);
    }

    private String extractAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        String accessToken = extractCookieValue(request, "access_token");
        if (accessToken != null) {
            return accessToken;
        }

        return extractCookieValue(request, "auth_token");
    }

    private void blacklistRefreshToken(HttpServletRequest request) {
        String refreshToken = extractCookieValue(request, "refresh_token");
        if (refreshToken != null) {
            tokenBlacklistService.blacklistToken(refreshToken);
        }
    }

    private String extractCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}