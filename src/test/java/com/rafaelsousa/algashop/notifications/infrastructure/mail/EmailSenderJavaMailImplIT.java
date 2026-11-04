package com.rafaelsousa.algashop.notifications.infrastructure.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;

@Disabled
class EmailSenderJavaMailImplIT {
	private static final int SMTP_PORT = 1025;
	private static final String MAILPIT_HOST = "localhost";

	private EmailSenderJavaMailImpl emailSender;

	@BeforeEach
	void setup() {
		MailProperties mailProperties = new MailProperties();
		mailProperties.setFrom("no-reply@alshop.local");

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setPort(SMTP_PORT);
		mailSender.setHost(MAILPIT_HOST);

		emailSender = new EmailSenderJavaMailImpl(mailSender, mailProperties);
	}

	@Test
	void givenValidData_whenSend_shouldNotGenerateErrors() {
		String toEmail = "john-" + UUID.randomUUID() + "@example.com";

		assertThatNoException().isThrownBy(() -> emailSender
			.send(toEmail, "John Doe", "Order confirmed", "<h1>Hello, John!</h1>"));
    }
}