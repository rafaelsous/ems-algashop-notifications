package com.algaworks.algashop.notifications.presentation.email;

import com.algaworks.algashop.notifications.application.email.SendEmailApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.algaworks.algashop.notifications.infrastructure.security.NotificationsSecurityConfig;

@WebMvcTest(controllers = EmailController.class)
@Import(NotificationsSecurityConfig.class)
class EmailControllerMvcTest {
	private static final String SEND_SCOPE = "emails:send";
	private static final String EMAIL_URL = "/api/v1/emails";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SendEmailApplicationService sendEmailApplicationService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void givenSendScope_whenSendingEmail_shouldReturnNoContent() throws Exception {
		mockMvc.perform(post(EMAIL_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(validEmailJson())
				.with(jwt().authorities(authority(SEND_SCOPE))))
			.andExpect(status().isNoContent());

		verify(sendEmailApplicationService).send(any());
	}

	@Test
	void givenWrongScope_whenSendingEmail_shouldReturnForbidden() throws Exception {
		mockMvc.perform(post(EMAIL_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content(validEmailJson())
				.with(jwt().authorities(authority("email-templates:read"))))
			.andExpect(status().isForbidden());

		verify(sendEmailApplicationService, never()).send(any());
	}

	@Test
	void givenInvalidEmailCommand_whenSendingEmail_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(post(EMAIL_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"recipient":{"email":"invalid-email","name":""},"templateId":null}
					""")
				.with(jwt().authorities(authority(SEND_SCOPE))))
			.andExpect(status().isBadRequest());

		verify(sendEmailApplicationService, never()).send(any());
	}

	private String validEmailJson() {
		return """
			{
			  "recipient": {
			    "email": "john@email.com",
			    "name": "John Doe"
			  },
			  "templateId": "%s",
			  "variables": {"orderId": "ORDER-123"}
			}
			""".formatted(UUID.randomUUID());
	}

	private SimpleGrantedAuthority authority(String scope) {
		return new SimpleGrantedAuthority("SCOPE_" + scope);
	}
}
