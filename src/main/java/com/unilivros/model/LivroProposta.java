package com.unilivros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "livro_propostas")
public class LivroProposta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Livro é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;
    
    @NotNull(message = "Proposta é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposta_id", nullable = false)
    private Proposta proposta;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLivroProposta tipo = TipoLivroProposta.OFERTA;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Construtores
    public LivroProposta() {}
    
    public LivroProposta(Livro livro, Proposta proposta, TipoLivroProposta tipo) {
        this.livro = livro;
        this.proposta = proposta;
        this.tipo = tipo;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
    
    public Proposta getProposta() { return proposta; }
    public void setProposta(Proposta proposta) { this.proposta = proposta; }
    
    public TipoLivroProposta getTipo() { return tipo; }
    public void setTipo(TipoLivroProposta tipo) { this.tipo = tipo; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Enum para tipo de livro na proposta
    public enum TipoLivroProposta {
        OFERTA("Oferta"),
        SOLICITACAO("Solicitação");
        
        private final String descricao;
        
        TipoLivroProposta(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
}
