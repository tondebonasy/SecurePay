package com.moneyguard.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSender, @Value("${MAIL_FROM:moneyguard@local.test}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void send(String to, String subject, String text) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("[MAIL_DISABLED] to={} subject={}", to, subject);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        sender.send(msg);
    }
}
