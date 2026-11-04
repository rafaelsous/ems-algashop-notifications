package com.rafaelsousa.algashop.notifications.application.email;

public interface EmailSender {
	void send(String toEmail, String toName, String subject, String htmlBody);
}
