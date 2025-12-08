package com.unilivros.service;

import com.unilivros.dto.AgendamentoDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com. unilivros.model. Agendamento;
import com. unilivros.model. Proposta;
import com.unilivros.repository.AgendamentoRepository;
import com. unilivros.repository.PropostaRepository;
import org. modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation. Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private ModelMapper modelMapper;

    // ✅ CRIAR AGENDAMENTO - Agora só precisa da Proposta
    public AgendamentoDTO criarAgendamento(AgendamentoDTO agendamentoDTO) {
        Proposta proposta = propostaRepository.findById(agendamentoDTO.getPropostaId())
                .orElseThrow(() -> new ResourceNotFoundException("Proposta", agendamentoDTO.getPropostaId()));

        // Verificar se já existe agendamento para esta proposta
//        if (proposta.getAgendamento() != null) {
//            throw new BusinessException("Já existe um agendamento para esta proposta");
//        }

        // Verificar se a proposta foi aceita
        if (proposta. getStatus() != Proposta.StatusProposta.ACEITA) {
            throw new BusinessException("Apenas propostas aceitas podem ter agendamento");
        }

        // Verificar se a proposta tem data e local
        if (proposta.getDataHoraSugerida() == null || proposta.getLocalSugerido() == null) {
            throw new BusinessException("Proposta não possui data/hora e local definidos");
        }

        // Verificar se a data é futura
        if (proposta.getDataHoraSugerida().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Data e hora devem ser futuras");
        }

        Agendamento agendamento = new Agendamento(proposta);
        agendamento.setStatus(Agendamento.StatusAgendamento.AGENDADO);
        agendamento = agendamentoRepository.save(agendamento);

        return new AgendamentoDTO(agendamento);
    }

    @Transactional(readOnly = true)
    public AgendamentoDTO buscarPorId(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
        return new AgendamentoDTO(agendamento);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarTodos() {
        return agendamentoRepository.findAll(). stream()
                .map(AgendamentoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> buscarPorStatus(Agendamento.StatusAgendamento status) {
        return agendamentoRepository.findByStatus(status). stream()
                .map(AgendamentoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        // ✅ Buscar agendamentos cujas propostas têm data no período
        return agendamentoRepository.findAll(). stream()
                .filter(a -> {
                    LocalDateTime dataHora = a.getProposta().getDataHoraSugerida();
                    return dataHora != null &&
                            ! dataHora.isBefore(inicio) &&
                            ! dataHora.isAfter(fim);
                })
                . map(AgendamentoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> buscarPassadosPorStatus(Agendamento.StatusAgendamento status) {
        return agendamentoRepository.findAll().stream()
                .filter(a -> {
                    LocalDateTime dataHora = a.getProposta().getDataHoraSugerida();
                    return dataHora != null &&
                            dataHora.isBefore(LocalDateTime.now()) &&
                            a.getStatus() == status;
                })
                .map(AgendamentoDTO::new)
                .collect(Collectors.toList());
    }

    // ✅ ATUALIZAR STATUS DO AGENDAMENTO
    public AgendamentoDTO atualizarStatus(Long id, Agendamento.StatusAgendamento novoStatus) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));

        agendamento.setStatus(novoStatus);
        agendamento = agendamentoRepository.save(agendamento);

        return new AgendamentoDTO(agendamento);
    }

    public AgendamentoDTO confirmarAgendamento(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));

        if (agendamento.getStatus() != Agendamento.StatusAgendamento.AGENDADO) {
            throw new BusinessException("Apenas agendamentos pendentes podem ser confirmados");
        }

        agendamento.setStatus(Agendamento.StatusAgendamento.CONFIRMADO);
        agendamento = agendamentoRepository.save(agendamento);

        return new AgendamentoDTO(agendamento);
    }

    public AgendamentoDTO marcarComoRealizado(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));

        if (agendamento.getStatus() != Agendamento.StatusAgendamento. CONFIRMADO) {
            throw new BusinessException("Apenas agendamentos confirmados podem ser marcados como realizados");
        }

        agendamento.setStatus(Agendamento.StatusAgendamento.REALIZADO);
        agendamento = agendamentoRepository.save(agendamento);

        return new AgendamentoDTO(agendamento);
    }

    public AgendamentoDTO cancelarAgendamento(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));

        if (agendamento.getStatus() == Agendamento.StatusAgendamento.CANCELADO) {
            throw new BusinessException("Agendamento já está cancelado");
        }

        agendamento.setStatus(Agendamento.StatusAgendamento.CANCELADO);
        agendamento = agendamentoRepository.save(agendamento);

        return new AgendamentoDTO(agendamento);
    }

    public void deletarAgendamento(Long id) {
        if (! agendamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agendamento", id);
        }
        agendamentoRepository.deleteById(id);
    }

    // ✅ BUSCAR AGENDAMENTO POR PROPOSTA
//    @Transactional(readOnly = true)
//    public AgendamentoDTO buscarPorProposta(Long propostaId) {
//        Proposta proposta = propostaRepository.findById(propostaId)
//                .orElseThrow(() -> new ResourceNotFoundException("Proposta", propostaId));
//
//        if (proposta.getAgendamento() == null) {
//            throw new ResourceNotFoundException("Agendamento não encontrado para a proposta", propostaId);
//        }
//
//        return new AgendamentoDTO(proposta.getAgendamento());
//    }
}