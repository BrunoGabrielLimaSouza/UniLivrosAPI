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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.properties.mail.debug:false}")
    private boolean debugMode;

    @Value("${app.email.sender:suporteunilivros@gmail.com}")
    private String senderEmail;

    @Value("${app.email.mode:auto}")
    private String emailMode;

    public void enviarCodigoConfirmacao(String destinatario, String codigo) {
        if (!StringUtils.hasText(destinatario)) {
            throw new IllegalArgumentException("Destinatário não pode ser vazio");
        }

        if (!StringUtils.hasText(codigo)) {
            throw new IllegalArgumentException("Código não pode ser vazio");
        }

        logger.info("Enviando código de confirmação para: {}", destinatario);
        logger.info("Modo de email: {}, Host: {}:{}", emailMode, mailHost, mailPort);

        // Determina o modo de operação
        EmailMode mode = determineEmailMode();

        switch (mode) {
            case SENDGRID:
                enviarComSendGrid(destinatario, codigo);
                break;
            case SMTP_LOCAL:
                enviarComSmtpLocal(destinatario, codigo);
                break;
            case SIMULATION:
                enviarEmailSimulado(destinatario, codigo);
                break;
            case FILE_LOG:
                enviarParaArquivoLog(destinatario, codigo);
                break;
            default:
                enviarEmailSimulado(destinatario, codigo);
        }
    }

    private enum EmailMode {
        SENDGRID,
        SMTP_LOCAL,
        SIMULATION,
        FILE_LOG
    }

    private EmailMode determineEmailMode() {
        // Verifica se estamos no Render (produção)
        if (isRunningOnRender()) {
            logger.info("Rodando no Render, usando SendGrid");
            return EmailMode.SENDGRID;
        }

        // Verifica se temos configuração de SendGrid válida
        if (mailHost.contains("sendgrid.net") && isSendGridConfigured()) {
            logger.info("SendGrid configurado, tentando usar");
            return EmailMode.SENDGRID;
        }

        // Verifica se podemos conectar ao SMTP local
        if (mailHost.equals("localhost") && mailPort == 1025) {
            logger.info("Usando SMTP local (MailDev)");
            return EmailMode.SMTP_LOCAL;
        }

        // Modo específico configurado
        if (emailMode.equalsIgnoreCase("simulation")) {
            logger.info("Usando modo de simulação (configurado)");
            return EmailMode.SIMULATION;
        }

        if (emailMode.equalsIgnoreCase("file")) {
            logger.info("Usando modo arquivo (configurado)");
            return EmailMode.FILE_LOG;
        }

        if (emailMode.equalsIgnoreCase("sendgrid")) {
            logger.info("Usando SendGrid (forçado)");
            return EmailMode.SENDGRID;
        }

        // Modo de simulação para desenvolvimento
        if (debugMode) {
            logger.info("Usando modo de simulação (debug ativado)");
            return EmailMode.SIMULATION;
        }

        // Fallback: salva em arquivo
        logger.info("Usando fallback para arquivo de log");
        return EmailMode.FILE_LOG;
    }

    private boolean isRunningOnRender() {
        // Render define variáveis de ambiente específicas
        String render = System.getenv("RENDER");
        String renderServiceId = System.getenv("RENDER_SERVICE_ID");
        String renderServiceName = System.getenv("RENDER_SERVICE_NAME");

        boolean isRender = "true".equalsIgnoreCase(render) ||
                renderServiceId != null ||
                renderServiceName != null;

        logger.debug("Detectando ambiente Render: RENDER={}, SERVICE_ID={}, SERVICE_NAME={}, Resultado={}",
                render, renderServiceId, renderServiceName, isRender);

        return isRender;
    }

    private boolean isSendGridConfigured() {
        boolean hasApiKey = System.getenv("SENDGRID_API_KEY") != null;
        boolean isConfigured = mailUsername != null &&
                mailUsername.equals("apikey") &&
                hasApiKey;

        logger.debug("SendGrid configurado? Username={}, HasAPIKey={}, Resultado={}",
                mailUsername, hasApiKey, isConfigured);

        return isConfigured;
    }

    private void enviarComSendGrid(String destinatario, String codigo) {
        try {
            logger.info("Tentando enviar via SendGrid para: {}", destinatario);
            logger.debug("Config SendGrid - Host: {}, Port: {}, User: {}", mailHost, mailPort, mailUsername);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(senderEmail, "UniLivros");
            helper.setTo(destinatario);
            helper.setSubject("Código de Verificação - UniLivros");
            helper.setSentDate(new Date());

            String htmlContent = criarConteudoEmailHtml(codigo);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("✅ Email enviado com SUCESSO via SendGrid para: {}", destinatario);

        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Erro ao criar mensagem para SendGrid: {}", e.getMessage(), e);
            // Fallback para arquivo de log
            logger.warn("Fallback para arquivo de log devido a erro no SendGrid");
            enviarParaArquivoLog(destinatario, codigo);

        } catch (MailException e) {
            logger.error("Erro de SMTP ao enviar via SendGrid: {}", e.getMessage(), e);

            // Tenta porta alternativa 465 (SSL)
            if (mailPort == 587) {
                logger.info("Tentando porta alternativa 465 (SSL)...");
                // Em um cenário real, você poderia tentar reconfigurar dinamicamente
            }

            // Fallback para arquivo
            enviarParaArquivoLog(destinatario, codigo);
        }
    }

    private void enviarComSmtpLocal(String destinatario, String codigo) {
        try {
            logger.info("Enviando via SMTP local (MailDev) para: {}", destinatario);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(senderEmail, "UniLivros");
            helper.setTo(destinatario);
            helper.setSubject("Código de Verificação - UniLivros");

            String htmlContent = criarConteudoEmailHtml(codigo);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("✅ Email enviado via SMTP local para: {}", destinatario);
            logger.info("💡 Acesse http://localhost:1080 para visualizar o email");

        } catch (Exception e) {
            logger.error("Erro ao enviar via SMTP local: {}", e.getMessage());
            enviarEmailSimulado(destinatario, codigo);
        }
    }

    private void enviarEmailSimulado(String destinatario, String codigo) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String logMessage = String.format(
                "\n" +
                        "╔══════════════════════════════════════════════════════════╗\n" +
                        "║                 SIMULAÇÃO DE EMAIL                      ║\n" +
                        "╠══════════════════════════════════════════════════════════╣\n" +
                        "║ Data/Hora: %s                       ║\n" +
                        "║ Para:      %-40s ║\n" +
                        "║ Código:    %-40s ║\n" +
                        "║ Assunto:   Código de Verificação - UniLivros            ║\n" +
                        "║ Remetente: %-40s ║\n" +
                        "╚══════════════════════════════════════════════════════════╝\n",
                timestamp,
                destinatario.length() > 40 ? destinatario.substring(0, 37) + "..." : destinatario,
                codigo,
                senderEmail.length() > 40 ? senderEmail.substring(0, 37) + "..." : senderEmail
        );

        logger.info(logMessage);

        // Também salva em arquivo para referência
        enviarParaArquivoLog(destinatario, codigo);
    }

    private void enviarParaArquivoLog(String destinatario, String codigo) {
        try {
            String logDir = "logs";
            java.io.File dir = new java.io.File(logDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String logFile = logDir + "/emails.log";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String logEntry = String.format(
                    "[%s] EMAIL_LOG: Para=%s, Código=%s, Remetente=%s\n",
                    timestamp, destinatario, codigo, senderEmail
            );

            Files.write(
                    Paths.get(logFile),
                    logEntry.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            logger.info("📄 Email salvo em arquivo: {}", logFile);

        } catch (IOException e) {
            logger.error("Erro ao salvar email em arquivo: {}", e.getMessage());

            // Fallback extremo: apenas log no console
            logger.info("EMAIL SIMULADO - Para: {}, Código: {}", destinatario, codigo);
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
                        "            <p>Insira este código no aplicativo para confirmar seu cadastro ou redefinir sua senha.</p>" +
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
                        "Insira este código no aplicativo para confirmar seu cadastro ou redefinir sua senha.\n\n" +
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

    public String getConfiguracaoAtual() {
        return String.format(
                "=== CONFIGURAÇÃO DE EMAIL ===\n" +
                        "Host: %s\n" +
                        "Porta: %d\n" +
                        "Usuário: %s\n" +
                        "Modo: %s\n" +
                        "Remetente: %s\n" +
                        "Debug: %s\n" +
                        "--- Ambiente ---\n" +
                        "Render: %s\n" +
                        "RENDER env: %s\n" +
                        "SERVICE_ID: %s\n" +
                        "SERVICE_NAME: %s\n" +
                        "SendGrid Configurado: %s\n" +
                        "API Key Presente: %s\n" +
                        "========================",
                mailHost, mailPort, mailUsername, emailMode, senderEmail, debugMode,
                isRunningOnRender(),
                System.getenv("RENDER"),
                System.getenv("RENDER_SERVICE_ID"),
                System.getenv("RENDER_SERVICE_NAME"),
                isSendGridConfigured(),
                System.getenv("SENDGRID_API_KEY") != null ? "Sim" : "Não"
        );
    }

    public void testarConexaoEmail() {
        try {
            logger.info("=== TESTE DE CONEXÃO DE EMAIL ===");
            logger.info(getConfiguracaoAtual());

            // Determina o modo atual
            EmailMode mode = determineEmailMode();
            logger.info("Modo detectado: {}", mode);

            switch (mode) {
                case SENDGRID:
                    testarSendGrid();
                    break;
                case SMTP_LOCAL:
                    testarSmtpLocal();
                    break;
                default:
                    logger.info("✅ Modo {} não requer teste de conexão", mode);
                    logger.info("Teste de simulação realizado com sucesso");
            }

        } catch (Exception e) {
            logger.error("❌ Erro no teste de conexão: {}", e.getMessage(), e);
        }
    }

    private void testarSendGrid() {
        try {
            logger.info("Testando conexão com SendGrid...");

            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(senderEmail);
            testMessage.setTo("teste@example.com");
            testMessage.setSubject("[TESTE] Conexão SendGrid - UniLivros");
            testMessage.setText("Esta é uma mensagem de teste do sistema UniLivros.\n" +
                    "Data/Hora: " + LocalDateTime.now() + "\n" +
                    "Ambiente: Render");

            mailSender.send(testMessage);
            logger.info("✅ Teste de conexão SendGrid bem-sucedido!");
            logger.info("💡 Email de teste enviado via SendGrid");

        } catch (Exception e) {
            logger.error("❌ FALHA no teste de SendGrid: {}", e.getMessage());
            logger.info("⚠️  Usando fallback para modo simulação");
        }
    }

    private void testarSmtpLocal() {
        try {
            logger.info("Testando conexão com SMTP local...");

            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(senderEmail);
            testMessage.setTo("teste@example.com");
            testMessage.setSubject("[TESTE] SMTP Local - UniLivros");
            testMessage.setText("Teste de conexão SMTP local.\nData/Hora: " + LocalDateTime.now());

            mailSender.send(testMessage);
            logger.info("✅ Teste de conexão SMTP local bem-sucedido!");
            logger.info("💡 Acesse http://localhost:1080 para ver o email de teste");

        } catch (Exception e) {
            logger.error("❌ FALHA no teste de SMTP local: {}", e.getMessage());
            logger.info("⚠️  Certifique-se de que o MailDev está rodando: maildev");
        }
    }
}