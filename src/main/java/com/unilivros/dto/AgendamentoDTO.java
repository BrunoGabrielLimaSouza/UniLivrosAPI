package com.unilivros.dto;

import com.unilivros.model.Agendamento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AgendamentoDTO {
    
    private Long id;
    
    @NotNull(message = "Data e hora são obrigatórias")
    @Future(message = "Data e hora devem ser futuras")
    private LocalDateTime dataHora;
    
    @NotBlank(message = "Local é obrigatório")
    private String local;
    
    private String observacoes;
    
    @NotNull(message = "Status é obrigatório")
    private Agendamento.StatusAgendamento status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Long propostaId;
    private Long usuarioId;
    private String usuarioNome;
    
    // Construtores
    public AgendamentoDTO() {}
    
    public AgendamentoDTO(LocalDateTime dataHora, String local, Long propostaId, Long usuarioId) {
        this.dataHora = dataHora;
        this.local = local;
        this.propostaId = propostaId;
        this.usuarioId = usuarioId;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    
    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    
    public Agendamento.StatusAgendamento getStatus() { return status; }
    public void setStatus(Agendamento.StatusAgendamento status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getPropostaId() { return propostaId; }
    public void setPropostaId(Long propostaId) { this.propostaId = propostaId; }
    
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    
    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }
}
