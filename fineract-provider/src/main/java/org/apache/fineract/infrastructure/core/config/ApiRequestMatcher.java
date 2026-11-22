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

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayDeque;
import java.util.Deque;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Matches API requests the way the JAX-RS runtime resolves them to resources.
 *
 * <p>
 * Jersey routes {@code /caches}, {@code /caches/} and {@code /caches/.} to the same resource method, resolves entity
 * type path parameters such as {@code {entityType}} case-insensitively and serves {@code HEAD} with the {@code GET}
 * method. A plain path pattern match would treat those variants as different paths, so this matcher normalizes the
 * path, matches case-insensitively and lets {@code GET} rules cover {@code HEAD} requests too.
 */
final class ApiRequestMatcher implements RequestMatcher {

    private static final PathPatternParser PARSER = createParser();
    private static final UrlPathHelper URL_PATH_HELPER = UrlPathHelper.defaultInstance;

    private final HttpMethod method;
    private final PathPattern pattern;
    private final String patternString;

    private ApiRequestMatcher(HttpMethod method, String pattern) {
        this.method = method;
        this.pattern = PARSER.parse(pattern);
        this.patternString = pattern;
    }

    static ApiRequestMatcher of(HttpMethod method, String pattern) {
        return new ApiRequestMatcher(method, pattern);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return matches(request.getMethod(), URL_PATH_HELPER.getPathWithinApplication(request));
    }

    boolean matches(String requestMethod, String pathWithinApplication) {
        if (!matchesMethod(requestMethod)) {
            return false;
        }
        return pattern.matches(PathContainer.parsePath(normalize(pathWithinApplication)));
    }

    private boolean matchesMethod(String requestMethod) {
        if (method == null || method.matches(requestMethod)) {
            return true;
        }
        return HttpMethod.GET.equals(method) && HttpMethod.HEAD.matches(requestMethod);
    }

    /**
     * Removes empty, {@code .} and {@code ..} segments and the trailing slash.
     */
    static String normalize(String path) {
        final Deque<String> segments = new ArrayDeque<>();
        for (String segment : path.split("/", -1)) {
            if (segment.isEmpty() || ".".equals(segment)) {
                continue;
            }
            if ("..".equals(segment)) {
                segments.pollLast();
                continue;
            }
            segments.addLast(segment);
        }
        return "/" + String.join("/", segments);
    }

    private static PathPatternParser createParser() {
        final PathPatternParser parser = new PathPatternParser();
        parser.setCaseSensitive(false);
        return parser;
    }

    @Override
    public String toString() {
        return "ApiRequestMatcher [" + (method == null ? "*" : method.name()) + " " + patternString + "]";
    }
}
