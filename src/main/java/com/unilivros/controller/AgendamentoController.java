package com.unilivros.controller;

import com.unilivros.dto.AgendamentoDTO;
import com.unilivros.model.Agendamento;
import com.unilivros.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@CrossOrigin(origins = "*")
public class AgendamentoController {
    
    @Autowired
    private AgendamentoService agendamentoService;
    
    @PostMapping
    public ResponseEntity<AgendamentoDTO> criarAgendamento(@Valid @RequestBody AgendamentoDTO agendamentoDTO) {
        AgendamentoDTO agendamentoCriado = agendamentoService.criarAgendamento(agendamentoDTO);
        return new ResponseEntity<>(agendamentoCriado, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoDTO> buscarPorId(@PathVariable Long id) {
        AgendamentoDTO agendamento = agendamentoService.buscarPorId(id);
        return ResponseEntity.ok(agendamento);
    }
    
    @GetMapping
    public ResponseEntity<Page<AgendamentoDTO>> listar(Pageable pageable) {
        Page<AgendamentoDTO> agendamentos = agendamentoService.listar(pageable);
        return ResponseEntity.ok(agendamentos);
    }
    
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<AgendamentoDTO>> buscarPorUsuario(@PathVariable Long usuarioId) {
        List<AgendamentoDTO> agendamentos = agendamentoService.buscarPorUsuario(usuarioId);
        return ResponseEntity.ok(agendamentos);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AgendamentoDTO>> buscarPorStatus(@PathVariable Agendamento.StatusAgendamento status) {
        List<AgendamentoDTO> agendamentos = agendamentoService.buscarPorStatus(status);
        return ResponseEntity.ok(agendamentos);
    }
    
    @GetMapping("/usuario/{usuarioId}/status/{status}")
    public ResponseEntity<List<AgendamentoDTO>> buscarPorUsuarioEStatus(
            @PathVariable Long usuarioId, 
            @PathVariable Agendamento.StatusAgendamento status) {
        List<AgendamentoDTO> agendamentos = agendamentoService.buscarPorUsuarioEStatus(usuarioId, status);
        return ResponseEntity.ok(agendamentos);
    }
    
    @GetMapping("/periodo")
    public ResponseEntity<List<AgendamentoDTO>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<AgendamentoDTO> agendamentos = agendamentoService.buscarPorPeriodo(inicio, fim);
        return ResponseEntity.ok(agendamentos);
    }
//
//    @GetMapping("/usuario/{usuarioId}/periodo")
//    public ResponseEntity<List<AgendamentoDTO>> buscarPorUsuarioEPeriodo(
//            @PathVariable Long usuarioId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
//        List<AgendamentoDTO> agendamentos = agendamentoService.buscarPorUsuarioEPeriodo(usuarioId, inicio, fim);
//        return ResponseEntity.ok(agendamentos);
//    }
//
//    @GetMapping("/futuros/status/{status}")
//    public ResponseEntity<List<AgendamentoDTO>> buscarFuturosPorStatus(@PathVariable Agendamento.StatusAgendamento status) {
//        List<AgendamentoDTO> agendamentos = agendamentoService.buscarFuturosPorStatus(status);
//        return ResponseEntity.ok(agendamentos);
//    }
    
    @GetMapping("/passados/status/{status}")
    public ResponseEntity<List<AgendamentoDTO>> buscarPassadosPorStatus(@PathVariable Agendamento.StatusAgendamento status) {
        List<AgendamentoDTO> agendamentos = agendamentoService.buscarPassadosPorStatus(status);
        return ResponseEntity.ok(agendamentos);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AgendamentoDTO> atualizarAgendamento(
            @PathVariable Long id, 
            @Valid @RequestBody AgendamentoDTO agendamentoDTO) {
        AgendamentoDTO agendamentoAtualizado = agendamentoService.atualizarAgendamento(id, agendamentoDTO);
        return ResponseEntity.ok(agendamentoAtualizado);
    }
    
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<AgendamentoDTO> confirmarAgendamento(@PathVariable Long id) {
        AgendamentoDTO agendamento = agendamentoService.confirmarAgendamento(id);
        return ResponseEntity.ok(agendamento);
    }
    
    @PostMapping("/{id}/realizado")
    public ResponseEntity<AgendamentoDTO> marcarComoRealizado(@PathVariable Long id) {
        AgendamentoDTO agendamento = agendamentoService.marcarComoRealizado(id);
        return ResponseEntity.ok(agendamento);
    }
    
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<AgendamentoDTO> cancelarAgendamento(@PathVariable Long id) {
        AgendamentoDTO agendamento = agendamentoService.cancelarAgendamento(id);
        return ResponseEntity.ok(agendamento);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAgendamento(@PathVariable Long id) {
        agendamentoService.deletarAgendamento(id);
        return ResponseEntity.noContent().build();
    }
}
