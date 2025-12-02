package com.unilivros.controller;

import com.unilivros.dto.*;
import com.unilivros.exception.BusinessException;
import com.unilivros.exception.ResourceNotFoundException;
import com.unilivros.model.Usuario;
import com.unilivros.repository.UsuarioRepository;
import com.unilivros.security.JwtTokenProvider;
import com.unilivros.service.EmailService;
import com.unilivros.service.UsuarioService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

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

    // --- CADASTRO COM ENVIO DE E-MAIL (COM DIAGNÓSTICO DE ERRO) ---
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO> register(@Valid @RequestBody UsuarioDTO data) {
        try {
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
            // Garante que o semestre é convertido para String de forma segura.
            newUser.setSemestre(data.getSemestre() != null ? String.valueOf(data.getSemestre()) : null);
            newUser.setSenha(encryptedPassword);

            newUser.setVerificationCode(codigo);
            newUser.setEnabled(false);

            // Passo 1: Tenta salvar o usuário
            logger.info("Salvando usuário no banco de dados: {}", data.getEmail());
            this.usuarioRepository.save(newUser);

            // Passo 2: Tenta enviar o e-mail
            logger.info("Enviando email de confirmação para: {}", data.getEmail());
            emailService.enviarCodigoConfirmacao(newUser.getEmail(), codigo);

            return ResponseEntity.ok(new ApiResponseDTO(true, "Cadastro realizado com sucesso! Verifique seu e-mail para confirmar."));

        } catch (BusinessException e) {
            logger.warn("Erro de negócio no cadastro: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("ERRO GRAVE NO CADASTRO: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Erro interno no servidor. Tente novamente mais tarde."));
        }
    }

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        Usuario usuario = usuarioService.authenticateUser(loginDTO.getEmail(), loginDTO.getSenha());

        String token = tokenProvider.generateToken(usuario.getId());

        UsuarioDTO usuarioDTO = modelMapper.map(usuario, UsuarioDTO.class);
        usuarioDTO.setSenha(null);

        AuthResponseDTO response = new AuthResponseDTO(token, usuarioDTO);

        return ResponseEntity.ok(response);
    }

    // --- ESQUECI A SENHA (Solicitar Código) ---
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDTO> forgotPassword(@RequestBody VerificationDTO dto) {
        try {
            logger.info("Solicitando código de recuperação para: {}", dto.getEmail());
            logger.info("Usando serviço de email: SendGrid");

            Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", "Email", dto.getEmail()));

            String codigo = String.format("%06d", new Random().nextInt(999999));

            usuario.setVerificationCode(codigo);
            usuarioRepository.save(usuario);

            logger.info("Enviando email via SendGrid para: {}", dto.getEmail());
            emailService.enviarCodigoConfirmacao(usuario.getEmail(), codigo);

            return ResponseEntity.ok(new ApiResponseDTO(true, "Código de recuperação enviado para seu e-mail."));

        } catch (ResourceNotFoundException e) {
            logger.warn("Usuário não encontrado: {}", dto.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO(false, "E-mail não cadastrado no sistema."));
        } catch (Exception e) {
            logger.error("Erro ao processar recuperação de senha: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Erro ao enviar código de recuperação. Tente novamente."));
        }
    }

    // --- VERIFICAR CÓDIGO DE CADASTRO ---
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponseDTO> verifyEmail(@RequestBody VerificationDTO dto) {
        try {
            logger.info("Verificando código: {}", dto.getCode());

            Usuario usuario = usuarioRepository.findByVerificationCode(dto.getCode())
                    .orElseThrow(() -> new BusinessException("Código de verificação inválido."));

            usuario.setEnabled(true);
            usuario.setVerificationCode(null);
            usuarioRepository.save(usuario);

            logger.info("Email verificado com sucesso para usuário: {}", usuario.getEmail());
            return ResponseEntity.ok(new ApiResponseDTO(true, "Email verificado com sucesso!"));

        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao verificar email: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Erro ao verificar código."));
        }
    }

    @PostMapping("/test-email")
    public ResponseEntity<ApiResponseDTO> testEmail() {
        try {
            logger.info("Testando serviço de email SendGrid...");

            // Testa conexão
            emailService.testarConexaoSendGrid();

            // Envia email de teste
            String codigoTeste = "123456";
            emailService.enviarCodigoConfirmacao("teste@souunit.com.br", codigoTeste);

            return ResponseEntity.ok(new ApiResponseDTO(true, "Serviço de email testado com sucesso!"));

        } catch (Exception e) {
            logger.error("Falha no teste de email: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Falha no teste de email: " + e.getMessage()));
        }
    }

    // --- VALIDAR CÓDIGO DE RECUPERAÇÃO ---
    @PostMapping("/validate-reset-code")
    public ResponseEntity<ApiResponseDTO> validateResetCode(@RequestBody VerificationDTO dto) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", "Email", dto.getEmail()));

            if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(dto.getCode())) {
                return ResponseEntity.badRequest().body(new ApiResponseDTO(false, "Código inválido ou expirado."));
            }

            return ResponseEntity.ok(new ApiResponseDTO(true, "Código válido."));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO(false, "E-mail não encontrado."));
        } catch (Exception e) {
            logger.error("Erro ao validar código: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Erro ao validar código."));
        }
    }

    // --- RESETAR SENHA ---
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDTO> resetPassword(@RequestBody VerificationDTO dto) {
        try {
            logger.info("Resetando senha para: {}", dto.getEmail());

            Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", "Email", dto.getEmail()));

            if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(dto.getCode())) {
                throw new BusinessException("Código de recuperação inválido ou expirado.");
            }

            if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
                throw new BusinessException("Nova senha deve ter pelo menos 6 caracteres.");
            }

            usuario.setSenha(passwordEncoder.encode(dto.getNewPassword()));
            usuario.setVerificationCode(null);
            usuarioRepository.save(usuario);

            logger.info("Senha resetada com sucesso para: {}", dto.getEmail());
            return ResponseEntity.ok(new ApiResponseDTO(true, "Senha redefinida com sucesso!"));

        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO(false, "E-mail não encontrado."));
        } catch (Exception e) {
            logger.error("Erro ao resetar senha: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Erro ao redefinir senha."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        UsuarioDTO usuario = usuarioService.buscarPorEmail(email);
        usuario.setSenha(null);

        return ResponseEntity.ok(usuario);
    }

    // DTO auxiliar para respostas de API
    public static class ApiResponseDTO {
        private boolean success;
        private String message;

        public ApiResponseDTO() {}

        public ApiResponseDTO(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}