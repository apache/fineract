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
package org.apache.fineract.infrastructure.core.serialization;

import java.util.Collection;
import java.util.Set;

import org.apache.fineract.infrastructure.core.service.Page;

public interface ToApiJsonSerializer<T> {

    String serialize(Object object);

    String serializePretty(boolean prettyOn, Object object);

    String serializeResult(Object object);

    String serialize(ApiRequestJsonSerializationSettings settings, Collection<T> collection);

    String serialize(ApiRequestJsonSerializationSettings settings, T single);

    String serialize(ApiRequestJsonSerializationSettings settings, Page<T> singleObject);

    // TODO: TECHDEBT - bottom three will be deprecated going forward to remove
    // need for people to pass full list of supported parameters. It was only
    // used in cases where the partial response features was used (fields=x,y,x)
    // to report error if incorrect field was passed. From now on it will just
    // ignore unknown fields and fail silently
    String serialize(ApiRequestJsonSerializationSettings settings, Collection<T> collection, Set<String> supportedResponseParameters);

    String serialize(ApiRequestJsonSerializationSettings settings, T single, Set<String> supportedResponseParameters);

    String serialize(ApiRequestJsonSerializationSettings settings, Page<T> singleObject, Set<String> supportedResponseParameters);
}