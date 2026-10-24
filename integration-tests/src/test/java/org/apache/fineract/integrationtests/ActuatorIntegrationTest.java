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

import java.util.Map;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.ServerPagesApi;
import org.apache.fineract.integrationtests.client.feign.helpers.ServerPagesApi.ActuatorInfo;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.Test;

public class ActuatorIntegrationTest {

    private final ServerPagesApi serverPagesApi = FineractFeignClientHelper.createProviderRootFeignClient().create(ServerPagesApi.class);

    @Test
    public void testActuatorGitBuildInfo() {
        ApiResponse<ActuatorInfo> response = serverPagesApi.actuatorInfoWithHttpInfo();

        assertEquals(200, response.getStatusCode());
        assertEquals("application/json", String.join(",", response.getHeaders().get("Content-Type")));
        Map<String, Object> gitBuildInfo = response.getData().git();

        assertTrue(gitBuildInfo.containsKey("branch"));
        assertTrue(gitBuildInfo.containsKey("remote"));

    }

}
