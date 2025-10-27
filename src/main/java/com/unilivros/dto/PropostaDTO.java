package com.unilivros.dto;

import com.unilivros.model.Proposta;
import jakarta.validation.constraints.NotNull;
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
    
    private List<LivroPropostaDTO> livros;
    
    private AgendamentoDTO agendamento;
    
    // Construtores
    public PropostaDTO() {}
    
    public PropostaDTO(Long proponenteId, Long propostoId) {
        this.proponenteId = proponenteId;
        this.propostoId = propostoId;
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
    public void setProponenteId(Long proponenteId) { this.proponenteId = proponenteId; }
    
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
    
    public List<LivroPropostaDTO> getLivros() { return livros; }
    public void setLivros(List<LivroPropostaDTO> livros) { this.livros = livros; }
    
    public AgendamentoDTO getAgendamento() { return agendamento; }
    public void setAgendamento(AgendamentoDTO agendamento) { this.agendamento = agendamento; }
}
