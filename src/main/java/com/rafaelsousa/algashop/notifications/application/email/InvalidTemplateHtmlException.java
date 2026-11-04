package com.rafaelsousa.algashop.notifications.application.email;

import java.util.List;

public class InvalidTemplateHtmlException extends RuntimeException {

	public InvalidTemplateHtmlException(List<String> violations) {
		super("Invalid email template HTML: " + String.join("; ", violations));
	}
}
