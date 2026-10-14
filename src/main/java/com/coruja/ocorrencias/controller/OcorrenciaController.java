package com.coruja.ocorrencias.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coruja.ocorrencias.dto.request.OcorrenciaRequestDTO;
import com.coruja.ocorrencias.dto.response.OcorrenciaResponseDTO;
import com.coruja.ocorrencias.security.UsuarioDetails;
import com.coruja.ocorrencias.service.OcorrenciaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/ocorrencias")
public class OcorrenciaController {

    private final OcorrenciaService service;

    public OcorrenciaController(OcorrenciaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OcorrenciaResponseDTO> salvar(
            @Valid @RequestBody OcorrenciaRequestDTO dto,
            @AuthenticationPrincipal UsuarioDetails usuarioLogado) {

        OcorrenciaResponseDTO response =
                service.salvar(dto, usuarioLogado.getId());

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OcorrenciaResponseDTO> buscarPorId(
            @PathVariable Long id) {

        OcorrenciaResponseDTO response = service.buscarPorId(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OcorrenciaResponseDTO> atualizaPorId(
            @PathVariable Long id,
            @RequestBody OcorrenciaRequestDTO dto) {

        OcorrenciaResponseDTO response = service.atualizaPorId(id, dto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscarUltimasTresOcorrencia")
    public ResponseEntity<List<OcorrenciaResponseDTO>>
            buscarUltimasTresOcorrencias() {

        List<OcorrenciaResponseDTO> response =
                service.buscarUltimasTresOcorrencias();

        return ResponseEntity.ok(response);
    }
}