package com.unilivros.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "livros")
public class Livro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_id", unique = false)
    private String googleId;
    
    @NotBlank(message = "Título é obrigatório")
    @Size(min = 1, max = 200, message = "Título deve ter entre 1 e 200 caracteres")
    @Column(nullable = false)
    private String titulo;
    
    @NotBlank(message = "Autor é obrigatório")
    @Size(min = 1, max = 100, message = "Autor deve ter entre 1 e 100 caracteres")
    @Column(nullable = false)
    private String autor;
    
    @NotBlank(message = "Editora é obrigatória")
    @Size(min = 1, max = 100, message = "Editora deve ter entre 1 e 100 caracteres")
    @Column(nullable = false)
    private String editora;
    
    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1000, message = "Ano deve ser válido")
    @Max(value = 2024, message = "Ano não pode ser futuro")
    @Column(nullable = false)
    private Integer ano;
    
    @NotBlank(message = "Gênero é obrigatório")
    @Column(nullable = false)
    private String genero;
    
    @Column(unique = true)
    private String isbn;
    
    @NotNull(message = "Condição é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CondicaoLivro condicao;
    
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(length = 500)
    private String descricao;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    

    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UsuarioLivro> usuarios = new ArrayList<>();
    
    @OneToMany(mappedBy = "livro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LivroProposta> propostas = new ArrayList<>();
    

    public Livro() {}
    
    public Livro(String titulo, String autor, String editora, Integer ano, String genero, CondicaoLivro condicao) {
        this.titulo = titulo;
        this.autor = autor;
        this.editora = editora;
        this.ano = ano;
        this.genero = genero;
        this.condicao = condicao;
    }
    

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    
    public String getEditora() { return editora; }
    public void setEditora(String editora) { this.editora = editora; }
    
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public CondicaoLivro getCondicao() { return condicao; }
    public void setCondicao(CondicaoLivro condicao) { this.condicao = condicao; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<UsuarioLivro> getUsuarios() { return usuarios; }
    public void setUsuarios(List<UsuarioLivro> usuarios) { this.usuarios = usuarios; }
    
    public List<LivroProposta> getPropostas() { return propostas; }
    public void setPropostas(List<LivroProposta> propostas) { this.propostas = propostas; }
    

    public enum CondicaoLivro {
        NOVO("Novo"),
        SEMI_NOVO("Semi-novo"),
        USADO_BOM("Usado - Bom estado"),
        USADO_REGULAR("Usado - Estado regular"),
        USADO_RUIM("Usado - Estado ruim");
        
        private final String descricao;
        
        CondicaoLivro(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
}
