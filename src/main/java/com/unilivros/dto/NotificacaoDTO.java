package com. unilivros.dto;

import com.unilivros. model.Notificacao;
import java.time.LocalDateTime;

public class NotificacaoDTO {

    private Long id;
    private Long usuarioId;
    private String titulo;
    private String mensagem;
    private Notificacao.TipoNotificacao tipo;
    private Boolean lida;
    private Long propostaId;
    private Long trocaId;
    private LocalDateTime createdAt;

    public NotificacaoDTO() {}

    public NotificacaoDTO(Notificacao notificacao) {
        this.id = notificacao.getId();
        this.usuarioId = notificacao.getUsuario().getId();
        this.titulo = notificacao.getTitulo();
        this.mensagem = notificacao.getMensagem();
        this.tipo = notificacao.getTipo();
        this.lida = notificacao.getLida();
        this.propostaId = notificacao.getPropostaId();
        this.trocaId = notificacao. getTrocaId();
        this.createdAt = notificacao.getCreatedAt();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public Notificacao.TipoNotificacao getTipo() { return tipo; }
    public void setTipo(Notificacao.TipoNotificacao tipo) { this.tipo = tipo; }

    public Boolean getLida() { return lida; }
    public void setLida(Boolean lida) { this.lida = lida; }

    public Long getPropostaId() { return propostaId; }
    public void setPropostaId(Long propostaId) { this.propostaId = propostaId; }

    public Long getTrocaId() { return trocaId; }
    public void setTrocaId(Long trocaId) { this.trocaId = trocaId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}