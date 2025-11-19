package com.unilivros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "troca_usuarios")
public class TrocaUsuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @NotNull(message = "Troca é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "troca_id", nullable = false)
    private Troca troca;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoParticipacao tipo = TipoParticipacao.PARTICIPANTE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public TrocaUsuario() {}
    
    public TrocaUsuario(Usuario usuario, Troca troca, TipoParticipacao tipo) {
        this.usuario = usuario;
        this.troca = troca;
        this.tipo = tipo;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public Troca getTroca() { return troca; }
    public void setTroca(Troca troca) { this.troca = troca; }
    
    public TipoParticipacao getTipo() { return tipo; }
    public void setTipo(TipoParticipacao tipo) { this.tipo = tipo; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public enum TipoParticipacao {
        PARTICIPANTE("Participante"),
        OBSERVADOR("Observador");
        
        private final String descricao;
        
        TipoParticipacao(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
}
