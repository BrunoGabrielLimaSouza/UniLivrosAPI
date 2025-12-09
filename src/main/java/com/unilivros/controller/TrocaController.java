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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static com.unilivros.controller.AuthController.logger;

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
        // Obter o ID do usuário autenticado para garantir a segurança ou registrar
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o e-mail: " + emailUsuario));

        // A propostaId deve estar no DTO
        if (trocaDTO.getPropostaId() == null) {
            throw new BusinessException("A Proposta ID é obrigatória para criar uma troca.");
        }

        TrocaDTO trocaCriada = trocaService.criarTroca(trocaDTO.getPropostaId());
        return new ResponseEntity<>(trocaCriada, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrocaDTO> buscarPorId(@PathVariable Long id) {
        TrocaDTO troca = trocaService.buscarPorId(id);
        return ResponseEntity.ok(troca);
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<TrocaDTO>> listarMinhasTrocas() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o e-mail: " + emailUsuario));

        List<TrocaDTO> minhasTrocas = trocaService.buscarMinhasTrocas(usuario.getId());

        return ResponseEntity.ok(minhasTrocas);
    }

    @GetMapping
    public ResponseEntity<List<TrocaDTO>> listarTodasTrocas() {
        List<TrocaDTO> trocas = trocaService.listarTodas();
        return ResponseEntity.ok(trocas);
    }

    // Endpoint para confirmar a troca com o código QR
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
    public ResponseEntity<Double> obterMediaAvaliacaoGeral() {
        Double media = trocaService.obterMediaAvaliacaoGeral();
        return ResponseEntity.ok(media);
    }

}