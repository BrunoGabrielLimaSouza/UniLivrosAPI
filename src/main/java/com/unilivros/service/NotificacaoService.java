package com.unilivros.service;

import com.unilivros.dto. NotificacaoDTO;
import com.unilivros.dto.NotificacaoStatusDTO;
import com.unilivros.exception.ResourceNotFoundException;
import com. unilivros.model. Notificacao;
import com. unilivros.model.Usuario;
import com.unilivros.repository.NotificacaoRepository;
import com.unilivros.repository.UsuarioRepository;
import org.springframework. beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework. stereotype.Service;
import org. springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public NotificacaoDTO criarNotificacao(Long usuarioId, String titulo, String mensagem,
                                           Notificacao.TipoNotificacao tipo, Long propostaId, Long trocaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        Notificacao notificacao = new Notificacao(usuario, titulo, mensagem, tipo);
        notificacao.setPropostaId(propostaId);
        notificacao.setTrocaId(trocaId);

        notificacao = notificacaoRepository.save(notificacao);

        return new NotificacaoDTO(notificacao);
    }

    @Transactional(readOnly = true)
    public List<NotificacaoDTO> buscarPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                . orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        return notificacaoRepository.findByUsuarioOrderByCreatedAtDesc(usuario)
                .stream()
                .map(NotificacaoDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificacaoDTO> buscarNaoLidas(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        return notificacaoRepository. findByUsuarioAndLidaOrderByCreatedAtDesc(usuario, false)
                .stream()
                .map(NotificacaoDTO::new)
                . collect(Collectors.toList());
    }

    @Transactional
    public NotificacaoDTO marcarComoLida(Long id) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação", id));

        notificacao.setLida(true);
        notificacao = notificacaoRepository.save(notificacao);

        return new NotificacaoDTO(notificacao);
    }

    @Transactional
    public void marcarTodasComoLidas(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioAndLidaOrderByCreatedAtDesc(usuario, false);
        notificacoes.forEach(n -> n.setLida(true));
        notificacaoRepository.saveAll(notificacoes);
    }

    @Transactional(readOnly = true)
    public Long contarNaoLidas(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        return notificacaoRepository.countByUsuarioAndLida(usuario, false);
    }

    @Transactional
    public void deletarNotificacao(Long id) {
        if (!notificacaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notificação", id);
        }
        notificacaoRepository.deleteById(id);
    }

    public NotificacaoStatusDTO verificarStatusNaoLidas() {
        // 1. Obter o e-mail do usuário autenticado (padrão que você já usa)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        // 2. Buscar o ID do usuário pelo e-mail
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado ou não autenticado."));

        // 3. Contar notificações não lidas
        long count = notificacaoRepository.countByUsuarioIdAndLidaFalse(usuario.getId());

        // 4. Retornar o DTO
        boolean hasUnread = count > 0;
        return new NotificacaoStatusDTO(hasUnread);
    }
}