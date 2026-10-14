package com.coruja.ocorrencias.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coruja.ocorrencias.entity.OcorrenciaEntity;
import com.coruja.ocorrencias.entity.StatusOcorrenciaEntity;
import com.coruja.ocorrencias.entity.usuarios.Role;
import com.coruja.ocorrencias.entity.usuarios.Usuario;
import com.coruja.ocorrencias.exception.BusinessException;
import com.coruja.ocorrencias.exception.NotFoundException;
import com.coruja.ocorrencias.repository.OcorrenciaRepository;
import com.coruja.ocorrencias.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class AcessoOcorrenciaService {

    private final UsuarioRepository usuarioRepository;
    private final OcorrenciaRepository ocorrenciaRepository;

    public AcessoOcorrenciaService(
            UsuarioRepository usuarioRepository,
            OcorrenciaRepository ocorrenciaRepository) {

        this.usuarioRepository = usuarioRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
    }

    public Usuario usuarioAtual() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                        instanceof UsuarioDetails details)) {

            throw new AccessDeniedException("Acesso negado");
        }

        Usuario usuario = usuarioRepository.findById(details.getId())
                .orElseThrow(() ->
                        new AccessDeniedException("Acesso negado"));

        if (!usuario.isAtivo()) {
            throw new AccessDeniedException("Acesso negado");
        }

        return usuario;
    }

    public OcorrenciaEntity buscarPermitida(Long ocorrenciaId) {
        Usuario usuario = usuarioAtual();

        if (usuario.getRole() == Role.ADMIN) {
            return ocorrenciaRepository.findById(ocorrenciaId)
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "Ocorrencia nao encontrada"));
        }

        if (usuario.getRole() != Role.USER) {
            throw new AccessDeniedException("Acesso negado");
        }

        return ocorrenciaRepository
                .findByIdAndUsuario_Id(ocorrenciaId, usuario.getId())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Ocorrencia nao encontrada"));
    }

    public void exigirEdicaoPermitida(OcorrenciaEntity ocorrencia) {
        Usuario usuario = usuarioAtual();

        if (usuario.getRole() != Role.ADMIN
                && ocorrencia.getStatus()
                        == StatusOcorrenciaEntity.FINALIZADA) {

            throw new BusinessException(
                    "Reabra a ocorrencia antes de editar");
        }
    }
}