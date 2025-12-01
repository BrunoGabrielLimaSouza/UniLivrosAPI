package com.unilivros.controller;

import com.unilivros.dto.*;
import com.unilivros.exception.BusinessException; // <--- NOVO IMPORT
import com.unilivros.exception.ResourceNotFoundException; // <--- NOVO IMPORT
import com.unilivros.model.Usuario;
import com.unilivros.repository.UsuarioRepository;
import com.unilivros.security.JwtTokenProvider;
import com.unilivros.service.EmailService;
import com.unilivros.service.UsuarioService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.Optional;

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

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody UsuarioDTO data) {
        if (this.usuarioRepository.findByEmail(data.getEmail()).isPresent())
            throw new BusinessException("Email já cadastrado.");

        if (this.usuarioRepository.existsByMatricula(data.getMatricula()))
            throw new BusinessException("Matrícula já cadastrada.");

        String encryptedPassword = passwordEncoder.encode(data.getSenha());

        String codigo = String.format("%06d", new Random().nextInt(999999));

        Usuario newUser = new Usuario();
        newUser.setNome(data.getNome());
        newUser.setEmail(data.getEmail());
        newUser.setMatricula(data.getMatricula());
        newUser.setCurso(data.getCurso());
        newUser.setSemestre(data.getSemestre() != null ? String.valueOf(data.getSemestre()) : null);
        newUser.setSenha(encryptedPassword);

        newUser.setVerificationCode(codigo);
        newUser.setEnabled(false);

        this.usuarioRepository.save(newUser);

        emailService.enviarCodigoConfirmacao(newUser.getEmail(), codigo);

        return ResponseEntity.ok().build();
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

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody VerificationDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "Email", dto.getEmail()));

        String codigo = String.format("%06d", new Random().nextInt(999999));

        usuario.setVerificationCode(codigo);
        usuarioRepository.save(usuario);

        emailService.enviarCodigoConfirmacao(usuario.getEmail(), codigo);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody VerificationDTO dto) {
        Usuario usuario = usuarioRepository.findByVerificationCode(dto.getCode())
                .orElseThrow(() -> new BusinessException("Código de verificação inválido."));

        usuario.setEnabled(true);
        usuario.setVerificationCode(null);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate-reset-code")
    public ResponseEntity<Void> validateResetCode(@RequestBody VerificationDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "Email", dto.getEmail()));

        if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(dto.getCode())) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody VerificationDTO dto) {
        // Usa ResourceNotFoundException para email
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "Email", dto.getEmail()));

        if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(dto.getCode())) {
            throw new BusinessException("Código de recuperação inválido.");
        }

        usuario.setSenha(passwordEncoder.encode(dto.getNewPassword()));
        usuario.setVerificationCode(null);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        UsuarioDTO usuario = usuarioService.buscarPorEmail(email);
        usuario.setSenha(null);

        return ResponseEntity.ok(usuario);
    }
}