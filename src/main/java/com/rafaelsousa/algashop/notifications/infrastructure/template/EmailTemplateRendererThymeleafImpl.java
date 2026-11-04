package com.rafaelsousa.algashop.notifications.infrastructure.template;

import com.rafaelsousa.algashop.notifications.application.email.EmailTemplateRenderer;
import com.rafaelsousa.algashop.notifications.application.email.TemplateRenderException;
import org.springframework.expression.EvaluationException;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateEngineException;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Locale;
import java.util.Map;

@Component
public class EmailTemplateRendererThymeleafImpl implements EmailTemplateRenderer {
	private static final Locale LOCALE_US = Locale.US;

	private final TemplateEngine subjectTemplateEngine = createEngine(TemplateMode.TEXT);
	private final TemplateEngine bodyTemplateEngine = createEngine(TemplateMode.HTML);

	@Override
	public String renderSubject(String subjectTemplate, Map<String, Object> variables) {
		return process(variables, subjectTemplateEngine, subjectTemplate);
	}

	@Override
	public String renderBody(String bodyTemplate, Map<String, Object> variables) {
		return process(variables, bodyTemplateEngine, bodyTemplate);
	}

	@Override
	public void assertRenderable(String subjectTemplate, String bodyTemplate) {
		try {
			renderSubject(subjectTemplate, Map.of());
			renderBody(bodyTemplate, Map.of());
		} catch (TemplateRenderException ex) {
			if (!isEvaluationFailure(ex)) {
				throw ex;
			}
		}
	}

	private boolean isEvaluationFailure(TemplateRenderException ex) {
		for (Throwable t = ex; t != null; t = t.getCause()) {
			if (t instanceof EvaluationException) {
				return true;
			}
		}

		return false;
	}

	private String process(Map<String, Object> variables, TemplateEngine engine, String template) {
		try {
			Context context = new Context(LOCALE_US, variables);
			return engine.process(template, context);
		} catch (TemplateEngineException ex) {
			throw new TemplateRenderException("Failed to render email " + ex.getMessage(), ex);
		}
	}

	private static TemplateEngine createEngine(TemplateMode templateMode) {
		StringTemplateResolver resolver = new StringTemplateResolver();
		resolver.setTemplateMode(templateMode);
		resolver.setCacheable(false);

		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(resolver);

		return engine;
	}
}
