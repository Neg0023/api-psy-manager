package com.psymanager.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleIdTokenValidatorTest {

    private final GoogleIdTokenValidator validator = new GoogleIdTokenValidator("client-123");

    private Jwt.Builder base() {
        return Jwt.withTokenValue("token").header("alg", "RS256").subject("123");
    }

    @Test
    void successWhenAudienceMatchesAndEmailVerified() {
        Jwt jwt = base().audience(List.of("client-123")).claim("email_verified", true).build();
        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    void failsWhenAudienceMismatch() {
        Jwt jwt = base().audience(List.of("other-client")).claim("email_verified", true).build();
        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }

    @Test
    void failsWhenEmailNotVerified() {
        Jwt jwt = base().audience(List.of("client-123")).claim("email_verified", false).build();
        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }

    @Test
    void failsWhenEmailVerifiedClaimMissing() {
        Jwt jwt = base().audience(List.of("client-123")).build();
        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }
}
