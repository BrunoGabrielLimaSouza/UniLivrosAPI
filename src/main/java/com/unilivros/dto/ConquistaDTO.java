package com.unilivros.dto;

import com.unilivros.model.Conquista;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ConquistaDTO {
    
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;
    
    @NotBlank(message = "Ícone é obrigatório")
    private String icone;
    
    @NotNull(message = "XP necessário é obrigatório")
    private Integer xpNecessario;
    
    @NotNull(message = "Tipo é obrigatório")
    private Conquista.TipoConquista tipo;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Construtores
    public ConquistaDTO() {}
    
    public ConquistaDTO(String nome, String descricao, String icone, Integer xpNecessario, Conquista.TipoConquista tipo) {
        this.nome = nome;
        this.descricao = descricao;
        this.icone = icone;
        this.xpNecessario = xpNecessario;
        this.tipo = tipo;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }
    
    public Integer getXpNecessario() { return xpNecessario; }
    public void setXpNecessario(Integer xpNecessario) { this.xpNecessario = xpNecessario; }
    
    public Conquista.TipoConquista getTipo() { return tipo; }
    public void setTipo(Conquista.TipoConquista tipo) { this.tipo = tipo; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
