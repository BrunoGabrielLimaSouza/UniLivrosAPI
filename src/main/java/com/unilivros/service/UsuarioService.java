package com.unilivros.service;

import com.unilivros.dto.UsuarioDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.Usuario;
import com.unilivros.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
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
        
        return usuario;
    }
    
    public UsuarioDTO criarUsuario(UsuarioDTO usuarioDTO) {

        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }
        

        if (usuarioRepository.existsByMatricula(usuarioDTO.getMatricula())) {
            throw new BusinessException("Matrícula já cadastrada");
        }
        
        // Criptografar senha
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        
        Usuario usuario = modelMapper.map(usuarioDTO, Usuario.class);
        usuario = usuarioRepository.save(usuario);
        
        return modelMapper.map(usuario, UsuarioDTO.class);
    }
    
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        return modelMapper.map(usuario, UsuarioDTO.class);
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
    
    public UsuarioDTO atualizarUsuario(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        

        if (!usuarioExistente.getEmail().equals(usuarioDTO.getEmail()) && 
            usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }
        

        if (!usuarioExistente.getMatricula().equals(usuarioDTO.getMatricula()) && 
            usuarioRepository.existsByMatricula(usuarioDTO.getMatricula())) {
            throw new BusinessException("Matrícula já cadastrada");
        }
        

        if (usuarioDTO.getSenha() != null && !usuarioDTO.getSenha().isEmpty()) {
            usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        } else {
            usuarioDTO.setSenha(usuarioExistente.getSenha());
        }
        
        modelMapper.map(usuarioDTO, usuarioExistente);
        usuarioExistente = usuarioRepository.save(usuarioExistente);
        
        return modelMapper.map(usuarioExistente, UsuarioDTO.class);
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
}
