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
package org.apache.fineract.integrationtests.organization.teller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTellerHelper;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Covers the behaviour introduced for FINERACT-2724: a non-numeric {@code txnAmount} on the "allocate cash to cashier"
 * endpoint must be rejected with a field-specific "not a valid number" validation error (via
 * {@code HttpMessageNotReadableErrorController}) rather than the generic invalid-JSON error, and without requiring
 * {@code txnAmount} to be widened from {@code BigDecimal} to {@code String} on the API contract.
 */
public class AllocateCashToCashierValidationTest extends FeignIntegrationTest {

    private FeignTellerHelper tellerHelper;
    private Long tellerId;
    private Long cashierId;

    @BeforeAll
    public void ensureCashierFinancialActivityAccountsExist() {
        final FeignAccountHelper accountHelper = new FeignAccountHelper(fineractClient());
        final FeignFinancialActivityAccountHelper financialActivityAccountHelper = new FeignFinancialActivityAccountHelper(
                fineractClient());

        // Allocating cash to a cashier posts journal entries between these two financial-activity accounts; the
        // teller endpoint 404s if either mapping is missing, so tests must not rely on it being seeded already.
        ensureFinancialActivityAccountMapping(financialActivityAccountHelper, accountHelper,
                FinancialActivity.CASH_AT_MAINVAULT.getValue());
        ensureFinancialActivityAccountMapping(financialActivityAccountHelper, accountHelper, FinancialActivity.CASH_AT_TELLER.getValue());
    }

    private static void ensureFinancialActivityAccountMapping(final FeignFinancialActivityAccountHelper financialActivityAccountHelper,
            final FeignAccountHelper accountHelper, final Integer financialActivityId) {
        final boolean alreadyMapped = financialActivityAccountHelper.getAllMappings().stream()
                .anyMatch(mapping -> financialActivityId.equals(mapping.getFinancialActivityData().getId()));
        if (alreadyMapped) {
            return;
        }

        final Account assetAccount = accountHelper.createAssetAccount();
        financialActivityAccountHelper.createMapping(financialActivityId, assetAccount);
    }

    @BeforeEach
    public void setup() {
        tellerHelper = new FeignTellerHelper(fineractClient());

        final Long staffId = new FeignStaffHelper(fineractClient()).createStaff().getResourceId();
        tellerId = tellerHelper.createTeller().getResourceId();
        cashierId = tellerHelper.createCashier(tellerId, staffId);
    }

    @Test
    public void allocateCashWithNonNumericAmountReturnsFieldSpecificValidationError() {
        final CallFailedRuntimeException exception = tellerHelper.allocateCashWithTextAmountExpectingError(tellerId, cashierId,
                "not-a-number");

        assertEquals(400, exception.getStatus());
        final FeignErrors.RejectedValue rejected = FeignErrors.rejectedValue(exception);
        assertEquals("validation.msg.invalid.decimal.format", rejected.userMessageGlobalisationCode());
        assertEquals("txnAmount", rejected.parameterName());
        assertEquals("not-a-number", rejected.value());
    }

    @Test
    public void allocateCashWithValidNumericAmountIsNotRejectedAsInvalidNumber() {
        assertNotNull(tellerHelper.allocateCash(tellerId, cashierId, FeignTellerHelper.allocateCashRequest(BigDecimal.valueOf(100))));
    }

    @Test
    public void allocateCashWithMalformedJsonStillReturnsGenericInvalidJsonError() {
        final String malformedJson = "{\"currencyCode\":\"USD\",\"txnAmount\":100,\"txnDate\":\"01 January 2023\"";

        final CallFailedRuntimeException exception = tellerHelper.allocateCashWithBodyExpectingError(tellerId, cashierId, malformedJson);

        assertEquals(400, exception.getStatus());
        assertEquals("error.msg.invalid.json.data", exception.getUserMessageGlobalisationCode());
    }

}
