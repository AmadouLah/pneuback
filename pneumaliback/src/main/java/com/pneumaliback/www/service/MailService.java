package com.pneumaliback.www.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendVerificationEmail(String toEmail, String code) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email destinataire requis");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code de vérification requis");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Vérification de votre compte");
        message.setText("Bonjour,\n\n"
                + "Voici votre code de vérification: " + code + "\n"
                + "Ce code expire dans 15 minutes.\n\n"
                + "Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.\n\n"
                + "Cordialement,");

        try {
            mailSender.send(message);
            log.info("Email de vérification envoyé à {}", toEmail);
        } catch (Exception e) {
            log.error("Echec envoi d'email pour {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de vérification", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String code) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email destinataire requis");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code de réinitialisation requis");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText("Bonjour,\n\n"
                + "Voici votre code de réinitialisation: " + code + "\n"
                + "Ce code expire dans 15 minutes.\n\n"
                + "Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.\n\n"
                + "Cordialement,");

        try {
            mailSender.send(message);
            log.info("Email de réinitialisation envoyé à {}", toEmail);
        } catch (Exception e) {
            log.error("Echec envoi d'email de réinitialisation pour {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de réinitialisation", e);
        }
    }
}
