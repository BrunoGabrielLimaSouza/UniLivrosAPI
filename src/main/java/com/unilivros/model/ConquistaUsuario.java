package com.unilivros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "conquista_usuarios")
public class ConquistaUsuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @NotNull(message = "Conquista é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conquista_id", nullable = false)
    private Conquista conquista;
    
    @CreationTimestamp
    @Column(name = "conquistada_em", nullable = false, updatable = false)
    private LocalDateTime conquistadaEm;
    
    // Construtores
    public ConquistaUsuario() {}
    
    public ConquistaUsuario(Usuario usuario, Conquista conquista) {
        this.usuario = usuario;
        this.conquista = conquista;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public Conquista getConquista() { return conquista; }
    public void setConquista(Conquista conquista) { this.conquista = conquista; }
    
    public LocalDateTime getConquistadaEm() { return conquistadaEm; }
    public void setConquistadaEm(LocalDateTime conquistadaEm) { this.conquistadaEm = conquistadaEm; }
}
