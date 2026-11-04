package com.rafaelsousa.algashop.notifications.infrastructure.mail;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "algashop.notifications.mail")
public class MailProperties {

	@NotBlank
	private String from;
}
