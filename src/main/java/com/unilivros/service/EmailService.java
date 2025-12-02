package com.unilivros.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException; // Importar esta exceção
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

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
            message.setText("Olá!\\n\\nSeu código de verificação é: " + codigo + "\\n\\nInsira este código no aplicativo para confirmar seu cadastro ou redefinir sua senha.");

            mailSender.send(message);
            System.out.println("E-mail enviado com sucesso para: " + destinatario);
        } catch (MailException e) {
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
            throw new RuntimeException("Falha ao enviar e-mail de confirmação. Verifique as configurações SMT P.", e);
        } catch (Exception e) {
            System.err.println("Erro desconhecido ao preparar ou enviar e-mail: " + e.getMessage());
            throw new RuntimeException("Erro interno no serviço de e-mail.", e);
        }
    }
}