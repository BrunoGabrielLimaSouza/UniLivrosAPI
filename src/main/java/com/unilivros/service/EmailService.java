package com.unilivros.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Date;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetenteUsername;

    @Value("${spring.mail.properties.mail.debug:false}")
    private boolean debugMode;

    @Value("${app.email.sender:suporteunilivros@gmail.com}")
    private String senderEmail;

    public void enviarCodigoConfirmacao(String destinatario, String codigo) {
        if (!StringUtils.hasText(destinatario)) {
            throw new IllegalArgumentException("Destinatário não pode ser vazio");
        }

        if (!StringUtils.hasText(codigo)) {
            throw new IllegalArgumentException("Código não pode ser vazio");
        }

        logger.info("Enviando código de confirmação para: {}", destinatario);
        logger.info("Usando SendGrid com usuário: {}", remetenteUsername);

        // Modo debug para desenvolvimento
        if (debugMode && remetenteUsername.equals("apikey")) {
            logger.debug("Modo debug ativado para SendGrid");
            enviarEmailSimulado(destinatario, codigo);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Para SendGrid, o "from" deve ser um email verificado na sua conta SendGrid
            helper.setFrom(senderEmail, "UniLivros");
            helper.setTo(destinatario);
            helper.setSubject("Código de Verificação - UniLivros");
            helper.setSentDate(new Date());

            String htmlContent = criarConteudoEmailHtml(codigo);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("Email enviado com SUCESSO via SendGrid para: {}", destinatario);

        } catch (MessagingException e) {
            logger.error("Erro ao criar mensagem de email para {}: {}", destinatario, e.getMessage(), e);

            // Fallback para SimpleMailMessage
            try {
                logger.info("Tentando fallback com SimpleMailMessage...");
                SimpleMailMessage fallbackMessage = new SimpleMailMessage();
                fallbackMessage.setFrom(senderEmail);
                fallbackMessage.setTo(destinatario);
                fallbackMessage.setSubject("Código de Verificação - UniLivros");
                fallbackMessage.setText(criarConteudoEmailTexto(codigo));

                mailSender.send(fallbackMessage);
                logger.info("Fallback email enviado com sucesso para: {}", destinatario);

            } catch (MailException fallbackException) {
                logger.error("FALHA NO FALLBACK para {}: {}", destinatario, fallbackException.getMessage(), fallbackException);
                throw new RuntimeException("Falha ao enviar email. " + getErrorMessage(fallbackException), fallbackException);
            }

        } catch (MailException e) {
            logger.error("Erro de SMTP ao enviar email para {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar email. " + getErrorMessage(e), e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao enviar email para {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("Erro interno no serviço de email.", e);
        }
    }

    private String criarConteudoEmailHtml(String codigo) {
        return String.format(
                "<!DOCTYPE html>" +
                        "<html lang=\"pt-BR\">" +
                        "<head>" +
                        "    <meta charset=\"UTF-8\">" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                        "    <title>Código de Verificação - UniLivros</title>" +
                        "    <style>" +
                        "        body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #F9E7DC; }" +
                        "        .container { max-width: 600px; margin: 20px auto; padding: 0; background-color: #F6E3C7; border-radius: 32px; box-shadow: 0 4px 24px rgba(0,0,0,0.1); }" +
                        "        .header { background: #F9B233; color: white; padding: 30px; text-align: center; border-radius: 32px 32px 0 0; }" +
                        "        .content { padding: 40px; }" +
                        "        .code-container { background: white; padding: 25px; text-align: center; border-radius: 12px; margin: 30px 0; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                        "        .code { font-size: 32px; font-weight: bold; color: #4B2E2E; letter-spacing: 8px; }" +
                        "        .footer { background: #4B2E2E; color: white; padding: 20px; text-align: center; border-radius: 0 0 32px 32px; font-size: 12px; }" +
                        "        .btn { display: inline-block; background: #F9B233; color: white; padding: 12px 24px; text-decoration: none; border-radius: 16px; font-weight: bold; margin-top: 20px; }" +
                        "        .btn:hover { background: #e6a91f; }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class=\"container\">" +
                        "        <div class=\"header\">" +
                        "            <h1>UniLivros</h1>" +
                        "            <p>Sistema de Troca de Livros Universitários</p>" +
                        "        </div>" +
                        "        <div class=\"content\">" +
                        "            <h2 style=\"color: #4B2E2E;\">Confirmação de Cadastro</h2>" +
                        "            <p>Olá,</p>" +
                        "            <p>Seu código de verificação é:</p>" +
                        "            <div class=\"code-container\">" +
                        "                <div class=\"code\">%s</div>" +
                        "            </div>" +
                        "            <p>Insira este código para confirmar seu cadastro ou redefinir sua senha.</p>" +
                        "            <p><strong>Este código é válido por 1 hora.</strong></p>" +
                        "            <p>Se você não solicitou este código, ignore este email.</p>" +
                        "        </div>" +
                        "        <div class=\"footer\">" +
                        "            <p>© 2024 UniLivros - Todos os direitos reservados</p>" +
                        "            <p>Este é um email automático, por favor não responda.</p>" +
                        "        </div>" +
                        "    </div>" +
                        "</body>" +
                        "</html>",
                codigo
        );
    }

    private String criarConteudoEmailTexto(String codigo) {
        return String.format(
                "UniLivros - Sistema de Troca de Livros Universitários\n\n" +
                        "CÓDIGO DE VERIFICAÇÃO\n\n" +
                        "Seu código de verificação é: %s\n\n" +
                        "Insira este código para confirmar seu cadastro ou redefinir sua senha.\n\n" +
                        "Este código é válido por 1 hora.\n\n" +
                        "Se você não solicitou este código, ignore este email.\n\n" +
                        "Atenciosamente,\n" +
                        "Equipe UniLivros\n\n" +
                        "---\n" +
                        "© 2024 UniLivros - Todos os direitos reservados\n" +
                        "Este é um email automático, por favor não responda.",
                codigo
        );
    }

    private String getErrorMessage(MailException e) {
        String message = e.getMessage();

        if (message.contains("535") || message.contains("Invalid credentials")) {
            return "Credenciais SMTP inválidas. Verifique sua chave API do SendGrid.";
        } else if (message.contains("Could not connect to SMTP host")) {
            return "Não foi possível conectar ao servidor SendGrid. Verifique sua conexão com a internet.";
        } else if (message.contains("Connection timed out")) {
            return "Tempo limite de conexão excedido. Verifique as configurações de firewall.";
        } else if (message.contains("Authentication Required")) {
            return "Autenticação SMTP necessária. Verifique usuário e senha.";
        } else if (message.contains("501 5.1.7")) {
            return "Endereço de email inválido.";
        } else if (message.contains("Unauthorized")) {
            return "Chave API do SendGrid inválida ou expirada.";
        } else if (message.contains("Sender address not verified")) {
            return "Email remetente não verificado no SendGrid. Verifique o email: " + senderEmail;
        }

        return "Erro ao enviar email. Verifique as configurações do SendGrid.";
    }

    private void enviarEmailSimulado(String destinatario, String codigo) {
        logger.info("=== SIMULAÇÃO DE EMAIL (MODO DESENVOLVIMENTO) ===");
        logger.info("Para: {}", destinatario);
        logger.info("Código: {}", codigo);
        logger.info("Remetente: {}", senderEmail);
        logger.info("Assunto: Código de Verificação - UniLivros");
        logger.info("Conteúdo HTML gerado com sucesso");
        logger.info("==============================");

        // Salva em arquivo para teste
        try {
            String logMessage = String.format(
                    "[%s] Para: %s | Código: %s | Remetente: %s\n",
                    new Date(), destinatario, codigo, senderEmail
            );
            java.nio.file.Files.write(
                    java.nio.file.Paths.get("emails-simulados.log"),
                    logMessage.getBytes(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (java.io.IOException e) {
            logger.error("Erro ao salvar email simulado: {}", e.getMessage());
        }
    }

    public void testarConexaoSendGrid() {
        try {
            logger.info("Testando conexão com SendGrid...");
            logger.info("Host: smtp.sendgrid.net");
            logger.info("Usuário: {}", remetenteUsername);
            logger.info("Email remetente: {}", senderEmail);

            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(senderEmail);
            testMessage.setTo("teste@example.com");
            testMessage.setSubject("Teste de Conexão SendGrid - UniLivros");
            testMessage.setText("Esta é uma mensagem de teste do sistema UniLivros usando SendGrid.");

            mailSender.send(testMessage);
            logger.info("✅ Teste de conexão SendGrid bem-sucedido!");

        } catch (Exception e) {
            logger.error("❌ FALHA no teste de conexão SendGrid: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na conexão com SendGrid: " + e.getMessage(), e);
        }
    }
}