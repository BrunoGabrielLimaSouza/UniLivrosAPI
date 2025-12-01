package com.unilivros.dto;

public class AvaliacaoDTO {

    private Double nota;
    private String comentario;
    private String nomeAvaliador;

    public AvaliacaoDTO() {}

    public AvaliacaoDTO(Double nota, String comentario, String nomeAvaliador) {
        this.nota = nota;
        this.comentario = comentario;
        this.nomeAvaliador = nomeAvaliador;
    }

    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public String getNomeAvaliador() { return nomeAvaliador; }
    public void setNomeAvaliador(String nomeAvaliador) { this.nomeAvaliador = nomeAvaliador; }
}