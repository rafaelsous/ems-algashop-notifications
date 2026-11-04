package com.rafaelsousa.algashop.notifications.application.template.management;

import com.rafaelsousa.algashop.notifications.application.email.EmailTemplateHtmlValidator;
import com.rafaelsousa.algashop.notifications.application.email.EmailTemplateRenderer;
import com.rafaelsousa.algashop.notifications.domain.model.template.EmailTemplate;
import com.rafaelsousa.algashop.notifications.domain.model.template.EmailTemplateRepository;
import com.rafaelsousa.algashop.notifications.domain.model.template.TemplateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailTemplateManagementApplicationServiceTest {
	private static final String TEMPLATE_NAME = "Order placed";
	private static final String SUBJECT = "Order [[${orderId}]] placed";
	private static final String BODY = "<p>Order [[${orderId}]] was placed</p>";

	private final EmailTemplateRenderer renderer = mock(EmailTemplateRenderer.class);
	private final EmailTemplateRepository repository = mock(EmailTemplateRepository.class);
	private final EmailTemplateHtmlValidator validator = mock(EmailTemplateHtmlValidator.class);

	private EmailTemplateManagementApplicationService service;

	@BeforeEach
	void setUp() {
		service = new EmailTemplateManagementApplicationService(renderer, repository, validator);
	}

	@Test
	void givenValidInput_whenCreate_shouldValidateSaveAndReturnId() {
		EmailTemplateInput input = new EmailTemplateInput(TEMPLATE_NAME, SUBJECT, BODY);
		EmailTemplate savedTemplate = new EmailTemplate();
		when(repository.saveAndFlush(any(EmailTemplate.class))).thenReturn(savedTemplate);

		UUID result = service.create(input);

		assertThat(result).isEqualTo(savedTemplate.getId());
		verify(renderer).assertRenderable(SUBJECT, BODY);
		verify(validator).validateSyntax(BODY);

		var templateCaptor = org.mockito.ArgumentCaptor.forClass(EmailTemplate.class);
		verify(repository).saveAndFlush(templateCaptor.capture());
		assertThat(templateCaptor.getValue().getName()).isEqualTo(TEMPLATE_NAME);
		assertThat(templateCaptor.getValue().getSubject()).isEqualTo(SUBJECT);
		assertThat(templateCaptor.getValue().getBody()).isEqualTo(BODY);
	}

	@Test
	void givenExistingTemplateAndValidInput_whenUpdate_shouldUpdateValidateAndSave() {
		UUID id = UUID.randomUUID();
		EmailTemplate template = new EmailTemplate();
		EmailTemplateInput input = new EmailTemplateInput(TEMPLATE_NAME, SUBJECT, BODY);
		when(repository.findById(id)).thenReturn(Optional.of(template));

		service.update(id, input);

		assertThat(template.getName()).isEqualTo(TEMPLATE_NAME);
		assertThat(template.getSubject()).isEqualTo(SUBJECT);
		assertThat(template.getBody()).isEqualTo(BODY);
		verify(renderer).assertRenderable(SUBJECT, BODY);
		verify(validator).validateSyntax(BODY);
		verify(repository).saveAndFlush(template);
	}

	@Test
	void givenNonExistingTemplate_whenUpdate_shouldThrowTemplateNotFoundException() {
		UUID id = UUID.randomUUID();
		EmailTemplateInput input = new EmailTemplateInput(TEMPLATE_NAME, SUBJECT, BODY);
		when(repository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.update(id, input))
			.isInstanceOf(TemplateNotFoundException.class)
			.hasMessage("Email template not found: " + id);

		verifyNoInteractions(renderer, validator);
		verify(repository, never()).saveAndFlush(any());
	}

	@Test
	void givenExistingTemplate_whenDelete_shouldDeleteTemplate() {
		UUID id = UUID.randomUUID();
		EmailTemplate template = new EmailTemplate();
		when(repository.findById(id)).thenReturn(Optional.of(template));

		service.delete(id);

		verify(repository).delete(template);
	}

	@Test
	void givenNonExistingTemplate_whenDelete_shouldThrowTemplateNotFoundException() {
		UUID id = UUID.randomUUID();
		when(repository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete(id))
			.isInstanceOf(TemplateNotFoundException.class)
			.hasMessage("Email template not found: " + id);

		verify(repository, never()).delete(any(EmailTemplate.class));
	}

	@Test
	void givenInvalidRenderableTemplate_whenCreate_shouldNotSaveTemplate() {
		EmailTemplateInput input = new EmailTemplateInput(TEMPLATE_NAME, SUBJECT, BODY);
		RuntimeException exception = new RuntimeException("Invalid template");
		doThrow(exception).when(renderer).assertRenderable(SUBJECT, BODY);

		assertThatThrownBy(() -> service.create(input))
			.isSameAs(exception);

		verifyNoInteractions(validator);
		verify(repository, never()).saveAndFlush(any(EmailTemplate.class));
	}

	@Test
	void givenInvalidHtmlTemplate_whenCreate_shouldNotSaveTemplate() {
		EmailTemplateInput input = new EmailTemplateInput(TEMPLATE_NAME, SUBJECT, BODY);
		RuntimeException exception = new RuntimeException("Invalid HTML");
		doThrow(exception).when(validator).validateSyntax(BODY);

		assertThatThrownBy(() -> service.create(input))
			.isSameAs(exception);

		verify(renderer).assertRenderable(SUBJECT, BODY);
		verify(repository, never()).saveAndFlush(any(EmailTemplate.class));
	}
}
