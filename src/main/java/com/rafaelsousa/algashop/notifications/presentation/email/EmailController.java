package com.rafaelsousa.algashop.notifications.presentation.email;

import com.rafaelsousa.algashop.notifications.application.email.SendEmailApplicationService;
import com.rafaelsousa.algashop.notifications.application.email.command.SendEmailIntegrationCommand;
import com.rafaelsousa.algashop.notifications.infrastructure.security.check.SecurityAnnotations.CanSendEmails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/emails")
public class EmailController {
	private final SendEmailApplicationService sendEmailApplicationService;

	@PostMapping
	@CanSendEmails
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void sendEmail(@RequestBody @Valid SendEmailIntegrationCommand command) {
		sendEmailApplicationService.send(command.toInput());
	}
}
