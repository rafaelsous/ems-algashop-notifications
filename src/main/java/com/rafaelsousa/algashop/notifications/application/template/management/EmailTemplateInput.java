package com.rafaelsousa.algashop.notifications.application.template.management;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailTemplateInput(
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Size(max = 255) String subject,
		@NotBlank String body) {
}
