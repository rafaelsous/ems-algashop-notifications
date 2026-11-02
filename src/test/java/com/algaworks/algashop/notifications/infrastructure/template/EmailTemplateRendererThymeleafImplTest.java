package com.algaworks.algashop.notifications.infrastructure.template;

import com.algaworks.algashop.notifications.application.email.TemplateRenderException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class EmailTemplateRendererThymeleafImplTest {
	private final EmailTemplateRendererThymeleafImpl renderer = new EmailTemplateRendererThymeleafImpl();

	@Test
	void givenSubjectTemplateWithVariable_whenRender_shouldReplaceVariable() {
		String subject = renderer.renderSubject("Order [[${orderId}]] confirmed", Map.of("orderId", "0S3Y46J6QDYD4"));

		assertThat(subject).isEqualTo("Order 0S3Y46J6QDYD4 confirmed");
	}

	@Test
	void givenBodyTemplateWithNestedVariable_whenRender_shouldReplaceVariable() {
		String body = renderer.renderBody("<p>[[${name}]]</p>", Map.of("name", "John Doe"));

		assertThat(body).isEqualTo("<p>John Doe</p>");
	}

	@Test
	void givenBodyTemplateWithNestedAttributeVariable_whenRender_shouldReplaceVariable() {
		String body = renderer.renderBody(
			"<p th:text='${recipient.name}'>Rafael</p>",
			Map.of("recipient", Map.of("name", "John Doe"))
		);

		assertThat(body).isEqualTo("<p>John Doe</p>");
	}

	@Test
	void givenMissingVariable_whenRender_shouldReturnEmptyString() {
		String body = renderer.renderSubject("Hi [[${name}]]!", Map.of());

		assertThat(body).isEqualTo("Hi !");
	}

	@Test
	void givenTemplateEvaluateWithVariable_whenAssertRenderable_shouldNotThrowErrors() {
		assertThatCode(() -> renderer.assertRenderable(
			"Hi [[${name}]]!",
			"<p th:text='${name}'>John Doe</p>"))
				.doesNotThrowAnyException();
	}

	@Test
	void givenTemplateWithInvalidSyntax_whenAssertRenderable_shouldThrowException() {
		assertThatExceptionOfType(TemplateRenderException.class)
				.isThrownBy(() -> renderer.assertRenderable("Hi", "<p th:text='${'>John Doe</p>"));
	}
}
