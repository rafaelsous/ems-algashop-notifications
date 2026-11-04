package com.rafaelsousa.algashop.notifications.application.template.query;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmailTemplateOutput(
		UUID id,
		String name,
		String subject,
		String body,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

}
