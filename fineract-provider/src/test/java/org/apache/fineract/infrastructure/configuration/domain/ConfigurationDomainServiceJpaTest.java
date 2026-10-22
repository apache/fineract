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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.cache.domain.PlatformCacheRepository;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigurationDomainServiceJpaTest {

    private final GlobalConfigurationRepositoryWrapper repository = mock(GlobalConfigurationRepositoryWrapper.class);
    private final ConfigurationDomainServiceJpa underTest = new ConfigurationDomainServiceJpa(mock(PermissionRepository.class), repository,
            mock(PlatformCacheRepository.class));

    @Test
    void parsesLegacySpellingOfStoredValues() {
        stored(GlobalConfigurationConstants.WCL_CHARGE_ACCRUAL_TIME, "REAL-TIME");
        stored(GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME, " Real-Time ");
        stored(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE, "Submitted-Date");
        stored(GlobalConfigurationConstants.NEXT_PAYMENT_DUE_DATE, " NEXT-UNPAID-DUE-DATE ");
        stored(GlobalConfigurationConstants.ASSET_OWNER_TRANSFER_OUTSTANDING_INTEREST_CALCULATION_STRATEGY, "payable_outstanding_interest");

        assertEquals(ChargeAccrualTimeConfig.REAL_TIME, underTest.getWclChargeAccrualTime());
        assertTrue(underTest.isPlChargeAccrualRealTime());
        assertEquals(ChargeAccrualDateConfig.SUBMITTED_DATE, underTest.getAccrualDateConfigForCharge());
        assertEquals(NextPaymentDueDateConfig.NEXT_UNPAID_DUE_DATE, underTest.getNextPaymentDateConfigForLoan());
        assertEquals(OutstandingInterestStrategyConfig.PAYABLE_OUTSTANDING_INTEREST,
                underTest.getAssetOwnerTransferOustandingInterestStrategy());
    }

    @Test
    void usesDefaultsForBlankOrNotAllowedValues() {
        stored(GlobalConfigurationConstants.WCL_CHARGE_ACCRUAL_TIME, "hourly");
        stored(GlobalConfigurationConstants.PL_CHARGE_ACCRUAL_TIME, "realtime");
        stored(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE, null);
        stored(GlobalConfigurationConstants.NEXT_PAYMENT_DUE_DATE, "");
        stored(GlobalConfigurationConstants.ASSET_OWNER_TRANSFER_OUTSTANDING_INTEREST_CALCULATION_STRATEGY, "UNKNOWN");

        assertEquals(ChargeAccrualTimeConfig.EOD, underTest.getWclChargeAccrualTime());
        assertFalse(underTest.isPlChargeAccrualRealTime());
        assertEquals(ChargeAccrualDateConfig.DUE_DATE, underTest.getAccrualDateConfigForCharge());
        assertEquals(NextPaymentDueDateConfig.EARLIEST_UNPAID_DATE, underTest.getNextPaymentDateConfigForLoan());
        assertEquals(OutstandingInterestStrategyConfig.TOTAL_OUTSTANDING_INTEREST,
                underTest.getAssetOwnerTransferOustandingInterestStrategy());
    }

    @Test
    void parsesLoanStatusesIgnoringCaseWhitespaceAndDuplicates() {
        stored(GlobalConfigurationConstants.ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER, "active, Overpaid,ACTIVE");

        assertEquals(List.of(LoanStatus.ACTIVE, LoanStatus.OVERPAID), underTest.getAllowedLoanStatusesForExternalAssetTransfer());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "ACTIVE,ACTIV", "ACTIVE,,OVERPAID", "INVALID" })
    void usesDefaultLoanStatusesForBlankOrNotAllowedValues(final String stringValue) {
        stored(GlobalConfigurationConstants.ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER, stringValue);
        stored(GlobalConfigurationConstants.ALLOWED_LOAN_STATUSES_OF_DELAYED_SETTLEMENT_FOR_EXTERNAL_ASSET_TRANSFER, stringValue);

        assertEquals(List.of(LoanStatus.ACTIVE, LoanStatus.TRANSFER_IN_PROGRESS, LoanStatus.TRANSFER_ON_HOLD),
                underTest.getAllowedLoanStatusesForExternalAssetTransfer());
        assertEquals(List.of(LoanStatus.ACTIVE, LoanStatus.TRANSFER_IN_PROGRESS, LoanStatus.TRANSFER_ON_HOLD, LoanStatus.OVERPAID,
                LoanStatus.CLOSED_OBLIGATIONS_MET), underTest.getAllowedLoanStatusesOfDelayedSettlementForExternalAssetTransfer());
    }

    @Test
    void returnsCanonicalAccountMapping() {
        stored(GlobalConfigurationConstants.ACCOUNT_MAPPING_FOR_CHARGE, "income,EXPENSE", true);

        assertEquals("Income,Expense", underTest.getAccountMappingForCharge());
    }

    @Test
    void usesDefaultAccountMappingWhenDisabledOrNotAllowed() {
        stored(GlobalConfigurationConstants.ACCOUNT_MAPPING_FOR_CHARGE, "Expense", false);
        stored(GlobalConfigurationConstants.ACCOUNT_MAPPING_FOR_PAYMENT_TYPE, "Asset,Income", true);

        assertEquals("Income", underTest.getAccountMappingForCharge());
        assertEquals("Asset", underTest.getAccountMappingForPaymentType());
    }

    private void stored(final String name, final String stringValue) {
        stored(name, stringValue, false);
    }

    private void stored(final String name, final String stringValue, final boolean enabled) {
        when(repository.findOneByNameWithNotFoundDetection(name))
                .thenReturn(new GlobalConfigurationProperty().setName(name).setStringValue(stringValue).setEnabled(enabled));
    }
}
