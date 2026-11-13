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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feign.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.fineract.integrationtests.client.feign.helpers.ServerPagesApi;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.junit.jupiter.api.Test;

public class JerseyMetricsIntegrationTest {

    private final ServerPagesApi serverPagesApi = FineractFeignClientHelper.createProviderRootFeignClient().create(ServerPagesApi.class);

    @Test
    public void testApiRequestIsTaggedWithResourceTemplate() throws IOException {
        OfficeHelper.getHeadOffice();

        try (Response prometheus = serverPagesApi.actuatorPrometheus(); InputStream body = prometheus.body().asInputStream()) {
            assertEquals(200, prometheus.status());
            String metrics = new String(body.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(
                    metrics.lines()
                            .anyMatch(line -> line.startsWith("http_server_requests_seconds_count")
                                    && line.contains("uri=\"/fineract-provider/api/v1/offices/{officeId}\"")),
                    "No http.server.requests series for the offices template");
        }
    }
}
