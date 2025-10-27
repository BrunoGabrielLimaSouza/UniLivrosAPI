package com.unilivros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conquistas")
public class Conquista {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;
    
    @NotBlank(message = "Descrição é obrigatória")
    @Column(nullable = false, length = 500)
    private String descricao;
    
    @NotBlank(message = "Ícone é obrigatório")
    @Column(nullable = false)
    private String icone;
    
    @NotNull(message = "XP necessário é obrigatório")
    @Column(name = "xp_necessario", nullable = false)
    private Integer xpNecessario;
    
    @NotNull(message = "Tipo é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoConquista tipo;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relacionamentos
    @OneToMany(mappedBy = "conquista", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConquistaUsuario> usuarios = new ArrayList<>();
    
    // Construtores
    public Conquista() {}
    
    public Conquista(String nome, String descricao, String icone, Integer xpNecessario, TipoConquista tipo) {
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
    
    public TipoConquista getTipo() { return tipo; }
    public void setTipo(TipoConquista tipo) { this.tipo = tipo; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<ConquistaUsuario> getUsuarios() { return usuarios; }
    public void setUsuarios(List<ConquistaUsuario> usuarios) { this.usuarios = usuarios; }
    
    // Enum para tipo de conquista
    public enum TipoConquista {
        PRIMEIRA_TROCA("Primeira Troca"),
        TROCAS_MULTIPLAS("Múltiplas Trocas"),
        AVALIACAO_ALTA("Avaliação Alta"),
        LIVROS_DOADOS("Livros Doados"),
        PARTICIPACAO_ATIVA("Participação Ativa");
        
        private final String descricao;
        
        TipoConquista(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
}
