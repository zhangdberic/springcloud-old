package com.sc.oauth2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

@Configuration
@EnableOAuth2Client
public class OAuthClientConfiguration {

	@Bean
	@Primary
	public OAuth2ProtectedResourceDetails oauth2RemoteResource() {
		ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();

		List<String> scopes = new ArrayList<String>(2);
		scopes.add(OauthClientTestConfig.OAUTH_CLIENT_SERVICE_SCOPE);
		scopes.add("web");
		resource.setAccessTokenUri(OauthClientTestConfig.OAUTH_GET_TOKEN_URL);
		resource.setClientId(OauthClientTestConfig.OAUTH_CLIENT_ID);
		resource.setClientSecret(OauthClientTestConfig.OAUTH_CLIENT_SECRET);
		resource.setGrantType("password");
		resource.setScope(scopes);

		resource.setUsername(OauthClientTestConfig.OAUTH_USERNAME);
		resource.setPassword(OauthClientTestConfig.OAUTH_PASSWORD);

		return resource;
	}

	@Bean
	public OAuth2ClientContext oauth2ClientContext() {
		return new DefaultOAuth2ClientContext(new DefaultAccessTokenRequest());
	}

	@Bean
	@Primary
	public OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext oauth2ClientContext,
			OAuth2ProtectedResourceDetails details) {
		OAuth2RestTemplate template = new OAuth2RestTemplate(details, oauth2ClientContext);
		return template;
	}
}
