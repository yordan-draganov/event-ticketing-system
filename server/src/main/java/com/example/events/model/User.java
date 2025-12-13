package com.example.events.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NonNull
    @Column(nullable = false, length = 255)
    private String email;

    @NonNull
    @Column(nullable = false, length = 255)
    private String password;

    @NonNull
    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserRole role;


    @Column(name = "created_at", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}