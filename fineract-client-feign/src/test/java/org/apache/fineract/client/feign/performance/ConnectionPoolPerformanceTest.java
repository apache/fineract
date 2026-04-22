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
package org.apache.fineract.client.feign.performance;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import feign.RequestLine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.client.feign.FineractFeignClientConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("performance")
class ConnectionPoolPerformanceTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testConnectionPoolTTL() throws Exception {
        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port())
                .credentials("test", "test").connectionTimeToLive(2, TimeUnit.SECONDS).connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).build();

        wireMockServer.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"OK\"")));

        TestApi api = config.createClient(TestApi.class);

        String response1 = api.getTest();
        assertThat(response1).isEqualTo("OK");

        Thread.sleep(3000);

        String response2 = api.getTest();
        assertThat(response2).isEqualTo("OK");
    }

    @Test
    void testConnectionPoolTTLWithMultipleRequests() throws Exception {
        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port())
                .credentials("test", "test").connectionTimeToLive(1, TimeUnit.SECONDS).connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).build();

        wireMockServer.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"OK\"")));

        TestApi api = config.createClient(TestApi.class);

        for (int i = 0; i < 5; i++) {
            String response = api.getTest();
            assertThat(response).isEqualTo("OK");
            Thread.sleep(1500);
        }
    }

    @Test
    void testConcurrentRequests() throws Exception {
        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port())
                .credentials("test", "test").connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();

        wireMockServer.stubFor(get(urlEqualTo("/test")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"OK\"").withFixedDelay(100)));

        TestApi api = config.createClient(TestApi.class);

        int numRequests = 50;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<String>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numRequests; i++) {
            futures.add(executor.submit(api::getTest));
        }

        for (Future<String> future : futures) {
            assertThat(future.get()).isEqualTo("OK");
        }

        long duration = System.currentTimeMillis() - startTime;

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        assertThat(duration).isLessThan(2000);
    }

    @Test
    void testConcurrentRequestsWithHighLoad() throws Exception {
        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port())
                .credentials("test", "test").connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();

        wireMockServer.stubFor(get(urlEqualTo("/test")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"OK\"").withFixedDelay(50)));

        TestApi api = config.createClient(TestApi.class);

        int numRequests = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<String>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numRequests; i++) {
            futures.add(executor.submit(api::getTest));
        }

        int successCount = 0;
        for (Future<String> future : futures) {
            if ("OK".equals(future.get())) {
                successCount++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        assertThat(successCount).isEqualTo(numRequests);
        assertThat(duration).isLessThan(5000);
    }

    @Test
    void testConnectionRecycling() throws Exception {
        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port())
                .credentials("test", "test").connectionTimeToLive(500, TimeUnit.MILLISECONDS).connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).build();

        wireMockServer.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"OK\"")));

        TestApi api = config.createClient(TestApi.class);

        for (int i = 0; i < 10; i++) {
            String response = api.getTest();
            assertThat(response).isEqualTo("OK");
            Thread.sleep(600);
        }
    }

    @Test
    void testDefaultConnectionTimeToLive() throws Exception {
        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port())
                .credentials("test", "test").build();

        wireMockServer.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"OK\"")));

        TestApi api = config.createClient(TestApi.class);

        String response = api.getTest();
        assertThat(response).isEqualTo("OK");
    }

    @Test
    void testSequentialRequests() throws Exception {
        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port())
                .credentials("test", "test").connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();

        wireMockServer.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"OK\"")));

        TestApi api = config.createClient(TestApi.class);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            String response = api.getTest();
            assertThat(response).isEqualTo("OK");
        }

        long duration = System.currentTimeMillis() - startTime;

        assertThat(duration).isLessThan(5000);
    }

    @Test
    void testConnectionPoolWithDifferentEndpoints() throws Exception {
        FineractFeignClientConfig config = FineractFeignClientConfig.builder().baseUrl("http://localhost:" + wireMockServer.port())
                .credentials("test", "test").connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();

        wireMockServer.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"OK\"")));
        wireMockServer.stubFor(get(urlEqualTo("/test2"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"OK2\"")));
        wireMockServer.stubFor(get(urlEqualTo("/test3"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"OK3\"")));

        TestApi api = config.createClient(TestApi.class);

        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            int endpoint = i % 3;
            futures.add(executor.submit(() -> {
                switch (endpoint) {
                    case 0:
                        return api.getTest();
                    case 1:
                        return api.getTest2();
                    default:
                        return api.getTest3();
                }
            }));
        }

        for (Future<String> future : futures) {
            assertThat(future.get()).isIn("OK", "OK2", "OK3");
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }

    interface TestApi {

        @RequestLine("GET /test")
        String getTest();

        @RequestLine("GET /test2")
        String getTest2();

        @RequestLine("GET /test3")
        String getTest3();
    }
}
