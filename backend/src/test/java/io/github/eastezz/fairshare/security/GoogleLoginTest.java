package io.github.eastezz.fairshare.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * "Sign in with Google" with a made-up client id (no real Google account or secret needed).
 * Checks that the login sends the browser to Google correctly, and what happens when Google sends it back.
 */
@SpringBootTest(properties = { "app.google.client-id=test-client-id", "app.google.client-secret=test-secret" })
@AutoConfigureMockMvc
class GoogleLoginTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GoogleLoginSuccessHandler successHandler;

	@Test
	void googleIsSwitchedOnWithClientId() throws Exception {
		mockMvc.perform(get("/api/auth/providers")).andExpect(jsonPath("$.google").value(true));
	}

	@Test
	void startingTheLoginSendsTheBrowserToGoogle() throws Exception {
		mockMvc.perform(get("/oauth2/authorization/google"))
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string("Location", startsWith("https://accounts.google.com/o/oauth2/v2/auth")))
			.andExpect(header().string("Location", containsString("client_id=test-client-id")))
			.andExpect(header().string("Location",
					containsString("redirect_uri=http://localhost/login/oauth2/code/google")))
			.andExpect(header().string("Location", containsString("scope=openid%20email%20profile")));
	}

	@Test
	void successfulGoogleLoginLogsInOurOwnAccount() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		successHandler.onAuthenticationSuccess(request, response, googleLogin("g-handler", "Ida.Google@Gmail.com", true));

		assertThat(response.getRedirectedUrl()).isEqualTo("/");
		// The session now holds our account; "who am I" answers with our normalized email.
		MockHttpSession session = (MockHttpSession) request.getSession(false);
		mockMvc.perform(get("/api/auth/me").session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("ida.google@gmail.com"));
	}

	@Test
	void unverifiedGoogleEmailGoesBackToTheLoginPage() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		successHandler.onAuthenticationSuccess(request, response, googleLogin("g-unverified-2", "x@example.de", false));

		assertThat(response.getRedirectedUrl()).isEqualTo("/login?error=google");
	}

	/** What Spring hands over after Google confirmed the person: an ID token with these claims. */
	private static OAuth2AuthenticationToken googleLogin(String subject, String email, boolean emailVerified) {
		OidcIdToken idToken = new OidcIdToken("token-value", Instant.now(), Instant.now().plusSeconds(60),
				Map.of("sub", subject, "email", email, "email_verified", emailVerified));
		DefaultOidcUser user = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("OIDC_USER")), idToken);
		return new OAuth2AuthenticationToken(user, user.getAuthorities(), "google");
	}

}