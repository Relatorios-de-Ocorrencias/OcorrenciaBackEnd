package com.coruja.ocorrencias.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coruja.ocorrencias.dto.request.ObservacoesRequestDTO;
import com.coruja.ocorrencias.dto.request.ObservacoesUpdateRequestDTO;
import com.coruja.ocorrencias.dto.response.ObservacoesResponseDTO;
import com.coruja.ocorrencias.entity.FotoOcorrenciaEntity;
import com.coruja.ocorrencias.entity.OcorrenciaEntity;
import com.coruja.ocorrencias.entity.ObservacaoOcorrenciaEntity;
import com.coruja.ocorrencias.exception.NotFoundException;
import com.coruja.ocorrencias.mapper.ObservacaoMapper;
import com.coruja.ocorrencias.repository.ObservacaoRepository;
import com.coruja.ocorrencias.security.AcessoOcorrenciaService;
import com.coruja.ocorrencias.service.validation.validaFormatoFoto;
import com.coruja.ocorrencias.service.validation.storage.FotoStorageService;

@Service
@Transactional(readOnly = true)
public class OcorrenciaObservacaoService {

    private final ObservacaoMapper mapper;
    private final ObservacaoRepository repository;
    private final AcessoOcorrenciaService acesso;
    private final validaFormatoFoto validador;
    private final FotoStorageService armazenamento;

    public OcorrenciaObservacaoService(
            ObservacaoMapper mapper,
            ObservacaoRepository repository,
            AcessoOcorrenciaService acesso,
            validaFormatoFoto validador,
            FotoStorageService armazenamento) {

        this.mapper = mapper;
        this.repository = repository;
        this.acesso = acesso;
        this.validador = validador;
        this.armazenamento = armazenamento;
    }

    @Transactional
    public ObservacoesResponseDTO salvar(
            Long ocorrenciaId,
            ObservacoesRequestDTO dto) {

        OcorrenciaEntity ocorrencia =
                acesso.buscarPermitida(ocorrenciaId);

        acesso.exigirEdicaoPermitida(ocorrencia);

        ObservacaoOcorrenciaEntity observacao = mapper.toEntity(dto);
        observacao.setOcorrencia(ocorrencia);

        adicionarFotos(observacao, dto);

        return mapper.toDto(repository.save(observacao));
    }

    public ObservacoesResponseDTO buscarPorId(
            Long ocorrenciaId,
            Long observacaoId) {

        acesso.buscarPermitida(ocorrenciaId);

        ObservacaoOcorrenciaEntity observacao =
                buscarObservacao(ocorrenciaId, observacaoId);

        return mapper.toDto(observacao);
    }

    @Transactional
    public ObservacoesResponseDTO atualizaPorId(
            Long ocorrenciaId,
            Long observacaoId,
            ObservacoesUpdateRequestDTO dto) {

        OcorrenciaEntity ocorrencia =
                acesso.buscarPermitida(ocorrenciaId);

        acesso.exigirEdicaoPermitida(ocorrencia);

        ObservacaoOcorrenciaEntity observacao =
                buscarObservacao(ocorrenciaId, observacaoId);

        observacao.setTitulo(dto.getTitulo());
        observacao.setDescricao(dto.getDescricao());

        ObservacoesRequestDTO fotos = new ObservacoesRequestDTO();
        fotos.setImagens(dto.getImagens());

        adicionarFotos(observacao, fotos);

        return mapper.toDto(repository.save(observacao));
    }

    public List<ObservacoesResponseDTO> listarPorOcorrencia(
            Long ocorrenciaId) {

        acesso.buscarPermitida(ocorrenciaId);

        return repository.findByOcorrencia_Id(ocorrenciaId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    private ObservacaoOcorrenciaEntity buscarObservacao(
            Long ocorrenciaId,
            Long observacaoId) {

        ObservacaoOcorrenciaEntity observacao =
                repository.findById(observacaoId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Observacao nao encontrada"));

        if (!observacao.getOcorrencia().getId()
                .equals(ocorrenciaId)) {

            throw new NotFoundException("Observacao nao encontrada");
        }

        return observacao;
    }

    private void adicionarFotos(
            ObservacaoOcorrenciaEntity observacao,
            ObservacoesRequestDTO dto) {

        if (dto.getImagens() == null || dto.getImagens().isEmpty()) {
            return;
        }

        validador.salvarFoto(dto);

        List<String> caminhos = armazenamento.salvarFoto(dto);

        for (String caminho : caminhos) {
            FotoOcorrenciaEntity foto = new FotoOcorrenciaEntity();

            foto.setUrlFoto(caminho);
            foto.setObservacao(observacao);

            observacao.getFotos().add(foto);
        }
    }
}