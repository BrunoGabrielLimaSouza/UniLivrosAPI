package com.unilivros.dto;

import com.unilivros.model.LivroProposta;
import jakarta.validation.constraints.NotNull;

public class LivroPropostaDTO {
    
    private Long id;
    
    @NotNull(message = "Livro é obrigatório")
    private Long livroId;
    private LivroDTO livro;
    
    @NotNull(message = "Proposta é obrigatória")
    private Long propostaId;
    
    @NotNull(message = "Tipo é obrigatório")
    private LivroProposta.TipoLivroProposta tipo;
    
    // Construtores
    public LivroPropostaDTO() {}
    
    public LivroPropostaDTO(Long livroId, Long propostaId, LivroProposta.TipoLivroProposta tipo) {
        this.livroId = livroId;
        this.propostaId = propostaId;
        this.tipo = tipo;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getLivroId() { return livroId; }
    public void setLivroId(Long livroId) { this.livroId = livroId; }
    
    public LivroDTO getLivro() { return livro; }
    public void setLivro(LivroDTO livro) { this.livro = livro; }
    
    public Long getPropostaId() { return propostaId; }
    public void setPropostaId(Long propostaId) { this.propostaId = propostaId; }
    
    public LivroProposta.TipoLivroProposta getTipo() { return tipo; }
    public void setTipo(LivroProposta.TipoLivroProposta tipo) { this.tipo = tipo; }
}
