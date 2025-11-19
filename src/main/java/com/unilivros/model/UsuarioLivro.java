package com.unilivros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_livros")
public class UsuarioLivro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @NotNull(message = "Livro é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;
    
    @Column(name = "disponivel_para_troca", nullable = false)
    private Boolean disponivelParaTroca = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public UsuarioLivro() {}
    
    public UsuarioLivro(Usuario usuario, Livro livro) {
        this.usuario = usuario;
        this.livro = livro;
    }
    
    public UsuarioLivro(Usuario usuario, Livro livro, Boolean disponivelParaTroca) {
        this.usuario = usuario;
        this.livro = livro;
        this.disponivelParaTroca = disponivelParaTroca;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
    
    public Boolean getDisponivelParaTroca() { return disponivelParaTroca; }
    public void setDisponivelParaTroca(Boolean disponivelParaTroca) { this.disponivelParaTroca = disponivelParaTroca; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
