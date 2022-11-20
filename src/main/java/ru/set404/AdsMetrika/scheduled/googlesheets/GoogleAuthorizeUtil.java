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
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
public class GoogleAuthorizeUtil {
    private static final String APPLICATION_NAME = "AdsMetrika";
    private Credential credential;

    private Credential getCredential() throws IOException, GeneralSecurityException {
        //Google OAuth credentials json file
        InputStream in = GoogleAuthorizeUtil.class.getResourceAsStream("/credentials.json");
        assert in != null;
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(),
                new InputStreamReader(in));
        List<String> scopes = List.of(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                clientSecrets, scopes)
                .setDataStoreFactory(new MemoryDataStoreFactory()).setAccessType("offline").build();


        LocalServerReceiver localServerReceiver = new LocalServerReceiver();
        AuthorizationCodeInstalledApp auth = new AuthorizationCodeInstalledApp(flow, localServerReceiver);
        return authorization(auth);
    }

    public boolean isAuth() {
        return credential != null && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() == null || credential.getExpiresInSeconds() > 60L);
    }

    private Credential authorization(AuthorizationCodeInstalledApp auth) throws IOException {
        Credential var7;
        try {
            Credential credential = auth.getFlow().loadCredential("user");
            if (credential != null && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() == null || credential.getExpiresInSeconds() > 60L)) {
                return credential;
            }

            String redirectUri =  auth.getReceiver().getRedirectUri();
            AuthorizationCodeRequestUrl authorizationUrl = auth.getFlow().newAuthorizationUrl().setRedirectUri(redirectUri);
            System.out.println(authorizationUrl);
            String code = auth.getReceiver().waitForCode();
            TokenResponse response = auth.getFlow().newTokenRequest(code).setRedirectUri(redirectUri).execute();
            var7 = auth.getFlow().createAndStoreCredential(response, "user");
        } finally {
            auth.getReceiver().stop();
        }
        return var7;
    }
    public Sheets getSheetsService() throws IOException, GeneralSecurityException {
        if (credential == null)
            credential = getCredential();
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
