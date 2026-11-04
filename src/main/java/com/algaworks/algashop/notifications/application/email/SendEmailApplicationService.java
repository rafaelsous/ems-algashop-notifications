package com.algaworks.algashop.notifications.application.email;

import com.algaworks.algashop.notifications.domain.model.template.EmailTemplate;
import com.algaworks.algashop.notifications.domain.model.template.EmailTemplateRepository;
import com.algaworks.algashop.notifications.domain.model.template.TemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailApplicationService {
	private final EmailSender emailSender;
	private final EmailTemplateRenderer renderer;
	private final EmailTemplateRepository emailTemplateRepository;

	public void send(SendEmailInput input) {
		EmailTemplate template = emailTemplateRepository
			.findById(input.templateId())
			.orElseThrow(() -> new TemplateNotFoundException(input.templateId()));

		Map<String, Object> variables = buildVariables(input);
		String subject = renderer.renderSubject(template.getSubject(), variables);
		String body = renderer.renderBody(template.getBody(), variables);

		emailSender.send(input.recipientEmail(), input.recipientName(), subject, body);

		log.info("Email sent with template {}", template.getId());
	}

	private Map<String, Object> buildVariables(SendEmailInput input) {
		HashMap<String, Object> variables = new HashMap<>();

		if (input.variables() != null) {
			variables.putAll(input.variables());
		}

		HashMap<String, Object> recipient = new HashMap<>();
		variables.put("recipient", recipient);
		recipient.put("name", input.recipientName());
		recipient.put("email", input.recipientEmail());

		return variables;
	}
}
