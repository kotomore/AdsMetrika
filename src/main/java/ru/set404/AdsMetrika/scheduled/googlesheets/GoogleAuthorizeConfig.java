package ru.set404.AdsMetrika.scheduled.googlesheets;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;
import ru.set404.AdsMetrika.exceptions.OAuthCredentialEmptyException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
@SessionScope
@Setter
public class GoogleAuthorizeConfig {
    @Value("${google.application.name}")
    private String applicationName;
    @Value("${google.credentials.file.path}")
    private String credentialsFilePath;
    @Value("${google.tokens.directory.path}")
    private String tokensDirectoryPath;
    @Value("${google.redirect-uri}")
    private String redirectUri;
    private AuthorizationCodeInstalledApp auth;
    protected static final String PERMISSION_DENIED = "permission_denied";

    private Credential getCredential(String code) throws IOException, GeneralSecurityException {
        Credential var7;

        if (code.isEmpty() || code.equals(PERMISSION_DENIED)) {
            InputStream in = GoogleAuthorizeConfig.class.getResourceAsStream(credentialsFilePath);
            assert in != null;
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(),
                    new InputStreamReader(in));
            List<String> scopes = List.of(SheetsScopes.SPREADSHEETS);

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                    clientSecrets, scopes)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
                    //.setDataStoreFactory(new MemoryDataStoreFactory())
                    .setAccessType("offline").build();

            LocalServerReceiver localServerReceiver = new LocalServerReceiver.Builder()
                    .setPort(8888)
                    .build();
            auth = new AuthorizationCodeInstalledApp(flow, localServerReceiver);

            if (!code.equals(PERMISSION_DENIED)) {
                Credential credential = auth.getFlow().loadCredential("user");
                if (credential != null && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() == null || credential.getExpiresInSeconds() > 60L)) {
                    return credential;
                }
            }

            AuthorizationCodeRequestUrl authorizationUrl = auth.getFlow().newAuthorizationUrl().setRedirectUri(redirectUri);
            throw new OAuthCredentialEmptyException(String.valueOf(authorizationUrl));
        }

        TokenResponse response = auth.getFlow().newTokenRequest(code).setRedirectUri(redirectUri).execute();
        var7 = auth.getFlow().createAndStoreCredential(response, "user");
        auth.getReceiver().stop();
        return var7;
    }

    public Sheets getSheetsService(String code) {
        try {
            Credential credential = getCredential(code);
            return new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName(applicationName)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
