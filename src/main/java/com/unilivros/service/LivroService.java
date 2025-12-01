package com.unilivros.service;

import com.unilivros.dto.LivroDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.Livro;
import com.unilivros.model.Usuario;
import com.unilivros.model.UsuarioLivro;
import com.unilivros.repository.LivroRepository;
import com.unilivros.repository.UsuarioLivroRepository;
import com.unilivros.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LivroService {
    
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioLivroRepository usuarioLivroRepository;

    public List<LivroDTO> buscarLivrosDoUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));

        List<UsuarioLivro> meusLivrosRelacao = usuarioLivroRepository.findByUsuario(usuario);

        return meusLivrosRelacao.stream()
                .map(relacao -> converterParaDTO(relacao.getLivro()))
                .collect(Collectors.toList());
    }

    private LivroDTO converterParaDTO(Livro livro) {
        LivroDTO dto = new LivroDTO();
        dto.setId(livro.getId());
        dto.setTitulo(livro.getTitulo());
        dto.setAutor(livro.getAutor());
        dto.setEditora(livro.getEditora());
        dto.setAno(livro.getAno());
        dto.setGenero(livro.getGenero());
        dto.setCondicao(livro.getCondicao());
        dto.setDescricao(livro.getDescricao());
        return dto;
    }

    public LivroDTO criarLivro(LivroDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Livro livro = new Livro();
        livro.setTitulo(dto.getTitulo());
        livro.setAutor(dto.getAutor());
        livro.setEditora(dto.getEditora());
        livro.setAno(dto.getAno());
        livro.setGenero(dto.getGenero());
        livro.setCondicao(dto.getCondicao()); // Certifique-se que o Enum bate
        livro.setDescricao(dto.getDescricao());

        livro = livroRepository.save(livro);

        UsuarioLivro vinculo = new UsuarioLivro();
        vinculo.setUsuario(usuario);
        vinculo.setLivro(livro);
        vinculo.setDisponivelParaTroca(true); // Define se já entra disponível ou não

        usuarioLivroRepository.save(vinculo);

        return converterParaDTO(livro);
    }
    
    @Transactional(readOnly = true)
    public LivroDTO buscarPorId(Long id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro", id));
        return modelMapper.map(livro, LivroDTO.class);
    }
    
    @Transactional(readOnly = true)
    public LivroDTO buscarPorIsbn(String isbn) {
        Livro livro = livroRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Livro", "ISBN", isbn));
        return modelMapper.map(livro, LivroDTO.class);
    }
    
    @Transactional(readOnly = true)
    public List<LivroDTO> listarTodos() {
        return livroRepository.findAll().stream()
                .map(livro -> modelMapper.map(livro, LivroDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LivroDTO> buscarPorTitulo(String titulo) {
        return livroRepository.findByTituloContaining(titulo).stream()
                .map(livro -> modelMapper.map(livro, LivroDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LivroDTO> buscarPorAutor(String autor) {
        return livroRepository.findByAutorContaining(autor).stream()
                .map(livro -> modelMapper.map(livro, LivroDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LivroDTO> buscarPorGenero(String genero) {
        return livroRepository.findByGenero(genero).stream()
                .map(livro -> modelMapper.map(livro, LivroDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LivroDTO> buscarPorEditora(String editora) {
        return livroRepository.findByEditora(editora).stream()
                .map(livro -> modelMapper.map(livro, LivroDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LivroDTO> buscarPorAno(Integer ano) {
        return livroRepository.findByAno(ano).stream()
                .map(livro -> modelMapper.map(livro, LivroDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LivroDTO> buscarPorCondicao(Livro.CondicaoLivro condicao) {
        return livroRepository.findByCondicao(condicao).stream()
                .map(livro -> modelMapper.map(livro, LivroDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LivroDTO> buscarPorTermo(String termo) {
        return livroRepository.findByTituloContainingOrAutorContainingOrGeneroContaining(termo).stream()
                .map(livro -> modelMapper.map(livro, LivroDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LivroDTO> buscarPorAnoEntre(Integer anoInicio, Integer anoFim) {
        return livroRepository.findByAnoBetween(anoInicio, anoFim).stream()
                .map(livro -> modelMapper.map(livro, LivroDTO.class))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<String> listarGeneros() {
        return livroRepository.findDistinctGeneros();
    }
    
    @Transactional(readOnly = true)
    public List<String> listarEditoras() {
        return livroRepository.findDistinctEditoras();
    }
    
    public LivroDTO atualizarLivro(Long id, LivroDTO livroDTO) {
        Livro livroExistente = livroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro", id));
        
        // Verificar se ISBN já existe em outro livro
        if (livroDTO.getIsbn() != null && !livroDTO.getIsbn().isEmpty() && 
            !livroExistente.getIsbn().equals(livroDTO.getIsbn()) && 
            livroRepository.existsByIsbn(livroDTO.getIsbn())) {
            throw new BusinessException("ISBN já cadastrado");
        }
        
        modelMapper.map(livroDTO, livroExistente);
        livroExistente = livroRepository.save(livroExistente);
        
        return modelMapper.map(livroExistente, LivroDTO.class);
    }
    
    public void deletarLivro(Long id) {
        if (!livroRepository.existsById(id)) {
            throw new ResourceNotFoundException("Livro", id);
        }
        livroRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<LivroDTO> listarMaisRecentesPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Livro> livrosPage = livroRepository.findAllByOrderByCreatedAtDesc(pageable);
        return livrosPage.map(livro -> modelMapper.map(livro, LivroDTO.class));
    }
}
