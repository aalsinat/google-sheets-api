package reporting.domain;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleSheetsRepositoryFactory {
	final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
	final String private_key;

	public GoogleSheetsRepositoryFactory(String private_key) {
		this.private_key = private_key;
	}

	public GoogleSheetsRepository getRepository() {
		try {
			final NetHttpTransport trustedTransport = GoogleNetHttpTransport.newTrustedTransport();
			final Credential credential = this.authorize();

			GoogleSheetsRepository service = new GoogleSheetsRepository(
					new Sheets.Builder(trustedTransport, this.jacksonFactory, setTimeout(credential, 120000))
							.build());
			return service;
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	private HttpRequestInitializer setTimeout(final HttpRequestInitializer initializer, final int timeout) {
		return request -> {
			initializer.initialize(request);
			request.setConnectTimeout(timeout);
			request.setReadTimeout(timeout);
		};
	}

	private Credential authorize() throws IOException, GeneralSecurityException {

//		final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
//		final NetHttpTransport trustedTransport = GoogleNetHttpTransport.newTrustedTransport();
//
//		InputStream in = this.getClass()
//		                     .getClassLoader()
//		                     .getResourceAsStream(private_key);
//
//		GoogleCredential credential = GoogleCredential.fromStream(in)
//		                                              .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));


		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		String filePath = getClass().getResource(private_key)
		                            .getFile();

		GoogleCredential credential = GoogleCredential
				.fromStream(new FileInputStream(filePath))
				.createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

		return credential;
	}
}
