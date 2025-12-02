package com.example.demo.infraestructura.api.dto.security;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDTO {
	@NotBlank
    private String username;
    
    @NotBlank
    private String password;
}
