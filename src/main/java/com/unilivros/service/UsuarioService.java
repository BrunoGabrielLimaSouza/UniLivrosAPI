package com.unilivros.service;

import com.unilivros.dto.AvaliacaoDTO;
import com.unilivros.dto.ConquistaDTO;
import com.unilivros.dto.LivroDTO;
import com.unilivros.dto.UsuarioDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.Usuario;
import com.unilivros.model.UsuarioLivro;
import com.unilivros.repository.UsuarioLivroRepository;
import com.unilivros.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioLivroRepository usuarioLivroRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Usuario authenticateUser(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new BusinessException("Email ou senha inválidos");
        }

        if (!usuario.isEnabled()) {
            throw new BusinessException("Usuário não confirmado. Verifique seu e-mail.");
        }

        return usuario;
    }

    public UsuarioDTO criarUsuario(UsuarioDTO usuarioDTO) {
        // Validação manual de senha OBRIGATÓRIA na criação
        if (usuarioDTO.getSenha() == null || usuarioDTO.getSenha().trim().isEmpty()) {
            throw new BusinessException("A senha é obrigatória para o cadastro.");
        }
        if (usuarioDTO.getSenha().length() < 8) {
            throw new BusinessException("A senha deve ter pelo menos 8 caracteres.");
        }

        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }
        if (usuarioRepository.existsByMatricula(usuarioDTO.getMatricula())) {
            throw new BusinessException("Matrícula já cadastrada");
        }

        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));

        Usuario usuario = modelMapper.map(usuarioDTO, Usuario.class);
        usuario = usuarioRepository.save(usuario);

        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    public UsuarioDTO atualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        // Verifica se mudou o email e se já existe
        if (!usuarioExistente.getEmail().equals(usuarioDTO.getEmail()) &&
                usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }

        // Verifica se mudou a matrícula e se já existe
        if (usuarioDTO.getMatricula() != null &&
                !usuarioExistente.getMatricula().equals(usuarioDTO.getMatricula()) &&
                usuarioRepository.existsByMatricula(usuarioDTO.getMatricula())) {
            throw new BusinessException("Matrícula já cadastrada");
        }

        // ⚠️ LÓGICA DE SENHA OPCIONAL
        String senhaParaSalvar = usuarioExistente.getSenha(); // Por padrão, mantém a antiga

        if (usuarioDTO.getSenha() != null && !usuarioDTO.getSenha().trim().isEmpty()) {
            // Se o usuário enviou uma nova senha, valida e encripta
            if (usuarioDTO.getSenha().length() < 8) {
                throw new BusinessException("A nova senha deve ter pelo menos 8 caracteres.");
            }
            senhaParaSalvar = passwordEncoder.encode(usuarioDTO.getSenha());
        }

        // Mapeia os dados novos (Nome, Curso, etc) para a entidade existente
        modelMapper.map(usuarioDTO, usuarioExistente);

        // Reaplica a senha correta (Antiga ou Nova Encriptada)
        // Isso impede que o ModelMapper sobrescreva com null
        usuarioExistente.setSenha(senhaParaSalvar);

        usuarioExistente = usuarioRepository.save(usuarioExistente);

        return modelMapper.map(usuarioExistente, UsuarioDTO.class);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    @Transactional(readOnly = true)
    public List<LivroDTO> buscarLivrosPorUsuarioId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        List<UsuarioLivro> relacoes = usuarioLivroRepository.findByUsuario(usuario);

        return relacoes.stream()
                .map(rel -> modelMapper.map(rel.getLivro(), LivroDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoDTO> buscarAvaliacoesPorUsuarioId(Long id) {
        return new ArrayList<>();
    }

    @Transactional(readOnly = true)
    public List<ConquistaDTO> buscarConquistasPorUsuarioId(Long id) {
        return new ArrayList<>();
    }

    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "email", email));
        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorMatricula(String matricula) {
        Usuario usuario = usuarioRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "matrícula", matricula));
        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarPorCurso(String curso) {
        return usuarioRepository.findByCurso(curso).stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarPorSemestre(Integer semestre) {
        return usuarioRepository.findBySemestre(semestre).stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarPorCursoESemestre(String curso, Integer semestre) {
        return usuarioRepository.findByCursoAndSemestre(curso, semestre).stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarPorXpMinimo(Integer xpMinimo) {
        return usuarioRepository.findByXpGreaterThanEqualOrderByXpDesc(xpMinimo).stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarPorAvaliacaoMinima(Double avaliacaoMinima) {
        return usuarioRepository.findByAvaliacaoGreaterThanEqualOrderByAvaliacaoDesc(avaliacaoMinima).stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarPorNomeOuEmail(String termo) {
        return usuarioRepository.findByNomeContainingOrEmailContaining(termo, termo).stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class))
                .collect(Collectors.toList());
    }

    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário", id);
        }
        usuarioRepository.deleteById(id);
    }

    public void adicionarXp(Long usuarioId, Integer xp) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        usuario.setXp(usuario.getXp() + xp);
        usuarioRepository.save(usuario);
    }

    public void atualizarAvaliacao(Long usuarioId, Double novaAvaliacao) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        Double avaliacaoAtual = usuario.getAvaliacao();
        if (avaliacaoAtual == null || avaliacaoAtual == 0.0) {
            usuario.setAvaliacao(novaAvaliacao);
        } else {
            usuario.setAvaliacao((avaliacaoAtual + novaAvaliacao) / 2);
        }

        usuarioRepository.save(usuario);
    }

    public UsuarioDTO uploadAvatar(Long id, MultipartFile file) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        try {
            // Valida se é uma imagem
            if (!file.getContentType().startsWith("image/")) {
                throw new BusinessException("O arquivo deve ser uma imagem.");
            }

            // Converte o arquivo para Base64
            byte[] fileBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);

            // Monta a String pronta para o src do HTML (ex: data:image/png;base64,.....)
            String avatarDataUrl = "data:" + file.getContentType() + ";base64," + base64Image;

            usuario.setAvatarUrl(avatarDataUrl);

            // Salva no banco
            usuario = usuarioRepository.save(usuario);

            return modelMapper.map(usuario, UsuarioDTO.class);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar imagem", e);
        }
    }
}