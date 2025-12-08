package com.unilivros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trocas")
public class Troca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTroca status = StatusTroca.PENDENTE;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;

    @DecimalMin(value = "0.0", message = "Avaliação deve ser pelo menos 0.0")
    @DecimalMax(value = "5.0", message = "Avaliação deve ser no máximo 5.0")
    @Column
    private Double avaliacao;

    @Column(length = 500)
    private String comentario;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ Relacionamento direto com Proposta
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposta_id", nullable = false)
    private Proposta proposta;

    @OneToMany(mappedBy = "troca", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrocaUsuario> usuarios = new ArrayList<>();

    public Troca() {}

    public Troca(Proposta proposta) {
        this.proposta = proposta;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StatusTroca getStatus() { return status; }
    public void setStatus(StatusTroca status) { this.status = status; }

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

    public Proposta getProposta() { return proposta; }
    public void setProposta(Proposta proposta) { this.proposta = proposta; }

    public List<TrocaUsuario> getUsuarios() { return usuarios; }
    public void setUsuarios(List<TrocaUsuario> usuarios) { this.usuarios = usuarios; }

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

    public enum StatusTroca {
        PENDENTE("Pendente"),
        CONFIRMADA("Confirmada"),
        CONCLUIDA("Concluída"),
        CANCELADA("Cancelada");

        private final String descricao;

        StatusTroca(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}