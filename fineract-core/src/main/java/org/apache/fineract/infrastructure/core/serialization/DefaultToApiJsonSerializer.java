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

import com.google.gson.Gson;
import java.util.Collection;
import java.util.Set;
import org.apache.fineract.infrastructure.core.service.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * An abstract helper implementation of {@link ToApiJsonSerializer} for resources to serialize their Java data objects
 * into JSON.
 */
@Component
public class DefaultToApiJsonSerializer<T> implements ToApiJsonSerializer<T> {

    private final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOff;
    private final CommandProcessingResultJsonSerializer commandProcessingResultSerializer;
    private final GoogleGsonSerializerHelper helper;

    @Autowired
    public DefaultToApiJsonSerializer(final ExcludeNothingWithPrettyPrintingOffJsonSerializerGoogleGson excludeNothingWithPrettyPrintingOff,
            final CommandProcessingResultJsonSerializer commandProcessingResultSerializer, final GoogleGsonSerializerHelper helper) {
        this.excludeNothingWithPrettyPrintingOff = excludeNothingWithPrettyPrintingOff;
        this.commandProcessingResultSerializer = commandProcessingResultSerializer;
        this.helper = helper;
    }

    @Override
    public String serializeResult(final Object object) {
        return this.commandProcessingResultSerializer.serialize(object);
    }

    @Override
    public String serialize(final Object object) {
        return this.excludeNothingWithPrettyPrintingOff.serialize(object);
    }

    @Override
    public String serialize(final ApiRequestJsonSerializationSettings settings, final Collection<T> collection,
            final Set<String> supportedResponseParameters) {
        final Gson delegatedSerializer = findAppropriateSerializer(settings, supportedResponseParameters);
        return serializeWithSettings(delegatedSerializer, settings, collection.toArray());
    }

    @Override
    public String serialize(final ApiRequestJsonSerializationSettings settings, final T singleObject,
            final Set<String> supportedResponseParameters) {
        final Gson delegatedSerializer = findAppropriateSerializer(settings, supportedResponseParameters);
        return serializeWithSettings(delegatedSerializer, settings, singleObject);
    }

    @Override
    public String serialize(final ApiRequestJsonSerializationSettings settings, final Page<T> singleObject,
            final Set<String> supportedResponseParameters) {
        final Gson delegatedSerializer = findAppropriateSerializer(settings, supportedResponseParameters);
        return serializeWithSettings(delegatedSerializer, settings, singleObject);
    }

    @Override
    public String serialize(final ApiRequestJsonSerializationSettings settings, final Collection<T> collection) {
        final Gson delegatedSerializer = findAppropriateSerializer(settings);
        return serializeWithSettings(delegatedSerializer, settings, collection.toArray());
    }

    @Override
    public String serialize(final ApiRequestJsonSerializationSettings settings, final T singleObject) {
        final Gson delegatedSerializer = findAppropriateSerializer(settings);
        return serializeWithSettings(delegatedSerializer, settings, singleObject);
    }

    @Override
    public String serialize(final ApiRequestJsonSerializationSettings settings, final Page<T> singleObject) {
        final Gson delegatedSerializer = findAppropriateSerializer(settings);
        return serializeWithSettings(delegatedSerializer, settings, singleObject);
    }

    private String serializeWithSettings(final Gson gson, final ApiRequestJsonSerializationSettings settings, final Object[] dataObject) {
        return gson != null ? this.helper.serializedJsonFrom(gson, dataObject) : serialize(dataObject);
    }

    private String serializeWithSettings(final Gson gson, final ApiRequestJsonSerializationSettings settings, final Object dataObject) {
        return gson != null ? this.helper.serializedJsonFrom(gson, dataObject) : serialize(dataObject);
    }

    private Gson findAppropriateSerializer(final ApiRequestJsonSerializationSettings settings,
            final Set<String> supportedResponseParameters) {

        return settings.isPartialResponseRequired() ? this.helper.createGsonBuilderWithParameterExclusionSerializationStrategy(
                supportedResponseParameters, settings.getParametersForPartialResponse()) : null;
    }

    private Gson findAppropriateSerializer(final ApiRequestJsonSerializationSettings settings) {
        return settings.isPartialResponseRequired()
                ? helper.createGsonBuilderForPartialResponseFiltering(settings.getParametersForPartialResponse())
                : null;
    }
}
