package com.example.events.service;

import com.example.events.DTO.AuthResponse;
import com.example.events.DTO.LoginRequest;
import com.example.events.DTO.SignupRequest;
import com.example.events.DTO.UserDTO;
import com.example.events.exception.InvalidCredentialsException;
import com.example.events.exception.UserExistsException;
import com.example.events.exception.UserNotFoundException;
import com.example.events.mapper.UserMapper;
import com.example.events.model.User;
import com.example.events.model.UserRole;
import com.example.events.repository.UserRepository;
import com.example.events.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTokenBlacklistService tokenBlacklistService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpServletRequest httpServletRequest;

    private User user;
    private UUID userId;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name("testuser")
                .password("encodedPassword")
                .role(UserRole.user)
                .build();

        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setName("testuser");
        signupRequest.setPassword("password123");
        signupRequest.setRole(UserRole.user);

        loginRequest = new LoginRequest();
        loginRequest.setName("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void testSignUpSuccess() {
        Mockito.when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        Mockito.when(userRepository.existsByName(signupRequest.getName())).thenReturn(false);
        Mockito.when(userMapper.toEntity(signupRequest)).thenReturn(user);
        Mockito.when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        Mockito.when(jwtUtil.generateToken(Mockito.any(UUID.class), Mockito.anyString(), Mockito.anyString()))
                .thenReturn("test-token");

        AuthResponse response = userService.signUp(signupRequest);

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals(userId, response.getUserId());
        assertEquals("testuser", response.getName());
        assertEquals("test@example.com", response.getEmail());
        Mockito.verify(userMapper).toEntity(signupRequest);
        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }

    @Test
    void testSignUpNameAlreadyExists() {
        Mockito.when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        Mockito.when(userRepository.existsByName(signupRequest.getName())).thenReturn(true);

        assertThrows(UserExistsException.class, () -> {
            userService.signUp(signupRequest);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void testLoginSuccess() {
        Mockito.when(userRepository.findByName(loginRequest.getName())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        Mockito.when(jwtUtil.generateToken(Mockito.any(UUID.class), Mockito.anyString(), Mockito.anyString()))
                .thenReturn("test-token");

        AuthResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals(userId, response.getUserId());
        assertEquals("testuser", response.getName());
        Mockito.verify(userRepository).findByName(loginRequest.getName());
    }

    @Test
    void testLoginUserNotFound() {
        Mockito.when(userRepository.findByName(loginRequest.getName())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.login(loginRequest);
        });

        Mockito.verify(userRepository).findByName(loginRequest.getName());
    }

    @Test
    void testLoginInvalidPassword() {
        Mockito.when(userRepository.findByName(loginRequest.getName())).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            userService.login(loginRequest);
        });

        Mockito.verify(userRepository).findByName(loginRequest.getName());
    }

    @Test
    void testRefreshAccessTokenSuccess() {
        Mockito.when(tokenBlacklistService.isTokenBlacklisted("refresh-token")).thenReturn(false);
        Mockito.when(jwtUtil.validateRefreshToken("refresh-token")).thenReturn(true);
        Mockito.when(jwtUtil.extractUserId("refresh-token")).thenReturn(userId);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(jwtUtil.generateToken(Mockito.any(UUID.class), Mockito.anyString(), Mockito.anyString()))
                .thenReturn("new-access-token");

        AuthResponse response = userService.refreshAccessToken("refresh-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.getToken());
        assertEquals("Access token refreshed successfully", response.getMessage());
    }

    @Test
    void testDeleteUserSuccess() {
        Mockito.when(httpServletRequest.getAttribute("userName")).thenReturn("testuser");
        Mockito.when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer test-token");
        Mockito.when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));

        String result = userService.deleteUser(httpServletRequest);

        assertEquals("User deleted successfully", result);
        Mockito.verify(userRepository).delete(user);
        Mockito.verify(tokenBlacklistService).blacklistToken("test-token");
    }

    @Test
    void testChangePasswordSuccess() {
        Mockito.when(httpServletRequest.getAttribute("userName")).thenReturn("testuser");
        Mockito.when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer test-token");
        Mockito.when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("oldPassword", user.getPassword())).thenReturn(true);
        Mockito.when(passwordEncoder.matches("newPassword", user.getPassword())).thenReturn(false);
        Mockito.when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        String result = userService.changePassword("oldPassword", "newPassword", httpServletRequest);

        assertEquals("Password changed successfully. Please login again with your new password.", result);
        Mockito.verify(userRepository).save(user);
        Mockito.verify(tokenBlacklistService).blacklistToken("test-token");
    }

    @Test
    void testChangePasswordIncorrectOldPassword() {
        Mockito.when(httpServletRequest.getAttribute("userName")).thenReturn("testuser");
        Mockito.when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            userService.changePassword("wrongPassword", "newPassword", httpServletRequest);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void testChangeNameSuccess() {
        Mockito.when(httpServletRequest.getAttribute("userName")).thenReturn("testuser");
        Mockito.when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
        Mockito.when(userRepository.existsByName("newname")).thenReturn(false);
        Mockito.when(jwtUtil.generateToken(Mockito.any(UUID.class), Mockito.anyString(), Mockito.anyString()))
                .thenReturn("new-token");

        AuthResponse response = userService.changeName("newname", httpServletRequest);

        assertNotNull(response);
        assertEquals("new-token", response.getToken());
        Mockito.verify(userRepository).save(user);
    }

    @Test
    void testChangeNameAlreadyTaken() {
        Mockito.when(httpServletRequest.getAttribute("userName")).thenReturn("testuser");
        Mockito.when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
        Mockito.when(userRepository.existsByName("newname")).thenReturn(true);

        assertThrows(UserExistsException.class, () -> {
            userService.changeName("newname", httpServletRequest);
        });

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void testGetUserRole() {
        Mockito.when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));

        String role = userService.getUserRole("testuser");

        assertEquals("user", role);
        Mockito.verify(userRepository).findByName("testuser");
    }

    @Test
    void testGetUserById() {
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userMapper.toDTO(user)).thenReturn(UserDTO.builder()
                .id(userId)
                .name("testuser")
                .email("test@example.com")
                .role(UserRole.user)
                .build());

        UserDTO userDTO = userService.getUserById(userId);

        assertNotNull(userDTO);
        assertEquals(userId, userDTO.getId());
        assertEquals("testuser", userDTO.getName());
        assertEquals("test@example.com", userDTO.getEmail());
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userMapper).toDTO(user);
    }

    @Test
    void testGetUserByIdNotFound() {
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        Mockito.verify(userRepository).findById(userId);
    }

    @Test
    void testGetAllUsers() {
        User user2 = User.builder()
                .id(UUID.randomUUID())
                .email("test2@example.com")
                .name("testuser2")
                .password("encodedPassword2")
                .role(UserRole.admin)
                .build();

        UserDTO userDTO1 = UserDTO.builder()
                .id(userId)
                .name("testuser")
                .email("test@example.com")
                .role(UserRole.user)
                .build();

        UserDTO userDTO2 = UserDTO.builder()
                .id(user2.getId())
                .name("testuser2")
                .email("test2@example.com")
                .role(UserRole.admin)
                .build();

        List<User> users = Arrays.asList(user, user2);
        Mockito.when(userRepository.findAll()).thenReturn(users);
        Mockito.when(userMapper.toDTO(user)).thenReturn(userDTO1);
        Mockito.when(userMapper.toDTO(user2)).thenReturn(userDTO2);

        List<UserDTO> userDTOs = userService.getAllUsers();

        assertNotNull(userDTOs);
        assertEquals(2, userDTOs.size());
        assertEquals("testuser", userDTOs.get(0).getName());
        assertEquals("testuser2", userDTOs.get(1).getName());
        Mockito.verify(userRepository).findAll();
        Mockito.verify(userMapper, Mockito.times(2)).toDTO(Mockito.any(User.class));
    }
}