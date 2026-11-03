package com.algaworks.algashop.notifications.infrastructure.persistence.template;

import com.algaworks.algashop.notifications.application.template.query.EmailTemplateOutput;
import com.algaworks.algashop.notifications.application.template.query.EmailTemplateQueryService;
import com.algaworks.algashop.notifications.domain.model.template.EmailTemplate;
import com.algaworks.algashop.notifications.domain.model.template.EmailTemplateRepository;
import com.algaworks.algashop.notifications.domain.model.template.TemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailTemplateQueryServiceImpl implements EmailTemplateQueryService {
	private final EmailTemplateRepository emailTemplateRepository;

	@Override
	public EmailTemplateOutput findById(UUID id) {
		return emailTemplateRepository.findById(id)
				.map(this::toResponse)
				.orElseThrow(() -> new TemplateNotFoundException(id));
	}

	@Override
	public Page<EmailTemplateOutput> findAll(Pageable pageable) {
		return emailTemplateRepository.findAll(pageable)
				.map(this::toResponse);
	}

	private EmailTemplateOutput toResponse(EmailTemplate template) {
		return new EmailTemplateOutput(
			template.getId(), template.getName(), template.getSubject(),
			template.getBody(), template.getCreatedAt(), template.getUpdatedAt()
		);
	}
}
