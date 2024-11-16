package com.alex.spring.security.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreo(String to, String subject, String text) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(to);
        mensaje.setSubject(subject);
        mensaje.setText(text);
        mailSender.send(mensaje);
    }
}