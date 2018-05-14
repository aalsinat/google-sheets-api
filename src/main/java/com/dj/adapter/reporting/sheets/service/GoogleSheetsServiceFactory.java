package com.dj.adapter.reporting.sheets.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class GoogleSheetsServiceFactory implements ServiceFactory {
    final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
    final String APPLICATION_NAME = "FlowIT Reporting";
    final String CLIENT_SECRET_DIR = "client_secret.json";

    public GoogleSheetsServiceFactory() {
    }

    @Override
    public GoogleSheetsService getService(String credentials) {
        try {
            final NetHttpTransport trustedTransport = GoogleNetHttpTransport.newTrustedTransport();
            final Credential credential = this.authorize(credentials);
            GoogleSheetsService service = GoogleSheetsService
                    .of(new Sheets.Builder(trustedTransport, this.jacksonFactory, credential)
                                .setApplicationName(this.APPLICATION_NAME)
                                .build());
            return service;
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Credential authorize(String clientSecret) throws IOException, GeneralSecurityException {

        final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
        final NetHttpTransport trustedTransport = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(clientSecret);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jacksonFactory, new InputStreamReader(in));

        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(trustedTransport, jacksonFactory, clientSecrets, scopes)
                .setDataStoreFactory(new MemoryDataStoreFactory())
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }
}
