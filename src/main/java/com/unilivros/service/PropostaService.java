package com.unilivros.service;

import com.unilivros.dto.PropostaDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.Notificacao;
import com. unilivros.model. Proposta;
import com.unilivros.model.Usuario;
import com.unilivros.repository.PropostaRepository;
import com.unilivros.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory. annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction. annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream. Collectors;

@Service
@Transactional
public class PropostaService {

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private NotificacaoService notificacaoService;

    public PropostaDTO criarProposta(PropostaDTO propostaDTO) {
        // Verificar se proponente existe
        Usuario proponente = usuarioRepository.findById(propostaDTO.getProponenteId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário proponente", propostaDTO.getProponenteId()));

        // Verificar se proposto existe
        Usuario proposto = usuarioRepository.findById(propostaDTO.getPropostoId())
                . orElseThrow(() -> new ResourceNotFoundException("Usuário proposto", propostaDTO.getPropostoId()));

        // Verificar se não é a mesma pessoa
        if (proponente.getId().equals(proposto.getId())) {
            throw new BusinessException("Não é possível criar proposta para si mesmo");
        }

        Proposta proposta = new Proposta(proponente, proposto);
        proposta = propostaRepository.save(proposta);

        // ✅ Criar notificação para quem RECEBEU a proposta
        notificacaoService.criarNotificacao(
                proposto.getId(),
                "Nova proposta de troca! ",
                proponente.getNome() + " enviou uma proposta de troca para você.",
                Notificacao.TipoNotificacao.PROPOSTA_RECEBIDA,
                proposta.getId(),
                null
        );

        // ✅ Mapear para DTO e preencher todos os campos
        PropostaDTO resultado = new PropostaDTO(proposta);
        resultado.setLivroOferecidoId(propostaDTO.getLivroOferecidoId());
        resultado.setLivroOferecidoTitulo(propostaDTO.getLivroOferecidoTitulo());
        resultado.setLivroDesejadoId(propostaDTO.getLivroDesejadoId());
        resultado.setLivroDesejadoTitulo(propostaDTO.getLivroDesejadoTitulo());
        resultado.setDataTroca(propostaDTO.getDataTroca());
        resultado.setLocal(propostaDTO.getLocal());
        resultado.setObservacao(propostaDTO.getObservacao());
        resultado.setNomeUsuarioRelacionado(proposto.getNome());

        return resultado;
    }

    public PropostaDTO aceitarProposta(Long id) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta", id));

        if (proposta. getStatus() != Proposta.StatusProposta.PENDENTE) {
            throw new BusinessException("Apenas propostas pendentes podem ser aceitas");
        }

        proposta.setStatus(Proposta.StatusProposta.ACEITA);
        proposta. setDataResposta(LocalDateTime.now());
        proposta = propostaRepository.save(proposta);

        // ✅ CRIAR NOTIFICAÇÃO para quem ENVIOU a proposta
        notificacaoService.criarNotificacao(
                proposta.getProponente().getId(),
                "Sua proposta foi aceita!",
                proposta.getProposto().getNome() + " aceitou sua proposta de troca.",
                Notificacao.TipoNotificacao.PROPOSTA_ACEITA,
                proposta.getId(),
                null
        );

        return modelMapper.map(proposta, PropostaDTO. class);
    }

    public PropostaDTO rejeitarProposta(Long id) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta", id));

        if (proposta. getStatus() != Proposta. StatusProposta.PENDENTE) {
            throw new BusinessException("Apenas propostas pendentes podem ser rejeitadas");
        }

        proposta.setStatus(Proposta.StatusProposta.REJEITADA);
        proposta.setDataResposta(LocalDateTime.now());
        proposta = propostaRepository.save(proposta);

        // ✅ CRIAR NOTIFICAÇÃO para quem ENVIOU a proposta
        notificacaoService.criarNotificacao(
                proposta.getProponente().getId(),
                "Proposta rejeitada",
                proposta.getProposto().getNome() + " rejeitou sua proposta de troca.",
                Notificacao.TipoNotificacao.PROPOSTA_REJEITADA,
                proposta.getId(),
                null
        );

