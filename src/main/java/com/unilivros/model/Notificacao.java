package com.unilivros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation. constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Usuário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank(message = "Título é obrigatório")
    @Column(nullable = false)
    private String titulo;

    @NotBlank(message = "Mensagem é obrigatória")
    @Column(nullable = false, length = 500)
    private String mensagem;

    @NotNull(message = "Tipo é obrigatório")
    @Enumerated(EnumType. STRING)
    @Column(nullable = false)
    private TipoNotificacao tipo;

    @Column(name = "lida")
    private Boolean lida = false;

    @Column(name = "proposta_id")
    private Long propostaId;

    @Column(name = "troca_id")
    private Long trocaId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Notificacao() {}

    public Notificacao(Usuario usuario, String titulo, String mensagem, TipoNotificacao tipo) {
        this.usuario = usuario;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.tipo = tipo;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this. titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public TipoNotificacao getTipo() { return tipo; }
    public void setTipo(TipoNotificacao tipo) { this.tipo = tipo; }

    public boolean getLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }

    public Long getPropostaId() { return propostaId; }
    public void setPropostaId(Long propostaId) { this.propostaId = propostaId; }

    public Long getTrocaId() { return trocaId; }
    public void setTrocaId(Long trocaId) { this.trocaId = trocaId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this. createdAt = createdAt; }

    public enum TipoNotificacao {
        PROPOSTA_RECEBIDA("Nova proposta de troca"),
        PROPOSTA_ACEITA("Proposta aceita"),
        PROPOSTA_REJEITADA("Proposta rejeitada"),
        PROPOSTA_CANCELADA("Proposta cancelada"),
        TROCA_CONCLUIDA("Troca concluída"),
        AVALIACAO_RECEBIDA("Nova avaliação recebida"),
        MENSAGEM("Nova mensagem");

        private final String descricao;

        TipoNotificacao(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}