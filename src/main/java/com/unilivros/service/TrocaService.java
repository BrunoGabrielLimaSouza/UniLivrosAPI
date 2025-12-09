package com.unilivros.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.unilivros.dto.TrocaDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.LivroProposta;
import com.unilivros.model.Proposta;
import com.unilivros.model.Troca;
import com.unilivros.model.Troca.StatusTroca;
import com.unilivros.repository.LivroPropostaRepository;
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
    private LivroPropostaRepository livroPropostaRepository;

    @Autowired
    private ModelMapper modelMapper;


    public TrocaDTO criarTroca(Long propostaId) {
        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta não encontrada com o ID: " + propostaId));

        if (proposta.getStatus() != Proposta.StatusProposta.ACEITA) {
            throw new BusinessException("Não é possível criar uma troca de uma proposta que não está ACEITA.");
        }

        // Cria a entidade Troca
        Troca troca = new Troca();
        troca.setProposta(proposta);
        troca.setStatus(StatusTroca.PENDENTE); // Inicia como PENDENTE

        // Gera um código QR único (UUID)
        String qrCodeData = "TROCA:" + UUID.randomUUID().toString();
        troca.setQrCode(qrCodeData);

        // Gera a imagem Base64 do QR Code para o DTO
        try {
            String qrCodeBase64 = gerarQRCodeBase64(qrCodeData);
            TrocaDTO dto = convertToDto(trocaRepository.save(troca));
            dto.setQrCodeBase64(qrCodeBase64);
            return dto;
        } catch (WriterException | IOException e) {
            throw new BusinessException("Erro ao gerar o QR Code para a troca.");
        }
    }

    // =========================================================================

    /**
     * Busca uma troca pelo ID.
     */
    public TrocaDTO buscarPorId(Long id) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca não encontrada com o ID: " + id));
        return convertToDto(troca);
    }

    /**
     * Lista todas as trocas (uso administrativo).
     */
    public List<TrocaDTO> listarTodas() {
        return trocaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lista as trocas onde o usuário é o proponente ou o proposto.
     * NOTA: Assume-se que a entidade Troca tem acesso aos IDs dos usuários via Proposta.
     */
    public List<TrocaDTO> buscarMinhasTrocas(Long usuarioId) {
        // É uma simplificação, o ideal seria buscar via Proposta.
        List<Troca> trocas = trocaRepository.findByUsuario(usuarioId);

        return trocas.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Confirma a troca (passa de PENDENTE para CONFIRMADA) usando o código QR.
     */
    @Transactional
    public TrocaDTO confirmarTroca(Long id, String codigoQR) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca não encontrada com o ID: " + id));

        if (troca.getStatus() != StatusTroca.PENDENTE) {
            throw new BusinessException("A troca não pode ser confirmada. Status atual: " + troca.getStatus());
        }

        if (!troca.getQrCode().equals(codigoQR)) {
            throw new BusinessException("Código QR inválido para a troca.");
        }

        troca.setStatus(StatusTroca.CONFIRMADA);
        troca.setDataConfirmacao(LocalDateTime.now());
        return convertToDto(trocaRepository.save(troca));
    }

    /**
     * Conclui a troca (passa de CONFIRMADA para CONCLUIDA) e registra a avaliação.
     */
    @Transactional
    public TrocaDTO concluirTroca(Long id, Double avaliacao, String comentario) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca não encontrada com o ID: " + id));

        if (troca.getStatus() != StatusTroca.CONFIRMADA) {
            throw new BusinessException("A troca deve estar CONFIRMADA para ser concluída. Status atual: " + troca.getStatus());
        }

        if (avaliacao < 0.0 || avaliacao > 5.0) {
            throw new BusinessException("A avaliação deve estar entre 0.0 e 5.0.");
        }

        troca.setStatus(StatusTroca.CONCLUIDA);
        troca.setAvaliacao(avaliacao);
        troca.setComentario(comentario);

        // *Lógica para atualizar a média de avaliação dos usuários envolvidos seria adicionada aqui,
        // mas foi omitida para simplificação.*

        return convertToDto(trocaRepository.save(troca));
    }

    /**
     * Cancela a troca.
     */
    @Transactional
    public TrocaDTO cancelarTroca(Long id) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca não encontrada com o ID: " + id));

        if (troca.getStatus() == StatusTroca.CONCLUIDA) {
            throw new BusinessException("Não é possível cancelar uma troca CONCLUÍDA.");
        }

        troca.setStatus(StatusTroca.CANCELADA);
        return convertToDto(trocaRepository.save(troca));
    }

    /**
     * Deleta uma troca.
     */
    @Transactional
    public void deletarTroca(Long id) {
        Troca troca = trocaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Troca não encontrada com o ID: " + id));

        // Regra de negócio: só pode deletar se for CANCELADA ou PENDENTE
        if (troca.getStatus() == StatusTroca.CONFIRMADA || troca.getStatus() == StatusTroca.CONCLUIDA) {
            throw new BusinessException("Não é possível deletar uma troca que está CONFIRMADA ou CONCLUÍDA.");
        }

        trocaRepository.delete(troca);
    }

    // Métodos de Métricas e Busca (Faltantes)

    /**
     * Conta o número de trocas por status.
     */
    public Long contarTrocasPorStatus(StatusTroca status) {
        return trocaRepository.countByStatus(status);
    }

    /**
     * Retorna a média de avaliação de todas as trocas concluídas.
     */
    public Double obterMediaAvaliacaoGeral() {
        return trocaRepository.findAverageAvaliacao();
    }


    private TrocaDTO convertToDto(Troca troca) {
        TrocaDTO dto = modelMapper.map(troca, TrocaDTO.class);

        // 1. Gerar a imagem do QR Code se o código existir
        if (troca.getQrCode() != null && !troca.getQrCode().isEmpty()) {
            try {
                // Gera a imagem on-the-fly e define no DTO
                String qrCodeImage = gerarQRCodeBase64(troca.getQrCode());
                dto.setQrCodeBase64(qrCodeImage);
            } catch (Exception e) {
                // Log de erro silencioso para não quebrar a listagem inteira
                System.err.println("Erro ao gerar imagem QR para a troca " + troca.getId() + ": " + e.getMessage());
            }
        }

        // Lógica de mapeamento de títulos e dados da proposta
        if (troca.getProposta() != null) {
            dto.setPropostaId(troca.getProposta().getId());
            dto.setDataHora(troca.getProposta().getDataHoraSugerida());
            dto.setLocal(troca.getProposta().getLocalSugerido());
            dto.setObservacoes(troca.getProposta().getObservacoes());

            List<LivroProposta> livros = livroPropostaRepository.findByProposta(troca.getProposta());
            for (LivroProposta lp : livros) {
                if (lp.getTipo() == LivroProposta.TipoLivroProposta.OFERTA) {
                    dto.setLivro1Titulo(lp.getLivro().getTitulo());
                } else if (lp.getTipo() == LivroProposta.TipoLivroProposta.SOLICITACAO) {
                    dto.setLivro2Titulo(lp.getLivro().getTitulo());
                }
            }
        }
        return dto;
    }

    private String gerarQRCodeBase64(String data) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 256, 256);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream); // Altere 'PNG' conforme necessário

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}