package com.unilivros.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
            message.setText("Olá!\n\nSeu código de verificação é: " + codigo + "\n\nInsira este código no aplicativo para confirmar seu cadastro ou redefinir sua senha.");

            mailSender.send(message);
            System.out.println("E-mail enviado com sucesso para: " + destinatario);
        } catch (Exception e) {
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
            // Em produção, você pode lançar uma exceção personalizada aqui
        }
    }
}