package com.rafaelsousa.algashop.notifications.infrastructure.template;

import com.rafaelsousa.algashop.notifications.application.email.InvalidTemplateHtmlException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EmailTemplateHtmlValidatorJsoupImplTest {
	private final EmailTemplateHtmlValidatorJsoupImpl validator = new EmailTemplateHtmlValidatorJsoupImpl();

	@Test
	void givenWellFormedHtml_whenValidateSyntax_shouldNotThrowErrors() {
		assertThatCode(() -> validator.validateSyntax("<h1>Hello</h1>"))
				.doesNotThrowAnyException();
	}

	@Test
	void givenMalFormedHtml_whenValidateSyntax_shouldThrowInvalidTemplateException() {
		assertThatExceptionOfType(InvalidTemplateHtmlException.class)
				.isThrownBy(() -> validator.validateSyntax("<div><h1>Hello</div>"))
				.withMessageContaining("Malformed HTML");
	}

	@Test
	void givenEmptyHtml_whenValidateSyntax_shouldThrowIllegalArgumentException() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> validator.validateSyntax(""))
				.withMessageContaining("HTML template cannot be empty");
	}

	@Test
	void givenHtmlWithScript_whenValidateSyntax_shouldThrowInvalidTemplateException() {
		assertThatExceptionOfType(InvalidTemplateHtmlException.class)
				.isThrownBy(() -> validator.validateSyntax("<script>alert('XSS')</script>"))
				.withMessageContaining("Forbidden tag <script> found in HTML template");
	}

	@Test
	void givenHtmlWithInlineEvenHandler_whenValidateSyntax_shouldThrowInvalidTemplateException() {
		assertThatExceptionOfType(InvalidTemplateHtmlException.class)
				.isThrownBy(() -> validator.validateSyntax("<a href='http://example.com' onclick='alert(\"XSS\")'>Click me</a>"))
				.withMessageContaining("Event attribute 'onclick' on tag <a> is not allowed");
	}

	@Test
	void givenHtmlWithJavascriptUrl_whenValidateSyntax_shouldThrowInvalidTemplateException() {
		assertThatExceptionOfType(InvalidTemplateHtmlException.class)
				.isThrownBy(() -> validator.validateSyntax("<a href='javascript:alert(\"XSS\")'>Click me</a>"))
				.withMessageContaining("Attribute 'href' on tag <a> has a forbidden URL scheme");
	}

	@Test
	void givenHtmlWithValidHref_whenValidateSyntax_shouldNotThrowErrors() {
		assertThatCode(() -> validator.validateSyntax("<a href='http://example.com'>Click me</a>"))
				.doesNotThrowAnyException();
	}

	@Test
	void givenHtmlWithThymeleafAttribute_whenValidateSyntax_shouldNotThrowErrors() {
		assertThatCode(() -> validator.validateSyntax("<a href='http://example.com' th:onclick='alert(\"XSS\")'>Click me</a>"))
				.doesNotThrowAnyException();
	}
}
