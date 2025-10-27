package com.unilivros.dto;

import com.unilivros.model.Troca;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TrocaDTO {
    
    private Long id;
    
    @NotNull(message = "Status é obrigatório")
    private Troca.StatusTroca status;
    
    private String qrCode;
    
    private LocalDateTime dataConfirmacao;
    
    @DecimalMin(value = "0.0", message = "Avaliação deve ser pelo menos 0.0")
    @DecimalMax(value = "5.0", message = "Avaliação deve ser no máximo 5.0")
    private Double avaliacao;
    
    private String comentario;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Long agendamentoId;
    private AgendamentoDTO agendamento;
    
    // Construtores
    public TrocaDTO() {}
    
    public TrocaDTO(Long agendamentoId) {
        this.agendamentoId = agendamentoId;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Troca.StatusTroca getStatus() { return status; }
    public void setStatus(Troca.StatusTroca status) { this.status = status; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public LocalDateTime getDataConfirmacao() { return dataConfirmacao; }
    public void setDataConfirmacao(LocalDateTime dataConfirmacao) { this.dataConfirmacao = dataConfirmacao; }
    
    public Double getAvaliacao() { return avaliacao; }
    public void setAvaliacao(Double avaliacao) { this.avaliacao = avaliacao; }
    
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getAgendamentoId() { return agendamentoId; }
    public void setAgendamentoId(Long agendamentoId) { this.agendamentoId = agendamentoId; }
    
    public AgendamentoDTO getAgendamento() { return agendamento; }
    public void setAgendamento(AgendamentoDTO agendamento) { this.agendamento = agendamento; }
}
