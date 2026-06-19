package com.jeil.delivery.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;
    private static final String FROM_ADDRESS = "webmaster@jeilexpress.com"; // 임시

    // 텍스트 메일 발송
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_ADDRESS);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    // 첨부 파일 하나 메일 발송
    public void sendFileMessage(String to, String subject, MultipartFile file) throws IOException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(FROM_ADDRESS);
            helper.setTo(to);
            helper.setSubject(subject);

            if (file != null && !file.isEmpty()) {
                helper.addAttachment(file.getOriginalFilename(), file);
            }
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    // 첨부 파일 여러 개 메일 발송
    public void sendFilesMessage(String to, String subject, MultipartFile[] files) throws IOException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(FROM_ADDRESS);
            helper.setTo(to);
            helper.setSubject(subject);

            if (files != null) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        helper.addAttachment(file.getOriginalFilename(), file);
                    }
                }
            }
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}