package com.algaworks.algashop.notifications.application.template.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EmailTemplateQueryService {
	EmailTemplateOutput findById(UUID id);
	Page<EmailTemplateOutput> findAll(Pageable pageable);
}
