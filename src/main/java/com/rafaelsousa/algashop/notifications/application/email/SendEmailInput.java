package com.rafaelsousa.algashop.notifications.application.email;

import java.util.Map;
import java.util.UUID;

public record SendEmailInput(
		String recipientEmail,
		String recipientName,
		UUID templateId,
		Map<String, Object> variables) {
}
