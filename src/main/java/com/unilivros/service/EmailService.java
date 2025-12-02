package com.unilivros.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class); // Logger adicionado

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public void enviarCodigoConfirmacao(String destinatario, String codigo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(remetente);
            message.setTo(destinatario);
            message.setSubject("Código de Verificação - UniLivros");
            message.setText(String.format(
                    "Olá!\n\nSeu código de verificação é: %s\n\nInsira este código para confirmar seu cadastro ou redefinir sua senha.",
                    codigo
            ));

            mailSender.send(message);
            logger.info("E-mail enviado com sucesso para: {}", destinatario);

        } catch (MailException e) {
            logger.error("Erro ao enviar e-mail para {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar e-mail de confirmação. Verifique as configurações SMTP. Se estiver usando Gmail, certifique-se de usar uma SENHA DE APLICATIVO (App Password) e não a senha de login.", e);
        } catch (Exception e) {
            logger.error("Erro desconhecido ao preparar ou enviar e-mail: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno no serviço de e-mail.", e);
        }
    }
}