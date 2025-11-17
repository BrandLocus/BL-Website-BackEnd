package com.hvc.brandlocus.services.impl.email;

import com.hvc.brandlocus.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendEmail(String emailSubject, String toEmail, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(emailSubject);
            mailMessage.setText(message);

            javaMailSender.send(mailMessage);

            log.info("Email successfully sent to {}", toEmail);

        } catch (Exception ex) {
            log.error("Failed to send Gmail email to {}: {}", toEmail, ex.getMessage(), ex);
            throw new RuntimeException("Failed to send email via Gmail", ex);
        }
    }
}
