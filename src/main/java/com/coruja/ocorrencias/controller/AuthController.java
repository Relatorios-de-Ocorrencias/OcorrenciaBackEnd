package com.coruja.ocorrencias.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import com.coruja.ocorrencias.entity.usuarios.Role;
import com.coruja.ocorrencias.entity.usuarios.Usuario;
import com.coruja.ocorrencias.security.AcessoOcorrenciaService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AcessoOcorrenciaService acesso;
    public AuthController(AcessoOcorrenciaService acesso) { this.acesso = acesso; }

    @GetMapping("/csrf")
    public CsrfResposta csrf(CsrfToken csrf) {
        return new CsrfResposta(csrf.getHeaderName(), csrf.getToken());
    }
    @GetMapping("/me")
    public UsuarioResposta me() {
        Usuario usuario = acesso.usuarioAtual();
        return new UsuarioResposta(usuario.getId(), usuario.getNome(),
                usuario.getEmail(), usuario.getRole());
    }
    public record CsrfResposta(String headerName, String token) {}
    public record UsuarioResposta(Long id, String nome, String email, Role role) {}
}
