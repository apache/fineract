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
package org.apache.fineract.infrastructure.core.jersey;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.annotation.PartialResponseParameters;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.exceptionmapper.UnsupportedParameterExceptionMapper;
import org.springframework.stereotype.Component;

/**
 * Applies the partial response query parameter ({@code ?fields=a,b}) to resource methods annotated with
 * {@link PartialResponseParameters}.
 *
 * <p>
 * The GSON based serialization used to do this while writing the response, through an exclusion strategy that matched
 * field names at any depth. Resources that return the object itself are serialized by Jackson, which has no such
 * strategy, so the same pruning happens here on the serialized tree.
 */
@Provider
@Component
@RequiredArgsConstructor
public class PartialResponseFilter implements ContainerResponseFilter {

    private static final String FIELDS_PARAMETER = "fields";

    private final ObjectMapper objectMapper;
    private final UnsupportedParameterExceptionMapper unsupportedParameterExceptionMapper;

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
        final String fields = requestContext.getUriInfo().getQueryParameters().getFirst(FIELDS_PARAMETER);
        if (StringUtils.isBlank(fields) || !isPrunableEntity(responseContext)) {
            return;
        }
        final Set<String> requested = split(fields);
        if (requested.isEmpty()) {
            return;
        }
        final Set<String> supported = supportedParameters(responseContext.getEntityAnnotations());
        if (supported == null) {
            // the method does not take part in partial responses
            return;
        }

        final Predicate<String> shouldSkip;
        if (supported.isEmpty()) {
            // no supported set was declared: keep only what was asked for, at any depth
            shouldSkip = name -> !requested.contains(name);
        } else {
            final List<String> unsupported = new ArrayList<>(requested);
            unsupported.removeAll(supported);
            if (!unsupported.isEmpty()) {
                final Response error = unsupportedParameterExceptionMapper.toResponse(new UnsupportedParameterException(unsupported));
                responseContext.setStatus(error.getStatus());
                responseContext.setEntity(error.getEntity(), new Annotation[0], MediaType.APPLICATION_JSON_TYPE);
                return;
            }
            final Set<String> skip = new LinkedHashSet<>(supported);
            skip.removeAll(requested);
            shouldSkip = skip::contains;
        }

        final JsonNode tree = objectMapper.valueToTree(responseContext.getEntity());
        prune(tree, shouldSkip);
        responseContext.setEntity(tree, responseContext.getEntityAnnotations(), responseContext.getMediaType());
    }

    private boolean isPrunableEntity(final ContainerResponseContext responseContext) {
        final Object entity = responseContext.getEntity();
        if (entity == null || entity instanceof String || entity instanceof JsonNode) {
            return false;
        }
        final MediaType mediaType = responseContext.getMediaType();
        return mediaType != null && MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType);
    }

    private Set<String> supportedParameters(final Annotation[] annotations) {
        for (final Annotation annotation : annotations) {
            if (annotation instanceof PartialResponseParameters partialResponseParameters) {
                return new LinkedHashSet<>(Arrays.asList(partialResponseParameters.value()));
            }
        }
        return null;
    }

    private Set<String> split(final String fields) {
        final Set<String> parameters = new LinkedHashSet<>();
        for (final String field : StringUtils.split(fields, ',')) {
            final String trimmed = field.trim();
            if (!trimmed.isEmpty()) {
                parameters.add(trimmed);
            }
        }
        return parameters;
    }

    private void prune(final JsonNode node, final Predicate<String> shouldSkip) {
        if (node instanceof ObjectNode objectNode) {
            final Iterator<String> names = objectNode.fieldNames();
            final List<String> toRemove = new ArrayList<>();
            while (names.hasNext()) {
                final String name = names.next();
                if (shouldSkip.test(name)) {
                    toRemove.add(name);
                }
            }
            objectNode.remove(toRemove);
        }
        for (final JsonNode child : node) {
            prune(child, shouldSkip);
        }
    }
}
