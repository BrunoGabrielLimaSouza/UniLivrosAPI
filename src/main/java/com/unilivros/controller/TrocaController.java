package com.unilivros.controller;

import com.unilivros.dto.TrocaDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.Troca;
import com.unilivros.model.Usuario;
import com.unilivros.repository.TrocaRepository;
import com.unilivros.repository.UsuarioRepository;
import com.unilivros.service.TrocaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/trocas")
@CrossOrigin(origins = "*")
public class TrocaController {

    @Autowired
    private TrocaService trocaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TrocaRepository trocaRepository;

    @PostMapping
    public ResponseEntity<TrocaDTO> criarTroca(@Valid @RequestBody TrocaDTO trocaDTO) {
        TrocaDTO trocaCriada = trocaService.criarTroca(trocaDTO);
        return new ResponseEntity<>(trocaCriada, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrocaDTO> buscarPorId(@PathVariable Long id) {
        TrocaDTO troca = trocaService.buscarPorId(id);
        return ResponseEntity.ok(troca);
    }

    @GetMapping
    public ResponseEntity<List<TrocaDTO>> listarTodas() {
        List<TrocaDTO> trocas = trocaService.listarTodas();
        return ResponseEntity.ok(trocas);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TrocaDTO>> buscarPorStatus(@PathVariable Troca.StatusTroca status) {
        List<TrocaDTO> trocas = trocaService.buscarPorStatus(status);
        return ResponseEntity.ok(trocas);
    }

    // ✅ Buscar minhas trocas
    @GetMapping("/minhas")
    public ResponseEntity<List<TrocaDTO>> buscarMinhasTrocas() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String emailUsuario = authentication.getName();

            System.out.println("🔍 Buscando trocas para usuário: " + emailUsuario);

            Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            System.out.println("🔍 ID do usuário: " + usuario.getId());

            List<TrocaDTO> trocas = trocaService.buscarPorUsuario(usuario.getId());

            System.out.println("🔍 Total de trocas encontradas: " + trocas.size());

            return ResponseEntity.ok(trocas);
        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar trocas: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<TrocaDTO>> buscarPorUsuario(@PathVariable Long usuarioId) {
        List<TrocaDTO> trocas = trocaService.buscarPorUsuario(usuarioId);
        return ResponseEntity.ok(trocas);
    }

    @GetMapping("/usuario/{usuarioId}/status/{status}")
    public ResponseEntity<List<TrocaDTO>> buscarPorUsuarioEStatus(
            @PathVariable Long usuarioId,
            @PathVariable Troca.StatusTroca status) {
        List<TrocaDTO> trocas = trocaService.buscarPorUsuarioEStatus(usuarioId, status);
        return ResponseEntity.ok(trocas);
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<TrocaDTO>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<TrocaDTO> trocas = trocaService.buscarPorPeriodo(inicio, fim);
        return ResponseEntity.ok(trocas);
    }

    @GetMapping("/avaliacao-minima/{avaliacaoMinima}")
    public ResponseEntity<List<TrocaDTO>> buscarPorAvaliacaoMinima(@PathVariable Double avaliacaoMinima) {
        List<TrocaDTO> trocas = trocaService.buscarPorAvaliacaoMinima(avaliacaoMinima);
        return ResponseEntity.ok(trocas);
    }

    @PostMapping("/{id}/gerar-qr")
    public ResponseEntity<TrocaDTO> gerarQRCode(@PathVariable Long id) {
        TrocaDTO troca = trocaService.gerarQRCode(id);
        return ResponseEntity.ok(troca);
    }

    // ✅ Validar QR Code e mudar status para CONCLUÍDA
    @PostMapping("/validar-qrcode")
    public ResponseEntity<TrocaDTO> validarQRCode(@RequestParam String qrCode) {
        try {
            System.out.println("========================================");
            System.out.println("📱 Validando QR Code: " + qrCode);

            // Buscar troca pelo QR Code
            Troca troca = trocaRepository.findByQrCode(qrCode)
                    .orElseThrow(() -> new BusinessException("QR Code inválido ou não encontrado"));

            System.out.println("🔍 Troca encontrada: ID = " + troca.getId());
            System.out.println("🔍 Status atual: " + troca.getStatus());

            // Verificar se a troca está pendente
            if (troca.getStatus() != Troca.StatusTroca.PENDENTE) {
                throw new BusinessException("Esta troca não está mais pendente. Status: " + troca.getStatus());
            }

            // ✅ Mudar status para CONCLUÍDA
            troca.setStatus(Troca.StatusTroca.CONCLUIDA);
            troca.setDataConfirmacao(LocalDateTime.now());
            troca = trocaRepository.save(troca);

            System.out.println("✅ Troca concluída com sucesso!");
            System.out.println("📅 Data de confirmação: " + troca.getDataConfirmacao());
            System.out.println("========================================");

            // Retornar DTO
            TrocaDTO trocaDTO = new TrocaDTO();
            trocaDTO.setId(troca.getId());
            trocaDTO.setStatus(troca.getStatus());
            trocaDTO.setQrCode(troca.getQrCode());
            trocaDTO.setDataConfirmacao(troca.getDataConfirmacao());
            trocaDTO.setCreatedAt(troca.getCreatedAt());

            // Dados da proposta
            if (troca.getProposta() != null) {
                trocaDTO.setPropostaId(troca.getProposta().getId());
                trocaDTO.setDataHora(troca.getProposta().getDataHoraSugerida());
                trocaDTO.setLocal(troca.getProposta().getLocalSugerido());
                trocaDTO.setObservacoes(troca.getProposta().getObservacoes());
            }

            return ResponseEntity.ok(trocaDTO);

        } catch (Exception e) {
            System.err.println("❌ Erro ao validar QR Code: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<TrocaDTO> confirmarTroca(
            @PathVariable Long id,
            @RequestParam String codigoQR) {
        TrocaDTO troca = trocaService.confirmarTroca(id, codigoQR);
        return ResponseEntity.ok(troca);
    }

    @PostMapping("/{id}/concluir")
    public ResponseEntity<TrocaDTO> concluirTroca(
            @PathVariable Long id,
            @RequestParam Double avaliacao,
            @RequestParam(required = false) String comentario) {
        TrocaDTO troca = trocaService.concluirTroca(id, avaliacao, comentario);
        return ResponseEntity.ok(troca);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<TrocaDTO> cancelarTroca(@PathVariable Long id) {
        TrocaDTO troca = trocaService.cancelarTroca(id);
        return ResponseEntity.ok(troca);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarTroca(@PathVariable Long id) {
        trocaService.deletarTroca(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}/count")
    public ResponseEntity<Long> contarTrocasPorStatus(@PathVariable Troca.StatusTroca status) {
        Long count = trocaService.contarTrocasPorStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/media-avaliacao")
    public ResponseEntity<Double> obterMediaAvaliacao() {
        Double media = trocaService.obterMediaAvaliacao();
        return ResponseEntity.ok(media);
    }
}