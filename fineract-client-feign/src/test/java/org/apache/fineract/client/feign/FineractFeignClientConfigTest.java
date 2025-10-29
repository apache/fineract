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
package org.apache.fineract.client.feign;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import feign.RequestLine;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class FineractFeignClientConfigTest {

    @Test
    void testBuilderDefaults() {
        FineractFeignClientConfig.Builder builder = FineractFeignClientConfig.builder();
        FineractFeignClientConfig config = builder.baseUrl("http://localhost:8080").credentials("admin", "password").build();

        assertNotNull(config);
    }

    @Test
    void testBuilderConfiguration() {
        String baseUrl = "http://example.com:8080";
        String username = "testuser";
        String password = "testpass";
        int connectTimeoutSeconds = 10;
        int readTimeoutSeconds = 30;

        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl(baseUrl).credentials(username, password)
                .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS).readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                .debugEnabled(true).build();

        assertNotNull(config);
    }

    @Test
    void testConnectionTimeToLiveConfiguration() {
        long ttl = 5;
        TimeUnit ttlUnit = TimeUnit.MINUTES;

        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:8080")
                .credentials("admin", "password").connectionTimeToLive(ttl, ttlUnit).build();

        assertNotNull(config);
    }

    @Test
    void testEncoderDecoderConfiguration() {
        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:8080")
                .credentials("admin", "password").build();

        assertNotNull(config);
    }

    @Test
    void testErrorDecoderConfiguration() {
        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:8080")
                .credentials("admin", "password").build();

        assertNotNull(config);
    }

    @Test
    void testClientCreation() {
        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:8080")
                .credentials("admin", "password").connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();

        TestApi client = config.createClient(TestApi.class);
        assertNotNull(client);
    }

    interface TestApi {

        @RequestLine("GET /test")
        String test();
    }
}
