package com.psymanager.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class AdminAuthoritiesConverterTest {

    private final AdminAuthoritiesConverter converter = new AdminAuthoritiesConverter("admin@test.com");

    private Jwt jwtWithEmail(String email) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "RS256").subject("123");
        if (email != null) {
            builder.claim("email", email);
        }
        return builder.build();
    }

    @Test
    void grantsRoleAdminForMatchingEmail() {
        assertThat(converter.convert(jwtWithEmail("admin@test.com")))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void matchIsCaseInsensitive() {
        assertThat(converter.convert(jwtWithEmail("ADMIN@TEST.COM"))).hasSize(1);
    }

    @Test
    void grantsNothingForDifferentEmail() {
        assertThat(converter.convert(jwtWithEmail("intruder@test.com"))).isEmpty();
    }

    @Test
    void grantsNothingWhenEmailMissing() {
        assertThat(converter.convert(jwtWithEmail(null))).isEmpty();
    }
}
