package com.unilivros. dto;

import com.unilivros.model.Proposta;
import jakarta.validation.constraints. NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class PropostaDTO {

    private Long id;

    @NotNull(message = "Status é obrigatório")
    private Proposta.StatusProposta status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dataResposta;

    private Long proponenteId;
    private String proponenteNome;
    private String proponenteEmail;

    private Long propostoId;
    private String propostoNome;
    private String propostoEmail;

    // ✅ Campos dos livros
    private Long livroOferecidoId;
    private String livroOferecidoTitulo;
    private Long livroDesejadoId;
    private String livroDesejadoTitulo;

    // ✅ Campos da troca
    private String nomeUsuarioRelacionado;
    private String dataTroca;
    private String local;
    private String observacao;

    private List<LivroPropostaDTO> livros;
    private AgendamentoDTO agendamento;

    // Construtores
    public PropostaDTO() {}

    public PropostaDTO(Long proponenteId, Long propostoId) {
        this.proponenteId = proponenteId;
        this.propostoId = propostoId;
    }

    // ✅ CONSTRUTOR COMPLETO
    public PropostaDTO(Proposta proposta) {
        this.id = proposta.getId();
        this.status = proposta.getStatus();
        this.createdAt = proposta.getCreatedAt();
        this.updatedAt = proposta.getUpdatedAt();
        this.dataResposta = proposta.getDataResposta();

        if (proposta.getProponente() != null) {
            this.proponenteId = proposta.getProponente().getId();
            this.proponenteNome = proposta. getProponente().getNome();
            this.proponenteEmail = proposta.getProponente().getEmail();
        }

        if (proposta. getProposto() != null) {
            this.propostoId = proposta.getProposto().getId();
            this.propostoNome = proposta.getProposto(). getNome();
            this.propostoEmail = proposta.getProposto().getEmail();
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Proposta.StatusProposta getStatus() { return status; }
    public void setStatus(Proposta.StatusProposta status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }

    public Long getProponenteId() { return proponenteId; }
    public void setProponenteId(Long proponenteId) { this. proponenteId = proponenteId; }

    public String getProponenteNome() { return proponenteNome; }
    public void setProponenteNome(String proponenteNome) { this.proponenteNome = proponenteNome; }

    public String getProponenteEmail() { return proponenteEmail; }
    public void setProponenteEmail(String proponenteEmail) { this.proponenteEmail = proponenteEmail; }

    public Long getPropostoId() { return propostoId; }
    public void setPropostoId(Long propostoId) { this.propostoId = propostoId; }

    public String getPropostoNome() { return propostoNome; }
    public void setPropostoNome(String propostoNome) { this.propostoNome = propostoNome; }

    public String getPropostoEmail() { return propostoEmail; }
    public void setPropostoEmail(String propostoEmail) { this.propostoEmail = propostoEmail; }

    // ✅ Getters/Setters dos livros
    public Long getLivroOferecidoId() { return livroOferecidoId; }
    public void setLivroOferecidoId(Long livroOferecidoId) { this. livroOferecidoId = livroOferecidoId; }

    public String getLivroOferecidoTitulo() { return livroOferecidoTitulo; }
    public void setLivroOferecidoTitulo(String livroOferecidoTitulo) { this.livroOferecidoTitulo = livroOferecidoTitulo; }

    public Long getLivroDesejadoId() { return livroDesejadoId; }
    public void setLivroDesejadoId(Long livroDesejadoId) { this.livroDesejadoId = livroDesejadoId; }

    public String getLivroDesejadoTitulo() { return livroDesejadoTitulo; }
    public void setLivroDesejadoTitulo(String livroDesejadoTitulo) { this.livroDesejadoTitulo = livroDesejadoTitulo; }

    // ✅ Getters/Setters da troca
    public String getNomeUsuarioRelacionado() { return nomeUsuarioRelacionado; }
    public void setNomeUsuarioRelacionado(String nomeUsuarioRelacionado) { this.nomeUsuarioRelacionado = nomeUsuarioRelacionado; }

    public String getDataTroca() { return dataTroca; }
    public void setDataTroca(String dataTroca) { this.dataTroca = dataTroca; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public List<LivroPropostaDTO> getLivros() { return livros; }
    public void setLivros(List<LivroPropostaDTO> livros) { this. livros = livros; }

    public AgendamentoDTO getAgendamento() { return agendamento; }
    public void setAgendamento(AgendamentoDTO agendamento) { this.agendamento = agendamento; }
}