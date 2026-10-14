package com.coruja.ocorrencias.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coruja.ocorrencias.entity.OcorrenciaEntity;

public interface OcorrenciaRepository
        extends JpaRepository<OcorrenciaEntity, Long> {

    Optional<OcorrenciaEntity> findByIdAndUsuario_Id(
            Long id,
            Long usuarioId
    );

    List<OcorrenciaEntity> findTop3ByOrderByCriadoEmDesc();

    List<OcorrenciaEntity> findTop3ByUsuario_IdOrderByCriadoEmDesc(
            Long usuarioId
    );
}