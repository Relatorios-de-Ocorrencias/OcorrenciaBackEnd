package com.coruja.ocorrencias.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coruja.ocorrencias.entity.usuarios.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
}
