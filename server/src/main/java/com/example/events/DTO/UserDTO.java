package com.example.events.DTO;

import com.example.events.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private UUID id;
    private String email;
    private String name;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}