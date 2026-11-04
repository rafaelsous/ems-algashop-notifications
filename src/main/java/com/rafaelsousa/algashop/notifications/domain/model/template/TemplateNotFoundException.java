package com.rafaelsousa.algashop.notifications.domain.model.template;

import com.rafaelsousa.algashop.notifications.domain.model.DomainEntityNotFoundException;

import java.util.UUID;

public class TemplateNotFoundException extends DomainEntityNotFoundException {

	public TemplateNotFoundException(UUID templateId) {
		super("Email template not found: " + templateId);
	}
}
