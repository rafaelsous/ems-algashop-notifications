package com.rafaelsousa.algashop.notifications.infrastructure.mail;

import com.rafaelsousa.algashop.notifications.application.email.EmailSender;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailSenderJavaMailImpl implements EmailSender {
	private final JavaMailSender mailSender;
	private final MailProperties mailProperties;

	@Override
	public void send(String toEmail, String toName, String subject, String htmlBody) {
		MimeMessage message = mailSender.createMimeMessage();

		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
			helper.setFrom(mailProperties.getFrom());
			helper.setTo(new InternetAddress(toEmail, toName, StandardCharsets.UTF_8.name()));
			helper.setSubject(subject);
			helper.setText(htmlBody, true);
		} catch (Exception ex) {
			throw new MailPreparationException("Fail to build email message", ex);
		}

		// MailException must escape this adapter so Kafka's DefaultErrorHandler can retry.
		mailSender.send(message);
	}
}
