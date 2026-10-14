package com.coruja.ocorrencias.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.coruja.ocorrencias.service.UsuarioAdminService;
import com.coruja.ocorrencias.service.UsuarioAdminService.AlterarUsuario;
import com.coruja.ocorrencias.service.UsuarioAdminService.CriarUsuario;
import com.coruja.ocorrencias.service.UsuarioAdminService.UsuarioResposta;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {
    private final UsuarioAdminService service;
    public UsuarioAdminController(UsuarioAdminService service) {
        this.service = service;
    }
    @GetMapping
    public List<UsuarioResposta> listar() { return service.listar(); }

    @PostMapping
    public ResponseEntity<UsuarioResposta> criar(@Valid @RequestBody CriarUsuario dados) {
        return ResponseEntity.status(201).body(service.criar(dados));
    }
    @PutMapping("/{id}")
    public UsuarioResposta alterar(@PathVariable Long id,
            @Valid @RequestBody AlterarUsuario dados) {
        return service.alterar(id, dados);
    }
}
