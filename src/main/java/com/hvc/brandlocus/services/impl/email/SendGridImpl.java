package com.hvc.brandlocus.services.impl.email;

import com.hvc.brandlocus.services.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RequiredArgsConstructor
@Service
@Slf4j
public class SendGridImpl implements EmailService {
    @Value("${sendgrid.mail.username}")
    private String fromEmail;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    private final SendGrid sendGridClient;

    @Override
    public void sendEmail(String emailSubject, String toEmail, String message) throws Exception {
        log.info("Trying to send email via SendGrid to {}", toEmail);

        try {
            Email from = new Email(fromEmail, "FoodFlow");
            Email to = new Email(toEmail);
            Content content = new Content("text/plain", message);
            Mail mail = new Mail(from, emailSubject, to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGridClient.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent successfully via SendGrid to {}", toEmail);
            } else {
                log.error("SendGrid failed to send email to {}. Status: {}, Response: {}",
                        toEmail, response.getStatusCode(), response.getBody());
                throw new RuntimeException("SendGrid failed: " + response.getBody());
            }
        } catch (IOException e) {
            log.error("SendGrid IOException sending email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("SendGrid failed: " + e.getMessage(), e);
        }
    }
}
