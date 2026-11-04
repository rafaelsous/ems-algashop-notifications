package com.rafaelsousa.algashop.notifications.application.email.command;

import com.rafaelsousa.algashop.notifications.application.email.SendEmailInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailIntegrationCommand {

	@Valid
	@NotNull
	private Recipient recipient;

	@NotNull
	private UUID templateId;

	@Builder.Default
	private Map<String, Object> variables = new HashMap<>() {};

	public SendEmailInput toInput() {
		return new SendEmailInput(
			recipient.getEmail(),
			recipient.getName(),
			templateId,
			variables
		);
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Recipient {

		@NotBlank @Email @Size(max = 254)
		private String email;

		@NotBlank
		private String name;
	}
}
