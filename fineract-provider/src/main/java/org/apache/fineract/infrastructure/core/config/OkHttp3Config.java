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

package org.apache.fineract.infrastructure.core.config;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class OkHttp3Config {

    private final FineractProperties fineractProperties;

    @Bean
    public OkHttpClient okHttpClient() throws Exception {
        var okBuilder = new OkHttpClient.Builder();

        if (Boolean.TRUE.equals(fineractProperties.getInsecureHttpClient())) {
            final X509TrustManager insecureX509TrustManager = new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}// NOSONAR

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}// NOSONAR

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[] {};
                }
            };

            SSLContext insecureSSLContext = SSLContext.getInstance("TLS");
            insecureSSLContext.init(null, new TrustManager[] { insecureX509TrustManager }, new SecureRandom());

            okBuilder.sslSocketFactory(insecureSSLContext.getSocketFactory(), insecureX509TrustManager);
            HostnameVerifier insecureHostnameVerifier = (hostname, session) -> true;// NOSONAR
            okBuilder.hostnameVerifier(insecureHostnameVerifier);
        }

        return okBuilder.build();
    }
}
