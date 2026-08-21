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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.CurrencyConfigurationData;
import org.apache.fineract.client.models.CurrencyUpdateRequest;
import org.apache.fineract.client.models.GetClientsChargesPageItems;
import org.apache.fineract.client.models.GetClientsClientIdChargesResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsClientIdChargesRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class ClientChargeRoundingTest extends FeignLoanTestBase {

    private Long clientId;
    private static final String DATE = "01 January 2026";

    @BeforeEach
    public void setup() {
        enableRequiredCurrencies();
        clientId = createClient();
    }

    @Test
    public void shouldRoundUsdClientChargeTo_TwoDecimalPlaces() {
        PostChargesResponse chargesResponse = createFlatClientCharge(19.876, "USD");

        applyChargeToClient(clientId, chargesResponse.getResourceId(), new BigDecimal("19.876"));

        GetClientsChargesPageItems charge = getSingleClientCharge(clientId);

        assertNotNull(charge);
        BigDecimal actualChargeAmount = charge.getAmount();
        BigDecimal expectedChargeAmount = new BigDecimal("19.88");
        assertAmountEquals(expectedChargeAmount, actualChargeAmount);
    }

    @Test
    public void shouldRoundJpyClientChargeTo_ZeroDecimalPlaces() {
        PostChargesResponse chargesResponse = createFlatClientCharge(19.8, "JPY");

        applyChargeToClient(clientId, chargesResponse.getResourceId(), new BigDecimal("19.8"));

        GetClientsChargesPageItems charge = getSingleClientCharge(clientId);

        assertNotNull(charge);
        BigDecimal actualChargeAmount = charge.getAmount();
        BigDecimal expectedChargeAmount = new BigDecimal("20");
        assertAmountEquals(expectedChargeAmount, actualChargeAmount);
    }

    @Test
    public void shouldRoundUpJpyClientCharge_whenValueIsAboveHalfTo_ZeroDecimalPlaces() {
        PostChargesResponse chargesResponse = createFlatClientCharge(0.55, "JPY");

        applyChargeToClient(clientId, chargesResponse.getResourceId(), new BigDecimal("0.55"));

        GetClientsChargesPageItems charge = getSingleClientCharge(clientId);

        assertNotNull(charge);
        BigDecimal actualChargeAmount = charge.getAmount();
        BigDecimal expectedChargeAmount = new BigDecimal("1");
        assertAmountEquals(expectedChargeAmount, actualChargeAmount);
    }

    @Test
    public void shouldFailToAddJpyClientCharge_whenRoundedToZero() {
        PostChargesResponse chargesResponse = createFlatClientCharge(0.5, "JPY");

        CallFailedRuntimeException exception = applyChargeToClientExpectingError(clientId, chargesResponse.getResourceId(),
                new BigDecimal("0.5"));

        assertEquals(400, exception.getStatus());
        assertTrue(exception.getResponseBody().contains("error.msg.client.charge.amount.rounded.to.zero"));
        assertFalse(hasAnyClientCharges(clientId), "Expected no client charge to be created when rounded amount becomes 0");
    }

    // -----------------------------
    // HELPERS
    // -----------------------------

    private PostChargesResponse createFlatClientCharge(double amount, String currencyCode) {
        String uniqueChargeName = "Client Charge Flat " + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(3) // CLIENT
                .chargeTimeType(2).chargeCalculationType(1) // FLAT
                .amount(amount).currencyCode(currencyCode).locale("en").active(true).penalty(false));
    }

    private void applyChargeToClient(Long clientId, Long chargeId, BigDecimal amount) {
        chargesHelper.addClientCharge(clientId, clientChargeRequest(chargeId, amount));
    }

    private CallFailedRuntimeException applyChargeToClientExpectingError(Long clientId, Long chargeId, BigDecimal amount) {
        return assertThrows(CallFailedRuntimeException.class,
                () -> chargesHelper.addClientCharge(clientId, clientChargeRequest(chargeId, amount)));
    }

    private PostClientsClientIdChargesRequest clientChargeRequest(Long chargeId, BigDecimal amount) {
        return new PostClientsClientIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(amount)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(DATETIME_PATTERN)//
                .dueDate(DATE);
    }

    private GetClientsChargesPageItems getSingleClientCharge(Long clientId) {
        GetClientsClientIdChargesResponse response = chargesHelper.getClientCharges(clientId);
        assertNotNull(response);
        Set<GetClientsChargesPageItems> pageItems = response.getPageItems();
        assertNotNull(pageItems);
        assertEquals(1, pageItems.size());
        return pageItems.iterator().next();
    }

    private boolean hasAnyClientCharges(Long clientId) {
        GetClientsClientIdChargesResponse response = chargesHelper.getClientCharges(clientId);
        return response != null && response.getPageItems() != null && !response.getPageItems().isEmpty();
    }

    private void assertAmountEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }

    /**
     * Currencies used by these tests must be explicitly enabled in the currency configuration. Otherwise, charge
     * creation and persistence may succeed while retrieval APIs do not return the charges.
     */
    private void enableRequiredCurrencies() {
        CurrencyUpdateRequest request = new CurrencyUpdateRequest().currencies(List.of("USD", "JPY"));

        FineractFeignClientHelper.getFineractFeignClient().currency().updateCurrencies(request);

        CurrencyConfigurationData data = FineractFeignClientHelper.getFineractFeignClient().currency().retrieveCurrencies();

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
