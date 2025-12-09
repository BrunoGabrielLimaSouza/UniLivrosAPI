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

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    public static final Logger logger = LoggerFactory.getLogger(AuthController.class);

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

    // --- CADASTRO COM ENVIO DE E-MAIL ---
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
            newUser.setSemestre(data.getSemestre());
            newUser.setSenha(encryptedPassword);

            newUser.setVerificationCode(codigo);
            newUser.setEnabled(false);

            this.usuarioRepository.save(newUser);

            logger.info("Tentando enviar email de confirmação para: {}", data.getEmail());
            emailService.enviarCodigoConfirmacao(newUser.getEmail(), codigo);

            return ResponseEntity.ok(new ApiResponseDTO(true,
                    "Cadastro realizado com sucesso! " +
                            "Verifique seu e-mail para o código de confirmação. " +
                            "Se não receber, verifique a pasta de spam."));

        } catch (BusinessException e) {
            logger.warn("Erro de negócio no cadastro: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("ERRO NO CADASTRO: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false,
                            "Erro interno no servidor. O usuário foi criado, " +
                                    "mas não foi possível enviar o email de confirmação. " +
                                    "Entre em contato com o suporte."));
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

            Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", "Email", dto.getEmail()));

            String codigo = String.format("%06d", new Random().nextInt(999999));

            usuario.setVerificationCode(codigo);
            usuarioRepository.save(usuario);

            logger.info("Gerado código: {} para email: {}", codigo, dto.getEmail());
            emailService.enviarCodigoConfirmacao(usuario.getEmail(), codigo);

            String mensagem = "Código de recuperação processado. " +
                    "Verifique seu e-mail. " +
                    "Se não receber em alguns minutos, verifique a pasta de spam " +
                    "ou entre em contato com o suporte.";

            return ResponseEntity.ok(new ApiResponseDTO(true, mensagem));

        } catch (ResourceNotFoundException e) {
            logger.warn("Usuário não encontrado: {}", dto.getEmail());
            // Por segurança, não revelamos se o email existe ou não
            return ResponseEntity.ok(new ApiResponseDTO(true,
                    "Se o email estiver cadastrado, você receberá um código de recuperação em alguns minutos."));
        } catch (Exception e) {
            logger.error("Erro ao processar recuperação de senha: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false,
                            "Erro ao processar solicitação. " +
                                    "O código foi gerado, mas pode haver problemas no envio do email. " +
                                    "Tente novamente em alguns minutos ou entre em contato com o suporte."));
        }
    }

    // --- VERIFICAR CÓDIGO DE CADASTRO ---
    @PostMapping("/verify-email/{codigoVerificacao}")
    public ResponseEntity<ApiResponseDTO> verifyEmail(@PathVariable String codigoVerificacao) {
        try {
            logger.info("Verificando código: {}", codigoVerificacao);

            Usuario usuario = usuarioRepository.findByVerificationCode(codigoVerificacao)
                    .orElseThrow(() -> new BusinessException("Código de verificação inválido."));

            usuario.setEnabled(true);
            usuario.setVerificationCode(null);
            usuarioRepository.save(usuario);

            logger.info("Email verificado com sucesso para usuário: {}", usuario.getEmail());
            return ResponseEntity.ok(new ApiResponseDTO(true, "Email verificado com sucesso! Você já pode fazer login."));

        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        } catch (Exception e) {
            // Log para o servidor (manter a stack trace completa)
            logger.error("Erro ao verificar email: ", e);

            // Retorna o tipo de erro para o frontend (sem detalhes sensíveis)
            String erroDetalhado = "Erro ao verificar código. Detalhe: " + e.getClass().getSimpleName();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, erroDetalhado));
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
            return ResponseEntity.ok(new ApiResponseDTO(true, "Senha redefinida com sucesso! Faça login com sua nova senha."));

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

    // --- ENDPOINTS DE DIAGNÓSTICO ---

    @GetMapping("/email-config")
    public ResponseEntity<ApiResponseDTO> getEmailConfig() {
        try {
            String config = emailService.getConfiguracaoAtual();
            return ResponseEntity.ok(new ApiResponseDTO(true, config));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false, "Erro ao obter configuração: " + e.getMessage()));
        }
    }

    @PostMapping("/test-email")
    public ResponseEntity<ApiResponseDTO> testEmail(@RequestBody VerificationDTO dto) {
        try {
            logger.info("Testando serviço de email...");
            emailService.testarConexaoEmail();

            // Envia email de teste
            String codigoTeste = "123456";
            String emailTeste = dto.getEmail() != null ? dto.getEmail() : "teste@souunit.com.br";

            emailService.enviarCodigoConfirmacao(emailTeste, codigoTeste);

            return ResponseEntity.ok(new ApiResponseDTO(true,
                    "Serviço de email testado com sucesso! " +
                            "Verifique o email: " + emailTeste));

        } catch (Exception e) {
            logger.error("Falha no teste de email: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO(false,
                            "Falha no teste de email. Sistema usando fallback. " +
                                    "Erro: " + e.getMessage()));
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