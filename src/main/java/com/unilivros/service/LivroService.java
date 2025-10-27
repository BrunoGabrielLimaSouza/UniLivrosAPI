package com.unilivros.service;

import com.unilivros.dto.LivroDTO;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.Livro;
import com.unilivros.repository.LivroRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LivroService {
    
    @Autowired
    private LivroRepository livroRepository;
    
    @Autowired
    private ModelMapper modelMapper;
    
    public LivroDTO criarLivro(LivroDTO livroDTO) {
        // Verificar se ISBN já existe (se fornecido)
        if (livroDTO.getIsbn() != null && !livroDTO.getIsbn().isEmpty() && 
            livroRepository.existsByIsbn(livroDTO.getIsbn())) {
            throw new BusinessException("ISBN já cadastrado");
        }
        
        Livro livro = modelMapper.map(livroDTO, Livro.class);
        livro = livroRepository.save(livro);
        
        return modelMapper.map(livro, LivroDTO.class);
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
}
