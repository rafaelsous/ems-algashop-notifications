package com.rafaelsousa.algashop.notifications.domain.model.template;

import com.rafaelsousa.algashop.notifications.domain.model.AbstractAuditableAggregateRoot;
import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name = "email_template")
public class EmailTemplate extends AbstractAuditableAggregateRoot<EmailTemplate> {

	@Id
	private UUID id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private String subject;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String body;

	@Builder
	public EmailTemplate(UUID id, String name, String subject, String body) {
		this.id = Generators.timeBasedEpochRandomGenerator().generate();
		this.name = name;
		this.subject = subject;
		this.body = body;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || getClass() != object.getClass()) return false;
		EmailTemplate that = (EmailTemplate) object;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
