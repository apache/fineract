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

import feign.RequestLine;
import feign.Response;
import java.util.Map;
import org.apache.fineract.client.models.ApiResponse;

/**
 * Pages the server publishes outside {@code /api}: the actuator, the Swagger UI with its spec, and the legacy API docs.
 * They are not part of the OpenAPI spec, so the generated client has no operations for them. Use it on
 * {@code FineractFeignClientHelper.createProviderRootFeignClient()}, whose base URL is the {@code /fineract-provider}
 * root.
 */
public interface ServerPagesApi {

    @RequestLine("GET /actuator/info")
    ApiResponse<ActuatorInfo> actuatorInfoWithHttpInfo();

    @RequestLine("GET /swagger-ui/index.html")
    Response swaggerUi();

    @RequestLine("GET /fineract.json")
    Response openApiSpec();

    @RequestLine("GET /legacy-docs/apiLive.htm")
    Response legacyApiDocs();

    record ActuatorInfo(Map<String, Object> git) {
    }
}
