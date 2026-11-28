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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders.createActivePersonClient;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The client resources now return typed objects, which Jackson writes instead of GSON. These tests read the raw JSON,
 * not the generated client model, because the generated client accepts both date shapes and ignores missing fields.
 */
public class FeignClientsLocalDateLocaleTest extends FeignIntegrationTest {

    private static final BigDecimal CLIENT_CHARGE_AMOUNT = BigDecimal.valueOf(200);

    private FeignClientHelper clientHelper;
    private FeignChargesHelper chargesHelper;
    private LocalDate submittedOn;
    private LocalDate activatedOn;

    @BeforeAll
    void setupHelpers() {
        clientHelper = new FeignClientHelper(fineractClient());
        chargesHelper = new FeignChargesHelper(fineractClient());
        final LocalDate today = Utils.getLocalDateOfTenant();
        submittedOn = today.minusDays(17);
        activatedOn = today.minusDays(7);
    }

    @Test
    void clientDatesAreWrittenAsArraysAndMissingExternalIdIsLeftOut() {
        final PostClientsRequest request = createActivePersonClient(format(activatedOn)).submittedOnDate(format(submittedOn));
        final Long clientId = clientHelper.createClient(request).getClientId();

        try (Response response = api().getClient(clientId)) {
            final JsonNode json = readTree(response);
            assertDate(json.get("activationDate"), activatedOn);
            assertDate(json.get("timeline").get("submittedOnDate"), submittedOn);
            assertDate(json.get("timeline").get("activatedOnDate"), activatedOn);
            assertThat(json.has("externalId")).isFalse();
        }
    }

    @Test
    void chargeAndTransactionDatesAreWrittenAsArrays() {
        final Long clientId = clientHelper.createClient(createActivePersonClient(format(activatedOn))).getClientId();
        final Long chargeId = chargesHelper.createClientSpecifiedDueDateCharge(20).getResourceId();
        final Long clientChargeId = chargesHelper
                .addClientCharge(clientId, ChargeRequestBuilders.clientCharge(chargeId, format(activatedOn), CLIENT_CHARGE_AMOUNT))
                .getResourceId();
        final Long transactionId = chargesHelper
                .payClientCharge(clientId, clientChargeId, ChargeRequestBuilders.payClientCharge(format(activatedOn), 10))
                .getTransactionId();

        try (Response response = api().getClientCharge(clientId, clientChargeId)) {
            assertDate(readTree(response).get("dueDate"), activatedOn);
        }
        try (Response response = api().getClientTransaction(clientId, transactionId)) {
            final JsonNode json = readTree(response);
            assertDate(json.get("date"), activatedOn);
            assertDate(json.get("submittedOnDate"), Utils.getLocalDateOfTenant());
        }
    }

    private ClientApiForTests api() {
        return fineractClient().create(ClientApiForTests.class);
    }

    private static String format(final LocalDate date) {
        return Utils.dateFormatter.format(date);
    }

    private static void assertDate(final JsonNode node, final LocalDate expected) {
        assertThat(node.isArray()).as("%s is a [year, month, day] array", node).isTrue();
        assertThat(node).hasSize(3);
        assertThat(node.get(0).asInt()).isEqualTo(expected.getYear());
        assertThat(node.get(1).asInt()).isEqualTo(expected.getMonthValue());
        assertThat(node.get(2).asInt()).isEqualTo(expected.getDayOfMonth());
    }

    private static JsonNode readTree(final Response response) {
        assertEquals(200, response.status());
        try {
            return ObjectMapperFactory.getShared().readTree(response.body().asInputStream());
        } catch (final Exception e) {
            throw new AssertionError("Unreadable response: " + response.body(), e);
        }
    }

    interface ClientApiForTests {

        @RequestLine("GET /v1/clients/{clientId}")
        Response getClient(@Param("clientId") Long clientId);

        @RequestLine("GET /v1/clients/{clientId}/charges/{chargeId}")
        Response getClientCharge(@Param("clientId") Long clientId, @Param("chargeId") Long chargeId);

        @RequestLine("GET /v1/clients/{clientId}/transactions/{transactionId}")
        Response getClientTransaction(@Param("clientId") Long clientId, @Param("transactionId") Long transactionId);
    }
}
