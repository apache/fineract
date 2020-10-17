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
package org.apache.fineract.client.util;

import org.apache.fineract.client.ApiClient;
import org.apache.fineract.client.auth.ApiKeyAuth;

/**
 * Fineract Client Java SDK API entry point. This is recommended to be used instead of {@link ApiClient}.
 *
 * @author Michael Vorburger.ch
 */
public class FineractClient {

    private final ApiClient apiClient;

    private FineractClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static FineractClientBuilder builder() {
        return new FineractClientBuilder();
    }

    public <S> S createService(Class<S> serviceClass) {
        return apiClient.createService(serviceClass);
    }

    public static class FineractClientBuilder {

        private String baseURL;
        private String tenant;
        private String username;
        private String password;

        private FineractClientBuilder() {}

        public FineractClientBuilder baseURL(String baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        public FineractClientBuilder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public FineractClientBuilder basicAuth(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public FineractClient build() {
            ApiClient apiClient = new ApiClient("basicAuth", has("username", username), has("password", password));
            apiClient.getAdapterBuilder().baseUrl(has("baseURL", baseURL));
            ApiKeyAuth authorization = new ApiKeyAuth("header", "fineract-platform-tenantid");
            authorization.setApiKey(has("tenant", tenant));
            apiClient.addAuthorization("tenantid", authorization);
            return new FineractClient(apiClient);
        }

        private <T> T has(String propertyName, T value) throws IllegalStateException {
            if (value == null) {
                throw new IllegalStateException("Must call " + propertyName + "(...) to create valid Builder");
            }
            return value;
        }
    }
}
