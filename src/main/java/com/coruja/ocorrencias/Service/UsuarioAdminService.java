package com.coruja.ocorrencias.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.coruja.ocorrencias.entity.usuarios.Role;
import com.coruja.ocorrencias.entity.usuarios.Usuario;
import com.coruja.ocorrencias.exception.BusinessException;
import com.coruja.ocorrencias.exception.NotFoundException;
import com.coruja.ocorrencias.repository.UsuarioRepository;
import com.coruja.ocorrencias.security.AcessoOcorrenciaService;
import jakarta.validation.constraints.*;

@Service
@Transactional(readOnly = true)
public class UsuarioAdminService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final AcessoOcorrenciaService acesso;

    public UsuarioAdminService(UsuarioRepository repository,
            PasswordEncoder encoder, AcessoOcorrenciaService acesso) {
        this.repository = repository;
        this.encoder = encoder;
        this.acesso = acesso;
    }

    public List<UsuarioResposta> listar() {
        exigirAdmin();
        return repository.findAll().stream().map(this::resposta).toList();
    }

    @Transactional
    public UsuarioResposta criar(CriarUsuario dados) {
        exigirAdmin();
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        if (repository.findByEmail(email).isPresent()) {
            throw new BusinessException("Email ja cadastrado");
        }
        if (dados.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("Senha deve ter no maximo 72 bytes");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(encoder.encode(dados.senha()));
        usuario.setRole(dados.role());
        usuario.setAtivo(true);
        return resposta(repository.save(usuario));
    }

    @Transactional
    public UsuarioResposta alterar(Long id, AlterarUsuario dados) {
        Usuario administrador = exigirAdmin();
        if (administrador.getId().equals(id)
                && (dados.role() != Role.ADMIN || !dados.ativo())) {
            throw new BusinessException(
                    "Voce nao pode desativar ou rebaixar sua propria conta");
        }
        Usuario usuario = repository.findById(id).orElseThrow(() ->
                new NotFoundException("Usuario nao encontrado"));
        usuario.setRole(dados.role());
        usuario.setAtivo(dados.ativo());
        return resposta(repository.save(usuario));
    }

    private Usuario exigirAdmin() {
        Usuario usuario = acesso.usuarioAtual();
        if (usuario.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Acesso negado");
        }
        return usuario;
    }

    private UsuarioResposta resposta(Usuario usuario) {
        return new UsuarioResposta(usuario.getId(), usuario.getNome(),
                usuario.getEmail(), usuario.getRole(), usuario.isAtivo());
    }

    public record CriarUsuario(
            @NotBlank @Size(max = 255) String nome,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(min = 12, max = 72) String senha,
            @NotNull Role role) {}

    public record AlterarUsuario(@NotNull Role role, @NotNull Boolean ativo) {}

    public record UsuarioResposta(Long id, String nome, String email,
            Role role, boolean ativo) {}
}
