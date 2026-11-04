package com.rafaelsousa.algashop.notifications.infrastructure.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class SpringDataAuditingConfig {

	@Bean
	public DateTimeProvider auditingDateTimeProvider() {
		return () -> Optional.of(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));
	}

}
