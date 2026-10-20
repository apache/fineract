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
package org.apache.fineract.infrastructure.configuration.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationAllowedValues.AllowedValues;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyValueNotAllowedException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

class GlobalConfigurationAllowedValuesTest {

    private static AllowedValues<?> allowedValuesOf(final String configName) {
        return GlobalConfigurationAllowedValues.find(configName).orElseThrow();
    }

    @Test
    void configWithoutRestrictionHasNoAllowedValues() {
        assertTrue(GlobalConfigurationAllowedValues.find(GlobalConfigurationConstants.REPORT_EXPORT_S3_FOLDER_NAME).isEmpty());
    }

    @Test
    void singleValueConfigListsEnumValues() {
        final AllowedValues<?> allowedValues = allowedValuesOf(GlobalConfigurationConstants.WCL_CHARGE_ACCRUAL_TIME);

        assertEquals(List.of("eod", "real-time"), allowedValues.values());
        assertFalse(allowedValues.multipleValuesAllowed());
    }

    @Test
    void loanStatusConfigExcludesInvalidStatus() {
        final AllowedValues<?> allowedValues = allowedValuesOf(
                GlobalConfigurationConstants.ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER);

        assertTrue(allowedValues.multipleValuesAllowed());
        assertTrue(allowedValues.values().contains("ACTIVE"));
        assertFalse(allowedValues.values().contains("INVALID"));
    }

    @Test
    void singleValueIsCanonicalizedIgnoringCaseAndWhitespace() {
        final AllowedValues<?> allowedValues = allowedValuesOf(GlobalConfigurationConstants.WCL_CHARGE_ACCRUAL_TIME);

        assertEquals("eod", allowedValues.canonicalize(" EOD "));
        assertEquals("real-time", allowedValues.canonicalize("Real-Time"));
    }

    @Test
    void singleValueConfigDoesNotSplitOnSeparator() {
        final AllowedValues<?> allowedValues = allowedValuesOf(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE);

        assertFalse(allowedValues.isAllowed("due-date,submitted-date"));
    }

    @Test
    void multipleValuesAreCanonicalizedAndDeduplicated() {
        final AllowedValues<?> allowedValues = allowedValuesOf(GlobalConfigurationConstants.ACCOUNT_MAPPING_FOR_PAYMENT_TYPE);

        assertEquals("Asset,Liability", allowedValues.canonicalize("asset, LIABILITY ,Asset"));
    }

    @Test
    void isAllowedRequiresEveryElementToMatch() {
        final AllowedValues<?> allowedValues = allowedValuesOf(
                GlobalConfigurationConstants.ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER);

        assertTrue(allowedValues.isAllowed("active,Overpaid"));
        assertFalse(allowedValues.isAllowed("ACTIVE,ACTIV"));
        assertFalse(allowedValues.isAllowed("ACTIVE,,OVERPAID"));
        assertFalse(allowedValues.isAllowed(null));
    }

    @Test
    void resolveReturnsAllowedConstantOrDefault() {
        final AllowedValues<NextPaymentDueDateConfig> allowedValues = GlobalConfigurationAllowedValues.NEXT_PAYMENT_DUE_DATE;

        assertEquals(NextPaymentDueDateConfig.NEXT_UNPAID_DUE_DATE, allowedValues.resolve("Next-Unpaid-Due-Date"));
        assertEquals(NextPaymentDueDateConfig.EARLIEST_UNPAID_DATE, allowedValues.resolve("UNKNOWN"));
        assertEquals(NextPaymentDueDateConfig.EARLIEST_UNPAID_DATE, allowedValues.resolve(" "));
        assertEquals(NextPaymentDueDateConfig.EARLIEST_UNPAID_DATE, allowedValues.resolve(null));
    }

    @Test
    void resolveFailsWithoutDefaultWhenValueIsBlankOrNotAllowed() {
        final AllowedValues<OutstandingInterestStrategyConfig> allowedValues = GlobalConfigurationAllowedValues.OUTSTANDING_INTEREST_STRATEGY;

        assertEquals(OutstandingInterestStrategyConfig.PAYABLE_OUTSTANDING_INTEREST, allowedValues.resolve("payable_outstanding_interest"));
        assertThrows(GlobalConfigurationPropertyValueNotAllowedException.class, () -> allowedValues.resolve("UNKNOWN"));
        assertThrows(GlobalConfigurationPropertyValueNotAllowedException.class, () -> allowedValues.resolve(" "));
        assertThrows(GlobalConfigurationPropertyValueNotAllowedException.class, () -> allowedValues.resolve(null));
    }

    @Test
    void resolveAllFailsWithoutDefaultWhenAnyElementIsNotAllowed() {
        final AllowedValues<LoanStatus> allowedValues = GlobalConfigurationAllowedValues.ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER;

        assertEquals(List.of(LoanStatus.ACTIVE, LoanStatus.OVERPAID), allowedValues.resolveAll("Active,overpaid,ACTIVE"));
        assertThrows(GlobalConfigurationPropertyValueNotAllowedException.class, () -> allowedValues.resolveAll("ACTIVE,INVALID"));
    }

    @Test
    void transferConfigsHaveNoDefault() {
        assertFalse(GlobalConfigurationAllowedValues.OUTSTANDING_INTEREST_STRATEGY.hasDefault());
        assertFalse(GlobalConfigurationAllowedValues.ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER.hasDefault());
        assertFalse(GlobalConfigurationAllowedValues.ALLOWED_LOAN_STATUSES_OF_DELAYED_SETTLEMENT_FOR_EXTERNAL_ASSET_TRANSFER.hasDefault());
    }

    @Test
    void resolveValueReturnsCanonicalValueOrDefault() {
        final AllowedValues<?> allowedValues = GlobalConfigurationAllowedValues.ACCOUNT_MAPPING_FOR_CHARGE;

        assertEquals("Income,Expense", allowedValues.resolveValue("income, EXPENSE"));
        assertEquals("Income", allowedValues.resolveValue("Income,Asset"));
    }

    @Test
    void everyDefaultIsAnAllowedCanonicalValue() {
        for (final AllowedValues<?> allowedValues : GlobalConfigurationAllowedValues.ALL) {
            if (!allowedValues.hasDefault()) {
                continue;
            }
            assertTrue(allowedValues.isAllowed(allowedValues.defaultValue()), allowedValues.configName());
            assertEquals(allowedValues.defaultValue(), allowedValues.canonicalize(allowedValues.defaultValue()),
                    allowedValues.configName());
        }
    }

    @Test
    void everyRestrictedConfigIsFoundByName() {
        for (final AllowedValues<?> allowedValues : GlobalConfigurationAllowedValues.ALL) {
            assertSame(allowedValues, GlobalConfigurationAllowedValues.find(allowedValues.configName()).orElseThrow());
        }
    }
}
