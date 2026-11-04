package com.rafaelsousa.algashop.notifications.application.template.management;

import com.rafaelsousa.algashop.notifications.application.email.EmailTemplateHtmlValidator;
import com.rafaelsousa.algashop.notifications.application.email.EmailTemplateRenderer;
import com.rafaelsousa.algashop.notifications.domain.model.template.EmailTemplate;
import com.rafaelsousa.algashop.notifications.domain.model.template.EmailTemplateRepository;
import com.rafaelsousa.algashop.notifications.domain.model.template.TemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailTemplateManagementApplicationService {
	private final EmailTemplateRenderer renderer;
	private final EmailTemplateRepository repository;
	private final EmailTemplateHtmlValidator validator;

	@Transactional
	public UUID create(EmailTemplateInput input) {
		validateTemplate(input);

		EmailTemplate emailTemplate = repository.saveAndFlush(
			EmailTemplate.builder()
				.name(input.name())
				.subject(input.subject())
				.body(input.body())
			.build()
		);

		return emailTemplate.getId();
	}

	@Transactional
	public void update(UUID id, EmailTemplateInput input) {
		EmailTemplate emailTemplate = findOrFail(id);
		validateTemplate(input);

		emailTemplate.setName(input.name());
		emailTemplate.setSubject(input.subject());
		emailTemplate.setBody(input.body());

		repository.saveAndFlush(emailTemplate);
	}

	@Transactional
	public void delete(UUID id) {
		EmailTemplate emailTemplate = findOrFail(id);

		repository.delete(emailTemplate);
	}

	private EmailTemplate findOrFail(UUID id) {
		return repository.findById(id)
			.orElseThrow(() -> new TemplateNotFoundException(id));
	}

	private void validateTemplate(EmailTemplateInput input) {
		renderer.assertRenderable(input.subject(), input.body());
		validator.validateSyntax(input.body());
	}
}
