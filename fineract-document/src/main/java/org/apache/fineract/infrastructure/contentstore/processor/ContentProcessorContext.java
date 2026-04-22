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
package org.apache.fineract.infrastructure.contentstore.processor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public final class ContentProcessorContext {

    private final InputStream inputStream;
    private final Map<String, Object> parameters;
    private final Map<String, Object> results;

    public ContentProcessorContext(InputStream inputStream) {
        this(inputStream, Map.of(), new HashMap<>());
    }

    public ContentProcessorContext(InputStream inputStream, Map<String, Object> parameters) {
        this(inputStream, parameters, new HashMap<>());
    }

    ContentProcessorContext(InputStream inputStream, Map<String, Object> parameters, Map<String, Object> results) {
        this.inputStream = inputStream;
        this.parameters = parameters;
        this.results = results;
    }

    public <R> R getParameter(String key) {
        return getParameter(key, null);
    }

    public <R> R getParameter(String key, R defaultValue) {
        final var val = parameters.get(key);

        return val != null ? (R) val : defaultValue;
    }

    public void setResult(String key, Object value) {
        results.put(key, value);
    }

    public <R> R getResult(String key) {
        return getResult(key, null);
    }

    public <R> R getResult(String key, R defaultValue) {
        final var val = results.get(key);

        return val != null ? (R) val : defaultValue;
    }

    public ContentProcessorContext clone(InputStream inputStream) {
        return new ContentProcessorContext(inputStream, this.parameters, this.results);
    }
}
