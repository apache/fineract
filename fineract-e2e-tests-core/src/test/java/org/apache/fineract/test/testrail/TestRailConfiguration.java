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
package org.apache.fineract.test.testrail;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.fineract.client.auth.HttpBasicAuth;
import org.apache.fineract.client.util.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;

@Configuration
@Conditional(TestRailEnabledCondition.class)
public class TestRailConfiguration {

    @Autowired
    private TestRailProperties testRailProperties;

    @Bean
    public TestRailApiClient testRailApiClient() {
        String testRailBaseUrl = testRailProperties.getBaseUrl();
        String testRailUsername = testRailProperties.getUsername();
        String testRailPassword = testRailProperties.getPassword();

        if (isBlank(testRailBaseUrl)) {
            throw new IllegalStateException("TestRail base URL has not been set");
        }
        if (isBlank(testRailUsername)) {
            throw new IllegalStateException("TestRail username has not been set");
        }
        if (isBlank(testRailPassword)) {
            throw new IllegalStateException("TestRail password has not been set");
        }

        HttpBasicAuth httpBasicAuth = new HttpBasicAuth();
        httpBasicAuth.setCredentials(testRailUsername, testRailPassword);
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(httpBasicAuth).addInterceptor(new TestRailIndexPhpInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(JSON.GsonCustomConverterFactory.create(new JSON().getGson()))
                .client(httpClient).baseUrl(testRailBaseUrl).build();

        return retrofit.create(TestRailApiClient.class);
    }

    // Needed otherwise Retrofit 2 will be mad on the URL query/path parameters
    private static final class TestRailIndexPhpInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            String finalUrl = request.url().toString().replace("api/v2", "index.php?/api/v2");
            return chain.proceed(request.newBuilder().url(finalUrl).build());
        }
    }
}
