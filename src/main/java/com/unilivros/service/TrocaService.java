package com.unilivros.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.unilivros.dto.TrocaDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.Agendamento;
import com.unilivros.model.Troca;
import com.unilivros.repository.AgendamentoRepository;
import com.unilivros.repository.TrocaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrocaService {
    
    @Autowired
    private TrocaRepository trocaRepository;
    
    @Autowired
    private AgendamentoRepository agendamentoRepository;
    
    @Autowired
    private ModelMapper modelMapper;
    
    public TrocaDTO criarTroca(TrocaDTO trocaDTO) {
        // Verificar se agendamento existe
        Agendamento agendamento = agendamentoRepository.findById(trocaDTO.getAgendamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", trocaDTO.getAgendamentoId()));
        
        // Verificar se agendamento está confirmado
        if (agendamento.getStatus() != Agendamento.StatusAgendamento.CONFIRMADO) {
            throw new BusinessException("Apenas agendamentos confirmados podem ter troca");
        }
        
        // Verificar se já existe troca para este agendamento
        if (trocaRepository.existsByAgendamento(agendamento)) {
            throw new BusinessException("Já existe troca para este agendamento");
        }
        
        Troca troca = new Troca(agendamento);
        troca = trocaRepository.save(troca);
        
        return modelMapper.map(troca, TrocaDTO.class);
    }
    
    @Transactional(readOnly = true)
    public TrocaDTO buscarPorId(Long id) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca", id));
        return modelMapper.map(troca, TrocaDTO.class);
    }
    
    @Transactional(readOnly = true)
    public Page<TrocaDTO> listar(Pageable pageable) {
        Page<Troca> page = trocaRepository.findAll(pageable);
        return page.map(troca -> modelMapper.map(troca, TrocaDTO.class));
    }
    
    @Transactional(readOnly = true)
    public List<TrocaDTO> buscarPorStatus(Troca.StatusTroca status) {
        return trocaRepository.findByStatus(status).stream()
                .map(troca -> modelMapper.map(troca, TrocaDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TrocaDTO> buscarPorUsuario(Long usuarioId) {
        return trocaRepository.findByUsuario(usuarioId).stream()
                .map(troca -> modelMapper.map(troca, TrocaDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TrocaDTO> buscarPorUsuarioEStatus(Long usuarioId, Troca.StatusTroca status) {
        return trocaRepository.findByUsuarioAndStatus(usuarioId, status).stream()
                .map(troca -> modelMapper.map(troca, TrocaDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TrocaDTO> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return trocaRepository.findByDataConfirmacaoBetween(inicio, fim).stream()
                .map(troca -> modelMapper.map(troca, TrocaDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TrocaDTO> buscarPorAvaliacaoMinima(Double avaliacaoMinima) {
        return trocaRepository.findByAvaliacaoGreaterThanEqual(avaliacaoMinima).stream()
                .map(troca -> modelMapper.map(troca, TrocaDTO.class))
                .collect(Collectors.toList());
    }
    
    public TrocaDTO gerarQRCode(Long id) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca", id));
        
        if (troca.getStatus() != Troca.StatusTroca.PENDENTE) {
            throw new BusinessException("Apenas trocas pendentes podem gerar QR Code");
        }
        
        // Gerar código único para a troca
        String codigoTroca = UUID.randomUUID().toString();
        troca.setQrCode(codigoTroca);
        troca = trocaRepository.save(troca);
        
        // Gerar QR Code
        try {
            String qrCodeData = "TROCA:" + troca.getId() + ":" + codigoTroca;
            String qrCodeBase64 = gerarQRCodeBase64(qrCodeData);
            
            TrocaDTO trocaDTO = modelMapper.map(troca, TrocaDTO.class);
            trocaDTO.setQrCode(qrCodeBase64);
            
            return trocaDTO;
        } catch (Exception e) {
            throw new BusinessException("Erro ao gerar QR Code: " + e.getMessage());
        }
    }
    
    public TrocaDTO confirmarTroca(Long id, String codigoQR) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca", id));
        
        if (troca.getStatus() != Troca.StatusTroca.PENDENTE) {
            throw new BusinessException("Apenas trocas pendentes podem ser confirmadas");
        }
        
        if (!troca.getQrCode().equals(codigoQR)) {
            throw new BusinessException("Código QR inválido");
        }
        
        troca.setStatus(Troca.StatusTroca.CONFIRMADA);
        troca.setDataConfirmacao(LocalDateTime.now());
        troca = trocaRepository.save(troca);
        
        return modelMapper.map(troca, TrocaDTO.class);
    }
    
    public TrocaDTO concluirTroca(Long id, Double avaliacao, String comentario) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca", id));
        
        if (troca.getStatus() != Troca.StatusTroca.CONFIRMADA) {
            throw new BusinessException("Apenas trocas confirmadas podem ser concluídas");
        }
        
        troca.setStatus(Troca.StatusTroca.CONCLUIDA);
        troca.setAvaliacao(avaliacao);
        troca.setComentario(comentario);
        troca = trocaRepository.save(troca);
        
        return modelMapper.map(troca, TrocaDTO.class);
    }
    
    public TrocaDTO cancelarTroca(Long id) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca", id));
        
        if (troca.getStatus() == Troca.StatusTroca.CANCELADA) {
            throw new BusinessException("Troca já está cancelada");
        }
        
        troca.setStatus(Troca.StatusTroca.CANCELADA);
        troca = trocaRepository.save(troca);
        
        return modelMapper.map(troca, TrocaDTO.class);
    }
    
    public void deletarTroca(Long id) {
        if (!trocaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Troca", id);
        }
        trocaRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Long contarTrocasPorStatus(Troca.StatusTroca status) {
        return trocaRepository.countByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public Double obterMediaAvaliacao() {
        return trocaRepository.findAverageAvaliacao();
    }
    
    private String gerarQRCodeBase64(String data) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}
