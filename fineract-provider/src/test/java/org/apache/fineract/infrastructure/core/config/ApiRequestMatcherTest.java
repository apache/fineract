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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;

class ApiRequestMatcherTest {

    private final ApiRequestMatcher caches = ApiRequestMatcher.of(HttpMethod.PUT, "/api/*/caches");
    private final ApiRequestMatcher clientDocuments = ApiRequestMatcher.of(HttpMethod.GET, "/api/*/clients/*/documents/**");

    @ParameterizedTest
    @ValueSource(strings = { "/api/v1/caches", "/api/v1/caches/", "/api/v1/caches/.", "/api/v1//caches", "/api/v1/./caches",
            "/api/v1/x/../caches", "/api/v1/CACHES" })
    void matchesPathVariantsThatResolveToTheSameResource(String path) {
        assertThat(caches.matches("PUT", path)).isTrue();
    }

    @Test
    void doesNotMatchOtherResourcesOrMethods() {
        assertThat(caches.matches("PUT", "/api/v1/cachesx")).isFalse();
        assertThat(caches.matches("PUT", "/api/v1/caches/1")).isFalse();
        assertThat(caches.matches("GET", "/api/v1/caches")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { "/api/v1/clients/1/documents", "/api/v1/Clients/1/documents", "/api/v1/CLIENTS/1/documents/2/attachment",
            "/api/v1/clients/1/documents/" })
    void matchesEntityTypeSegmentCaseInsensitively(String path) {
        assertThat(clientDocuments.matches("GET", path)).isTrue();
    }

    @Test
    void getRulesAlsoCoverHeadRequests() {
        assertThat(clientDocuments.matches("HEAD", "/api/v1/clients/1/documents")).isTrue();
        assertThat(caches.matches("HEAD", "/api/v1/caches")).isFalse();
    }

    @Test
    void normalizeRemovesEmptyAndDotSegments() {
        assertThat(ApiRequestMatcher.normalize("/api/v1//hooks/./1/")).isEqualTo("/api/v1/hooks/1");
        assertThat(ApiRequestMatcher.normalize("/api/v1/x/../hooks")).isEqualTo("/api/v1/hooks");
    }
}
