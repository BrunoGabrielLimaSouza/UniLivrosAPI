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

    private String qrCode; // Texto do QR Code

    private String qrCodeBase64; // Imagem Base64 do QR Code

    private LocalDateTime dataConfirmacao;

    @DecimalMin(value = "0.0", message = "Avaliação deve ser pelo menos 0.0")
    @DecimalMax(value = "5.0", message = "Avaliação deve ser no máximo 5.0")
    private Double avaliacao;

    private String comentario;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long propostaId;

    // Dados que vêm da Proposta
    private LocalDateTime dataHora;
    private String local;
    private String observacoes;

    public TrocaDTO() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Troca.StatusTroca getStatus() { return status; }
    public void setStatus(Troca.StatusTroca status) { this.status = status; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }

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

    public Long getPropostaId() { return propostaId; }
    public void setPropostaId(Long propostaId) { this.propostaId = propostaId; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}