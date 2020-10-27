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

import com.squareup.okhttp.OkHttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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
import org.springframework.stereotype.Service;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

@Service
public final class ProcessorHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessorHelper.class);

    /**
     * Configure HTTP client to be "insecure", as in skipping host SSL certificate verification. While this can be
     * useful during development e.g. when using self-signed certificates, it should never be enabled in production (due
     * to "man in the middle").
     */
    private final boolean insecureHttpClient = Boolean.getBoolean("fineract.insecureHttpClient");
    private final SSLContext insecureSSLContext;

    public ProcessorHelper() throws KeyManagementException, NoSuchAlgorithmException {
        if (insecureHttpClient) {
            insecureSSLContext = createInsecureSSLContext();
        } else {
            insecureSSLContext = null;
        }
    }

    private OkHttpClient createClient() {
        final OkHttpClient client = new OkHttpClient();
        if (insecureHttpClient) {
            configureInsecureClient(client);
        }
        return client;
    }

    private void configureInsecureClient(final OkHttpClient client) {
        client.setSslSocketFactory(insecureSSLContext.getSocketFactory());

        final HostnameVerifier hostnameVerifier = new HostnameVerifier() {

            @Override
            public boolean verify(final String hostname, final SSLSession session) {
                return true;
            }
        };
        client.setHostnameVerifier(hostnameVerifier);
    }

    private SSLContext createInsecureSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        final TrustManager[] certs = new TrustManager[] { new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {}

            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {}
        } };

        SSLContext insecureSSLContext = SSLContext.getInstance("TLS");
        insecureSSLContext.init(null, certs, new SecureRandom());
        return insecureSSLContext;
    }

    @SuppressWarnings("rawtypes")
    public Callback createCallback(final String url) {
        return new Callback() {

            @Override
            public void success(final Object o, final Response response) {
                LOG.info("URL: {} - Status: {}", url, response.getStatus());
            }

            @Override
            public void failure(final RetrofitError retrofitError) {
                LOG.error("URL: {} - RetrofitError occured", url, retrofitError);
            }
        };
    }

    public WebHookService createWebHookService(final String url) {
        final OkHttpClient client = createClient();
        final RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(url).setClient(new OkClient(client)).build();
        return restAdapter.create(WebHookService.class);
    }

    public Callback createCallback(final String url, String payload) {

        return new Callback() {

            @Override
            public void success(final Object o, final Response response) {
                LOG.info("URL : {} \tStatus : {}", url, response.getStatus());
            }

            @Override
            public void failure(final RetrofitError retrofitError) {
                LOG.error("URL: {} - RetrofitError occured", url, retrofitError);
            }
        };
    }
}
