package com.unilivros. dto;

import com.unilivros.model.Agendamento;
import jakarta.validation. constraints.NotNull;
import java.time.LocalDateTime;

public class AgendamentoDTO {

    private Long id;

    @NotNull(message = "Status é obrigatório")
    private Agendamento.StatusAgendamento status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long propostaId;

    // ✅ Dados vêm da Proposta
    private LocalDateTime dataHoraSugerida;
    private String localSugerido;
    private String observacoes;

    public AgendamentoDTO() {}

    public AgendamentoDTO(Agendamento agendamento) {
        this.id = agendamento.getId();
        this.status = agendamento.getStatus();
        this. createdAt = agendamento. getCreatedAt();
        this.updatedAt = agendamento.getUpdatedAt();

        if (agendamento.getProposta() != null) {
            this.propostaId = agendamento.getProposta(). getId();
            this.dataHoraSugerida = agendamento.getProposta().getDataHoraSugerida();
            this.localSugerido = agendamento.getProposta().getLocalSugerido();
            this.observacoes = agendamento.getProposta().getObservacoes();
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Agendamento. StatusAgendamento getStatus() { return status; }
    public void setStatus(Agendamento. StatusAgendamento status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getPropostaId() { return propostaId; }
    public void setPropostaId(Long propostaId) { this.propostaId = propostaId; }

    public LocalDateTime getDataHoraSugerida() { return dataHoraSugerida; }
    public void setDataHoraSugerida(LocalDateTime dataHoraSugerida) { this.dataHoraSugerida = dataHoraSugerida; }

    public String getLocalSugerido() { return localSugerido; }
    public void setLocalSugerido(String localSugerido) { this.localSugerido = localSugerido; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}