package com.psymanager.form;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.FormsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import com.psymanager.common.CryptoService;
import com.psymanager.common.exception.BadRequestException;
import com.psymanager.common.exception.IntegrationException;
import com.psymanager.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Fluxo OAuth (Authorization Code, offline) com a conta Google do Admin para a Forms API.
 * Guarda o refresh token cifrado e constrói o cliente da Forms API sob demanda.
 */
@Service
public class GoogleOAuthService {

    private static final String APP_NAME = "Psy Manager";
    private static final List<String> SCOPES = List.of(
            FormsScopes.FORMS_BODY, "openid", "email", "profile");

    private final AppProperties props;
    private final CryptoService cryptoService;
    private final GoogleOAuthTokenRepository tokenRepository;
    private final HttpTransport httpTransport;
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    public GoogleOAuthService(AppProperties props, CryptoService cryptoService,
                              GoogleOAuthTokenRepository tokenRepository) {
        this.props = props;
        this.cryptoService = cryptoService;
        this.tokenRepository = tokenRepository;
        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Falha ao iniciar o transporte HTTP do Google", e);
        }
    }

    /** URL de consentimento que o Admin deve abrir para autorizar a Forms API. */
    public String buildConsentUrl() {
        ensureConfigured();
        return flow().newAuthorizationUrl()
                .setRedirectUri(props.google().redirectUri())
                .set("access_type", "offline")
                .set("prompt", "consent")
                .build();
    }

    /** Troca o código pelo refresh token e o persiste cifrado (após validar o e-mail). */
    @Transactional
    public void handleCallback(String code) {
        ensureConfigured();
        try {
            GoogleTokenResponse tokenResponse = flow().newTokenRequest(code)
                    .setRedirectUri(props.google().redirectUri())
                    .execute();

            GoogleIdToken idToken = tokenResponse.parseIdToken();
            String email = idToken != null ? idToken.getPayload().getEmail() : null;
            if (email == null || !email.equalsIgnoreCase(props.adminEmail())) {
                throw new BadRequestException("A conta Google autorizada deve ser o ADMIN_EMAIL");
            }

            String refreshToken = tokenResponse.getRefreshToken();
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new BadRequestException(
                        "O Google não retornou refresh token. Remova o acesso do app na sua Conta Google e tente novamente.");
            }

            GoogleOAuthToken entity = tokenRepository.findByAdminEmail(email)
                    .orElseGet(GoogleOAuthToken::new);
            entity.setAdminEmail(email);
            entity.setRefreshTokenEnc(cryptoService.encrypt(refreshToken));
            entity.setScope(String.join(" ", SCOPES));
            tokenRepository.save(entity);
        } catch (IOException e) {
            throw new IntegrationException("Falha ao trocar o código de autorização do Google", e);
        }
    }

    /** Cliente da Forms API autenticado com o refresh token armazenado. */
    public Forms formsClient() {
        ensureConfigured();
        GoogleOAuthToken token = tokenRepository.findByAdminEmail(props.adminEmail())
                .orElseThrow(() -> new BadRequestException(
                        "Conta Google não conectada. Faça o consentimento da Forms API primeiro."));

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(props.google().clientId())
                .setClientSecret(props.google().clientSecret())
                .setRefreshToken(cryptoService.decrypt(token.getRefreshTokenEnc()))
                .build();

        return new Forms.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APP_NAME)
                .build();
    }

    private GoogleAuthorizationCodeFlow flow() {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory,
                props.google().clientId(), props.google().clientSecret(), SCOPES)
                .setAccessType("offline")
                .build();
    }

    private void ensureConfigured() {
        if (isBlank(props.google().clientId()) || isBlank(props.google().clientSecret())) {
            throw new BadRequestException("Integração Google não configurada (client id/secret ausentes)");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
