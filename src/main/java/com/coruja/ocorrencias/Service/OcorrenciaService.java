package com.coruja.ocorrencias.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coruja.ocorrencias.dto.request.OcorrenciaRequestDTO;
import com.coruja.ocorrencias.dto.response.OcorrenciaResponseDTO;
import com.coruja.ocorrencias.entity.OcorrenciaEntity;
import com.coruja.ocorrencias.entity.StatusOcorrenciaEntity;
import com.coruja.ocorrencias.entity.usuarios.Role;
import com.coruja.ocorrencias.entity.usuarios.Usuario;
import com.coruja.ocorrencias.mapper.OcorrenciaMapper;
import com.coruja.ocorrencias.repository.OcorrenciaRepository;
import com.coruja.ocorrencias.security.AcessoOcorrenciaService;

@Service
@Transactional(readOnly = true)
public class OcorrenciaService {

    private final OcorrenciaMapper ocorrenciaMapper;
    private final OcorrenciaRepository repository;
    private final AcessoOcorrenciaService acesso;

    public OcorrenciaService(
            OcorrenciaMapper ocorrenciaMapper,
            OcorrenciaRepository repository,
            AcessoOcorrenciaService acesso) {

        this.ocorrenciaMapper = ocorrenciaMapper;
        this.repository = repository;
        this.acesso = acesso;
    }

    @Transactional
    public OcorrenciaResponseDTO salvar(
            OcorrenciaRequestDTO dto,
            Long usuarioId) {

        Usuario usuario = acesso.usuarioAtual();

        if (!usuario.getId().equals(usuarioId)) {
            throw new AccessDeniedException("Acesso negado");
        }

        OcorrenciaEntity ocorrencia = ocorrenciaMapper.toEntity(dto);

        ocorrencia.setUsuario(usuario);

        OcorrenciaEntity ocorrenciaSalva = repository.save(ocorrencia);

        return ocorrenciaMapper.toDto(ocorrenciaSalva);
    }

    public OcorrenciaResponseDTO buscarPorId(Long id) {
        OcorrenciaEntity ocorrencia = acesso.buscarPermitida(id);

        return ocorrenciaMapper.toDto(ocorrencia);
    }

    @Transactional
    public OcorrenciaResponseDTO atualizaPorId(
            Long id,
            OcorrenciaRequestDTO dto) {

        OcorrenciaEntity ocorrencia = acesso.buscarPermitida(id);

        acesso.exigirEdicaoPermitida(ocorrencia);

        ocorrencia.setTitulo(dto.getTitulo());
        ocorrencia.setCliente(dto.getCliente());
        ocorrencia.setLocalizacao(dto.getLocalizacao());
        ocorrencia.setSetor(dto.getSetor());
        ocorrencia.setArea(dto.getArea());
        ocorrencia.setDepartamento(dto.getDepartamento());
        ocorrencia.setResponsavel(dto.getResponsavel());
        ocorrencia.setData_inspecao(
                LocalDate.parse(dto.getData_inspecao()));
        ocorrencia.setRevisao(dto.getRevisao());

        OcorrenciaEntity ocorrenciaSalva = repository.save(ocorrencia);

        return ocorrenciaMapper.toDto(ocorrenciaSalva);
    }

    @Transactional
    public OcorrenciaResponseDTO fechar(Long id) {
        OcorrenciaEntity ocorrencia = acesso.buscarPermitida(id);
        ocorrencia.setStatus(StatusOcorrenciaEntity.FINALIZADA);
        return ocorrenciaMapper.toDto(repository.save(ocorrencia));
    }

    @Transactional
    public OcorrenciaResponseDTO reabrir(Long id) {
        OcorrenciaEntity ocorrencia = acesso.buscarPermitida(id);
        ocorrencia.setStatus(StatusOcorrenciaEntity.DRAFT);
        return ocorrenciaMapper.toDto(repository.save(ocorrencia));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(acesso.buscarPermitida(id));
    }

    public List<OcorrenciaResponseDTO> buscarUltimasTresOcorrencias() {
        Usuario usuario = acesso.usuarioAtual();

        List<OcorrenciaEntity> ocorrencias;

        if (usuario.getRole() == Role.ADMIN) {
            ocorrencias = repository.findTop3ByOrderByCriadoEmDesc();
        } else if (usuario.getRole() == Role.USER) {
            ocorrencias = repository
                    .findTop3ByUsuario_IdOrderByCriadoEmDesc(
                            usuario.getId());
        } else {
            throw new AccessDeniedException("Acesso negado");
        }

        return ocorrencias.stream()
                .map(ocorrenciaMapper::toDto)
                .toList();
    }
}
