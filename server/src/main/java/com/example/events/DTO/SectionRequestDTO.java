package com.example.events.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionRequestDTO {

    @NotBlank(message = "Section name is required")
    @Size(max = 100, message = "Section name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    @NotNull(message = "Rows count is required")
    @Min(value = 1, message = "Rows count must be at least 1")
    private Integer rows;

    @NotNull(message = "Columns count is required")
    @Min(value = 1, message = "Columns count must be at least 1")
    private Integer cols;
}