package com.rafaelsousa.algashop.notifications.domain.model;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Getter
@Setter(AccessLevel.PRIVATE)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditableAggregateRoot<T extends AbstractAggregateRoot<T>>
		extends AbstractAggregateRoot<T> {

	@CreatedDate
	protected OffsetDateTime createdAt;

	@LastModifiedDate
	protected OffsetDateTime updatedAt;

	@Version
	protected Long version;

}
