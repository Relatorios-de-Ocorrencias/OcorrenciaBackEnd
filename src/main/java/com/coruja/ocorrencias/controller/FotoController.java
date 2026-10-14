package com.coruja.ocorrencias.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.coruja.ocorrencias.config.FileStorageConfig;
import com.coruja.ocorrencias.entity.FotoOcorrenciaEntity;
import com.coruja.ocorrencias.exception.NotFoundException;
import com.coruja.ocorrencias.repository.FotoRepository;
import com.coruja.ocorrencias.security.AcessoOcorrenciaService;

@RestController
public class FotoController {

    private final FotoRepository repository;
    private final AcessoOcorrenciaService acesso;
    private final FileStorageConfig armazenamento;

    public FotoController(
            FotoRepository repository,
            AcessoOcorrenciaService acesso,
            FileStorageConfig armazenamento) {

        this.repository = repository;
        this.acesso = acesso;
        this.armazenamento = armazenamento;
    }

    @GetMapping("/arquivos/{nome}")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> buscar(
            @PathVariable String nome) throws IOException {

        if (nome.contains("/") || nome.contains("\\")) {
            throw new NotFoundException("Foto nao encontrada");
        }

        FotoOcorrenciaEntity foto = repository
                .findByUrlFoto("ocorrencias/" + nome)
                .orElseThrow(() ->
                        new NotFoundException("Foto nao encontrada"));

        acesso.buscarPermitida(
                foto.getObservacao().getOcorrencia().getId());

        Path raiz = Path.of(armazenamento.getUploadDir())
                .toAbsolutePath()
                .normalize()
                .toRealPath();

        Path caminho = raiz.resolve(nome).normalize();

        if (!caminho.startsWith(raiz)
                || !Files.isRegularFile(caminho)) {

            throw new NotFoundException("Foto nao encontrada");
        }

        Path real = caminho.toRealPath();

        if (!real.startsWith(raiz)) {
            throw new NotFoundException("Foto nao encontrada");
        }

        String tipo = Files.probeContentType(real);

        MediaType mediaType = tipo == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(tipo);

        Resource recurso = new UrlResource(real.toUri());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("Cache-Control", "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .body(recurso);
    }
}