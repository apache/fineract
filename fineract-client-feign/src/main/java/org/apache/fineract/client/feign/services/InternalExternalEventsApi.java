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
package org.apache.fineract.client.feign.services;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 * Internal testing API for external events. These endpoints are only available when the TEST profile is active.
 */
public interface InternalExternalEventsApi {

    /**
     * Returns the raw JSON array of external events persisted in {@code m_external_event}, so callers can tell whether
     * an event was ever recorded independently of whether it was delivered to the message broker.
     */
    @RequestLine("GET v1/internal/externalevents?type={type}&aggregateRootId={aggregateRootId}")
    @Headers("Content-Type: application/json")
    String getExternalEvents(@Param("type") String type, @Param("aggregateRootId") Long aggregateRootId);
}
