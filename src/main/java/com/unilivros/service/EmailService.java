package com.unilivros.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail. MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail. javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet. MimeMessage;
import java. io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format. DateTimeFormatter;
import java. util.Date;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:localhost}")
    private String mailHost;

    @Value("${spring.mail.port:1025}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.properties.mail.debug:false}")
    private boolean debugMode;

    @Value("${app.email.sender:suporteunilivros@gmail.com}")
    private String senderEmail;

    @Value("${app.email.mode:simulation}")
    private String emailMode;

    public void enviarCodigoConfirmacao(String destinatario, String codigo) {
        if (! StringUtils.hasText(destinatario)) {
            throw new IllegalArgumentException("Destinatário não pode ser vazio");
        }

        if (!StringUtils.hasText(codigo)) {
            throw new IllegalArgumentException("Código não pode ser vazio");
        }

        // ===== LOG DE DEBUG COMPLETO =====
        logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.info("🔍 DEBUG COMPLETO - CONFIGURAÇÃO");
        logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.info("mailHost: '{}'", mailHost);
        logger.info("mailPort: {}", mailPort);
        logger.info("mailUsername: '{}'", mailUsername);
        logger.info("mailUsername vazio? {}", ! StringUtils.hasText(mailUsername));
        logger.info("mailUsername == 'apikey'? {}", "apikey".equals(mailUsername));
        logger.info("emailMode: '{}'", emailMode);
        logger.info("senderEmail: '{}'", senderEmail);

        // Testa condições SendGrid
        boolean hostOk = "smtp.sendgrid.net".equalsIgnoreCase(mailHost);
        boolean portOk = mailPort == 587;
        boolean userOk = "apikey".equals(mailUsername);

        logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.info("VALIDAÇÃO SENDGRID:");
        logger.info("  Host correto?  {} (esperado: smtp.sendgrid. net, atual: {})", hostOk, mailHost);
        logger.info("  Porta correta? {} (esperado: 587, atual: {})", portOk, mailPort);
        logger.info("  Username correto? {} (esperado: apikey, atual: '{}')", userOk, mailUsername);
        logger.info("  TODAS OK? {}", hostOk && portOk && userOk);
        logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        // ==================================

        logger.info("📧 Enviando código de confirmação para: {}", destinatario);
        logger.info("🔧 Modo: {}, Host: {}:{}", emailMode, mailHost, mailPort);

        // Determina o modo de operação
        EmailMode mode = determineEmailMode();
        logger.info("📮 Modo detectado: {}", mode);

        try {
            switch (mode) {
                case GMAIL:
                    enviarComGmail(destinatario, codigo);
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
        } catch (Exception e) {
            logger.error("❌ Erro ao enviar email: {}", e.getMessage());
            logger.warn("⚠️ Fallback para simulação");
            enviarEmailSimulado(destinatario, codigo);
        }
    }

    private enum EmailMode {
        GMAIL,
        SMTP_LOCAL,
        SIMULATION,
        FILE_LOG
    }

    private EmailMode determineEmailMode() {
        logger.info("🔍 Determinando modo de email.. .");
        logger.info("🔍 emailMode configurado: '{}'", emailMode);

        // Modo explícito configurado
        if ("simulation".equalsIgnoreCase(emailMode)) {
            logger.info("✅ Modo simulação (configurado explicitamente)");
            return EmailMode.SIMULATION;
        }

        if ("file".equalsIgnoreCase(emailMode)) {
            logger.info("✅ Modo arquivo (configurado explicitamente)");
            return EmailMode.FILE_LOG;
        }

        // Modo "real" ou "auto" → detecta automaticamente
        if ("real".equalsIgnoreCase(emailMode) || "auto".equalsIgnoreCase(emailMode)) {
            logger.info("🔍 Modo real/auto - detectando servidor SMTP.. .");

            // Detecta Gmail ou SendGrid
            if (isSmtpConfigured()) {
                String service = mailHost.contains("sendgrid") ? "SendGrid" : "Gmail";
                logger.info("✅ {} configurado - usando {}", service, service);
                return EmailMode.GMAIL;
            }

            // SMTP local (MailDev)
            if (isMailDevLocal()) {
                logger.info("✅ MailDev local detectado");
                return EmailMode. SMTP_LOCAL;
            }
        }

        // Fallback para simulação
        logger.info("⚠️ Nenhuma configuração detectada - usando simulação");
        return EmailMode. SIMULATION;
    }

    private boolean isSmtpConfigured() {
        // Detecta Gmail
        boolean isGmail = "smtp.gmail. com".equalsIgnoreCase(mailHost)
                && mailPort == 587
                && StringUtils.hasText(mailUsername)
                && !"apikey".equals(mailUsername);

        // Detecta SendGrid
        boolean isSendGrid = "smtp.sendgrid. net".equalsIgnoreCase(mailHost)
                && mailPort == 587
                && "apikey".equals(mailUsername);

        boolean isConfigured = isGmail || isSendGrid;

        logger. debug("SMTP configurado? Host={}, Port={}, User={}, Gmail={}, SendGrid={}, Resultado={}",
                mailHost, mailPort, maskEmail(mailUsername), isGmail, isSendGrid, isConfigured);

        return isConfigured;
    }

    private boolean isMailDevLocal() {
        boolean isLocal = "localhost".equalsIgnoreCase(mailHost) && mailPort == 1025;
        logger.debug("MailDev local?  Host={}, Port={}, Resultado={}", mailHost, mailPort, isLocal);
        return isLocal;
    }

    /**
     * Envia email via Gmail (PRODUÇÃO ou LOCAL)
     */
    private void enviarComGmail(String destinatario, String codigo) {
        try {
            logger.info("📨 Enviando via Gmail para: {}", destinatario);
            logger.debug("Gmail Config - Host: {}:{}, User: {}", mailHost, mailPort, maskEmail(mailUsername));

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(senderEmail, "UniLivros");
            helper.setTo(destinatario);
            helper.setSubject("UniLivros - Código de Verificação");
            helper.setSentDate(new Date());

            String htmlContent = criarConteudoEmailHtml(codigo);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("✅ Email enviado com SUCESSO via Gmail para: {}", destinatario);

        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("❌ Erro ao criar mensagem Gmail: {}", e.getMessage());
            throw new RuntimeException("Erro ao enviar email via Gmail", e);

        } catch (MailException e) {
            logger.error("❌ Erro SMTP Gmail: {}", e.getMessage());
            throw new RuntimeException("Erro de conexão Gmail", e);
        }
    }

    /**
     * Envia via SMTP local (MailDev)
     */
    private void enviarComSmtpLocal(String destinatario, String codigo) {
        try {
            logger.info("🔧 Enviando via MailDev local para: {}", destinatario);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(senderEmail, "UniLivros");
            helper.setTo(destinatario);
            helper. setSubject("UniLivros - Código de Verificação");

            String htmlContent = criarConteudoEmailHtml(codigo);
            helper. setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("✅ Email enviado via MailDev");
            logger.info("💡 Acesse http://localhost:1080 para visualizar");

        } catch (Exception e) {
            logger.error("❌ Erro ao enviar via MailDev: {}", e.getMessage());
            throw new RuntimeException("Erro ao enviar via MailDev", e);
        }
    }

    /**
     * Modo simulação (apenas logs)
     */
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
                truncate(destinatario, 40),
                codigo,
                truncate(senderEmail, 40)
        );

        logger.info(logMessage);
        enviarParaArquivoLog(destinatario, codigo);
    }

    /**
     * Salva email em arquivo de log
     */
    private void enviarParaArquivoLog(String destinatario, String codigo) {
        try {
            String logDir = "logs";
            java. io.File dir = new java.io.File(logDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String logFile = logDir + "/emails.log";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String logEntry = String.format(
                    "[%s] EMAIL_LOG: Para=%s, Código=%s, Remetente=%s\n",
                    timestamp, destinatario, codigo, senderEmail
            );

            Files. write(
                    Paths.get(logFile),
                    logEntry. getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            logger.info("📄 Email salvo em: {}", logFile);

        } catch (IOException e) {
            logger.error("❌ Erro ao salvar em arquivo: {}", e.getMessage());
            logger.info("💡 EMAIL CONSOLE - Para: {}, Código: {}", destinatario, codigo);
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
                        "        .header h1 { margin: 0; font-size: 32px; }" +
                        "        .header p { margin: 5px 0 0 0; }" +
                        "        .content { padding: 40px; }" +
                        "        .content h2 { color: #4B2E2E; margin-top: 0; }" +
                        "        .code-container { background: white; padding: 25px; text-align: center; border-radius: 12px; margin: 30px 0; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                        "        .code { font-size: 36px; font-weight: bold; color: #F9B233; letter-spacing: 8px; }" +
                        "        .warning { background: #FFF3CD; border-left: 4px solid #F9B233; padding: 15px; border-radius: 8px; margin: 20px 0; }" +
                        "        .footer { background: #4B2E2E; color: white; padding: 20px; text-align: center; border-radius: 0 0 32px 32px; font-size: 12px; }" +
                        "        .footer p { margin: 5px 0; }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class=\"container\">" +
                        "        <div class=\"header\">" +
                        "            <h1>📚 UniLivros</h1>" +
                        "            <p>Sistema de Troca de Livros Universitários</p>" +
                        "        </div>" +
                        "        <div class=\"content\">" +
                        "            <h2>Confirmação de Cadastro</h2>" +
                        "            <p>Olá,</p>" +
                        "            <p>Seu código de verificação é:</p>" +
                        "            <div class=\"code-container\">" +
                        "                <div class=\"code\">%s</div>" +
                        "            </div>" +
                        "            <p>Insira este código no aplicativo para confirmar seu cadastro ou redefinir sua senha.</p>" +
                        "            <div class=\"warning\">" +
                        "                <strong>⚠️ Importante:</strong> Este código é válido por 1 hora." +
                        "            </div>" +
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

    public String getConfiguracaoAtual() {
        return String. format(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "📧 CONFIGURAÇÃO DE EMAIL\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "Host: %s\n" +
                        "Porta: %d\n" +
                        "Usuário: %s\n" +
                        "Modo: %s\n" +
                        "Remetente: %s\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "🌍 DETECÇÃO\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "Gmail: %s\n" +
                        "MailDev: %s\n" +
                        "Render: %s\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                mailHost, mailPort, maskEmail(mailUsername), emailMode, senderEmail,
                isSmtpConfigured() ? "✅" : "❌",
                isMailDevLocal() ? "✅" : "❌",
                System.getenv("RENDER") != null ? "✅" : "❌"
        );
    }

    public void testarConexaoEmail() {
        logger.info("🧪 TESTE DE CONEXÃO");
        logger.info(getConfiguracaoAtual());

        EmailMode mode = determineEmailMode();
        logger.info("📮 Modo ativo: {}", mode);

        if (mode == EmailMode.GMAIL) {
            logger.info("✅ Gmail configurado e pronto para uso");
        } else if (mode == EmailMode. SMTP_LOCAL) {
            logger.info("✅ MailDev local pronto");
        } else {
            logger.info("ℹ️ Modo {} ativo", mode);
        }
    }

    private String maskEmail(String email) {
        if (! StringUtils.hasText(email) || email.length() < 3) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 2) {
            return email.substring(0, 2) + "***" + email.substring(atIndex);
        }
        return "***";
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ?  str.substring(0, maxLength - 3) + "..." : str;
    }
}