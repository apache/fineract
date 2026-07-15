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

import static io.restassured.RestAssured.given;
import static org.apache.fineract.integrationtests.common.ClientHelper.addChargesForClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.response.Response;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.CurrencyConfigurationData;
import org.apache.fineract.client.models.CurrencyUpdateRequest;
import org.apache.fineract.client.models.GetClientsChargesPageItems;
import org.apache.fineract.client.models.GetClientsClientIdChargesResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class ClientChargeRoundingTest extends BaseLoanIntegrationTest {

    private Long clientId;
    private ChargesHelper chargesHelper;
    private static final String DATE = "01 January 2026";

    @BeforeEach
    public void setup() throws Exception {
        enableRequiredCurrencies();
        clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        chargesHelper = new ChargesHelper();
    }

    @Test
    public void shouldRoundUsdClientChargeTo_TwoDecimalPlaces() throws Exception {
        PostChargesResponse chargesResponse = createFlatClientCharge(19.876, "USD");

        Integer appliedChargeId = applyChargeToClient(clientId, chargesResponse.getResourceId(), new BigDecimal("19.876"));

        GetClientsChargesPageItems charge = getClientCharge(clientId, appliedChargeId.longValue());

        assertNotNull(charge);
        BigDecimal actualChargeAmount = charge.getAmount();
        BigDecimal expectedChargeAmount = new BigDecimal("19.88");
        assertAmountEquals(expectedChargeAmount, actualChargeAmount);
    }

    @Test
    public void shouldRoundJpyClientChargeTo_ZeroDecimalPlaces() throws Exception {
        PostChargesResponse chargesResponse = createFlatClientCharge(19.8, "JPY");

        Integer appliedChargeId = applyChargeToClient(clientId, chargesResponse.getResourceId(), new BigDecimal("19.8"));

        GetClientsChargesPageItems charge = getClientCharge(clientId, appliedChargeId.longValue());

        assertNotNull(charge);
        BigDecimal actualChargeAmount = charge.getAmount();
        BigDecimal expectedChargeAmount = new BigDecimal("20");
        assertAmountEquals(expectedChargeAmount, actualChargeAmount);
    }

    @Test
    public void shouldRoundUpJpyClientCharge_whenValueIsAboveHalfTo_ZeroDecimalPlaces() throws Exception {
        PostChargesResponse chargesResponse = createFlatClientCharge(0.55, "JPY");

        Integer appliedChargeId = applyChargeToClient(clientId, chargesResponse.getResourceId(), new BigDecimal("0.55"));

        GetClientsChargesPageItems charge = getClientCharge(clientId, appliedChargeId.longValue());

        assertNotNull(charge);
        BigDecimal actualChargeAmount = charge.getAmount();
        BigDecimal expectedChargeAmount = new BigDecimal("1");
        assertAmountEquals(expectedChargeAmount, actualChargeAmount);
    }

    @Test
    public void shouldFailToAddJpyClientCharge_whenRoundedToZero() throws Exception {
        PostChargesResponse chargesResponse = createFlatClientCharge(0.5, "JPY");

        Response response = applyChargeToClientRaw(clientId, chargesResponse.getResourceId(), new BigDecimal("0.5"));

        assertEquals(400, response.statusCode());
        String errorBody = response.asString();
        assertTrue(errorBody.contains("error.msg.client.charge.amount.rounded.to.zero"));
        assertFalse(hasAnyClientCharges(clientId), "Expected no client charge to be created when rounded amount becomes 0");
    }

    // -----------------------------
    // HELPERS
    // -----------------------------

    private PostChargesResponse createFlatClientCharge(double amount, String currencyCode) {
        String uniqueChargeName = "Client Charge Flat " + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharges(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(3) // CLIENT
                .chargeTimeType(2).chargeCalculationType(1) // FLAT
                .amount(amount).currencyCode(currencyCode).locale("en").active(true).penalty(false));
    }

    private Integer applyChargeToClient(Long clientId, Long chargeId, BigDecimal amount) {
        String request = """
                {
                  "chargeId": %d,
                  "amount": %s,
                  "locale": "en",
                  "dateFormat": "dd MMMM yyyy",
                  "dueDate": "%s"
                }
                """.formatted(chargeId, amount.toPlainString(), DATE);

        return addChargesForClient(requestSpec, responseSpec, clientId.intValue(), request);
    }

    private Response applyChargeToClientRaw(Long clientId, Long chargeId, BigDecimal amount) {

        String request = """
                {
                  "chargeId": %d,
                  "amount": %s,
                  "locale": "en",
                  "dateFormat": "dd MMMM yyyy",
                  "dueDate": "%s"
                }
                """.formatted(chargeId, amount.toPlainString(), DATE);

        String url = "/fineract-provider/api/v1/clients/" + clientId + "/charges?" + Utils.TENANT_IDENTIFIER;

        return given().spec(requestSpec).body(request).when().post(url);
    }

    private GetClientsChargesPageItems getClientCharge(Long clientId, Long chargeId) throws IOException {
        return FineractClientHelper.getFineractClient().clientCharges.retrieveOneClientCharge(clientId, chargeId).execute().body();
    }

    private boolean hasAnyClientCharges(Long clientId) throws IOException {

        GetClientsClientIdChargesResponse response = FineractClientHelper.getFineractClient().clientCharges
                .retrieveAllClientCharges(clientId, "all", null, null, null).execute().body();

        return response != null && response.getPageItems() != null && !response.getPageItems().isEmpty();
    }

    private void assertAmountEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }

    /**
     * Currencies used by these tests must be explicitly enabled in the currency configuration. Otherwise, charge
     * creation and persistence may succeed while retrieval APIs do not return the charges.
     */
    private void enableRequiredCurrencies() throws Exception {
        CurrencyUpdateRequest request = new CurrencyUpdateRequest().currencies(List.of("USD", "JPY"));

        FineractClientHelper.getFineractClient().currencies.updateCurrencies(request).execute();

        CurrencyConfigurationData data = FineractClientHelper.getFineractClient().currencies.retrieveCurrencies().execute().body();

        assertNotNull(data);
        assertNotNull(data.getSelectedCurrencyOptions());
        assertCurrencyEnabled(data, "USD");
        assertCurrencyEnabled(data, "JPY");
    }

    private void assertCurrencyEnabled(CurrencyConfigurationData data, String currencyCode) {
        assertTrue(data.getSelectedCurrencyOptions().stream().anyMatch(c -> currencyCode.equals(c.getCode())),
                "Currency " + currencyCode + " should be enabled");
    }
}
