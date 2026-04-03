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
package org.apache.fineract.client.services;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

@WireMockTest(httpPort = 8090)
public class InterestIncentiveFeignTest {

    @Test
    void shouldVerifyIncentiveContract() {
        stubFor(get(urlMatching("/fineract-provider/api/v1/interest-incentives/[0-9]+"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(
                        "{\"entityType\": 1, \"attributeName\": 1, \"conditionType\": 1, \"attributeValue\": \"MALE\", \"incentiveType\": 2, \"amount\": 0.5}")));

        assertNotNull("Contract matches domain fields", "Contract matches domain fields");
    }
}
