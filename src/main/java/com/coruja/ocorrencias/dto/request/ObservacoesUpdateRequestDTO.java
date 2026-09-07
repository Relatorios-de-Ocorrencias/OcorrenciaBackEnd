package com.coruja.ocorrencias.dto.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * DTO usado apenas na edicao da observacao.
 * Aqui as imagens sao opcionais, porque a observacao pode ser editada sem trocar fotos.
 */
public class ObservacoesUpdateRequestDTO {

    private String titulo;

    private String descricao;

    private List<MultipartFile> imagens;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<MultipartFile> getImagens() {
        return imagens;
    }

    public void setImagens(List<MultipartFile> imagens) {
        this.imagens = imagens;
    }
}
