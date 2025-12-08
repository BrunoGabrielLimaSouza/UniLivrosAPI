package com.unilivros.service;

import com.unilivros.dto.PropostaDTO;
import com.unilivros.dto.TrocaDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.*;
import com.unilivros.repository.AgendamentoRepository;
import com.unilivros.repository.LivroPropostaRepository;
import com.unilivros.repository. LivroRepository;
import com.unilivros.repository.PropostaRepository;
import com. unilivros.repository. UsuarioRepository;
import org. modelmapper.ModelMapper;
import org.springframework.beans.factory. annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction. annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util. List;
import java.util. stream.Collectors;

@Service
@Transactional
public class PropostaService {

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private LivroPropostaRepository livroPropostaRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private TrocaService trocaService;

    @Autowired
    private ModelMapper modelMapper;

    // ✅ CRIAR PROPOSTA
    public PropostaDTO criarProposta(PropostaDTO propostaDTO) {
        Usuario proponente = usuarioRepository.findById(propostaDTO.getProponenteId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário proponente", propostaDTO.getProponenteId()));

        Usuario proposto = usuarioRepository.findById(propostaDTO.getPropostoId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário proposto", propostaDTO.getPropostoId()));

        if (proponente.getId(). equals(proposto.getId())) {
            throw new BusinessException("Não é possível criar proposta para si mesmo");
        }

        Proposta proposta = new Proposta(proponente, proposto);
        proposta.setStatus(Proposta.StatusProposta.PENDENTE);

        // ✅ Salvar dados do agendamento direto na proposta
        proposta.setDataHoraSugerida(propostaDTO.getDataHoraSugerida());
        proposta.setLocalSugerido(propostaDTO.getLocalSugerido());
        proposta.setObservacoes(propostaDTO.getObservacoes());

        proposta = propostaRepository.save(proposta);

        // ✅ Salvar livros
        if (propostaDTO.getLivroOferecidoId() != null) {
            Livro livroOferecido = livroRepository.findById(propostaDTO.getLivroOferecidoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Livro oferecido", propostaDTO.getLivroOferecidoId()));

            livroPropostaRepository.save(new LivroProposta(livroOferecido, proposta, LivroProposta.TipoLivroProposta. OFERTA));
        }

        if (propostaDTO.getLivroDesejadoId() != null) {
            Livro livroDesejado = livroRepository.findById(propostaDTO.getLivroDesejadoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Livro desejado", propostaDTO.getLivroDesejadoId()));

            livroPropostaRepository.save(new LivroProposta(livroDesejado, proposta, LivroProposta.TipoLivroProposta.SOLICITACAO));
        }

        // ✅ Criar notificação
        notificacaoService.criarNotificacao(
                proposto.getId(),
                "Nova proposta de troca! ",
                proponente.getNome() + " enviou uma proposta de troca para você.",
                Notificacao.TipoNotificacao.PROPOSTA_RECEBIDA,
                proposta. getId(),
                null
        );

        // ✅ Montar DTO de resposta
        PropostaDTO resultado = new PropostaDTO(proposta);
        resultado.setNomeUsuarioRelacionado(proposto.getNome());

        List<LivroProposta> livrosAssociados = livroPropostaRepository.findByProposta(proposta);
        for (LivroProposta lp : livrosAssociados) {
            if (lp.getTipo() == LivroProposta.TipoLivroProposta.OFERTA) {
                resultado.setLivroOferecidoId(lp. getLivro().getId());
                resultado.setLivroOferecidoTitulo(lp.getLivro().getTitulo());
            } else if (lp.getTipo() == LivroProposta.TipoLivroProposta. SOLICITACAO) {
                resultado.setLivroDesejadoId(lp.getLivro().getId());
                resultado.setLivroDesejadoTitulo(lp. getLivro().getTitulo());
            }
        }

        return resultado;
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
                .map(proposta -> modelMapper.map(proposta, PropostaDTO. class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropostaDTO> buscarPorProponente(Long proponenteId) {
        Usuario proponente = usuarioRepository. findById(proponenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", proponenteId));

        return propostaRepository.findByProponente(proponente).stream()
                .map(proposta -> modelMapper.map(proposta, PropostaDTO.class))
                . collect(Collectors.toList());
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
        return propostaRepository.findByStatus(status). stream()
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

    public PropostaDTO aceitarProposta(Long id) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta", id));

        if (proposta.getStatus() != Proposta.StatusProposta. PENDENTE) {
            throw new BusinessException("Apenas propostas pendentes podem ser aceitas");
        }

        proposta.setStatus(Proposta.StatusProposta.ACEITA);
        proposta. setDataResposta(LocalDateTime.now());

        // ✅ Criar Agendamento
        Agendamento agendamento = new Agendamento(proposta);
        agendamento. setStatus(Agendamento.StatusAgendamento.CONFIRMADO); // Já confirmar
        agendamento = agendamentoRepository.save(agendamento);

        proposta.setAgendamento(agendamento);
        proposta = propostaRepository.save(proposta);

        // ✅ Criar Troca automaticamente
        TrocaDTO trocaDTO = new TrocaDTO();
        trocaDTO.setAgendamentoId(agendamento.getId());
        trocaDTO. setStatus(Troca.StatusTroca.PENDENTE);
        trocaService.criarTroca(trocaDTO);

        // Notificação
        notificacaoService.criarNotificacao(
                proposta.getProponente().getId(),
                "Sua proposta foi aceita! ",
                proposta.getProposto().getNome() + " aceitou sua proposta de troca.",
                Notificacao.TipoNotificacao.PROPOSTA_ACEITA,
                proposta.getId(),
                null
        );

        return new PropostaDTO(proposta);
    }

    // ✅ REJEITAR PROPOSTA
    public PropostaDTO rejeitarProposta(Long id) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta", id));

        if (proposta.getStatus() != Proposta.StatusProposta.PENDENTE) {
            throw new BusinessException("Apenas propostas pendentes podem ser rejeitadas");
        }

        proposta. setStatus(Proposta.StatusProposta.REJEITADA);
        proposta.setDataResposta(LocalDateTime.now());
        proposta = propostaRepository.save(proposta);

        notificacaoService.criarNotificacao(
                proposta. getProponente().getId(),
                "Proposta rejeitada",
                proposta.getProposto().getNome() + " rejeitou sua proposta de troca.",
                Notificacao.TipoNotificacao.PROPOSTA_REJEITADA,
                proposta.getId(),
                null
        );

        return new PropostaDTO(proposta);
    }

    public PropostaDTO cancelarProposta(Long id) {
        Proposta proposta = propostaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta", id));

        if (proposta. getStatus() == Proposta.StatusProposta.CANCELADA) {
            throw new BusinessException("Proposta já está cancelada");
        }

        proposta.setStatus(Proposta.StatusProposta.CANCELADA);
        proposta.setDataResposta(LocalDateTime.now());
        proposta = propostaRepository.save(proposta);

        return new PropostaDTO(proposta);
    }

    public void deletarProposta(Long id) {
        if (!propostaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proposta", id);
        }
        propostaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Long contarPropostasPorProponenteEStatus(Long proponenteId, Proposta.StatusProposta status) {
        Usuario proponente = usuarioRepository.findById(proponenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", proponenteId));

        return propostaRepository. countByProponenteAndStatus(proponente, status);
    }

    @Transactional(readOnly = true)
    public Long contarPropostasPorPropostoEStatus(Long propostoId, Proposta.StatusProposta status) {
        Usuario proposto = usuarioRepository.findById(propostoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", propostoId));

        return propostaRepository.countByPropostoAndStatus(proposto, status);
    }

    // ✅ BUSCAR PROPOSTAS RECEBIDAS (COM DADOS COMPLETOS)
    @Transactional(readOnly = true)
    public List<PropostaDTO> buscarPropostasRecebidas(Long usuarioId) {
        return propostaRepository.findByProposto_Id(usuarioId)
                .stream()
                . map(proposta -> {
                    PropostaDTO dto = new PropostaDTO(proposta);
                    dto.setNomeUsuarioRelacionado(proposta.getProponente().getNome());

                    // ✅ Buscar livros associados
                    List<LivroProposta> livros = livroPropostaRepository. findByProposta(proposta);
                    for (LivroProposta lp : livros) {
                        if (lp.getTipo() == LivroProposta.TipoLivroProposta. OFERTA) {
                            dto.setLivroOferecidoId(lp. getLivro().getId());
                            dto.setLivroOferecidoTitulo(lp.getLivro().getTitulo());
                        } else if (lp.getTipo() == LivroProposta. TipoLivroProposta.SOLICITACAO) {
                            dto.setLivroDesejadoId(lp.getLivro().getId());
                            dto.setLivroDesejadoTitulo(lp.getLivro(). getTitulo());
                        }
                    }

                    // ✅ PREENCHER DADOS DA PROPOSTA (data, local, observações)
                    dto.setDataHoraSugerida(proposta.getDataHoraSugerida());
                    dto.setLocalSugerido(proposta.getLocalSugerido());
                    dto.setObservacoes(proposta.getObservacoes());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ✅ BUSCAR PROPOSTAS ENVIADAS (COM DADOS COMPLETOS)
    @Transactional(readOnly = true)
    public List<PropostaDTO> buscarPropostasEnviadas(Long usuarioId) {
        return propostaRepository.findByProponente_Id(usuarioId)
                .stream()
                .map(proposta -> {
                    PropostaDTO dto = new PropostaDTO(proposta);
                    dto.setNomeUsuarioRelacionado(proposta.getProposto().getNome());

                    // ✅ Buscar livros associados
                    List<LivroProposta> livros = livroPropostaRepository.findByProposta(proposta);
                    for (LivroProposta lp : livros) {
                        if (lp.getTipo() == LivroProposta.TipoLivroProposta.OFERTA) {
                            dto.setLivroOferecidoId(lp.getLivro(). getId());
                            dto.setLivroOferecidoTitulo(lp.getLivro().getTitulo());
                        } else if (lp.getTipo() == LivroProposta.TipoLivroProposta.SOLICITACAO) {
                            dto.setLivroDesejadoId(lp.getLivro().getId());
                            dto.setLivroDesejadoTitulo(lp. getLivro().getTitulo());
                        }
                    }

                    // ✅ PREENCHER DADOS DA PROPOSTA (data, local, observações)
                    dto.setDataHoraSugerida(proposta.getDataHoraSugerida());
                    dto.setLocalSugerido(proposta.getLocalSugerido());
                    dto.setObservacoes(proposta.getObservacoes());

                    return dto;
                })
                . collect(Collectors.toList());
    }
}