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
    private String remetente;

    @Value("${spring.mail.properties.mail.debug:false}")
    private boolean debugMode;

    public void enviarCodigoConfirmacao(String destinatario, String codigo) {
        if (!StringUtils.hasText(destinatario)) {
            throw new IllegalArgumentException("Destinatário não pode ser vazio");
        }

        if (!StringUtils.hasText(codigo)) {
            throw new IllegalArgumentException("Código não pode ser vazio");
        }

        logger.info("Iniciando envio de email para: {}", destinatario);
        logger.info("Remetente configurado: {}", remetente);

        if (debugMode) {
            logger.debug("Modo debug ativado para email");
            // Log de simulação para desenvolvimento
            logger.info("=== SIMULAÇÃO DE EMAIL (DEV MODE) ===");
            logger.info("Para: {}", destinatario);
            logger.info("Código: {}", codigo);
            logger.info("Assunto: Código de Verificação - UniLivros");
            logger.info("==============================");
            return; // Não envia email real em modo debug
        }

        try {
            // Tentativa com MimeMessage (mais robusta)
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject("Código de Verificação - UniLivros");
            helper.setSentDate(new Date());

            String htmlContent = String.format(
                    "<html>" +
                            "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                            "    <div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                            "        <h2 style='color: #2c3e50; text-align: center;'>UniLivros - Confirmação de Cadastro</h2>" +
                            "        <hr style='border: 1px solid #eee;'>" +
                            "        <p>Olá,</p>" +
                            "        <p>Seu código de verificação é:</p>" +
                            "        <div style='background-color: #f8f9fa; padding: 15px; text-align: center; border-radius: 5px; margin: 20px 0;'>" +
                            "            <h1 style='color: #2c3e50; margin: 0; letter-spacing: 5px; font-size: 28px;'>%s</h1>" +
                            "        </div>" +
                            "        <p>Insira este código para confirmar seu cadastro ou redefinir sua senha.</p>" +
                            "        <p style='color: #7f8c8d; font-size: 12px;'>" +
                            "            Este código é válido por 1 hora. Se você não solicitou este código, ignore este email." +
                            "        </p>" +
                            "        <hr style='border: 1px solid #eee;'>" +
                            "        <p style='text-align: center; color: #95a5a6; font-size: 12px;'>" +
                            "            UniLivros &copy; 2024 - Sistema de Troca de Livros Universitários" +
                            "        </p>" +
                            "    </div>" +
                            "</body>" +
                            "</html>",
                    codigo
            );

            helper.setText(htmlContent, true); // true = HTML

            logger.debug("Configuração SMTP:");
            logger.debug("Host: {}", System.getProperty("mail.smtp.host"));
            logger.debug("Port: {}", System.getProperty("mail.smtp.port"));
            logger.debug("Auth: {}", System.getProperty("mail.smtp.auth"));

            mailSender.send(mimeMessage);
            logger.info("Email enviado com SUCESSO para: {}", destinatario);

        } catch (MessagingException e) {
            logger.error("Erro ao criar mensagem de email para {}: {}", destinatario, e.getMessage(), e);

            // Fallback para SimpleMailMessage
            try {
                logger.info("Tentando fallback com SimpleMailMessage...");
                SimpleMailMessage fallbackMessage = new SimpleMailMessage();
                fallbackMessage.setFrom(remetente);
                fallbackMessage.setTo(destinatario);
                fallbackMessage.setSubject("Código de Verificação - UniLivros");
                fallbackMessage.setText(String.format(
                        "Olá!\n\nSeu código de verificação é: %s\n\n" +
                                "Insira este código no aplicativo para confirmar seu cadastro ou redefinir sua senha.\n\n" +
                                "Este código é válido por 1 hora.\n\n" +
                                "Atenciosamente,\nEquipe UniLivros",
                        codigo
                ));

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

    private String getErrorMessage(MailException e) {
        String message = e.getMessage();

        if (message.contains("535-5.7.8") || message.contains("Invalid credentials")) {
            return "Credenciais SMTP inválidas. Use uma SENHA DE APLICATIVO do Google.";
        } else if (message.contains("Could not connect to SMTP host")) {
            return "Não foi possível conectar ao servidor SMTP. Verifique sua conexão com a internet.";
        } else if (message.contains("Connection timed out")) {
            return "Tempo limite de conexão excedido. Verifique as configurações de firewall.";
        } else if (message.contains("Authentication Required")) {
            return "Autenticação SMTP necessária. Verifique usuário e senha.";
        } else if (message.contains("501 5.1.7")) {
            return "Endereço de email inválido.";
        }

        return "Verifique as configurações SMTP. Para Gmail, use uma SENHA DE APLICATIVO (App Password).";
    }

    public void testarConexaoSMTP() {
        try {
            logger.info("Testando conexão SMTP...");
            logger.info("Host: {}", System.getProperty("mail.smtp.host"));
            logger.info("Usuário: {}", remetente);

            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(remetente);
            testMessage.setTo("teste@" + remetente.substring(remetente.indexOf('@') + 1));
            testMessage.setSubject("Teste de Conexão SMTP");
            testMessage.setText("Esta é uma mensagem de teste do sistema UniLivros.");

            mailSender.send(testMessage);
            logger.info("Teste de conexão SMTP bem-sucedido!");

        } catch (Exception e) {
            logger.error("FALHA no teste de conexão SMTP: {}", e.getMessage(), e);
            throw e;
        }
    }
}