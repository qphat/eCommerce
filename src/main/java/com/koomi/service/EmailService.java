package com.koomi.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Transactional
    public void sendVerificationOtpEmail(String userEmail,
                                         String otp,
                                         String subject,
                                         String text) throws MessagingException {

        try {
            // Tạo MimeMessage thay vì MimeMailMessage
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mailMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
            mailMessageHelper.setSubject(subject);
            mailMessageHelper.setText(text); // Đặt `true` nếu nội dung là HTML
            mailMessageHelper.setTo(userEmail);

            // Gửi email
            javaMailSender.send(mimeMessage);

        } catch (MailException | MessagingException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage());
        }
    }
}
