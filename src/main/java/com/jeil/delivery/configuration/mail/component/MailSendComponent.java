package com.jeil.delivery.configuration.mail.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.jeil.delivery.configuration.mail.domain.MailDTO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailSendComponent {

	private final JavaMailSender javaMailSender;
	private final SpringTemplateEngine springTemplateEngine;

	@Value("${mail.from.address}")
	private String mailFromAddress; // 보낸 사람 이메일

    @Value("${cors.allowed-origins}")
    private String corsAllowedOriginsUrl; // url 주소


    @Async
	public void sendMail(Context context, String templatePath, MailDTO mailDTO) {
        try {

        	MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom(mailFromAddress);

			helper.setTo(mailDTO.getTo());

			context.setVariable("url", corsAllowedOriginsUrl);
			String html = springTemplateEngine.process(templatePath, context);

	        helper.setSubject(mailDTO.getTitle());

	        helper.setText(html, true);

			javaMailSender.send(message);

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
