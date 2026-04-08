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

import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A class to encapsulate settings we allow on API that affect how JSON is to be serialized for the response to api
 * call.
 */
@Getter
@RequiredArgsConstructor
public class ApiRequestJsonSerializationSettings {

    private final Set<String> parametersForPartialResponse;
    private final boolean template;
    private final boolean makerCheckerable;
    private final boolean includeJson;

    public static ApiRequestJsonSerializationSettings from(final Set<String> parametersForPartialResponse, final boolean template,
            final boolean makerCheckerable, final boolean includeJson) {

        // FIXME - KW - rather than always creating new objects for this could
        // just send by common ones like, prettyprint=false, empty response
        // parameters
        return new ApiRequestJsonSerializationSettings(parametersForPartialResponse, template, makerCheckerable, includeJson);
    }

    public boolean isPartialResponseRequired() {
        return !this.parametersForPartialResponse.isEmpty();
    }
}
