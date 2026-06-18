package com.psymanager.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Validação adicional do ID Token do Google (além de assinatura/issuer/expiração
 * já garantidos pelo decoder padrão):
 * <ul>
 *   <li>a audiência ({@code aud}) deve corresponder ao GOOGLE_CLIENT_ID;</li>
 *   <li>o e-mail deve estar verificado ({@code email_verified}).</li>
 * </ul>
 * Falhas aqui resultam em 401 (token inválido). A checagem de qual e-mail tem
 * acesso (ADMIN_EMAIL) é feita como autorização — ver {@link AdminAuthoritiesConverter}.
 */
public class GoogleIdTokenValidator implements OAuth2TokenValidator<Jwt> {

    private final String clientId;

    public GoogleIdTokenValidator(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience() == null || !jwt.getAudience().contains(clientId)) {
            return failure("A audiência (aud) do ID Token não corresponde ao GOOGLE_CLIENT_ID");
        }

        Object emailVerified = jwt.getClaim("email_verified");
        boolean verified = (emailVerified instanceof Boolean b && b)
                || "true".equalsIgnoreCase(String.valueOf(emailVerified));
        if (!verified) {
            return failure("O e-mail do ID Token não está verificado");
        }

        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult failure(String description) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", description, null));
    }
}
