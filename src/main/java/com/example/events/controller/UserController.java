package com.example.events.controller;

import com.example.events.DTO.UserDTO;
import com.example.events.exception.UserAlreadyLoggedOutException;
import com.example.events.exception.UserExistsException;
import com.example.events.exception.UserNotFoundException;
import com.example.events.model.User;
import com.example.events.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> createUser(@RequestBody User user) throws UserExistsException {
        userService.signUp(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> logUser(@RequestBody User user, HttpServletRequest request) throws UserNotFoundException {
        return userService.login(user, request);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logOutUser(HttpServletRequest request) throws UserAlreadyLoggedOutException {
        return userService.logout(request);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestBody String name, HttpServletRequest request) throws
            UserAlreadyLoggedOutException, UserNotFoundException {
        return userService.delete(name, request);
    }

    @PatchMapping("/pass")
    public ResponseEntity<String> changePassword(@RequestBody String password, HttpServletRequest request)
            throws UserAlreadyLoggedOutException {
        return userService.changePassword(password, request);
    }

    @PatchMapping("/name")
    public ResponseEntity<String> changeName(@RequestBody String name, HttpServletRequest request)
            throws UserAlreadyLoggedOutException, UserExistsException {
        return userService.changeName(name, request);
    }

    @GetMapping("/type/{name}")
    public ResponseEntity<String> getUserRole(@PathVariable String name) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getUserRole(name));
    }

    @PostMapping("/")
    public ResponseEntity<UserDTO> getUserById(@RequestBody String idStr) throws UserNotFoundException {
        UUID id = UUID.fromString(idStr.trim());
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}