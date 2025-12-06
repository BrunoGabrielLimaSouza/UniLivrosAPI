package com.unilivros.service;

import com.unilivros.dto.LivroDTO;
import com.unilivros.dto.UsuarioDTO;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    public List<UsuarioDTO> buscarDonosDosLivros(String isbn) {

        // Buscar a entidade Livro pelo ISBN
        Livro livro = livroRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Livro com ISBN não encontrado"));

        // Buscar relações UsuarioLivro pela entidade Livro
        List<UsuarioLivro> donos = usuarioLivroRepository.findByLivro(livro);

        return donos.stream()
                .map(relacao -> new UsuarioDTO(relacao.getUsuario()))
                .collect(Collectors.toList());
    }

    private LivroDTO converterParaDTO(Livro livro) {
        return modelMapper.map(livro, LivroDTO.class);
    }

    private UsuarioDTO converterParaDTO(Usuario  usuario) {
        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    public LivroDTO criarLivro(LivroDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Livro livro = new Livro();
        livro.setTitulo(dto.getTitulo());
        livro.setAutor(dto.getAutor());
        livro.setAno(dto.getAno());
        livro.setCondicao(dto.getCondicao());
        livro.setDescricao(dto.getDescricao());
        livro.setIsbn(dto.getIsbn());

        livro.setEditora((dto.getEditora() != null && !dto.getEditora().isEmpty()) ? dto.getEditora() : "Não informada");
        livro.setGenero((dto.getGenero() != null && !dto.getGenero().isEmpty()) ? dto.getGenero() : "Geral");

        livro = livroRepository.save(livro);

        UsuarioLivro vinculo = new UsuarioLivro();
        vinculo.setUsuario(usuario);
        vinculo.setLivro(livro);
        vinculo.setDisponivelParaTroca(true); // Já entra disponível na estante

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

    /**
     * Busca todos os usuários que possuem livros com o mesmo googleId
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarUsuariosPorGoogleId(String googleId) {
        // Busca todos os livros com esse googleId
        List<Livro> livros = livroRepository.findByGoogleId(googleId);

        if (livros.isEmpty()) {
            return Collections.emptyList();
        }

        // Busca todos os usuários que possuem algum desses livros (sem duplicatas)
        Set<Usuario> usuariosSet = new HashSet<>();
        for (Livro livro : livros) {
            List<UsuarioLivro> relacoes = usuarioLivroRepository. findByLivro(livro);
            relacoes.forEach(rel -> {
                if (rel.getDisponivelParaTroca()) {
                    usuariosSet.add(rel.getUsuario());
                }
            });
        }

        // Converte para DTO com informações adicionais
        return usuariosSet.stream()
                .map(this::converterParaUsuarioDTO)
                . collect(Collectors.toList());
    }

    /**
     * Busca todos os usuários que possuem um livro específico do backend
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarUsuariosPorLivroId(Long livroId) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new ResourceNotFoundException("Livro", livroId));

        // Busca todas as relações usuário-livro para este livro
        List<UsuarioLivro> relacoes = usuarioLivroRepository. findByLivro(livro);

        // Pega apenas usuários que têm o livro disponível para troca
        return relacoes. stream()
                .filter(rel -> rel.getDisponivelParaTroca())
                .map(rel -> {
                    Usuario usuario = rel.getUsuario();
                    UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);

                    // Garante que os campos extras estão preenchidos
                    dto.setAvaliacao(usuario.getAvaliacao());
                    dto.setTotalTrocas(usuario.getTotalTrocas());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Converte Usuario para DTO com informações extras necessárias para o frontend
     */
    private UsuarioDTO converterParaUsuarioDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario. getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario. getEmail());
        dto.setAvaliacao(usuario.getAvaliacao());
        dto.setTotalTrocas(usuario. getTotalTrocas());
        dto.setCurso(usuario.getCurso());
        dto.setSemestre(usuario.getSemestre());
        return dto;
    }
}