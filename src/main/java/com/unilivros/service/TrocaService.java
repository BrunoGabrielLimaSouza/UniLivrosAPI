package com.unilivros.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.unilivros.dto.TrocaDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.Proposta;
import com.unilivros.model.Troca;
import com.unilivros.repository.PropostaRepository;
import com.unilivros.repository.TrocaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private PropostaRepository propostaRepository;

    @Autowired
    private ModelMapper modelMapper;

    // ✅ Criar Troca (agora recebe propostaId)
    public TrocaDTO criarTroca(TrocaDTO trocaDTO) {
        // Verificar se proposta existe
        Proposta proposta = propostaRepository.findById(trocaDTO.getPropostaId())
                .orElseThrow(() -> new ResourceNotFoundException("Proposta", trocaDTO.getPropostaId()));

        // Verificar se proposta foi aceita
        if (proposta.getStatus() != Proposta.StatusProposta.ACEITA) {
            throw new BusinessException("Apenas propostas aceitas podem ter troca");
        }

        // Verificar se já existe troca para esta proposta
        if (trocaRepository.existsByProposta(proposta)) {
            throw new BusinessException("Já existe troca para esta proposta");
        }

        Troca troca = new Troca(proposta);
        troca.setStatus(Troca.StatusTroca.PENDENTE);

        // Gerar QR Code
        String qrCode = "TROCA:" + UUID.randomUUID().toString();
        troca.setQrCode(qrCode);

        troca = trocaRepository.save(troca);

        return convertToDTO(troca);
    }

    @Transactional(readOnly = true)
    public TrocaDTO buscarPorId(Long id) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca", id));
        return convertToDTO(troca);
    }

    @Transactional(readOnly = true)
    public List<TrocaDTO> listarTodas() {
        return trocaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrocaDTO> buscarPorStatus(Troca.StatusTroca status) {
        return trocaRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrocaDTO> buscarPorUsuario(Long usuarioId) {
        System.out.println("========================================");
        System.out.println("🔍 Buscando trocas para usuário ID: " + usuarioId);

        List<Troca> trocas = trocaRepository.findByUsuario(usuarioId);

        System.out.println("🔍 Total de trocas encontradas no repository: " + trocas.size());

        if (trocas.isEmpty()) {
            // Vamos verificar se existem trocas no banco
            List<Troca> todasTrocas = trocaRepository.findAll();
            System.out.println("🔍 Total de trocas no banco: " + todasTrocas.size());

            for (Troca t : todasTrocas) {
                System.out.println("  📦 Troca ID: " + t.getId() + " | Status: " + t.getStatus());
                System.out.println("     QR Code: " + (t.getQrCode() != null ? "Sim" : "Não"));
                System.out.println("     Participantes: " + t.getUsuarios().size());
                for (var tu : t.getUsuarios()) {
                    System.out.println("       - Usuário ID: " + tu.getUsuario().getId() +
                            " | Nome: " + tu.getUsuario().getNome() +
                            " | Tipo: " + tu.getTipo());
                }
            }
        }

        System.out.println("========================================");

        return trocas.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrocaDTO> buscarPorUsuarioEStatus(Long usuarioId, Troca.StatusTroca status) {
        return trocaRepository.findByUsuarioAndStatus(usuarioId, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrocaDTO> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return trocaRepository.findByDataConfirmacaoBetween(inicio, fim).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrocaDTO> buscarPorAvaliacaoMinima(Double avaliacaoMinima) {
        return trocaRepository.findByAvaliacaoGreaterThanEqual(avaliacaoMinima).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TrocaDTO gerarQRCode(Long id) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca", id));

        if (troca.getStatus() != Troca.StatusTroca.PENDENTE) {
            throw new BusinessException("Apenas trocas pendentes podem gerar QR Code");
        }

        if (troca.getQrCode() == null || troca.getQrCode().isEmpty()) {
            String qrCode = "TROCA:" + UUID.randomUUID().toString();
            troca.setQrCode(qrCode);
            troca = trocaRepository.save(troca);
        }

        try {
            String qrCodeBase64 = gerarQRCodeBase64(troca.getQrCode());

            TrocaDTO trocaDTO = convertToDTO(troca);
            trocaDTO.setQrCodeBase64(qrCodeBase64); // Campo adicional para a imagem

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

        if (! troca.getQrCode().equals(codigoQR)) {
            throw new BusinessException("Código QR inválido");
        }

        troca.setStatus(Troca.StatusTroca.CONFIRMADA);
        troca.setDataConfirmacao(LocalDateTime.now());
        troca = trocaRepository.save(troca);

        return convertToDTO(troca);
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

        return convertToDTO(troca);
    }

    public TrocaDTO cancelarTroca(Long id) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca", id));

        if (troca.getStatus() == Troca.StatusTroca.CANCELADA) {
            throw new BusinessException("Troca já está cancelada");
        }

        troca.setStatus(Troca.StatusTroca.CANCELADA);
        troca = trocaRepository.save(troca);

        return convertToDTO(troca);
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

    private TrocaDTO convertToDTO(Troca troca) {
        TrocaDTO dto = new TrocaDTO();
        dto.setId(troca.getId());
        dto.setStatus(troca.getStatus());
        dto.setQrCode(troca.getQrCode());
        dto.setDataConfirmacao(troca.getDataConfirmacao());
        dto.setAvaliacao(troca.getAvaliacao());
        dto.setComentario(troca.getComentario());
        dto.setCreatedAt(troca.getCreatedAt());
        dto.setUpdatedAt(troca.getUpdatedAt());

        if (troca.getProposta() != null) {
            dto.setPropostaId(troca.getProposta().getId());
            dto.setDataHora(troca.getProposta().getDataHoraSugerida());
            dto.setLocal(troca.getProposta().getLocalSugerido());
            dto.setObservacoes(troca.getProposta().getObservacoes());
        }

        return dto;
    }

    private String gerarQRCodeBase64(String data) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 256, 256);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}