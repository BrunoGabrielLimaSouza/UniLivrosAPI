package com.unilivros.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework. stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta. mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file. Paths;
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

    @Value("${spring. mail.port:1025}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.properties.mail.debug:false}")
    private boolean debugMode;

    @Value("${app.email.sender:suporteunilivros@gmail.com}")
    private String senderEmail;

    @Value("${app. email.mode:simulation}")
    private String emailMode;

    public void enviarCodigoConfirmacao(String destinatario, String codigo) {
        if (!StringUtils.hasText(destinatario)) {
            throw new IllegalArgumentException("Destinatário não pode ser vazio");
        }

        if (!StringUtils.hasText(codigo)) {
            throw new IllegalArgumentException("Código não pode ser vazio");
        }

        logger.info("📧 Enviando código de confirmação para: {}", destinatario);
        logger.info("🔧 Modo: {}, Host: {}:{}", emailMode, mailHost, mailPort);

        // Determina o modo de operação
        EmailMode mode = determineEmailMode();
        logger.info("📮 Modo detectado: {}", mode);

        try {
            switch (mode) {
                case SENDGRID_API:
                    enviarComSendGridAPI(destinatario, codigo);
                    break;
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
        SENDGRID_API,
        GMAIL,
        SMTP_LOCAL,
        SIMULATION,
        FILE_LOG
    }

    private EmailMode determineEmailMode() {
        logger.info("🔍 Determinando modo de email...");
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
            logger.info("🔍 Modo real/auto - detectando servidor...");

            // Prioridade 1: SendGrid API (mais confiável no Render)
            if (isSendGridApiConfigured()) {
                logger. info("✅ SendGrid API configurado");
                return EmailMode.SENDGRID_API;
            }

            // Prioridade 2: SMTP (Gmail ou SendGrid SMTP)
            if (isSmtpConfigured()) {
                String service = mailHost.contains("sendgrid") ? "SendGrid SMTP" : "Gmail";
                logger.info("✅ {} configurado", service);
                return EmailMode.GMAIL;
            }

            // Prioridade 3: MailDev local
            if (isMailDevLocal()) {
                logger.info("✅ MailDev local detectado");
                return EmailMode. SMTP_LOCAL;
            }
        }

        // Fallback para simulação
        logger. info("⚠️ Nenhuma configuração detectada - usando simulação");
        return EmailMode.SIMULATION;
    }

    private boolean isSendGridApiConfigured() {
        String apiKey = System.getenv("SENDGRID_API_KEY");
        boolean configured = apiKey != null && !apiKey. isEmpty() && apiKey.startsWith("SG.");
        logger.info("🔍 SendGrid API Key presente?  {}", configured);
        return configured;
    }

    private boolean isSmtpConfigured() {
        boolean isGmail = "smtp.gmail.com".equalsIgnoreCase(mailHost)
                && mailPort == 587
                && StringUtils.hasText(mailUsername)
                && !"apikey".equals(mailUsername);

        boolean isSendGrid = "smtp.sendgrid.net".equalsIgnoreCase(mailHost)
                && (mailPort == 587 || mailPort == 2525 || mailPort == 465)
                && "apikey".equals(mailUsername);

        return isGmail || isSendGrid;
    }

    private boolean isMailDevLocal() {
        return "localhost".equalsIgnoreCase(mailHost) && mailPort == 1025;
    }

    private void enviarComSendGridAPI(String destinatario, String codigo) {
        try {
            logger.info("📨 Enviando via SendGrid API HTTP para: {}", destinatario);

            String apiKey = System.getenv("SENDGRID_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                logger.error("❌ SENDGRID_API_KEY não configurada");
                throw new RuntimeException("SendGrid API Key não encontrada");
            }

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");

            String htmlContent = criarConteudoEmailHtml(codigo);

            // Cria JSON manualmente (mais simples e confiável)
            String jsonBody = String.format(
                    "{" +
                            "  \"personalizations\": [{\"to\": [{\"email\": \"%s\"}]}]," +
                            "  \"from\": {\"email\": \"%s\", \"name\": \"UniLivros\"}," +
                            "  \"subject\": \"UniLivros - Código de Verificação\"," +
                            "  \"content\": [{\"type\": \"text/html\", \"value\": %s}]" +
                            "}",
                    destinatario,
                    senderEmail,
                    new Gson().toJson(htmlContent)
            );

            request. setBody(jsonBody);
            Response response = sg.api(request);

            logger.info("🔍 SendGrid Response: Status {}", response.getStatusCode());

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("✅ Email enviado com SUCESSO via SendGrid API para: {}", destinatario);
            } else {
                logger. error("❌ Erro SendGrid API: Status {}, Body: {}",
                        response.getStatusCode(), response. getBody());
                throw new RuntimeException("Erro ao enviar via SendGrid API: " + response.getStatusCode());
            }

        } catch (IOException e) {
            logger.error("❌ Erro ao chamar SendGrid API: {}", e.getMessage(), e);
            throw new RuntimeException("Erro de conexão SendGrid API", e);
        }
    }

    private void enviarComGmail(String destinatario, String codigo) {
        try {
            logger.info("📨 Enviando via SMTP para: {}", destinatario);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(senderEmail, "UniLivros");
            helper.setTo(destinatario);
            helper.setSubject("UniLivros - Código de Verificação");
            helper.setSentDate(new Date());

            String htmlContent = criarConteudoEmailHtml(codigo);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("✅ Email enviado com SUCESSO via SMTP para: {}", destinatario);

        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("❌ Erro ao criar mensagem: {}", e.getMessage());
            throw new RuntimeException("Erro ao enviar email via SMTP", e);
        } catch (MailException e) {
            logger.error("❌ Erro SMTP: {}", e.getMessage());
            throw new RuntimeException("Erro de conexão SMTP", e);
        }
    }

    private void enviarComSmtpLocal(String destinatario, String codigo) {
        try {
            logger.info("🔧 Enviando via MailDev local para: {}", destinatario);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(senderEmail, "UniLivros");
            helper.setTo(destinatario);
            helper.setSubject("UniLivros - Código de Verificação");

            String htmlContent = criarConteudoEmailHtml(codigo);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("✅ Email enviado via MailDev");
            logger.info("💡 Acesse http://localhost:1080 para visualizar");

        } catch (Exception e) {
            logger.error("❌ Erro ao enviar via MailDev: {}", e.getMessage());
            throw new RuntimeException("Erro ao enviar via MailDev", e);
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
                truncate(destinatario, 40),
                codigo,
                truncate(senderEmail, 40)
        );

        logger.info(logMessage);
        enviarParaArquivoLog(destinatario, codigo);
    }

    private void enviarParaArquivoLog(String destinatario, String codigo) {
        try {
            String logDir = "logs";
            java.io.File dir = new java.io.File(logDir);
            if (!dir.exists()) {
                dir. mkdirs();
            }

            String logFile = logDir + "/emails. log";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String logEntry = String.format(
                    "[%s] EMAIL_LOG: Para=%s, Código=%s, Remetente=%s\n",
                    timestamp, destinatario, codigo, senderEmail
            );

            Files.write(
                    Paths.get(logFile),
                    logEntry.getBytes(),
                    StandardOpenOption. CREATE,
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
                        "    <title>Código de Verificação - UniLivros</title>" +
                        "    <style>" +
                        "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #F9E7DC; }" +
                        "        .container { max-width: 600px; margin: 20px auto; padding: 0; background-color: #F6E3C7; border-radius: 32px; box-shadow: 0 4px 24px rgba(0,0,0,0.1); }" +
                        "        . header { background: #F9B233; color: white; padding: 30px; text-align: center; border-radius: 32px 32px 0 0; }" +
                        "        .header h1 { margin: 0; font-size: 32px; }" +
                        "        .content { padding: 40px; }" +
                        "        .code-container { background: white; padding: 25px; text-align: center; border-radius: 12px; margin: 30px 0; }" +
                        "        .code { font-size: 36px; font-weight: bold; color: #F9B233; letter-spacing: 8px; }" +
                        "        . footer { background: #4B2E2E; color: white; padding: 20px; text-align: center; border-radius: 0 0 32px 32px; font-size: 12px; }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class=\"container\">" +
                        "        <div class=\"header\"><h1>UniLivros</h1></div>" +
                        "        <div class=\"content\">" +
                        "            <h2>Código de Verificação</h2>" +
                        "            <p>Seu código de verificação é:</p>" +
                        "            <div class=\"code-container\"><div class=\"code\">%s</div></div>" +
                        "            <p>Este código é válido por 1 hora.</p>" +
                        "        </div>" +
                        "        <div class=\"footer\"><p>© 2024 UniLivros</p></div>" +
                        "    </div>" +
                        "</body>" +
                        "</html>",
                codigo
        );
    }

    private String maskEmail(String email) {
        if (! StringUtils.hasText(email) || email.length() < 3) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex > 2) return email.substring(0, 2) + "***" + email.substring(atIndex);
        return "***";
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ?  str.substring(0, maxLength - 3) + "..." : str;
    }
}