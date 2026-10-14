package com.coruja.ocorrencias.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coruja.ocorrencias.entity.FotoOcorrenciaEntity;

public interface FotoRepository
        extends JpaRepository<FotoOcorrenciaEntity, Long> {

    Optional<FotoOcorrenciaEntity> findByUrlFoto(String urlFoto);
}