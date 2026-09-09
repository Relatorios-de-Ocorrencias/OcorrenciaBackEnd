package com.coruja.ocorrencias.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.coruja.ocorrencias.entity.usuarios.Usuario;

public class UsuarioDetails extends User {

    private final Long id;

    public UsuarioDetails(Usuario usuario) {
        super(
            usuario.getEmail(),
            usuario.getSenha(),
            usuario.isAtivo(),
            true,
            true,
            true,
            List.of(
                new SimpleGrantedAuthority(
                    "ROLE_" + usuario.getRole().name()
                )
            )
        );

        this.id = usuario.getId();
    }

    public Long getId() {
        return id;
    }
}