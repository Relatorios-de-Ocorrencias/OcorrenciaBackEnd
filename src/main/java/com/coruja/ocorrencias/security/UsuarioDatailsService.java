package com.coruja.ocorrencias.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.coruja.ocorrencias.entity.usuarios.Usuario;
import com.coruja.ocorrencias.repository.UsuarioRepository;

public class UsuarioDatailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDatailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        this.usuarioRepository = usuarioRepository;

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> 
                        new UsernameNotFoundException("Usuario nao encontrado"));

        return new UsuarioDetails(usuario);

    }

}
