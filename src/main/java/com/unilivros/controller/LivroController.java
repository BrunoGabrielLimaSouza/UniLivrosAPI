package com.unilivros.controller;

import com.unilivros.dto.LivroDTO;
import com.unilivros.dto.UsuarioDTO;
import com.unilivros.model.Livro;
import com.unilivros.service.LivroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livros")
@CrossOrigin(origins = "*")
public class LivroController {
    
    @Autowired
    private LivroService livroService;

    
    @PostMapping
    public ResponseEntity<LivroDTO> criarLivro(@Valid @RequestBody LivroDTO livroDTO) {
        LivroDTO livroCriado = livroService.criarLivro(livroDTO);
        return new ResponseEntity<>(livroCriado, HttpStatus.CREATED);
    }

    @GetMapping("/meus-livros")
    public ResponseEntity<List<LivroDTO>> listarMeusLivros() {
        // 1. Pega o e-mail do usuário autenticado (via Token JWT)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();

        // 2. Chama o serviço para buscar os livros desse usuário
        List<LivroDTO> livros = livroService.buscarLivrosDoUsuario(emailUsuario);

        return ResponseEntity.ok(livros);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivroDTO> buscarPorId(@PathVariable Long id) {
        LivroDTO livro = livroService.buscarPorId(id);
        return ResponseEntity.ok(livro);
    }
    
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<LivroDTO> buscarPorIsbn(@PathVariable String isbn) {
        LivroDTO livro = livroService.buscarPorIsbn(isbn);
        return ResponseEntity.ok(livro);
    }
    
    @GetMapping
    public ResponseEntity<List<LivroDTO>> listarTodos() {
        List<LivroDTO> livros = livroService.listarTodos();
        return ResponseEntity.ok(livros);
    }
    
    @GetMapping("/titulo")
    public ResponseEntity<List<LivroDTO>> buscarPorTitulo(@RequestParam String titulo) {
        List<LivroDTO> livros = livroService.buscarPorTitulo(titulo);
        return ResponseEntity.ok(livros);
    }
    
    @GetMapping("/autor")
    public ResponseEntity<List<LivroDTO>> buscarPorAutor(@RequestParam String autor) {
        List<LivroDTO> livros = livroService.buscarPorAutor(autor);
        return ResponseEntity.ok(livros);
    }
    
    @GetMapping("/genero")
    public ResponseEntity<List<LivroDTO>> buscarPorGenero(@RequestParam String genero) {
        List<LivroDTO> livros = livroService.buscarPorGenero(genero);
        return ResponseEntity.ok(livros);
    }
    
    @GetMapping("/editora")
    public ResponseEntity<List<LivroDTO>> buscarPorEditora(@RequestParam String editora) {
        List<LivroDTO> livros = livroService.buscarPorEditora(editora);
        return ResponseEntity.ok(livros);
    }
    
    @GetMapping("/ano")
    public ResponseEntity<List<LivroDTO>> buscarPorAno(@RequestParam Integer ano) {
        List<LivroDTO> livros = livroService.buscarPorAno(ano);
        return ResponseEntity.ok(livros);
    }
    
    @GetMapping("/condicao")
    public ResponseEntity<List<LivroDTO>> buscarPorCondicao(@RequestParam Livro.CondicaoLivro condicao) {
        List<LivroDTO> livros = livroService.buscarPorCondicao(condicao);
        return ResponseEntity.ok(livros);
    }
    
    @GetMapping("/buscar")
    public ResponseEntity<List<LivroDTO>> buscarPorTermo(@RequestParam String termo) {
        List<LivroDTO> livros = livroService.buscarPorTermo(termo);
        return ResponseEntity.ok(livros);
    }
    
    @GetMapping("/ano-entre")
    public ResponseEntity<List<LivroDTO>> buscarPorAnoEntre(
            @RequestParam Integer anoInicio, 
            @RequestParam Integer anoFim) {
        List<LivroDTO> livros = livroService.buscarPorAnoEntre(anoInicio, anoFim);
        return ResponseEntity.ok(livros);
    }
    
    @GetMapping("/generos")
    public ResponseEntity<List<String>> listarGeneros() {
        List<String> generos = livroService.listarGeneros();
        return ResponseEntity.ok(generos);
    }
    
    @GetMapping("/editoras")
    public ResponseEntity<List<String>> listarEditoras() {
        List<String> editoras = livroService.listarEditoras();
        return ResponseEntity.ok(editoras);
    }
    @GetMapping("/recentes")
    public ResponseEntity<Page<LivroDTO>> listarMaisRecentesPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<LivroDTO> livrosRecentes = livroService.listarMaisRecentesPaginado(page, size);
        return ResponseEntity.ok(livrosRecentes);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<LivroDTO> atualizarLivro(
            @PathVariable Long id, 
            @Valid @RequestBody LivroDTO livroDTO) {
        LivroDTO livroAtualizado = livroService.atualizarLivro(id, livroDTO);
        return ResponseEntity.ok(livroAtualizado);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLivro(@PathVariable Long id) {
        livroService.deletarLivro(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/isbn/{isbn}/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarDonosDosLivros(@PathVariable String isbn){
        List<UsuarioDTO> usuarios = livroService.buscarDonosDosLivros(isbn);

        return ResponseEntity.ok(usuarios);
    }
}
