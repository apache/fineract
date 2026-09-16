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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanAccountRepaymentCalculationTest extends FeignLoanTestBase {

    @Test
    public void loanAccountWithEnableDownPaymentWithInstallmentsInMultipleOfNullRepaymentScheduleCalculationTest() {
        try {

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(disbursementDate.toString());

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Long clientId = createClient();

            // Loan Product creation with down-payment configuration and installmentAmountInMultiplesOf as null
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursements(
                    enableDownPayment, "25", enableAutoRepaymentForDownPayment, null);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account with amount 1250

            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(), 1250.0,
                    loanExternalIdStr, LoanProductTestBuilder.DEFAULT_STRATEGY);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // verify loan schedule
            assertNotNull(loanDetails.getRepaymentSchedule());

            // first period [2023-03-03 to 2023-03-03] down payment installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 312.5, 1, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 3, 3), false);

            // second period [2023-03-03 to 2023-04-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 312.5, 2, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 4, 3), false);

            // third period [2023-04-03 to 2023-05-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 312.5, 3, LocalDate.of(2023, 4, 3),
                    LocalDate.of(2023, 5, 3), false);

            // fourth period [2023-05-03 to 2023-06-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 312.5, 4, LocalDate.of(2023, 5, 3),
                    LocalDate.of(2023, 6, 3), false);

            // disbursement
            disburseLoan(loanId, "03 March 2023", 1250.0);

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // first period [2023-03-03 to 2023-03-03] down payment installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 312.5, 1, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 3, 3), false);

            // second period [2023-03-03 to 2023-04-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 312.5, 2, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 4, 3), false);

            // third period [2023-04-03 to 2023-05-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 312.5, 3, LocalDate.of(2023, 4, 3),
                    LocalDate.of(2023, 5, 3), false);

            // fourth period [2023-05-03 to 2023-06-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 312.5, 4, LocalDate.of(2023, 5, 3),
                    LocalDate.of(2023, 6, 3), false);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    @Test
    public void loanAccountWithEnableDownPaymentWithInstallmentsInMultipleOfSetAsOneRepaymentScheduleCalculationTest() {
        try {

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(disbursementDate.toString());

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Long clientId = createClient();

            // Loan Product creation with down-payment configuration and installmentAmountInMultiplesOf as 1
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursements(
                    enableDownPayment, "25", enableAutoRepaymentForDownPayment, "1");

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account with amount 1250

            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(), 1250.0,
                    loanExternalIdStr, LoanProductTestBuilder.DEFAULT_STRATEGY);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // verify loan schedule
            assertNotNull(loanDetails.getRepaymentSchedule());

            // first period [2023-03-03 to 2023-03-03] down payment installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 312.0, 1, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 3, 3), false);

            // second period [2023-03-03 to 2023-04-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 313.0, 2, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 4, 3), false);

            // third period [2023-04-03 to 2023-05-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 313.0, 3, LocalDate.of(2023, 4, 3),
                    LocalDate.of(2023, 5, 3), false);

            // fourth period [2023-05-03 to 2023-06-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 312.0, 4, LocalDate.of(2023, 5, 3),
                    LocalDate.of(2023, 6, 3), false);

            // disbursement
            disburseLoan(loanId, "03 March 2023", 1250.0);

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // first period [2023-03-03 to 2023-03-03] down payment installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 312.0, 1, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 3, 3), false);

            // second period [2023-03-03 to 2023-04-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 313.0, 2, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 4, 3), false);

            // third period [2023-04-03 to 2023-05-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 313.0, 3, LocalDate.of(2023, 4, 3),
                    LocalDate.of(2023, 5, 3), false);

            // fourth period [2023-05-03 to 2023-06-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 312.0, 4, LocalDate.of(2023, 5, 3),
                    LocalDate.of(2023, 6, 3), false);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    @Test
    public void loanAccountWithDisableDownPaymentWithInstallmentsInMultipleOfNullRepaymentScheduleCalculationTest() {
        try {

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(disbursementDate.toString());

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = false;

            final Long clientId = createClient();

            // Loan Product creation with down-payment configuration and installmentAmountInMultiplesOf as null
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursements(
                    enableDownPayment, null, false, null);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());

            // create loan account with amount 1250

            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(), 1250.0,
                    loanExternalIdStr, LoanProductTestBuilder.DEFAULT_STRATEGY);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());

            // verify loan schedule
            assertNotNull(loanDetails.getRepaymentSchedule());

            // first period [2023-03-03 to 2023-04-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 416.67, 1, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 4, 3), false);

            // second period [2023-04-03 to 2023-05-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 416.67, 2, LocalDate.of(2023, 4, 3),
                    LocalDate.of(2023, 5, 3), false);

            // third period [2023-05-03 to 2023-06-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 416.66, 3, LocalDate.of(2023, 5, 3),
                    LocalDate.of(2023, 6, 3), false);

            // disbursement
            disburseLoan(loanId, "03 March 2023", 1250.0);

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // first period [2023-03-03 to 2023-04-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 416.67, 1, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 4, 3), false);

            // second period [2023-04-03 to 2023-05-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 416.67, 2, LocalDate.of(2023, 4, 3),
                    LocalDate.of(2023, 5, 3), false);

            // third period [2023-05-03 to 2023-06-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 416.66, 3, LocalDate.of(2023, 5, 3),
                    LocalDate.of(2023, 6, 3), false);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    @Test
    public void loanAccountWithDisableDownPaymentWithInstallmentsInMultipleOfSetAsOneRepaymentScheduleCalculationTest() {
        try {

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(disbursementDate.toString());

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = false;

            final Long clientId = createClient();

            // Loan Product creation with down-payment configuration and installmentAmountInMultiplesOf as 1
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursements(
                    enableDownPayment, null, false, "1");

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());

            // create loan account with amount 1250

            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(), 1250.0,
                    loanExternalIdStr, LoanProductTestBuilder.DEFAULT_STRATEGY);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());

            // verify loan schedule
            assertNotNull(loanDetails.getRepaymentSchedule());

            // first period [2023-03-03 to 2023-04-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 417.0, 1, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 4, 3), false);

            // second period [2023-04-03 to 2023-05-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 417.0, 2, LocalDate.of(2023, 4, 3),
                    LocalDate.of(2023, 5, 3), false);

            // third period [2023-05-03 to 2023-06-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 416.0, 3, LocalDate.of(2023, 5, 3),
                    LocalDate.of(2023, 6, 3), false);

            // disbursement
            disburseLoan(loanId, "03 March 2023", 1250.0);

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails.getRepaymentSchedule());

            // first period [2023-03-03 to 2023-04-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 417.0, 1, LocalDate.of(2023, 3, 3),
                    LocalDate.of(2023, 4, 3), false);

            // second period [2023-04-03 to 2023-05-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 417.0, 2, LocalDate.of(2023, 4, 3),
                    LocalDate.of(2023, 5, 3), false);

            // third period [2023-05-03 to 2023-06-03] regular installment
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 416.0, 3, LocalDate.of(2023, 5, 3),
                    LocalDate.of(2023, 6, 3), false);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    private void verifyPeriodDetails(GetLoansLoanIdRepaymentPeriod period, double expectedAmount, int expectedPeriodNumber,
            LocalDate expectedPeriodFromDate, LocalDate expectedPeriodDueDate, boolean isComplete) {
        assertEquals(expectedPeriodNumber, period.getPeriod());
        assertEquals(expectedPeriodFromDate, period.getFromDate());
        assertEquals(expectedPeriodDueDate, period.getDueDate());
        assertEquals(expectedAmount, Utils.getDoubleValue(period.getTotalInstallmentAmountForPeriod()));
        assertEquals(isComplete, period.getComplete());
    }

    private Long createLoanAccountMultipleRepaymentsDisbursement(final Long clientId, final Long loanProductId,
            final Double principalAmount, final String externalId, final String repaymentStrategy) {

        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, "03 March 2023", principalAmount, 3)//
                .externalId(externalId)//
                .transactionProcessingStrategyCode(repaymentStrategy);

        final Long loanId = loanHelper.applyForLoan(application).getLoanId();
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1250.0, "03 March 2023"));
        return loanId;
    }

    private GetLoanProductsProductIdResponse createLoanProductWithEnableDownPaymentAndMultipleDisbursements(Boolean enableDownPayment,
            String disbursedAmountPercentageForDownPayment, boolean enableAutoRepaymentForDownPayment,
            String installmentAmountInMultiplesOf) {
        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("3").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withInterestTypeAsDecliningBalance().withAmortizationTypeAsEqualInstallments()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .withInstallmentAmountInMultiplesOf(installmentAmountInMultiplesOf).buildRequest(null));
        return retrieveLoanProduct(loanProductId);
    }

}
