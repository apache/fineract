/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.hooks.processor;

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