package com.jardel.LogiDash.dto.auth;

public record LoginResponse(String token, String nome, String email, String role) {
}
