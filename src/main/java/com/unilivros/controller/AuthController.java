package com.unilivros.controller;

import com.unilivros.dto.AuthResponseDTO;
import com.unilivros.dto.LoginDTO;
import com.unilivros.dto.UsuarioDTO;
import com.unilivros.model.Usuario;
import com.unilivros.security.JwtTokenProvider;
import com.unilivros.service.UsuarioService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.unilivros.dto.*;
import com.unilivros.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody VerificationDTO dto) {
        Usuario usuario = usuarioRepository.findByVerificationCode(dto.getCode())
                .orElseThrow(() -> new RuntimeException("Código inválido"));

        usuario.setEnabled(true);
        usuario.setVerificationCode(null);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate-reset-code")
    public ResponseEntity<Void> validateResetCode(@RequestBody VerificationDTO dto) {
        // Apenas verifica se o código existe e bate com o email (lógica simplificada)
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email não encontrado"));

        if (!dto.getCode().equals(usuario.getVerificationCode())) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody VerificationDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email não encontrado"));

        if (!dto.getCode().equals(usuario.getVerificationCode())) {
            throw new RuntimeException("Código inválido");
        }

        usuario.setSenha(passwordEncoder.encode(dto.getNewPassword()));
        usuario.setVerificationCode(null);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok().build();
    }

    
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        UsuarioDTO usuarioCriado = usuarioService.criarUsuario(usuarioDTO);
        
        String token = tokenProvider.generateToken(usuarioCriado.getId());
        
        AuthResponseDTO response = new AuthResponseDTO(token, usuarioCriado);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        Usuario usuario = usuarioService.authenticateUser(loginDTO.getEmail(), loginDTO.getSenha());
        
        String token = tokenProvider.generateToken(usuario.getId());
        
        UsuarioDTO usuarioDTO = modelMapper.map(usuario, UsuarioDTO.class);
        usuarioDTO.setSenha(null);
        
        AuthResponseDTO response = new AuthResponseDTO(token, usuarioDTO);
        
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


