package com.algaworks.algashop.notifications.presentation.email;

import com.algaworks.algashop.notifications.application.email.SendEmailApplicationService;
import com.algaworks.algashop.notifications.application.email.command.SendEmailIntegrationCommand;
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
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void sendEmail(@RequestBody @Valid SendEmailIntegrationCommand command) {
		sendEmailApplicationService.send(command.toInput());
	}
}