        return modelMapper.map(proposta, PropostaDTO.class);
    }

    @Transactional(readOnly = true)
    public PropostaDTO buscarPorId(Long id) {
        Proposta proposta = propostaRepository. findById(id)
                . orElseThrow(() -> new ResourceNotFoundException("Proposta", id));
        return modelMapper.map(proposta, PropostaDTO.class);
    }

    @Transactional(readOnly = true)
    public List<PropostaDTO> listarTodas() {
        return propostaRepository.findAll().stream()
                .map(proposta -> modelMapper.map(proposta, PropostaDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropostaDTO> buscarPorProponente(Long proponenteId) {
        Usuario proponente = usuarioRepository.findById(proponenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", proponenteId));

        return propostaRepository.findByProponente(proponente). stream()
                .map(proposta -> modelMapper.map(proposta, PropostaDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropostaDTO> buscarPorProposto(Long propostoId) {
        Usuario proposto = usuarioRepository.findById(propostoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", propostoId));

        return propostaRepository.findByProposto(proposto).stream()
                .map(proposta -> modelMapper.map(proposta, PropostaDTO.class))
                .collect(Collectors. toList());
    }

    @Transactional(readOnly = true)
    public List<PropostaDTO> buscarPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        return propostaRepository. findByUsuario(usuario).stream()
                .map(proposta -> modelMapper.map(proposta, PropostaDTO.class))
                .collect(Collectors. toList());
    }

    @Transactional(readOnly = true)
    public List<PropostaDTO> buscarPorStatus(Proposta.StatusProposta status) {
        return propostaRepository. findByStatus(status).stream()
                .map(proposta -> modelMapper.map(proposta, PropostaDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropostaDTO> buscarPorUsuarioEStatus(Long usuarioId, Proposta.StatusProposta status) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        return propostaRepository.findByUsuarioAndStatus(usuario, status).stream()
                .map(proposta -> modelMapper.map(proposta, PropostaDTO.class))
                .collect(Collectors.toList());
    }

    public PropostaDTO cancelarProposta(Long id) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta", id));

        if (proposta.getStatus() == Proposta.StatusProposta.CANCELADA) {
            throw new BusinessException("Proposta já está cancelada");
        }

        proposta.setStatus(Proposta.StatusProposta. CANCELADA);
        proposta.setDataResposta(LocalDateTime.now());
        proposta = propostaRepository.save(proposta);

        return modelMapper.map(proposta, PropostaDTO.class);
    }

    public void deletarProposta(Long id) {
        if (!propostaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proposta", id);
        }
        propostaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Long contarPropostasPorProponenteEStatus(Long proponenteId, Proposta.StatusProposta status) {
        Usuario proponente = usuarioRepository. findById(proponenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", proponenteId));

        return propostaRepository.countByProponenteAndStatus(proponente, status);
    }

    @Transactional(readOnly = true)
    public Long contarPropostasPorPropostoEStatus(Long propostoId, Proposta.StatusProposta status) {
        Usuario proposto = usuarioRepository.findById(propostoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", propostoId));

        return propostaRepository.countByPropostoAndStatus(proposto, status);
    }

    // ✅ MÉTODOS CORRIGIDOS
    @Transactional(readOnly = true)
    public List<PropostaDTO> buscarPropostasRecebidas(Long usuarioId) {
        return propostaRepository.findByProposto_Id(usuarioId)
                .stream()
                . map(proposta -> {
                    PropostaDTO dto = new PropostaDTO(proposta);
                    // Nome do usuário que ENVIOU a proposta
                    dto.setNomeUsuarioRelacionado(proposta.getProponente().getNome());
                    // Campos temporários (depois implementar com livros reais)
                    dto.setLivroOferecidoTitulo("Livro Oferecido");
                    dto. setLivroDesejadoTitulo("Livro Desejado");
                    dto.setDataTroca("Não informada");
                    dto.setLocal("Não informado");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropostaDTO> buscarPropostasEnviadas(Long usuarioId) {
        return propostaRepository.findByProponente_Id(usuarioId)
                .stream()
                .map(proposta -> {
                    PropostaDTO dto = new PropostaDTO(proposta);
                    // Nome do usuário que RECEBEU a proposta
                    dto.setNomeUsuarioRelacionado(proposta.getProposto().getNome());
                    // Campos temporários (depois implementar com livros reais)
                    dto.setLivroOferecidoTitulo("Livro Oferecido");
                    dto. setLivroDesejadoTitulo("Livro Desejado");
                    dto.setDataTroca("Não informada");
                    dto.setLocal("Não informado");
                    return dto;
                })
                .collect(Collectors.toList());
    }
}