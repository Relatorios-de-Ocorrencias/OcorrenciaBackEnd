package com.coruja.ocorrencias.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

@RestController
@RequestMapping("/ocorrencias/{ocorrenciaId}/observacoes")
public class ObservacaoController {

    private final OcorrenciaObservacaoService service;

    public ObservacaoController(OcorrenciaObservacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ObservacoesResponseDTO> salvar(
            @PathVariable Long ocorrenciaId,
            @Valid @ModelAttribute ObservacoesRequestDTO dto) {

        return ResponseEntity.status(201)
                .body(service.salvar(ocorrenciaId, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObservacoesResponseDTO> buscaPorId(
            @PathVariable Long ocorrenciaId,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.buscarPorId(ocorrenciaId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ObservacoesResponseDTO> atualizaPorId(
            @PathVariable Long ocorrenciaId,
            @PathVariable Long id,
            @Valid @ModelAttribute ObservacoesUpdateRequestDTO dto) {

        return ResponseEntity.ok(
                service.atualizaPorId(ocorrenciaId, id, dto));
    }

    @GetMapping
    public ResponseEntity<List<ObservacoesResponseDTO>>
            listarPorOcorrencia(
                    @PathVariable Long ocorrenciaId) {

        return ResponseEntity.ok(
                service.listarPorOcorrencia(ocorrenciaId));
    }
}