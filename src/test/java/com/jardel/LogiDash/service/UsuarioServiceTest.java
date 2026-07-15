package com.jardel.LogiDash.service;

import com.jardel.LogiDash.database.model.UsuarioEntity;
import com.jardel.LogiDash.database.repository.IUsuarioRepository;
import com.jardel.LogiDash.dto.usuario.UsuarioRequest;
import com.jardel.LogiDash.dto.usuario.UsuarioResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("deve retornar uma excecao com email duplicado")
    void quandoEmailDuplicado_deveRetornarExcecao() {

        UsuarioRequest request = new UsuarioRequest("Nome", "email@example.com", "senha123", "ADMIN");

        when(usuarioRepository.existsByEmail("email@example.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(request)).isInstanceOf(IllegalArgumentException.class).hasMessage("Email já cadastrado");

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("deve criar um usuario corretamente")
    void quandoEmailNaoDuplicado_deveRetornarUsuarioCriado() {

        UsuarioRequest request = new UsuarioRequest("Nome", "email@example.com", "senha123", "ADMIN");

        UsuarioEntity usuarioSalvo = new UsuarioEntity();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setNome("Nome");
        usuarioSalvo.setEmail("email@example.com");
        usuarioSalvo.setSenha("hash_da_senha");
        usuarioSalvo.setRole(UsuarioEntity.Role.ADMIN);
        usuarioSalvo.setAtivo(true);

        when(usuarioRepository.existsByEmail("email@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash_da_senha");
        when(usuarioRepository.save(any())).thenReturn(usuarioSalvo);

        UsuarioResponse resultado = usuarioService.criar(request);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(resultado.nome()).isEqualTo("Nome");
        assertThat(resultado.email()).isEqualTo("email@example.com");
        assertThat(resultado.role()).isEqualTo("ADMIN");


        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getSenha()).isEqualTo("hash_da_senha");

        verify(passwordEncoder).encode("senha123");
    }

    @Test
    @DisplayName("deve alterar status do usuario corretamente")
    void quandoUsuarioExistir_deveAlternarStatus() {

        UsuarioEntity usuarioSalvo = new UsuarioEntity();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setNome("Nome");
        usuarioSalvo.setEmail("email@example.com");
        usuarioSalvo.setSenha("senha123");
        usuarioSalvo.setRole(UsuarioEntity.Role.ADMIN);
        usuarioSalvo.setAtivo(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioSalvo));
        when(usuarioRepository.save(any())).thenReturn(usuarioSalvo);

        usuarioService.alterarStatus(1L);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().isAtivo()).isFalse();
    }

    @Test
    @DisplayName("deve editar dados do usuario corretamente sem necessidade de alterar a senha")
    void quandoSenhaNula_deveEditarDadosSemAlterarSenha() {

        UsuarioRequest request = new UsuarioRequest("Nome Novo", "novo@example.com", null, "ADMIN");

        UsuarioEntity usuarioSalvo = new UsuarioEntity();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setNome("Nome Antigo");
        usuarioSalvo.setEmail("antigo@example.com");
        usuarioSalvo.setSenha("hash_antigo");
        usuarioSalvo.setRole(UsuarioEntity.Role.ADMIN);
        usuarioSalvo.setAtivo(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioSalvo));
        when(usuarioRepository.save(any())).thenReturn(usuarioSalvo);

        usuarioService.editar(1L, request);

        verifyNoInteractions(passwordEncoder);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());

        assertThat(captor.getValue().getNome()).isEqualTo("Nome Novo");
        assertThat(captor.getValue().getEmail()).isEqualTo("novo@example.com");
        assertThat(captor.getValue().getSenha()).isEqualTo("hash_antigo");
    }

    @Test
    @DisplayName("deve retornar exceção ao tentar alterar status de usuário inexistente")
    void quandoIdNaoExistir_deveRetornarExcecaoAoAlterarStatus() {

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> usuarioService.alterarStatus(99L)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Usuário não encontrado");

        verify(usuarioRepository, never()).save(any());
    }
}