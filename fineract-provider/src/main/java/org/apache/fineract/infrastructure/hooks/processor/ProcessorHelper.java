/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.processor;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

import com.squareup.okhttp.OkHttpClient;

@SuppressWarnings("unused")
public class ProcessorHelper {

	private final static Logger logger = LoggerFactory
			.getLogger(ProcessorHelper.class);

	@SuppressWarnings("null")
	public static OkHttpClient configureClient(final OkHttpClient client) {
		final TrustManager[] certs = new TrustManager[] { new X509TrustManager() {

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkServerTrusted(final X509Certificate[] chain,
					final String authType) throws CertificateException {
			}

			@Override
			public void checkClientTrusted(final X509Certificate[] chain,
					final String authType) throws CertificateException {
			}
		} };

		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(null, certs, new SecureRandom());
		} catch (final java.security.GeneralSecurityException ex) {
		}

		try {
			final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
				@Override
				public boolean verify(final String hostname,
						final SSLSession session) {
					return true;
				}
			};
			client.setHostnameVerifier(hostnameVerifier);
			client.setSslSocketFactory(ctx.getSocketFactory());
		} catch (final Exception e) {
		}

		return client;
	}

	public static OkHttpClient createClient() {
		final OkHttpClient client = new OkHttpClient();
		return configureClient(client);
	}

	@SuppressWarnings("rawtypes")
	public static Callback createCallback(final String url) {

		return new Callback() {
			@Override
			public void success(final Object o, final Response response) {
				logger.info("URL : " + url + "\tStatus : "
						+ response.getStatus());
			}

			@Override
			public void failure(final RetrofitError retrofitError) {
				logger.info(retrofitError.getMessage());
			}
		};
	}

	public static WebHookService createWebHookService(final String url) {

		final OkHttpClient client = ProcessorHelper.createClient();

		final RestAdapter restAdapter = new RestAdapter.Builder()
				.setEndpoint(url).setClient(new OkClient(client)).build();

		return restAdapter.create(WebHookService.class);
	}

}