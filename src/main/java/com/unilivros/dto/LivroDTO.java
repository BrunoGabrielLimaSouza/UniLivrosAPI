package com.unilivros.dto;

import com.unilivros.model.Livro;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class LivroDTO {
    
    private Long id;
    
    @NotBlank(message = "Título é obrigatório")
    @Size(min = 1, max = 200, message = "Título deve ter entre 1 e 200 caracteres")
    private String titulo;
    
    @NotBlank(message = "Autor é obrigatório")
    @Size(min = 1, max = 100, message = "Autor deve ter entre 1 e 100 caracteres")
    private String autor;
    
    @NotBlank(message = "Editora é obrigatória")
    @Size(min = 1, max = 100, message = "Editora deve ter entre 1 e 100 caracteres")
    private String editora;
    
    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1000, message = "Ano deve ser válido")
    @Max(value = 2024, message = "Ano não pode ser futuro")
    private Integer ano;
    
    @NotBlank(message = "Gênero é obrigatório")
    private String genero;
    
    private String isbn;
    
    @NotNull(message = "Condição é obrigatória")
    private Livro.CondicaoLivro condicao;
    
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    

    public LivroDTO() {}
    
    public LivroDTO(String titulo, String autor, String editora, Integer ano, String genero, Livro.CondicaoLivro condicao) {
        this.titulo = titulo;
        this.autor = autor;
        this.editora = editora;
        this.ano = ano;
        this.genero = genero;
        this.condicao = condicao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public Livro.CondicaoLivro getCondicao() { return condicao; }
    public void setCondicao(Livro.CondicaoLivro condicao) { this.condicao = condicao; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
