package com.algaworks.algashop.notifications.presentation.template;

import com.algaworks.algashop.notifications.application.template.management.EmailTemplateInput;
import com.algaworks.algashop.notifications.application.template.management.EmailTemplateManagementApplicationService;
import com.algaworks.algashop.notifications.application.template.query.EmailTemplateOutput;
import com.algaworks.algashop.notifications.application.template.query.EmailTemplateQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email-templates")
public class EmailTemplateController {
	private final EmailTemplateQueryService queryService;
	private final EmailTemplateManagementApplicationService managementApplicationService;

	@GetMapping
	public PagedModel<EmailTemplateOutput> list(Pageable pageable) {
		return new PagedModel<>(queryService.findAll(pageable));
	}

	@GetMapping("{id}")
	public EmailTemplateOutput findById(@PathVariable UUID id) {
		return queryService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<EmailTemplateOutput> create(@RequestBody @Valid EmailTemplateInput input) {
		UUID templateId = managementApplicationService.create(input);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(templateId)
			.toUri();

		return ResponseEntity.created(location).body(queryService.findById(templateId));
	}

	@PutMapping("{id}")
	public EmailTemplateOutput update(@PathVariable UUID id, @RequestBody @Valid EmailTemplateInput input) {
		managementApplicationService.update(id, input);
		return queryService.findById(id);
	}

	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
       managementApplicationService.delete(id);
	}
}
