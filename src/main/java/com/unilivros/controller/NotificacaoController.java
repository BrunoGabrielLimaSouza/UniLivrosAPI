package com.unilivros.controller;

import com.unilivros.dto.NotificacaoDTO;
import com.unilivros. model.Usuario;
import com.unilivros.repository.UsuarioRepository;
import com.unilivros.service.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework. security.core.Authentication;
import org.springframework.security.core. context.SecurityContextHolder;
import org.springframework.web.bind. annotation.*;

import java.util. List;

@RestController
@RequestMapping("/notificacoes")
@CrossOrigin(origins = "*")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext(). getAuthentication();
        String emailUsuario = authentication.getName();
        return usuarioRepository.findByEmail(emailUsuario)
                . orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoDTO>> listarNotificacoes() {
        Usuario usuario = getUsuarioAutenticado();
        List<NotificacaoDTO> notificacoes = notificacaoService.buscarPorUsuario(usuario.getId());
        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<List<NotificacaoDTO>> listarNaoLidas() {
        Usuario usuario = getUsuarioAutenticado();
        List<NotificacaoDTO> notificacoes = notificacaoService.buscarNaoLidas(usuario.getId());
        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> contarNaoLidas() {
        Usuario usuario = getUsuarioAutenticado();
        Long count = notificacaoService.contarNaoLidas(usuario. getId());
        return ResponseEntity. ok(count);
    }

    @PutMapping("/{id}/marcar-lida")
    public ResponseEntity<NotificacaoDTO> marcarComoLida(@PathVariable Long id) {
        NotificacaoDTO notificacao = notificacaoService.marcarComoLida(id);
        return ResponseEntity.ok(notificacao);
    }

    @PutMapping("/marcar-todas-lidas")
    public ResponseEntity<Void> marcarTodasComoLidas() {
        Usuario usuario = getUsuarioAutenticado();
        notificacaoService.marcarTodasComoLidas(usuario.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarNotificacao(@PathVariable Long id) {
        notificacaoService.deletarNotificacao(id);
        return ResponseEntity.noContent().build();
    }
}