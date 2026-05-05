package com.jardel.LogiDash.service;

import com.jardel.LogiDash.dto.auth.LoginRequest;
import com.jardel.LogiDash.dto.auth.LoginResponse;
import com.jardel.LogiDash.database.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        var token = jwtService.gerarToken(usuario);

        return new LoginResponse(token, usuario.getNome(), usuario.getEmail(), usuario.getRole().name());
    }
}
