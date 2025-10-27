package com.unilivros.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class UsuarioDTO {
    
    private Long id;
    
    @NotBlank(message = "Matrícula é obrigatória")
    private String matricula;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$", 
             message = "Senha deve conter pelo menos uma letra e um número")
    private String senha;
    
    @NotBlank(message = "Curso é obrigatório")
    private String curso;
    
    @NotNull(message = "Semestre é obrigatório")
    @Min(value = 1, message = "Semestre deve ser pelo menos 1")
    @Max(value = 20, message = "Semestre deve ser no máximo 20")
    private Integer semestre;
    
    private Integer xp = 0;
    
    @DecimalMin(value = "0.0", message = "Avaliação deve ser pelo menos 0.0")
    @DecimalMax(value = "5.0", message = "Avaliação deve ser no máximo 5.0")
    private Double avaliacao = 0.0;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Construtores
    public UsuarioDTO() {}
    
    public UsuarioDTO(String matricula, String nome, String email, String senha, String curso, Integer semestre) {
        this.matricula = matricula;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.curso = curso;
        this.semestre = semestre;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    
    public Integer getSemestre() { return semestre; }
    public void setSemestre(Integer semestre) { this.semestre = semestre; }
    
    public Integer getXp() { return xp; }
    public void setXp(Integer xp) { this.xp = xp; }
    
    public Double getAvaliacao() { return avaliacao; }
    public void setAvaliacao(Double avaliacao) { this.avaliacao = avaliacao; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
