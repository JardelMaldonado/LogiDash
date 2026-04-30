package com.jardel.LogiDash.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UsuarioRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,
        String senha,
        @NotBlank(message = "Perfil é obrigatório")
        @Pattern(regexp = "ADMIN|USER", message = "Perfil deve ser ADMIN ou USER")
        String role
) {}
