package com.rafaelsousa.algashop.notifications.presentation.template;

import com.rafaelsousa.algashop.notifications.application.template.management.EmailTemplateInput;
import com.rafaelsousa.algashop.notifications.application.template.management.EmailTemplateManagementApplicationService;
import com.rafaelsousa.algashop.notifications.application.template.query.EmailTemplateOutput;
import com.rafaelsousa.algashop.notifications.application.template.query.EmailTemplateQueryService;
import com.rafaelsousa.algashop.notifications.domain.model.template.TemplateNotFoundException;
import com.rafaelsousa.algashop.notifications.infrastructure.security.NotificationsSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = EmailTemplateController.class)
@Import(NotificationsSecurityConfig.class)
class EmailTemplateControllerMvcTest {
	private static final String READ_SCOPE = "email-templates:read";
	private static final String WRITE_SCOPE = "email-templates:write";
	private static final String TEMPLATE_URL = "/api/v1/email-templates";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmailTemplateQueryService queryService;

	@MockitoBean
	private EmailTemplateManagementApplicationService managementApplicationService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void givenReadScope_whenList_shouldReturnTemplates() throws Exception {
		UUID templateId = UUID.randomUUID();
		EmailTemplateOutput template = template(templateId);
		when(queryService.findAll(any())).thenReturn(new PageImpl<>(List.of(template)));

		mockMvc.perform(get(TEMPLATE_URL)
				.param("page", "0")
				.param("size", "20")
				.with(jwt().authorities(authority(READ_SCOPE))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].id").value(templateId.toString()))
			.andExpect(jsonPath("$.content[0].name").value("Order placed"))
			.andExpect(jsonPath("$.content[0].subject").value("Order confirmed"))
			.andExpect(jsonPath("$.content[0].body").value("<p>Order confirmed</p>"));

		verify(queryService).findAll(any());
	}

	@Test
	void givenExistingTemplateId_whenFindById_shouldReturnTemplate() throws Exception {
		UUID templateId = UUID.randomUUID();
		EmailTemplateOutput template = template(templateId);
		when(queryService.findById(templateId)).thenReturn(template);

		mockMvc.perform(get(TEMPLATE_URL + "/" + templateId)
				.with(jwt().authorities(authority(READ_SCOPE))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(templateId.toString()))
			.andExpect(jsonPath("$.name").value("Order placed"))
			.andExpect(jsonPath("$.subject").value("Order confirmed"))
			.andExpect(jsonPath("$.body").value("<p>Order confirmed</p>"));

		verify(queryService).findById(templateId);
	}

	@Test
	void givenInexistingTemplateId_whenFindById_shouldReturnNotFound() throws Exception {
		UUID templateId = UUID.randomUUID();
		when(queryService.findById(templateId)).thenThrow(new TemplateNotFoundException(templateId));

		mockMvc.perform(get(TEMPLATE_URL + "/" + templateId)
				.with(jwt().authorities(authority(READ_SCOPE))))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.title").value("Not found"))
			.andExpect(jsonPath("$.detail").value("Email template not found: " + templateId));

		verify(queryService).findById(templateId);
	}

	@Test
	void givenWriteScope_whenCreate_shouldReturnCreatedTemplate() throws Exception {
		UUID templateId = UUID.randomUUID();
		when(managementApplicationService.create(any(EmailTemplateInput.class)))
			.thenReturn(templateId);
		when(queryService.findById(templateId)).thenReturn(template(templateId));

		mockMvc.perform(post(TEMPLATE_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(validTemplateJson())
				.with(jwt().authorities(authority(WRITE_SCOPE))))
			.andExpect(status().isCreated())
			.andExpect(header().string(HttpHeaders.LOCATION, "http://localhost" + TEMPLATE_URL + "/" + templateId))
			.andExpect(jsonPath("$.id").value(templateId.toString()));

		verify(managementApplicationService).create(any(EmailTemplateInput.class));
		verify(queryService).findById(templateId);
	}

	@Test
	void givenWriteScope_whenUpdate_shouldReturnUpdatedTemplate() throws Exception {
		UUID templateId = UUID.randomUUID();
		when(queryService.findById(templateId)).thenReturn(template(templateId));

		mockMvc.perform(put(TEMPLATE_URL + "/" + templateId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(validTemplateJson())
				.with(jwt().authorities(authority(WRITE_SCOPE))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(templateId.toString()));

		verify(managementApplicationService).update(eq(templateId), any(EmailTemplateInput.class));
		verify(queryService).findById(templateId);
	}

	@Test
	void givenWriteScope_whenDelete_shouldReturnNoContent() throws Exception {
		UUID templateId = UUID.randomUUID();

		mockMvc.perform(delete(TEMPLATE_URL + "/" + templateId)
				.with(jwt().authorities(authority(WRITE_SCOPE))))
			.andExpect(status().isNoContent());

		verify(managementApplicationService).delete(templateId);
	}

	@Test
	void givenWrongScope_whenReadingTemplates_shouldReturnForbidden() throws Exception {
		mockMvc.perform(get(TEMPLATE_URL)
				.with(jwt().authorities(authority(WRITE_SCOPE))))
			.andExpect(status().isForbidden());

		verify(queryService, never()).findAll(any());
	}

	@Test
	void givenWrongScope_whenWritingTemplate_shouldReturnForbidden() throws Exception {
		UUID templateId = UUID.randomUUID();

		mockMvc.perform(put(TEMPLATE_URL + "/" + templateId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(validTemplateJson())
				.with(jwt().authorities(authority(READ_SCOPE))))
			.andExpect(status().isForbidden());

		verify(managementApplicationService, never()).update(any(), any());
	}

	@Test
	void givenInvalidTemplateInput_whenCreating_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(post(TEMPLATE_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"","subject":"","body":""}
					""")
				.with(jwt().authorities(authority(WRITE_SCOPE))))
			.andExpect(status().isBadRequest());

		verify(managementApplicationService, never()).create(any());
	}

	private EmailTemplateOutput template(UUID id) {
		return new EmailTemplateOutput(
			id,
			"Order placed",
			"Order confirmed",
			"<p>Order confirmed</p>",
			OffsetDateTime.parse("2026-01-01T10:00:00Z"),
			OffsetDateTime.parse("2026-01-01T10:00:00Z"));
	}

	private String validTemplateJson() {
		return """
			{"name":"Order placed","subject":"Order confirmed","body":"<p>Order confirmed</p>"}
			""";
	}

	private SimpleGrantedAuthority authority(String scope) {
		return new SimpleGrantedAuthority("SCOPE_" + scope);
	}
}
