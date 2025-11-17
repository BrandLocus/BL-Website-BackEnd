package com.hvc.brandlocus.services.impl.email;

import com.hvc.brandlocus.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailServiceStrategy {

    private final GmailServiceImpl gmailService;
    private final SendGridImpl sendGridService;

    @Value("${app.email.provider}") // "gmail" or "sendgrid"
    private String primaryProvider;

    private static final int MAX_RETRIES = 3;

    @Async
    public void send(String subject, String to, String message) {
        EmailService primary = getService(primaryProvider);
        EmailService secondary = primary == gmailService ? sendGridService : gmailService;

        boolean sent = trySendWithRetries(primary, subject, to, message);

        if (!sent) {
            log.warn("Primary provider {} failed, switching to secondary", primaryProvider);
            sent = trySendWithRetries(secondary, subject, to, message);
            if (!sent) {
                throw new RuntimeException("Both email providers failed to send the email");
            }
        }
    }

    private EmailService getService(String provider) {
        if ("gmail".equalsIgnoreCase(provider)) {
            return gmailService;
        } else if ("sendgrid".equalsIgnoreCase(provider)) {
            return sendGridService;
        }
        log.warn("Unknown provider '{}', defaulting to Gmail", provider);
        return gmailService;
    }

    private boolean trySendWithRetries(EmailService service, String subject, String to, String message) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                service.sendEmail(subject, to, message);
                log.info("Email sent successfully via {}", service.getClass().getSimpleName());
                return true;
            } catch (Exception e) {
                attempt++;
                log.error("Attempt {} failed for {}: {}", attempt, service.getClass().getSimpleName(), e.getMessage());
                try {
                    Thread.sleep(1000); // optional delay between retries
                } catch (InterruptedException ignored) {}
            }
        }
        return false;
    }
}
