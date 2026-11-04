package com.rafaelsousa.algashop.notifications.application.email;

import java.util.Map;

public interface EmailTemplateRenderer {

	String renderSubject(String subjectTemplate, Map<String, Object> variables);

	String renderBody(String bodyTemplate, Map<String, Object> variables);

	void assertRenderable(String subjectTemplate, String bodyTemplate);
}
