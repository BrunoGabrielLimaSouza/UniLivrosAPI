package com.unilivros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate. annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private Long id;

    // ✅ Agendamento agora só tem status e referências
    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposta_id", nullable = false, unique = true)
    private Proposta proposta;

    public Agendamento() {}

    public Agendamento(Proposta proposta) {
        this.proposta = proposta;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Proposta getProposta() { return proposta; }
    public void setProposta(Proposta proposta) { this.proposta = proposta; }

    // ✅ Métodos auxiliares para acessar dados da Proposta
    public LocalDateTime getDataHora() {
        return proposta != null ? proposta.getDataHoraSugerida() : null;
    }

    public String getLocal() {
        return proposta != null ? proposta.getLocalSugerido() : null;
    }

    public String getObservacoes() {
        return proposta != null ? proposta.getObservacoes() : null;
    }

    public enum StatusAgendamento {
        AGENDADO("Agendado"),
        CONFIRMADO("Confirmado"),
        REALIZADO("Realizado"),
        CANCELADO("Cancelado"),
        FALTADO("Faltado");

        private final String descricao;

        StatusAgendamento(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}