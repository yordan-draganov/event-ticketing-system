package com.example.events.security;

import com.example.events.DTO.ErrorResponse;
import com.example.events.exception.InvalidTokenException;
import com.example.events.exception.TokenBlacklistedException;
import com.example.events.exception.TokenExpiredException;
import com.example.events.repository.UserRepository;
import com.example.events.service.RedisTokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisTokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UserRepository userRepository,
                                   RedisTokenBlacklistService tokenBlacklistService,
                                   ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenBlacklistService = tokenBlacklistService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                logger.warn("Attempted to use blacklisted (logged out) token");
                handleException(response, HttpStatus.UNAUTHORIZED, "Token Blacklisted",
                        "This token has been invalidated. Please login again.", request.getRequestURI());
                return;
            }

            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                logger.error("JWT Token has expired: " + e.getMessage());
                handleException(response, HttpStatus.UNAUTHORIZED, "Token Expired",
                        "JWT token has expired. Please login again.", request.getRequestURI());
                return;
            } catch (Exception e) {
                logger.error("JWT Token extraction failed: " + e.getMessage());
                handleException(response, HttpStatus.UNAUTHORIZED, "Invalid Token",
                        "Failed to process JWT token: " + e.getMessage(), request.getRequestURI());
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validateToken(jwt, username)) {

                String role = jwtUtil.extractUserRole(jwt);
                String userId = jwtUtil.extractUserId(jwt);

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singletonList(authority)
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                request.setAttribute("userId", userId);
                request.setAttribute("userName", username);
                request.setAttribute("userRole", role);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                handleException(response, HttpStatus.UNAUTHORIZED, "Invalid Token",
                        "JWT token validation failed", request.getRequestURI());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleException(HttpServletResponse response, HttpStatus status, String error, String message, String path)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}