package com.coruja.ocorrencias.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.coruja.ocorrencias.dto.request.ObservacoesRequestDTO;
import com.coruja.ocorrencias.dto.request.ObservacoesUpdateRequestDTO;
import com.coruja.ocorrencias.dto.response.ObservacoesResponseDTO;
import com.coruja.ocorrencias.entity.FotoOcorrenciaEntity;
import com.coruja.ocorrencias.entity.OcorrenciaEntity;
import com.coruja.ocorrencias.entity.ObservacaoOcorrenciaEntity;
import com.coruja.ocorrencias.exception.NotFoundException;
import com.coruja.ocorrencias.mapper.ObservacaoMapper;
import com.coruja.ocorrencias.mapper.OcorrenciaMapper;
import com.coruja.ocorrencias.repository.ObservacaoRepository;
import com.coruja.ocorrencias.repository.OcorrenciaRepository;
import com.coruja.ocorrencias.service.validation.validaFormatoFoto;
import com.coruja.ocorrencias.service.validation.storage.FotoStorageService;

/**
 * Service de observacoes da ocorrencia.
 * Centraliza as regras para criar, buscar e atualizar observacoes, validando e salvando fotos.
 */
@Service
public class OcorrenciaObservacaoService {

    private final ObservacaoMapper observacoesMapper;
    private final ObservacaoRepository observacaoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final validaFormatoFoto validaFormatoFoto;
    private final FotoStorageService fotoStorageService;
    private final OcorrenciaRepository repository;

    public OcorrenciaObservacaoService(ObservacaoMapper observacoesMapper, ObservacaoRepository observacaoRepository,
            OcorrenciaRepository ocorrenciaRepository,
            com.coruja.ocorrencias.service.validation.validaFormatoFoto validaFormatoFoto,
            FotoStorageService fotoStorageService, OcorrenciaRepository repository, OcorrenciaMapper ocorrenciaMapper) {
        this.observacoesMapper = observacoesMapper;
        this.observacaoRepository = observacaoRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.validaFormatoFoto = validaFormatoFoto;
        this.fotoStorageService = fotoStorageService;
        this.repository = repository;
    }

    public ObservacoesResponseDTO salvar(Long ocorrenciaId, ObservacoesRequestDTO dto) {

        OcorrenciaEntity ocorrencia = ocorrenciaRepository.findById(ocorrenciaId)
                .orElseThrow(() -> new NotFoundException("Ocorrencia nao encontrada"));

        validaFormatoFoto.salvarFoto(dto);

        ObservacaoOcorrenciaEntity observacao = observacoesMapper.toEntity(dto);
        observacao.setOcorrencia(ocorrencia);

        List<String> caminhosFotos = fotoStorageService.salvarFoto(dto);

        for (String caminhoFoto : caminhosFotos) {
            FotoOcorrenciaEntity foto = new FotoOcorrenciaEntity();
            foto.setUrlFoto(caminhoFoto);
            foto.setObservacao(observacao);
            observacao.getFotos().add(foto);
        }

        ObservacaoOcorrenciaEntity observacaoSalva = observacaoRepository.save(observacao);

        return observacoesMapper.toDto(observacaoSalva);
    }

    public ObservacoesResponseDTO buscarPorId(Long id) {

        ObservacaoOcorrenciaEntity observacao = observacaoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ocorrencia nao encontrada"));

        return observacoesMapper.toDto(observacao);
    }

    public ObservacoesResponseDTO atualizaPorId (Long id, ObservacoesUpdateRequestDTO dto){

        ObservacaoOcorrenciaEntity observacao = observacaoRepository.findById(id).orElseThrow(() -> new NotFoundException("Observacao nao encontrada"));

        observacao.setTitulo(dto.getTitulo());
        observacao.setDescricao(dto.getDescricao());

        if (dto.getImagens() != null && !dto.getImagens().isEmpty()) {
            ObservacoesRequestDTO dtoComFotos = new ObservacoesRequestDTO();
            dtoComFotos.setTitulo(dto.getTitulo());
            dtoComFotos.setDescricao(dto.getDescricao());
            dtoComFotos.setImagens(dto.getImagens());

            validaFormatoFoto.salvarFoto(dtoComFotos);

            List<String> caminhosFotos = fotoStorageService.salvarFoto(dtoComFotos);

            for (String caminhoFoto : caminhosFotos) {
                FotoOcorrenciaEntity foto = new FotoOcorrenciaEntity();
                foto.setUrlFoto(caminhoFoto);
                foto.setObservacao(observacao);
                observacao.getFotos().add(foto);
            }
        }
        ObservacaoOcorrenciaEntity observacaoSalva = observacaoRepository.save(observacao);

    return observacoesMapper.toDto(observacaoSalva);

    }

    public List<ObservacoesResponseDTO> listarPorOcorrencia(Long ocorrenciaId) {
        ocorrenciaRepository.findById(ocorrenciaId)
            .orElseThrow(() -> new NotFoundException("Ocorrencia nao encontrada"));

        return observacaoRepository.findByOcorrencia_Id(ocorrenciaId)
                .stream()
                .map(observacoesMapper::toDto)
                .toList();
    }
}


