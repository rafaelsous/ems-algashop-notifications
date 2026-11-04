package com.rafaelsousa.algashop.notifications.domain.model;

public class DomainEntityNotFoundException extends RuntimeException {

	public DomainEntityNotFoundException(String message) {
		super(message);
	}
}
