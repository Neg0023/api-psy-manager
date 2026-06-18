package com.psymanager.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Concede a authority {@code ROLE_ADMIN} somente se o e-mail do ID Token for
 * exatamente o e-mail do psicólogo proprietário (ADMIN_EMAIL).
 *
 * <p>Combinado com a regra {@code hasRole("ADMIN")} no SecurityFilterChain, um
 * token válido de outro e-mail resulta em <b>403 Forbidden</b> (autenticado,
 * porém sem autorização) — conforme a especificação.
 */
public class AdminAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String adminEmail;

    public AdminAuthoritiesConverter(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null && adminEmail != null && email.equalsIgnoreCase(adminEmail)) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return Collections.emptyList();
    }
}
