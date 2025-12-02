package com.unilivros.controller;

import com.unilivros.dto.PropostaDTO;
import com.unilivros.model.Proposta;
import com.unilivros.model.Usuario;
import com.unilivros.repository.UsuarioRepository;
import com.unilivros.service.PropostaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/propostas")
@CrossOrigin(origins = "*")
public class PropostaController {
    
    @Autowired
    private PropostaService propostaService;

    @Autowired
    private UsuarioRepository  usuarioRepository;
    
    @PostMapping
    public ResponseEntity<PropostaDTO> criarProposta(@Valid @RequestBody PropostaDTO propostaDTO) {
        PropostaDTO propostaCriada = propostaService.criarProposta(propostaDTO);
        return new ResponseEntity<>(propostaCriada, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PropostaDTO> buscarPorId(@PathVariable Long id) {
        PropostaDTO proposta = propostaService.buscarPorId(id);
        return ResponseEntity.ok(proposta);
    }
    
    @GetMapping
    public ResponseEntity<List<PropostaDTO>> listarTodas() {
        List<PropostaDTO> propostas = propostaService.listarTodas();
        return ResponseEntity.ok(propostas);
    }
    
    @GetMapping("/proponente/{proponenteId}")
    public ResponseEntity<List<PropostaDTO>> buscarPorProponente(@PathVariable Long proponenteId) {
        List<PropostaDTO> propostas = propostaService.buscarPorProponente(proponenteId);
        return ResponseEntity.ok(propostas);
    }
    
    @GetMapping("/proposto/{propostoId}")
    public ResponseEntity<List<PropostaDTO>> buscarPorProposto(@PathVariable Long propostoId) {
        List<PropostaDTO> propostas = propostaService.buscarPorProposto(propostoId);
        return ResponseEntity.ok(propostas);
    }
    
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PropostaDTO>> buscarPorUsuario(@PathVariable Long usuarioId) {
        List<PropostaDTO> propostas = propostaService.buscarPorUsuario(usuarioId);
        return ResponseEntity.ok(propostas);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PropostaDTO>> buscarPorStatus(@PathVariable Proposta.StatusProposta status) {
        List<PropostaDTO> propostas = propostaService.buscarPorStatus(status);
        return ResponseEntity.ok(propostas);
    }
    
    @GetMapping("/usuario/{usuarioId}/status/{status}")
    public ResponseEntity<List<PropostaDTO>> buscarPorUsuarioEStatus(
            @PathVariable Long usuarioId, 
            @PathVariable Proposta.StatusProposta status) {
        List<PropostaDTO> propostas = propostaService.buscarPorUsuarioEStatus(usuarioId, status);
        return ResponseEntity.ok(propostas);
    }
    
    @PostMapping("/{id}/aceitar")
    public ResponseEntity<PropostaDTO> aceitarProposta(@PathVariable Long id) {
        PropostaDTO proposta = propostaService.aceitarProposta(id);
        return ResponseEntity.ok(proposta);
    }
    
    @PostMapping("/{id}/rejeitar")
    public ResponseEntity<PropostaDTO> rejeitarProposta(@PathVariable Long id) {
        PropostaDTO proposta = propostaService.rejeitarProposta(id);
        return ResponseEntity.ok(proposta);
    }
    
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PropostaDTO> cancelarProposta(@PathVariable Long id) {
        PropostaDTO proposta = propostaService.cancelarProposta(id);
        return ResponseEntity.ok(proposta);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProposta(@PathVariable Long id) {
        propostaService.deletarProposta(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/proponente/{proponenteId}/status/{status}/count")
    public ResponseEntity<Long> contarPropostasPorProponenteEStatus(
            @PathVariable Long proponenteId, 
            @PathVariable Proposta.StatusProposta status) {
        Long count = propostaService.contarPropostasPorProponenteEStatus(proponenteId, status);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/proposto/{propostoId}/status/{status}/count")
    public ResponseEntity<Long> contarPropostasPorPropostoEStatus(
            @PathVariable Long propostoId, 
            @PathVariable Proposta.StatusProposta status) {
        Long count = propostaService.contarPropostasPorPropostoEStatus(propostoId, status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/recebidas")
    public ResponseEntity<List<PropostaDTO>> listarPropostasRecebidas() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<PropostaDTO> recebidas = propostaService.buscarPropostasRecebidas(usuario.getId());

        return ResponseEntity.ok(recebidas);
    }

    @GetMapping("/enviadas")
    public ResponseEntity<List<PropostaDTO>> listarPropostasEnviadas() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<PropostaDTO> enviadas = propostaService.buscarPropostasEnviadas(usuario.getId());

        return ResponseEntity.ok(enviadas);
    }

}
