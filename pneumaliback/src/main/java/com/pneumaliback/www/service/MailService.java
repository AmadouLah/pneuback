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

    @Value("${app.admin.emails:}")
    private String adminEmails;

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
                + "Ce code expire dans 2 minutes.\n\n"
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

    public void sendSuspiciousLoginAlert(String toEmail, String ip, String userAgent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Alerte: tentative de connexion inhabituelle");
        message.setText("Bonjour,\n\n" +
                "Une tentative de connexion suspecte a été détectée.\n" +
                "IP: " + (ip == null ? "" : ip) + "\n" +
                "Navigateur/Appareil: " + (userAgent == null ? "" : userAgent) + "\n\n" +
                "Si ce n'était pas vous, veuillez sécuriser votre compte.\n\n" +
                "Cordialement,");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Echec envoi alerte login pour {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendWeeklySuspiciousReport(String subject, String body) {
        if (adminEmails == null || adminEmails.isBlank())
            return;
        String[] recipients = adminEmails.split(",");
        for (String to : recipients) {
            String email = to.trim();
            if (email.isEmpty())
                continue;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            try {
                mailSender.send(message);
            } catch (Exception e) {
                log.warn("Echec envoi rapport hebdo à {}: {}", email, e.getMessage());
            }
        }
    }

    /**
     * Envoie un email de notification lors du changement d'adresse email
     * 
     * @param newEmail Le nouvel email (destinataire)
     * @param oldEmail L'ancien email
     */
    public void sendEmailChangeNotification(String newEmail, String oldEmail) {
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Nouvel email requis");
        }
        if (oldEmail == null || oldEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Ancien email requis");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(newEmail);
        message.setSubject("Pneu Mali - Email address changed");
        message.setText("Pneu Mali\n\n"
                + "The email address associated with your account has been changed.\n\n"
                + "Previous email address: " + oldEmail + "\n\n"
                + "New email address: " + newEmail + "\n\n"
                + "If you did not make these changes, please contact us immediately.\n\n"
                + "Best regards,\n"
                + "Pneu Mali Team");

        try {
            mailSender.send(message);
            log.info("Email de notification de changement d'adresse envoyé de {} vers {}", oldEmail, newEmail);
        } catch (Exception e) {
            log.error("Echec envoi d'email de notification pour {}: {}", newEmail, e.getMessage());
            // Ne pas lever d'exception car le changement d'email est déjà effectué
        }
    }
}
