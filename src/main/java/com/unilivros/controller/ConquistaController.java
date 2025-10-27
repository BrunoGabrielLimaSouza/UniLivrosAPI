package com.unilivros.controller;

import com.unilivros.dto.ConquistaDTO;
import com.unilivros.model.Conquista;
import com.unilivros.service.ConquistaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conquistas")
@CrossOrigin(origins = "*")
public class ConquistaController {
    
    @Autowired
    private ConquistaService conquistaService;
    
    @PostMapping
    public ResponseEntity<ConquistaDTO> criarConquista(@Valid @RequestBody ConquistaDTO conquistaDTO) {
        ConquistaDTO conquistaCriada = conquistaService.criarConquista(conquistaDTO);
        return new ResponseEntity<>(conquistaCriada, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ConquistaDTO> buscarPorId(@PathVariable Long id) {
        ConquistaDTO conquista = conquistaService.buscarPorId(id);
        return ResponseEntity.ok(conquista);
    }
    
    @GetMapping
    public ResponseEntity<List<ConquistaDTO>> listarTodas() {
        List<ConquistaDTO> conquistas = conquistaService.listarTodas();
        return ResponseEntity.ok(conquistas);
    }
    
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ConquistaDTO>> buscarPorTipo(@PathVariable Conquista.TipoConquista tipo) {
        List<ConquistaDTO> conquistas = conquistaService.buscarPorTipo(tipo);
        return ResponseEntity.ok(conquistas);
    }
    
    @GetMapping("/disponiveis/{xp}")
    public ResponseEntity<List<ConquistaDTO>> buscarDisponiveisPorXp(@PathVariable Integer xp) {
        List<ConquistaDTO> conquistas = conquistaService.buscarDisponiveisPorXp(xp);
        return ResponseEntity.ok(conquistas);
    }
    
    @GetMapping("/nao-disponiveis/{xp}")
    public ResponseEntity<List<ConquistaDTO>> buscarNaoDisponiveisPorXp(@PathVariable Integer xp) {
        List<ConquistaDTO> conquistas = conquistaService.buscarNaoDisponiveisPorXp(xp);
        return ResponseEntity.ok(conquistas);
    }
    
    @GetMapping("/nome")
    public ResponseEntity<List<ConquistaDTO>> buscarPorNome(@RequestParam String nome) {
        List<ConquistaDTO> conquistas = conquistaService.buscarPorNome(nome);
        return ResponseEntity.ok(conquistas);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ConquistaDTO> atualizarConquista(
            @PathVariable Long id, 
            @Valid @RequestBody ConquistaDTO conquistaDTO) {
        ConquistaDTO conquistaAtualizada = conquistaService.atualizarConquista(id, conquistaDTO);
        return ResponseEntity.ok(conquistaAtualizada);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarConquista(@PathVariable Long id) {
        conquistaService.deletarConquista(id);
        return ResponseEntity.noContent().build();
    }
}
