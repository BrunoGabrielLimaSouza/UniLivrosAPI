package com.unilivros.service;

import com.unilivros.dto.AgendamentoDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.Agendamento;
import com.unilivros.model.Proposta;
import com.unilivros.model.Usuario;
import com.unilivros.repository.AgendamentoRepository;
import com.unilivros.repository.PropostaRepository;
import com.unilivros.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ModelMapper modelMapper;
    
    public AgendamentoDTO criarAgendamento(AgendamentoDTO agendamentoDTO) {

        Proposta proposta = propostaRepository.findById(agendamentoDTO.getPropostaId())
                .orElseThrow(() -> new ResourceNotFoundException("Proposta", agendamentoDTO.getPropostaId()));
        

        Usuario usuario = usuarioRepository.findById(agendamentoDTO.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", agendamentoDTO.getUsuarioId()));


        if (proposta.getStatus() != Proposta.StatusProposta.ACEITA) {
            throw new BusinessException("Apenas propostas aceitas podem ter agendamento");
        }
        

        if (proposta.getAgendamento() != null) {
            throw new BusinessException("Já existe agendamento para esta proposta");
        }
        

        if (agendamentoDTO.getDataHora().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Data e hora devem ser futuras");
        }
        
        Agendamento agendamento = new Agendamento(
                agendamentoDTO.getDataHora(),
                agendamentoDTO.getLocal(),
                proposta,
                usuario
        );
        
        if (agendamentoDTO.getObservacoes() != null) {
            agendamento.setObservacoes(agendamentoDTO.getObservacoes());
        }
        
        agendamento = agendamentoRepository.save(agendamento);
        
        return modelMapper.map(agendamento, AgendamentoDTO.class);
    }
    
    @Transactional(readOnly = true)
    public AgendamentoDTO buscarPorId(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
        return modelMapper.map(agendamento, AgendamentoDTO.class);
    }
    
    @Transactional(readOnly = true)
    public Page<AgendamentoDTO> listar(Pageable pageable) {
        Page<Agendamento> page = agendamentoRepository.findAll(pageable);
        return page.map(agendamento -> modelMapper.map(agendamento, AgendamentoDTO.class));
    }
    
    @Transactional(readOnly = true)
    public List<AgendamentoDTO> buscarPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));
        
        return agendamentoRepository.findByUsuario(usuario).stream()
                .map(agendamento -> modelMapper.map(agendamento, AgendamentoDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AgendamentoDTO> buscarPorStatus(Agendamento.StatusAgendamento status) {
        return agendamentoRepository.findByStatus(status).stream()
                .map(agendamento -> modelMapper.map(agendamento, AgendamentoDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AgendamentoDTO> buscarPorUsuarioEStatus(Long usuarioId, Agendamento.StatusAgendamento status) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));
        
        return agendamentoRepository.findByUsuarioAndStatus(usuario, status).stream()
                .map(agendamento -> modelMapper.map(agendamento, AgendamentoDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AgendamentoDTO> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return agendamentoRepository.findByDataHoraBetween(inicio, fim).stream()
                .map(agendamento -> modelMapper.map(agendamento, AgendamentoDTO.class))
                .collect(Collectors.toList());
    }
//
//    @Transactional(readOnly = true)
//    public List<AgendamentoDTO> buscarPorUsuarioEPeriodo(Long usuarioId, LocalDateTime inicio, LocalDateTime fim) {
//        Usuario usuario = usuarioRepository.findById(usuarioId)
//                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));
//
//        return agendamentoRepository.findByUsuarioAndDataHoraBetween(usuario, inicio, fim).stream()
//                .map(agendamento -> modelMapper.map(agendamento, AgendamentoDTO.class))
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public List<AgendamentoDTO> buscarFuturosPorStatus(Agendamento.StatusAgendamento status) {
//        return agendamentoRepository.findFuturosByStatus(LocalDateTime.now(), status).stream()
//                .map(agendamento -> modelMapper.map(agendamento, AgendamentoDTO.class))
//                .collect(Collectors.toList());
//    }
    
    @Transactional(readOnly = true)
    public List<AgendamentoDTO> buscarPassadosPorStatus(Agendamento.StatusAgendamento status) {
        return agendamentoRepository.findPassadosByStatus(LocalDateTime.now(), status).stream()
                .map(agendamento -> modelMapper.map(agendamento, AgendamentoDTO.class))
                .collect(Collectors.toList());
    }
    
    public AgendamentoDTO atualizarAgendamento(Long id, AgendamentoDTO agendamentoDTO) {
        Agendamento agendamentoExistente = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
        

        if (agendamentoDTO.getDataHora().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Data e hora devem ser futuras");
        }
        
        modelMapper.map(agendamentoDTO, agendamentoExistente);
        agendamentoExistente = agendamentoRepository.save(agendamentoExistente);
        
        return modelMapper.map(agendamentoExistente, AgendamentoDTO.class);
    }
    
    public AgendamentoDTO confirmarAgendamento(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
        
        if (agendamento.getStatus() != Agendamento.StatusAgendamento.AGENDADO) {
            throw new BusinessException("Apenas agendamentos pendentes podem ser confirmados");
        }
        
        agendamento.setStatus(Agendamento.StatusAgendamento.CONFIRMADO);
        agendamento = agendamentoRepository.save(agendamento);
        
        return modelMapper.map(agendamento, AgendamentoDTO.class);
    }
    
    public AgendamentoDTO marcarComoRealizado(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
        
        if (agendamento.getStatus() != Agendamento.StatusAgendamento.CONFIRMADO) {
            throw new BusinessException("Apenas agendamentos confirmados podem ser marcados como realizados");
        }
        
        agendamento.setStatus(Agendamento.StatusAgendamento.REALIZADO);
        agendamento = agendamentoRepository.save(agendamento);
        
        return modelMapper.map(agendamento, AgendamentoDTO.class);
    }
    
    public AgendamentoDTO cancelarAgendamento(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
        
        if (agendamento.getStatus() == Agendamento.StatusAgendamento.CANCELADO) {
            throw new BusinessException("Agendamento já está cancelado");
        }
        
        agendamento.setStatus(Agendamento.StatusAgendamento.CANCELADO);
        agendamento = agendamentoRepository.save(agendamento);
        
        return modelMapper.map(agendamento, AgendamentoDTO.class);
    }
    
    public void deletarAgendamento(Long id) {
        if (!agendamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agendamento", id);
        }
        agendamentoRepository.deleteById(id);
    }
}
