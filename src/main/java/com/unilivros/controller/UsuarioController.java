package com.unilivros.controller;

import com.unilivros.dto.UsuarioDTO;
import com.unilivros.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @PostMapping
    public ResponseEntity<UsuarioDTO> criarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO usuarioCriado = usuarioService.criarUsuario(usuarioDTO);
        return new ResponseEntity<>(usuarioCriado, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable Long id) {
        UsuarioDTO usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioDTO> buscarPorEmail(@PathVariable String email) {
        UsuarioDTO usuario = usuarioService.buscarPorEmail(email);
        return ResponseEntity.ok(usuario);
    }
    
    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<UsuarioDTO> buscarPorMatricula(@PathVariable String matricula) {
        UsuarioDTO usuario = usuarioService.buscarPorMatricula(matricula);
        return ResponseEntity.ok(usuario);
    }
    
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        List<UsuarioDTO> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/curso/{curso}")
    public ResponseEntity<List<UsuarioDTO>> buscarPorCurso(@PathVariable String curso) {
        List<UsuarioDTO> usuarios = usuarioService.buscarPorCurso(curso);
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/semestre/{semestre}")
    public ResponseEntity<List<UsuarioDTO>> buscarPorSemestre(@PathVariable Integer semestre) {
        List<UsuarioDTO> usuarios = usuarioService.buscarPorSemestre(semestre);
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/curso/{curso}/semestre/{semestre}")
    public ResponseEntity<List<UsuarioDTO>> buscarPorCursoESemestre(
            @PathVariable String curso, 
            @PathVariable Integer semestre) {
        List<UsuarioDTO> usuarios = usuarioService.buscarPorCursoESemestre(curso, semestre);
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/xp-minimo/{xpMinimo}")
    public ResponseEntity<List<UsuarioDTO>> buscarPorXpMinimo(@PathVariable Integer xpMinimo) {
        List<UsuarioDTO> usuarios = usuarioService.buscarPorXpMinimo(xpMinimo);
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/avaliacao-minima/{avaliacaoMinima}")
    public ResponseEntity<List<UsuarioDTO>> buscarPorAvaliacaoMinima(@PathVariable Double avaliacaoMinima) {
        List<UsuarioDTO> usuarios = usuarioService.buscarPorAvaliacaoMinima(avaliacaoMinima);
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioDTO>> buscarPorNomeOuEmail(@RequestParam String termo) {
        List<UsuarioDTO> usuarios = usuarioService.buscarPorNomeOuEmail(termo);
        return ResponseEntity.ok(usuarios);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizarUsuario(
            @PathVariable Long id, 
            @Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO usuarioAtualizado = usuarioService.atualizarUsuario(id, usuarioDTO);
        return ResponseEntity.ok(usuarioAtualizado);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/adicionar-xp")
    public ResponseEntity<Void> adicionarXp(@PathVariable Long id, @RequestParam Integer xp) {
        usuarioService.adicionarXp(id, xp);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/atualizar-avaliacao")
    public ResponseEntity<Void> atualizarAvaliacao(
            @PathVariable Long id, 
            @RequestParam Double novaAvaliacao) {
        usuarioService.atualizarAvaliacao(id, novaAvaliacao);
        return ResponseEntity.ok().build();
    }
}
