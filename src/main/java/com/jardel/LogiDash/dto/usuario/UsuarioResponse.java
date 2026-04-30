package com.jardel.LogiDash.dto.usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String role,
        boolean ativo
) {}
