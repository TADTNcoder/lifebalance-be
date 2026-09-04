package com.lifebalance.gateway.security;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public final class IdentityAccountStatusTokenValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error ACCOUNT_INACTIVE = new OAuth2Error(
            "invalid_token",
            "The user account is inactive or the session has been revoked",
            null
    );
    private static final OAuth2Error VALIDATION_UNAVAILABLE = new OAuth2Error(
            "temporarily_unavailable",
            "The account status validation service is unavailable",
            null
    );

    private final RestClient restClient;
    private final URI validationUrl;

    public IdentityAccountStatusTokenValidator(RestClient restClient, URI validationUrl) {
        this.restClient = restClient;
        this.validationUrl = validationUrl;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt == null || jwt.getTokenValue() == null || jwt.getTokenValue().isBlank()) {
            return OAuth2TokenValidatorResult.failure(ACCOUNT_INACTIVE);
        }

        try {
            restClient.get()
                    .uri(validationUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                    .retrieve()
                    .toBodilessEntity();
            return OAuth2TokenValidatorResult.success();
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden exception) {
            return OAuth2TokenValidatorResult.failure(ACCOUNT_INACTIVE);
        } catch (RestClientResponseException exception) {
            return OAuth2TokenValidatorResult.failure(VALIDATION_UNAVAILABLE);
        } catch (RestClientException exception) {
            return OAuth2TokenValidatorResult.failure(VALIDATION_UNAVAILABLE);
        }
    }
}
