package com.jardel.LogiDash.service;

import com.jardel.LogiDash.database.model.UsuarioEntity;
import com.jardel.LogiDash.database.repository.IUsuarioRepository;
import com.jardel.LogiDash.dto.usuario.UsuarioRequest;
import com.jardel.LogiDash.dto.usuario.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getRole().name(), u.isAtivo()))
                .toList();
    }

    public UsuarioResponse criar(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        var usuario = UsuarioEntity.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .role(UsuarioEntity.Role.valueOf(request.role()))
                .ativo(true)
                .build();
        var salvo = usuarioRepository.save(usuario);
        return new UsuarioResponse(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getRole().name(), salvo.isAtivo());
    }

    public UsuarioResponse editar(Long id, UsuarioRequest request) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setRole(UsuarioEntity.Role.valueOf(request.role()));
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }
        var salvo = usuarioRepository.save(usuario);
        return new UsuarioResponse(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getRole().name(), salvo.isAtivo());
    }

    public UsuarioResponse alterarStatus(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        usuario.setAtivo(!usuario.isAtivo());
        var salvo = usuarioRepository.save(usuario);
        return new UsuarioResponse(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getRole().name(), salvo.isAtivo());
    }
}
