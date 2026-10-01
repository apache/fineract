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
import feign.Param;
import feign.RequestLine;
import java.util.Map;
import org.apache.fineract.client.models.CommandProcessingResult;

/**
 * Updating an external service takes <code>{"&lt;property name&gt;": value}</code>, where the property names belong to
 * the service ({@code s3_access_key}, {@code username}, {@code server_key}). The generated
 * {@code PutExternalServiceRequest} only models the SMTP credentials, and a snake_case property cannot reach the wire
 * from this module: the Retrofit model shadows the Feign one here, and its getters would serialise as camelCase.
 */
@Headers({ "Accept: application/json", "Content-Type: application/json" })
public interface ExternalServiceCommandsApi {

    @RequestLine("PUT /v1/externalservice/{serviceName}")
    CommandProcessingResult updateProperties(@Param("serviceName") String serviceName, Map<String, String> properties);
}
