package org.mifosng.ui.infrastructure;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateFactory implements FactoryBean<RestTemplate>,
		InitializingBean {

	private RestTemplate restTemplate;

	@Override
	public RestTemplate getObject() {
		return restTemplate;
	}

	@Override
	public Class<RestTemplate> getObjectType() {
		return RestTemplate.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() {

		try {
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			HttpClient client = requestFactory.getHttpClient();
			ClientConnectionManager clientConnectionManager = client.getConnectionManager();
			SchemeRegistry registry = clientConnectionManager.getSchemeRegistry();

			Scheme oldScheme = registry.unregister("https");

			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());

			trustStore.load(null, null);
			
			X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
				
				@Override
				public void verify(String host, String[] cns, String[] subjectAlts)
						throws SSLException {
				}
				
				@Override
				public void verify(String host, X509Certificate cert) throws SSLException {
				}
				
				@Override
				public void verify(String host, SSLSocket ssl) throws IOException {
				}
			};

			SSLContext context = SSLContext.getInstance("TLS");
			SSLSocketFactory factory = new MySSLSocketFactory(context, trustStore);
			factory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			factory.setHostnameVerifier(hostnameVerifier);

			Scheme newHttpsScheme = new Scheme("https", 8443, factory);
			registry.register(newHttpsScheme);

			restTemplate = new RestTemplate(requestFactory);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		} catch (UnrecoverableKeyException e) {
			throw new RuntimeException(e);
		}
	}
}
