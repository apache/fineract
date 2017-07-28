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

import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Used to process the query parameters provided in a API request to see
 * features of the RESTful API are being asked for such as:
 * </p>
 * <ul>
 * <li>Pretty printing through pretty=true, defaults to false</li>
 * <li>Partial response through fields=id, name etc, when empty, the full data
 * is returned by default.</li>
 * </ul>
 */
@Component
public class ApiRequestParameterHelper {

    public ApiRequestJsonSerializationSettings process(final MultivaluedMap<String, String> queryParameters,
            final Set<String> mandatoryResponseParameters) {

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(queryParameters);
        if (!responseParameters.isEmpty()) {
            responseParameters.addAll(mandatoryResponseParameters);
        }
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(queryParameters);
        final boolean template = ApiParameterHelper.template(queryParameters);
        final boolean makerCheckerable = ApiParameterHelper.makerCheckerable(queryParameters);
        final boolean includeJson = ApiParameterHelper.includeJson(queryParameters);

        return ApiRequestJsonSerializationSettings.from(prettyPrint, responseParameters, template, makerCheckerable, includeJson);
    }

    public ApiRequestJsonSerializationSettings process(final MultivaluedMap<String, String> queryParameters) {

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(queryParameters);
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(queryParameters);
        final boolean template = ApiParameterHelper.template(queryParameters);
        final boolean makerCheckerable = ApiParameterHelper.makerCheckerable(queryParameters);
        final boolean includeJson = ApiParameterHelper.includeJson(queryParameters);

        return ApiRequestJsonSerializationSettings.from(prettyPrint, responseParameters, template, makerCheckerable, includeJson);
    }
}