package dev.tarazonarodriguezrojas.taller1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía los correos del sistema (RF-03) por SMTP (Brevo, Gmail u otro).
 * Si falta la configuración o el envío falla, el enlace queda en la consola.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final String smtpUsername;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        @Value("${app.mail.from:no-reply@taller.local}") String from,
                        @Value("${spring.mail.username:}") String smtpUsername) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.smtpUsername = smtpUsername;
    }

    public void sendPasswordRecovery(String to, String name, String link, int minutes) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null || smtpUsername == null || smtpUsername.isBlank()) {
            log.warn("[CORREO NO CONFIGURADO] Enlace para {}: {}", to, link);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Recuperación de contraseña - Taller");
        message.setText("Hola " + name + ",\n\n"
                + "Para restablecer su contraseña ingrese a este enlace (válido " + minutes + " minutos):\n\n"
                + link + "\n\n"
                + "Si usted no hizo esta solicitud, ignore este mensaje.");
        try {
            sender.send(message);
            log.info("[CORREO ENVIADO] A {} (remitente {})", to, from);
        } catch (MailException e) {
            log.error("[CORREO FALLÓ] A {}: {}", to, e.getMessage());
            log.info("Enlace para {}: {}", to, link);
        }
    }
}