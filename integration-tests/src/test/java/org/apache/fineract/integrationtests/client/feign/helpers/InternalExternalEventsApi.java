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
package org.apache.fineract.integrationtests.client.feign.helpers;

import feign.Headers;
import feign.QueryMap;
import feign.RequestLine;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;

/**
 * Feign interface for the internal-only external events API. This endpoint is only available when the server runs with
 * the TEST profile and is not part of the generated OpenAPI client. Check InternalExternalEventsApiResource.java for
 * the server-side implementation.
 */
@Headers({ "Accept: application/json", "Content-Type: application/json" })
public interface InternalExternalEventsApi {

    @RequestLine("GET /v1/internal/externalevents")
    List<ExternalEventResponse> getAllExternalEvents(@QueryMap(encoded = true) Map<String, Object> queryParams);

    @RequestLine("DELETE /v1/internal/externalevents")
    void deleteAllExternalEvents();
}
