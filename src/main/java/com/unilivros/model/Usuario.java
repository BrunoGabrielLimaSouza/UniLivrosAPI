package com.unilivros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Matrícula é obrigatória")
    @Column(unique = true, nullable = false)
    private String matricula;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false)
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$", 
             message = "Senha deve conter pelo menos uma letra e um número")
    @Column(nullable = false)
    private String senha;
    
    @NotBlank(message = "Curso é obrigatório")
    @Column(nullable = false)
    private String curso;
    
    @NotNull(message = "Semestre é obrigatório")
    @Min(value = 1, message = "Semestre deve ser pelo menos 1")
    @Max(value = 20, message = "Semestre deve ser no máximo 20")
    @Column(nullable = false)
    private Integer semestre;
    
    @Column(nullable = false)
    private Integer xp = 0;
    
    @DecimalMin(value = "0.0", message = "Avaliação deve ser pelo menos 0.0")
    @DecimalMax(value = "5.0", message = "Avaliação deve ser no máximo 5.0")
    @Column(precision = 3, scale = 2)
    private Double avaliacao = 0.0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relacionamentos
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UsuarioLivro> livros = new ArrayList<>();
    
    @OneToMany(mappedBy = "proponente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Proposta> propostasEnviadas = new ArrayList<>();
    
    @OneToMany(mappedBy = "proposto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Proposta> propostasRecebidas = new ArrayList<>();
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Agendamento> agendamentos = new ArrayList<>();
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConquistaUsuario> conquistas = new ArrayList<>();
    
    // Construtores
    public Usuario() {}
    
    public Usuario(String matricula, String nome, String email, String senha, String curso, Integer semestre) {
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
    
    public List<UsuarioLivro> getLivros() { return livros; }
    public void setLivros(List<UsuarioLivro> livros) { this.livros = livros; }
    
    public List<Proposta> getPropostasEnviadas() { return propostasEnviadas; }
    public void setPropostasEnviadas(List<Proposta> propostasEnviadas) { this.propostasEnviadas = propostasEnviadas; }
    
    public List<Proposta> getPropostasRecebidas() { return propostasRecebidas; }
    public void setPropostasRecebidas(List<Proposta> propostasRecebidas) { this.propostasRecebidas = propostasRecebidas; }
    
    public List<Agendamento> getAgendamentos() { return agendamentos; }
    public void setAgendamentos(List<Agendamento> agendamentos) { this.agendamentos = agendamentos; }
    
    public List<ConquistaUsuario> getConquistas() { return conquistas; }
    public void setConquistas(List<ConquistaUsuario> conquistas) { this.conquistas = conquistas; }
}
