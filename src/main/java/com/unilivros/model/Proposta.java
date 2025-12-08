package com.unilivros.model;

import jakarta.persistence.*;
import jakarta.validation.constraints. NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations. UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "propostas")
public class Proposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProposta status = StatusProposta.PENDENTE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;

    // ✅ CAMPOS DO AGENDAMENTO (agora na Proposta)
    @Column(name = "data_hora_sugerida")
    private LocalDateTime dataHoraSugerida;

    @Column(name = "local_sugerido")
    private String localSugerido;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proponente_id", nullable = false)
    private Usuario proponente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposto_id", nullable = false)
    private Usuario proposto;

    @OneToMany(mappedBy = "proposta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LivroProposta> livros = new ArrayList<>();

    @OneToOne(mappedBy = "proposta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Agendamento agendamento;

    public Proposta() {}

    public Proposta(Usuario proponente, Usuario proposto) {
        this.proponente = proponente;
        this.proposto = proposto;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StatusProposta getStatus() { return status; }
    public void setStatus(StatusProposta status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }

    public LocalDateTime getDataHoraSugerida() { return dataHoraSugerida; }
    public void setDataHoraSugerida(LocalDateTime dataHoraSugerida) { this.dataHoraSugerida = dataHoraSugerida; }

    public String getLocalSugerido() { return localSugerido; }
    public void setLocalSugerido(String localSugerido) { this.localSugerido = localSugerido; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Usuario getProponente() { return proponente; }
    public void setProponente(Usuario proponente) { this. proponente = proponente; }

    public Usuario getProposto() { return proposto; }
    public void setProposto(Usuario proposto) { this. proposto = proposto; }

    public List<LivroProposta> getLivros() { return livros; }
    public void setLivros(List<LivroProposta> livros) { this.livros = livros; }

    public Agendamento getAgendamento() { return agendamento; }
    public void setAgendamento(Agendamento agendamento) { this.agendamento = agendamento; }

    public enum StatusProposta {
        PENDENTE("Pendente"),
        ACEITA("Aceita"),
        REJEITADA("Rejeitada"),
        CANCELADA("Cancelada");

        private final String descricao;

        StatusProposta(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}