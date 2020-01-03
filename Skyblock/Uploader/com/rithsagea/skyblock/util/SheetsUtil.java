package com.rithsagea.skyblock.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

public class SheetsUtil {
	
	private static final String APPLICATION_NAME = "Skyblock Auction Data";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	
	public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException, GeneralSecurityException {
		InputStream in = SheetsUtil.class.getResourceAsStream("/resources/credentials.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
		
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
				.Builder(
						HTTP_TRANSPORT,
						JSON_FACTORY,
						clientSecrets,
						SCOPES)
				.setDataStoreFactory(new MemoryDataStoreFactory())
				.setAccessType("offline")
				.build();
		
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}
	
	public static Sheets getSheetsService() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		
		return new Sheets
				.Builder(
						HTTP_TRANSPORT,
						JSON_FACTORY,
						getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME)
				.build();
	}
}
