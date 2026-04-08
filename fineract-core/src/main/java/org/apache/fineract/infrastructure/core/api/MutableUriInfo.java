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
package org.apache.fineract.infrastructure.core.api;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MutableUriInfo implements UriInfo {

    private final UriInfo delegate;

    @Getter
    private final MultivaluedMap<String, String> additionalQueryParameters = new MultivaluedHashMap<>();

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return fillAdditionalQueryParameters(delegate.getQueryParameters());
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return fillAdditionalQueryParameters(delegate.getQueryParameters(decode));
    }

    private MultivaluedMap<String, String> fillAdditionalQueryParameters(MultivaluedMap<String, String> queryParameters) {
        MultivaluedMap<String, String> newQueryParameters = new MultivaluedHashMap<>(queryParameters);
        newQueryParameters.putAll(additionalQueryParameters);
        return newQueryParameters;
    }

    public void addAdditionalQueryParameter(String key, String value) {
        additionalQueryParameters.add(key, value);
    }

    public void putAdditionalQueryParameter(String key, List<String> values) {
        additionalQueryParameters.put(key, values);
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public String getPath(boolean decode) {
        return delegate.getPath(decode);
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return delegate.getPathSegments();
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode) {
        return delegate.getPathSegments(decode);
    }

    @Override
    public URI getRequestUri() {
        return delegate.getRequestUri();
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return delegate.getRequestUriBuilder();
    }

    @Override
    public URI getAbsolutePath() {
        return delegate.getAbsolutePath();
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return delegate.getAbsolutePathBuilder();
    }

    @Override
    public URI getBaseUri() {
        return delegate.getBaseUri();
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        return delegate.getBaseUriBuilder();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return delegate.getPathParameters();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        return delegate.getPathParameters(decode);
    }

    @Override
    public List<String> getMatchedURIs() {
        return delegate.getMatchedURIs();
    }

    @Override
    public List<String> getMatchedURIs(boolean decode) {
        return delegate.getMatchedURIs(decode);
    }

    @Override
    public List<Object> getMatchedResources() {
        return delegate.getMatchedResources();
    }

    @Override
    public URI resolve(URI uri) {
        return delegate.resolve(uri);
    }

    @Override
    public URI relativize(URI uri) {
        return delegate.relativize(uri);
    }
}
