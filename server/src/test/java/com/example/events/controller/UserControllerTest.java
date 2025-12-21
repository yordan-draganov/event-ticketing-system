package com.example.events.controller;

import com.example.events.DTO.*;
import com.example.events.exception.InvalidCredentialsException;
import com.example.events.exception.UserNotFoundException;
import com.example.events.model.UserRole;
import com.example.events.service.RedisTokenBlacklistService;
import com.example.events.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private RedisTokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest httpServletRequest;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private UserDTO userDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setName("testuser");
        signupRequest.setPassword("password123");
        signupRequest.setRole(UserRole.user);

        loginRequest = new LoginRequest();
        loginRequest.setName("testuser");
        loginRequest.setPassword("password123");

        authResponse = AuthResponse.builder()
                .token("test-token")
                .type("Bearer")
                .userId(userId)
                .name("testuser")
                .email("test@example.com")
                .role(UserRole.user)
                .message("Success")
                .build();

        userDTO = UserDTO.builder()
                .id(userId)
                .email("test@example.com")
                .name("testuser")
                .role(UserRole.user)
                .build();
    }

    @Test
    void testSignUpSuccess() {
        Mockito.when(userService.signUp(signupRequest)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = userController.signUp(signupRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-token", response.getBody().getToken());
        assertEquals("testuser", response.getBody().getName());
        Mockito.verify(userService).signUp(signupRequest);
    }

    @Test
    void testLoginSuccess() {
        Mockito.when(userService.login(loginRequest)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = userController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-token", response.getBody().getToken());
        assertEquals("testuser", response.getBody().getName());
        Mockito.verify(userService).login(loginRequest);
    }

    @Test
    void testLoginUserNotFound() {
        Mockito.doThrow(new UserNotFoundException("User not found with name: testuser"))
                .when(userService).login(Mockito.any(LoginRequest.class));

        assertThrows(UserNotFoundException.class, () -> {
            userController.login(loginRequest);
        });

        Mockito.verify(userService).login(loginRequest);
    }

    @Test
    void testLoginInvalidCredentials() {
        Mockito.doThrow(new InvalidCredentialsException("Invalid username or password"))
                .when(userService).login(Mockito.any(LoginRequest.class));

        assertThrows(InvalidCredentialsException.class, () -> {
            userController.login(loginRequest);
        });

        Mockito.verify(userService).login(loginRequest);
    }

    @Test
    void testLogoutSuccess() {
        Mockito.when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer test-token");

        ResponseEntity<String> response = userController.logout(httpServletRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful. Token has been invalidated.", response.getBody());
        Mockito.verify(tokenBlacklistService).blacklistToken("test-token");
    }

    @Test
    void testDeleteUserSuccess() {
        Mockito.when(userService.deleteUser(httpServletRequest)).thenReturn("User deleted successfully");

        ResponseEntity<String> response = userController.deleteUser(httpServletRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
        Mockito.verify(userService).deleteUser(httpServletRequest);
    }

    @Test
    void testChangePasswordSuccess() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");
        Mockito.when(userService.changePassword("oldPass", "newPass", httpServletRequest))
                .thenReturn("Password changed successfully. Please login again with your new password.");

        ResponseEntity<String> response = userController.changePassword(request, httpServletRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password changed successfully. Please login again with your new password.", response.getBody());
        Mockito.verify(userService).changePassword("oldPass", "newPass", httpServletRequest);
    }

    @Test
    void testChangeNameSuccess() {
        ChangeNameRequest request = new ChangeNameRequest("newname");
        Mockito.when(userService.changeName("newname", httpServletRequest)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = userController.changeName(request, httpServletRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-token", response.getBody().getToken());
        Mockito.verify(userService).changeName("newname", httpServletRequest);
    }

    @Test
    void testGetUserRole() {
        Mockito.when(userService.getUserRole("testuser")).thenReturn("user");

        ResponseEntity<String> response = userController.getUserRole("testuser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user", response.getBody());
        Mockito.verify(userService).getUserRole("testuser");
    }

    @Test
    void testGetUserById() {
        Mockito.when(userService.getUserById(userId)).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = userController.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals("testuser", response.getBody().getName());
        Mockito.verify(userService).getUserById(userId);
    }

    @Test
    void testGetUserByIdNotFound() {
        Mockito.doThrow(new UserNotFoundException("User not found with id: " + userId))
                .when(userService).getUserById(userId);

        assertThrows(UserNotFoundException.class, () -> {
            userController.getUserById(userId);
        });

        Mockito.verify(userService).getUserById(userId);
    }

    @Test
    void testGetAllUsers() {
        UserDTO userDTO2 = UserDTO.builder()
                .id(UUID.randomUUID())
                .email("test2@example.com")
                .name("testuser2")
                .role(UserRole.admin)
                .build();

        List<UserDTO> users = Arrays.asList(userDTO, userDTO2);
        Mockito.when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserDTO>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("testuser", response.getBody().get(0).getName());
        assertEquals("testuser2", response.getBody().get(1).getName());
        Mockito.verify(userService).getAllUsers();
    }
}