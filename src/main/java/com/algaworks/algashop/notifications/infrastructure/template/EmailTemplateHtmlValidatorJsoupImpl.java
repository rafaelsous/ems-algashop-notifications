package com.algaworks.algashop.notifications.infrastructure.template;

import com.algaworks.algashop.notifications.application.email.EmailTemplateHtmlValidator;

import com.algaworks.algashop.notifications.application.email.InvalidTemplateHtmlException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EmailTemplateHtmlValidatorJsoupImpl implements EmailTemplateHtmlValidator {
	private static final int MAX_VIOLATIONS = 10;
	private static final Set<String> FORBIDDEN_TAGS = Set.of("script", "iframe", "object", "embed");
	private static final Set<String> URL_ATTRIBUTES = Set.of("src", "href", "action", "formaction");
	private static final Set<String> FORBIDDEN_URL_SCHEMES = Set.of("javascript:", "vbscript:", "data:", "about:");

	@Override
	public void validateSyntax(String html) {
		Objects.requireNonNull(html);

		if (html.isEmpty()) {
			throw new IllegalArgumentException("HTML template cannot be empty");
		}

		Parser parser = Parser.htmlParser().setTrackErrors(MAX_VIOLATIONS);
		Document document = Jsoup.parse(html, parser);

		List<String> violations = new ArrayList<>();
		parser.getErrors().forEach(error -> {
		    violations.add(String.format("Malformed HTML at position %s: %s",
			    error.getPosition(), error.getErrorMessage()
		    ));
		});

		document.getAllElements().forEach(element -> checkElement(element, violations));

		if (!violations.isEmpty()) {
			throw new InvalidTemplateHtmlException(violations);
		}
	}

	private void checkElement(Element element, List<String> violations) {
		String tag = element.normalName();
		if (FORBIDDEN_TAGS.contains(tag)) {
			violations.add(String.format("Forbidden tag <%s> found in HTML template", tag));
		}

		element.attributes().forEach(attribute -> {
			String attrKey = attribute.getKey().toLowerCase(Locale.ROOT);
			if (attrKey.startsWith("on")) {
				violations.add(String.format("Event attribute '%s' on tag <%s> is not allowed", attrKey, tag));
			} else if (URL_ATTRIBUTES.contains(attrKey) && hasForbiddenScheme(attribute.getValue())) {
				violations.add(String.format("Attribute '%s' on tag <%s> has a forbidden URL scheme", attrKey, tag));
			}
		});
	}

	private boolean hasForbiddenScheme(String value) {
		String normalized = value.toLowerCase(Locale.ROOT);
		return FORBIDDEN_URL_SCHEMES.stream().anyMatch(normalized::startsWith);
	}
}
