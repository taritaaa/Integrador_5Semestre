package dev.tarazonarodriguezrojas.taller1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía los correos del sistema (RF-03) usando el servidor SMTP de Gmail.
 *
 * Configuración (ver application.properties): variables de entorno MAIL_USERNAME y MAIL_PASSWORD.
 * Si no están definidas, o si el envío falla, NO se rompe el flujo: el enlace de recuperación
 * se escribe en la consola para poder seguir probando.
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
            log.warn("Correo NO configurado (falta la variable MAIL_USERNAME / MAIL_PASSWORD). "
                    + "Enlace de recuperación para {}: {}", to, link);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Recuperación de contraseña - Taller");
        message.setText("Hola " + name + ",\n\n"
                + "Recibimos una solicitud para restablecer su contraseña. "
                + "Ingrese al siguiente enlace (válido por " + minutes + " minutos):\n\n"
                + link + "\n\n"
                + "Si usted no hizo esta solicitud, ignore este mensaje.");
        try {
            sender.send(message);
            log.info("Correo de recuperación enviado a {}", to);
        } catch (MailAuthenticationException e) {
            // Causa más común: se usó la contraseña normal de Gmail en vez de la "contraseña de aplicación"
            log.error("Gmail rechazó las credenciales. Use una CONTRASEÑA DE APLICACIÓN de 16 letras "
                    + "(requiere verificación en 2 pasos): {}", e.getMessage());
            log.info("Enlace de recuperación para {}: {}", to, link);
        } catch (MailException e) {
            log.error("No se pudo enviar el correo a {}: {}", to, e.getMessage());
            log.info("Enlace de recuperación para {}: {}", to, link);
        }
    }
}