package io.github.eastezz.fairshare.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * Registers Google as a login provider, but only when a Google client id is configured.
 * Without GOOGLE_CLIENT_ID (tests, CI, a fresh clone) this class is skipped and the app
 * works exactly as before, just without the "Sign in with Google" button.
 */
@Configuration
@ConditionalOnExpression("!'${app.google.client-id:}'.isBlank()")
public class GoogleLoginConfig {

	@Bean
	ClientRegistrationRepository clientRegistrationRepository(@Value("${app.google.client-id}") String clientId,
			@Value("${app.google.client-secret}") String clientSecret) {
		// CommonOAuth2Provider already knows Google's addresses; we only add our client and the scopes.
		// The redirect address is {baseUrl}/login/oauth2/code/google, as registered in Google Cloud.
		ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
			.clientId(clientId)
			.clientSecret(clientSecret)
			.scope("openid", "email", "profile")
			.build();
		return new InMemoryClientRegistrationRepository(google);
	}

}