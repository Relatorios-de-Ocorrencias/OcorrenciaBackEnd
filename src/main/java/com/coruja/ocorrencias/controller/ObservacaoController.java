package com.coruja.ocorrencias.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coruja.ocorrencias.dto.request.ObservacoesRequestDTO;
import com.coruja.ocorrencias.dto.request.ObservacoesUpdateRequestDTO;
import com.coruja.ocorrencias.dto.response.ObservacoesResponseDTO;
import com.coruja.ocorrencias.service.OcorrenciaObservacaoService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller REST das observacoes de uma ocorrencia.
 * Expoe endpoints para criar, buscar e atualizar observacoes vinculadas a uma
 * ocorrencia.
 */
@RestController
@RequestMapping("/ocorrencias/{ocorrenciaId}/observacoes")
public class ObservacaoController {

    private final OcorrenciaObservacaoService service;

    public ObservacaoController(OcorrenciaObservacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ObservacoesResponseDTO> salvar(@PathVariable Long ocorrenciaId,
            @Valid @ModelAttribute ObservacoesRequestDTO dto) {

        ObservacoesResponseDTO response = service.salvar(ocorrenciaId, dto);

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObservacoesResponseDTO> buscaPorId(@PathVariable Long id) {

        ObservacoesResponseDTO observacoes = service.buscarPorId(id);

        return ResponseEntity.ok(observacoes);
    }

    @PutMapping("{id}")
    public ResponseEntity<ObservacoesResponseDTO> atualizaPorId(@PathVariable Long id,
            @Valid @ModelAttribute ObservacoesUpdateRequestDTO dto) {

        ObservacoesResponseDTO observacoes = service.atualizaPorId(id, dto);

        return ResponseEntity.ok(observacoes);

    }

    @GetMapping
    public ResponseEntity<List<ObservacoesResponseDTO>> listarPorOcorrencia(
            @PathVariable Long ocorrenciaId) {
        List<ObservacoesResponseDTO> response = service.listarPorOcorrencia(ocorrenciaId);

        return ResponseEntity.ok(response);
    }
}
