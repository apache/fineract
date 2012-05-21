package org.mifosng.ui.infrastructure;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class CustomClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

	@Override
	protected void prepareConnection(HttpURLConnection connection,
			String httpMethod) throws IOException {
		
		if (connection instanceof HttpsURLConnection) {
			disableCertificateValidation();
			HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
			httpsConnection.setHostnameVerifier(new NullHostnameVerifier());
		}
		super.prepareConnection(connection, httpMethod);
	}

	private class NullHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	public static void disableCertificateValidation() {
		
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			
			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
			}
			
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
			}
		};
		
		TrustManager[] trustAllCerts = new TrustManager[1];
		trustAllCerts[0] = trustManager;
				
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}
	}

}
