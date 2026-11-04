package com.rafaelsousa.algashop.notifications.infrastructure.security.check;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SecurityAnnotations {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@PreAuthorize("hasAuthority('SCOPE_emails:send')")
	public @interface CanSendEmails {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@PreAuthorize("hasAuthority('SCOPE_email-templates:read')")
	public @interface CanReadEmailTemplates {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.TYPE})
	@PreAuthorize("hasAuthority('SCOPE_email-templates:write')")
	public @interface CanWriteEmailTemplates {}
}
