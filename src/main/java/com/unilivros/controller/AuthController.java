package com.unilivros.controller;

import com.unilivros.dto.AuthResponseDTO;
import com.unilivros.dto.LoginDTO;
import com.unilivros.dto.UsuarioDTO;
import com.unilivros.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        AuthResponseDTO response = usuarioService.register(usuarioDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResponseDTO response = usuarioService.login(loginDTO);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        UsuarioDTO usuario = usuarioService.buscarPorEmail(email);
        usuario.setSenha(null);
        
        return ResponseEntity.ok(usuario);
    }
}


