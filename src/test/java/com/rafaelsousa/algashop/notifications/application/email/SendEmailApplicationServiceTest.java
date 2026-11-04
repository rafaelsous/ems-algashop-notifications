package com.rafaelsousa.algashop.notifications.application.email;

import com.rafaelsousa.algashop.notifications.domain.model.template.EmailTemplate;
import com.rafaelsousa.algashop.notifications.domain.model.template.EmailTemplateRepository;
import com.rafaelsousa.algashop.notifications.domain.model.template.TemplateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SendEmailApplicationServiceTest {
	private static final String RECIPIENT_EMAIL = "john@email.com";
	private static final String RECIPIENT_NAME = "John Doe";
	private static final String SUBJECT_TEMPLATE = "Order [[${orderId}]] confirmed";
	private static final String BODY_TEMPLATE = "<p>Order [[${orderId}]] confirmed</p>";

	private final EmailSender emailSender = mock(EmailSender.class);
	private final EmailTemplateRenderer renderer = mock(EmailTemplateRenderer.class);
	private final EmailTemplateRepository emailTemplateRepository = mock(EmailTemplateRepository.class);

	private SendEmailApplicationService service;

	@BeforeEach
	void setUp() {
		service = new SendEmailApplicationService(
			emailSender,
			renderer,
			emailTemplateRepository);
	}

	@Test
	@SuppressWarnings("unchecked")
	void givenExistingTemplate_whenSend_shouldRenderAndSendEmail() {
		UUID templateId = UUID.randomUUID();
		EmailTemplate template = template();
		Map<String, Object> inputVariables = Map.of("orderId", "ORDER-123");
		SendEmailInput input = new SendEmailInput(
			RECIPIENT_EMAIL, RECIPIENT_NAME, templateId, inputVariables);

		when(emailTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));
		when(renderer.renderSubject(eq(SUBJECT_TEMPLATE), anyMap())).thenReturn("Order ORDER-123 confirmed");
		when(renderer.renderBody(eq(BODY_TEMPLATE), anyMap())).thenReturn("<p>Order ORDER-123 confirmed</p>");

		service.send(input);

		ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
		verify(renderer).renderSubject(eq(SUBJECT_TEMPLATE), variablesCaptor.capture());
		verify(renderer).renderBody(eq(BODY_TEMPLATE), same(variablesCaptor.getValue()));
		verify(emailSender).send(
			RECIPIENT_EMAIL,
			RECIPIENT_NAME,
			"Order ORDER-123 confirmed",
			"<p>Order ORDER-123 confirmed</p>");

		assertThat(variablesCaptor.getValue())
			.containsEntry("orderId", "ORDER-123")
			.containsEntry("recipient", Map.of(
				"name", RECIPIENT_NAME,
				"email", RECIPIENT_EMAIL));
	}

	@Test
	@SuppressWarnings("unchecked")
	void givenNullVariables_whenSend_shouldBuildVariablesWithRecipientOnly() {
		UUID templateId = UUID.randomUUID();
		EmailTemplate template = template();
		SendEmailInput input = new SendEmailInput(
			RECIPIENT_EMAIL, RECIPIENT_NAME, templateId, null);

		when(emailTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));
		when(renderer.renderSubject(eq(SUBJECT_TEMPLATE), anyMap())).thenReturn("Subject");
		when(renderer.renderBody(eq(BODY_TEMPLATE), anyMap())).thenReturn("Body");

		service.send(input);

		ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
		verify(renderer).renderSubject(eq(SUBJECT_TEMPLATE), variablesCaptor.capture());
		assertThat(variablesCaptor.getValue())
			.containsExactly(
				org.assertj.core.api.Assertions.entry("recipient", Map.of(
					"name", RECIPIENT_NAME,
					"email", RECIPIENT_EMAIL)));
	}

	@Test
	@SuppressWarnings("unchecked")
	void givenInputRecipientVariable_whenSend_shouldUseInputRecipientData() {
		UUID templateId = UUID.randomUUID();
		EmailTemplate template = template();
		SendEmailInput input = new SendEmailInput(
			RECIPIENT_EMAIL,
			RECIPIENT_NAME,
			templateId,
			Map.of("recipient", Map.of("name", "Another Person")));

		when(emailTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));
		when(renderer.renderSubject(eq(SUBJECT_TEMPLATE), anyMap())).thenReturn("Subject");
		when(renderer.renderBody(eq(BODY_TEMPLATE), anyMap())).thenReturn("Body");

		service.send(input);

		ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
		verify(renderer).renderSubject(eq(SUBJECT_TEMPLATE), variablesCaptor.capture());

		assertThat(variablesCaptor.getValue()).containsEntry("recipient", Map.of(
				"name", RECIPIENT_NAME,
				"email", RECIPIENT_EMAIL));
	}

	@Test
	void givenNonExistingTemplate_whenSend_shouldThrowTemplateNotFoundException() {
		UUID templateId = UUID.randomUUID();
		SendEmailInput input = new SendEmailInput(
			RECIPIENT_EMAIL, RECIPIENT_NAME, templateId, Map.of("orderId", "ORDER-123"));
		when(emailTemplateRepository.findById(templateId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.send(input))
			.isInstanceOf(TemplateNotFoundException.class)
			.hasMessage("Email template not found: " + templateId);

		verifyNoInteractions(renderer, emailSender);
	}

	@Test
	void givenSubjectRenderingFailure_whenSend_shouldNotSendEmail() {
		UUID templateId = UUID.randomUUID();
		EmailTemplate template = template();
		SendEmailInput input = new SendEmailInput(
			RECIPIENT_EMAIL, RECIPIENT_NAME, templateId, Map.of("orderId", "ORDER-123"));
		TemplateRenderException exception =
			new TemplateRenderException("Unable to render subject", new RuntimeException());

		when(emailTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));
		when(renderer.renderSubject(eq(SUBJECT_TEMPLATE), anyMap())).thenThrow(exception);

		assertThatThrownBy(() -> service.send(input))
			.isSameAs(exception);

		verify(renderer, never()).renderBody(anyString(), anyMap());
		verifyNoInteractions(emailSender);
	}

	private EmailTemplate template() {
		return EmailTemplate.builder()
			.name("Order placed")
			.subject(SUBJECT_TEMPLATE)
			.body(BODY_TEMPLATE)
			.build();
	}
}
