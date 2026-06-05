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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.client.models.GetFixedDepositAccountsAccountIdResponse;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignDepositTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.DepositRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestData;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestValidators;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Savings COB maturing a fixed deposit whose maturity instruction is transfer to savings. The maturity step credits
 * another account inside the chunk transaction, so the target is checked both outside and inside the same run.
 */
public class SavingsCOBFixedDepositMaturityIntegrationTest extends FeignDepositTestBase {

    private static final String SAVINGS_COB_JOB_NAME = "SAVINGS_COB";
    private static final DateTimeFormatter DAY_MONTH_YEAR = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
    private static final LocalDate ACTIVATION_DATE = LocalDate.of(2024, 1, 1);
    private static final int DEPOSIT_PERIOD_MONTHS = 6;
    private static final LocalDate MATURITY_DATE = ACTIVATION_DATE.plusMonths(DEPOSIT_PERIOD_MONTHS);

    @BeforeEach
    public void enableBusinessDateOnActivation() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        updateBusinessDate(BusinessDateType.BUSINESS_DATE, ACTIVATION_DATE);
        updateBusinessDate(BusinessDateType.COB_DATE, ACTIVATION_DATE.minusDays(1));
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    @Test
    public void savingsCOBTransfersMaturityAmountToSavingsOutsideTheRun() {
        final Long clientId = clientHelper.createClient();
        final Long savingsId = createActiveZeroInterestSavings(clientId);
        final Long fixedDepositId = createActiveFixedDepositTransferringTo(clientId, savingsId);

        closeMaturityDate(List.of(fixedDepositId));

        assertMaturityAmountTransferred(fixedDepositId, savingsId);
    }

    @Test
    public void savingsCOBTransfersMaturityAmountToSavingsProcessedInTheSameRun() {
        final Long clientId = clientHelper.createClient();
        final Long savingsId = createActiveZeroInterestSavings(clientId);
        final Long fixedDepositId = createActiveFixedDepositTransferringTo(clientId, savingsId);

        closeMaturityDate(List.of(savingsId, fixedDepositId));

        assertMaturityAmountTransferred(fixedDepositId, savingsId);
    }

    private void assertMaturityAmountTransferred(final Long fixedDepositId, final Long savingsId) {
        final GetFixedDepositAccountsAccountIdResponse fixedDeposit = fixedDepositHelper.getAccount(fixedDepositId);
        DepositTestValidators.verifyFixedDepositAccountIsClosed(fixedDeposit.getStatus());
        assertThat(fixedDeposit.getMaturityAmount()).as("Maturity amount must include the interest")
                .isGreaterThan(DepositTestData.DEPOSIT_AMOUNT);
        assertThat(fixedDeposit.getSummary().getAccountBalance()).as("Closed fixed deposit must be emptied")
                .isEqualByComparingTo(BigDecimal.ZERO);

        // The savings account starts empty and earns no interest, so its balance is exactly what the transfer credited.
        assertThat(savingsHelper.getSavingsDetails(savingsId).getSummary().getAccountBalance())
                .as("Savings account must receive the maturity amount once").isEqualByComparingTo(fixedDeposit.getMaturityAmount());
    }

    private Long createActiveZeroInterestSavings(final Long clientId) {
        final Long productId = savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().nominalAnnualInterestRate(0.0)).getResourceId();
        final Long savingsId = savingsHelper.submitApplication(clientId, productId, format(ACTIVATION_DATE)).getSavingsId();
        savingsHelper.approveSavings(savingsId, format(ACTIVATION_DATE));
        savingsHelper.activateSavings(savingsId, format(ACTIVATION_DATE));
        return savingsId;
    }

    private Long createActiveFixedDepositTransferringTo(final Long clientId, final Long savingsId) {
        final Long productId = fixedDepositProductHelper.createProduct(
                DepositRequestBuilders.withChart(DepositRequestBuilders.fixedDepositProduct(), format(ACTIVATION_DATE.minusMonths(1)),
                        format(ACTIVATION_DATE.plusYears(10)), DepositTestData.periodRangeChartSlabs()))
                .getResourceId();
        final Long fixedDepositId = fixedDepositHelper.submitApplication(DepositRequestBuilders
                .fixedDepositAccount(clientId, productId, format(ACTIVATION_DATE), DepositTestData.PreClosurePenalInterestOnType.WHOLE_TERM)
                .depositPeriod(DEPOSIT_PERIOD_MONTHS) //
                .maturityInstructionId(DepositTestData.AccountClosureType.TRANSFER_TO_SAVINGS) //
                .transferToSavingsId(savingsId)).getSavingsId();
        fixedDepositHelper.approve(fixedDepositId, format(ACTIVATION_DATE));
        fixedDepositHelper.activate(fixedDepositId, format(ACTIVATION_DATE));
        DepositTestValidators.verifyFixedDepositIsActive(fixedDepositHelper.getAccount(fixedDepositId).getStatus());
        return fixedDepositId;
    }

    private void closeMaturityDate(final List<Long> savingsIds) {
        updateBusinessDate(BusinessDateType.BUSINESS_DATE, MATURITY_DATE.plusDays(1));
        updateBusinessDate(BusinessDateType.COB_DATE, MATURITY_DATE);
        ok(() -> fineractClient().inlineJob().executeInlineJob(SAVINGS_COB_JOB_NAME, new InlineJobRequest().savingsIds(savingsIds)));
    }

    private void updateBusinessDate(final BusinessDateType type, final LocalDate date) {
        businessDateHelper.updateBusinessDate(type.name(), date.toString());
    }

    private static String format(final LocalDate date) {
        return DAY_MONTH_YEAR.format(date);
    }
}
