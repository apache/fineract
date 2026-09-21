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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

class CommandRequestMatcherTest {

    private final RequestMatcher matcher = SecurityConfig.commandMatcher(HttpMethod.POST, "/api/*/groups/*", "activate");

    private static MockHttpServletRequest post(String uri, String command) {
        return request("POST", uri, command);
    }

    private static MockHttpServletRequest request(String method, String uri, String command) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setServletPath(uri);
        if (command != null) {
            request.setParameter("command", command);
        }
        return request;
    }

    @Test
    void matchesCommandCaseInsensitively() {
        assertTrue(matcher.matches(post("/api/v1/groups/12", "activate")));
        assertTrue(matcher.matches(post("/api/v1/groups/12", "Activate")));
    }

    @Test
    void matchesCommandWithSurroundingWhitespace() {
        assertTrue(matcher.matches(post("/api/v1/groups/12", " activate")));
        assertTrue(matcher.matches(post("/api/v1/groups/12", "activate ")));
    }

    @Test
    void matchesPathWithTrailingSlash() {
        assertTrue(matcher.matches(post("/api/v1/groups/12/", "activate")));
    }

    @Test
    void rejectsOtherCommandsAbsentCommandAndOtherPaths() {
        assertFalse(matcher.matches(post("/api/v1/groups/12", "close")));
        assertFalse(matcher.matches(post("/api/v1/groups/12", null)));
        assertFalse(matcher.matches(post("/api/v1/centers/12", "activate")));
        assertFalse(matcher.matches(post("/api/v1/groups/12/accounts", "activate")));
    }

    @Test
    void rejectsOtherHttpMethods() {
        assertFalse(matcher.matches(request("PUT", "/api/v1/groups/12", "activate")));
        assertFalse(matcher.matches(request("GET", "/api/v1/groups/12", "activate")));
    }

    @Test
    void pathMatcherAcceptsOptionalTrailingSlashOnly() {
        RequestMatcher get = SecurityConfig.pathMatcher(HttpMethod.GET, "/api/*/groups/*");
        assertTrue(get.matches(request("GET", "/api/v1/groups/12", null)));
        assertTrue(get.matches(request("GET", "/api/v1/groups/12/", null)));
        assertFalse(get.matches(request("GET", "/api/v1/groups/12/accounts", null)));
        assertFalse(get.matches(request("POST", "/api/v1/groups/12", null)));
    }
}
