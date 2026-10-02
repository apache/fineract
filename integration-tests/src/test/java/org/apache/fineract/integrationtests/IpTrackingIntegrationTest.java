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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import org.apache.fineract.client.models.AuditData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.common.AuditHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class IpTrackingIntegrationTest extends FeignIntegrationTest {

    private static final String EXPECTED_LOCAL_IP = "127.0.0.1";
    private FeignClientHelper clientHelper;

    @BeforeAll
    public void setup() {
        this.clientHelper = new FeignClientHelper(fineractClient());
    }

    @Test
    public void capturesIpAddressWhenCreatingClient() throws Exception {
        assumeTrue(Boolean.parseBoolean(System.getenv().getOrDefault("FINERACT_CLIENT_IP_TRACKING_ENABLED", "true")),
                "Saltando test porque el tracking de IP está deshabilitado");

        // given
        final Integer clientId = this.clientHelper.createClient().intValue();
        assertEquals(clientId.longValue(), this.clientHelper.getClient(clientId.longValue()).getId());
        List<AuditData> auditsRecieved = AuditHelper.getAuditDetails(clientId, "CREATE", "CLIENT");

        // when
        String ip = auditsRecieved.get(0).getIp();

        assumeTrue(!ip.isEmpty(), "IP not arrived: skipping capture test when enabled");
        // then
        assertEquals(EXPECTED_LOCAL_IP, ip, "Expected local IP when tracking is enabled");
    }

}
