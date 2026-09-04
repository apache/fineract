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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanAccountBackdatedDisbursementTest extends FeignLoanTestBase {

    @Test
    public void loanAccountBackDatedDisbursementForLoanProductWithEnableDownPaymentAndScheduleStartDateTypeAsDisbursementDateTest() {
        try {

            // Set business date
            LocalDate businessDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(businessDate.toString());

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = createClient();

            // set repayment start date type as disbursement date
            final Integer repaymentStartDateType = 1;

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            // Loan Product creation with repayment start date type and down payment configuration with multi
            // disbursement
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
                    repaymentStartDateType, enableDownPayment, "25", enableAutoRepaymentForDownPayment);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(repaymentStartDateType, getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
            assertEquals("repaymentStartDateType.disbursementDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account with submitted date as business date (03 March 2023) and expected disbursement date
            // as future date (07 March 2023)
            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));

            // verify schedule is according to expected disbursement date
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // fourth installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));
            // first disbursement on a future date (7 March 2023)

            businessDate = LocalDate.of(2023, 3, 7);

            updateBusinessDate(businessDate.toString());

            disburseLoan(loanId, "07 March 2023", 500.0);

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to disbursement date after first disbursement
            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));

            // first installment down payment repayment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // fourth installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));

            // make repayment on 7 March to pay down payment installment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 March 2023").locale("en")
                            .transactionAmount(125.00));

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));
            assertEquals(375.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));

            // first installment down payment repayment
            // check down payment installment gets paid
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));
            assertEquals(125.00, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // fourth installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));

            // set business date

            businessDate = LocalDate.of(2023, 3, 8);

            updateBusinessDate(businessDate.toString());

            // second disbursement backdated 5 March

            disburseLoan(loanId, "05 March 2023", 500.0);

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to backdated disbursement date after second disbursement

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));
            assertEquals(875.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));

            // first installment down payment repayment for 5 March disbursal
            // check down payment installment gets paid
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));
            assertEquals(125.00, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second installment down payment repayment for 7 March disbursal
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // third installment [5 March 2023 - 5 April 2023]
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 5), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(4).getComplete());

            // fourth installment [5 April 2023 - 5 May 2023]
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(5).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 5), loanDetails.getRepaymentSchedule().getPeriods().get(5).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 5), loanDetails.getRepaymentSchedule().getPeriods().get(5).getDueDate());
            assertEquals(250.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(5).getComplete());

            // fifth installment [5 May 2023 - 5 June 2023]
            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().get(6).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 5), loanDetails.getRepaymentSchedule().getPeriods().get(6).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 5), loanDetails.getRepaymentSchedule().getPeriods().get(6).getDueDate());
            assertEquals(250.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(6).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(6).getComplete());

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void loanAccountBackDatedDisbursementForLoanProductWithEnableDownPaymentAndScheduleStartDateTypeAsSubmittedOnDateTest() {
        try {

            // Set business date
            LocalDate businessDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(businessDate.toString());

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = createClient();

            // set repayment start date type as submitted on date
            final Integer repaymentStartDateType = 2;

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            // Loan Product creation with repayment start date type and down payment configuration with multi
            // disbursement
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
                    repaymentStartDateType, enableDownPayment, "25", enableAutoRepaymentForDownPayment);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(repaymentStartDateType, getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
            assertEquals("repaymentStartDateType.submittedOnDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account with submitted date as business date (03 March 2023) and expected disbursement date
            // as future date (07 March 2023)
            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);

            // verify loan schedule is according to submitted date

            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));

            // verify schedule is according to expected disbursement date
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // fourth installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));

            // first disbursement on a future date (7 March 2023)

            businessDate = LocalDate.of(2023, 3, 7);

            updateBusinessDate(businessDate.toString());

            disburseLoan(loanId, "07 March 2023", 500.0);

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to submitted date after first disbursement
            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));

            // first installment down payment repayment for 7 March disbursal
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));

            // second installment [3 March 2023 - 3 April 2023]
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment [3 April 2023 - 3 May 2023]
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // fourth installment [3 May 2023 - 3 June 2023]
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));

            // make repayment on 7 March to pay downpayment insatllment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 March 2023").locale("en")
                            .transactionAmount(125.00));

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // check down payment installment gets paid for 7 March disbursal

            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));
            assertEquals(125.00, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second installment [3 March 2023 - 3 April 2023]
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment [3 April 2023 - 3 May 2023]
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // fourth installment [3 May 2023 - 3 June 2023]
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));

            businessDate = LocalDate.of(2023, 3, 8);

            updateBusinessDate(businessDate.toString());

            // second disbursement backdated 5 March
            disburseLoan(loanId, "05 March 2023", 500.0);

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to submitted date after second disbursement

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // first installment down payment repayment for 5 March disbursal
            // check down payment installment gets paid
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));
            assertEquals(125.00, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second installment down payment repayment for 7 March disbursal
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // third installment [3 March 2023 - 3 April 2023]
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(4).getComplete());

            // fourth installment [3 April 2023 - 3 May 2023]
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(5).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getDueDate());
            assertEquals(250.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(5).getComplete());

            // fifth installment [3 May 2023 - 3 June 2023]
            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().get(6).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 3), loanDetails.getRepaymentSchedule().getPeriods().get(6).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 3), loanDetails.getRepaymentSchedule().getPeriods().get(6).getDueDate());
            assertEquals(250.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(6).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(6).getComplete());

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void loanAccountBackDatedDisbursementAfterTwoRepaymentsForLoanProductWithEnableDownPaymentAndScheduleStartDateTypeAsDisbursementDateTest() {
        try {

            // Set business date
            LocalDate businessDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(businessDate.toString());

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = createClient();

            // set repayment start date type as disbursement date
            final Integer repaymentStartDateType = 1;

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            // Loan Product creation with repayment start date type and down payment configuration with multi
            // disbursement
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
                    repaymentStartDateType, enableDownPayment, "25", enableAutoRepaymentForDownPayment);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(repaymentStartDateType, getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
            assertEquals("repaymentStartDateType.disbursementDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account with submitted date as business date (03 March 2023) and expected disbursement date
            // as future date (07 March 2023)
            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);

            // verify loan schedule is according to expected disbursement date

            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));

            // verify schedule is according to expected disbursement date
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // fourth installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));

            // first disbursement on a future date (7 March 2023)

            businessDate = LocalDate.of(2023, 3, 7);

            updateBusinessDate(businessDate.toString());

            disburseLoan(loanId, "07 March 2023", 500.0);

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to disbursement date after first disbursement
            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));

            // first installment down payment repayment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // fourth installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));

            // make repayment on 7 March to pay downpayment insatllment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 March 2023").locale("en")
                            .transactionAmount(125.00));

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // check down payment installment gets paid

            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));
            assertEquals(375.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));

            // first installment down payment repayment
            // check down payment installment gets paid
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));
            assertEquals(125.00, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // fourth installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));

            // set business date to next repayment due business date
            businessDate = LocalDate.of(2023, 4, 7);

            updateBusinessDate(businessDate.toString());

            // make 2nd repayment on 7 April to pay installment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 April 2023").locale("en")
                            .transactionAmount(125.00));

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            assertEquals(250.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));

            // first installment down payment repayment
            // check down payment installment gets paid
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));
            assertEquals(125.00, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));
            assertEquals(125.00, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalPaidForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(2).getComplete());

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // fourth installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));

            // make backdate disbursement for 5 march with business date 7 April

            disburseLoan(loanId, "05 March 2023", 500.0);

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to backdated disbursement date after second disbursement

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify transactions get reprocessed and installments paid accordingly
            assertEquals(750.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));

            // first installment down payment repayment for 5 March disbursal
            // check down payment installment gets paid
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));
            assertEquals(125.00, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second installment down payment repayment for 7 March disbursal
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(125.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));
            assertEquals(125.00, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalPaidForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // third installment [5 March 2023 - 5 April 2023]
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 5), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(4).getComplete());

            // fourth installment [5 April 2023 - 5 May 2023]
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(5).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 5), loanDetails.getRepaymentSchedule().getPeriods().get(5).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 5), loanDetails.getRepaymentSchedule().getPeriods().get(5).getDueDate());
            assertEquals(250.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(5).getComplete());

            // fifth installment [5 May 2023 - 5 June 2023]
            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().get(6).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 5), loanDetails.getRepaymentSchedule().getPeriods().get(6).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 5), loanDetails.getRepaymentSchedule().getPeriods().get(6).getDueDate());
            assertEquals(250.00,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(6).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(6).getComplete());

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void loanAccountBackDatedDisbursementWithDisbursementDateBeforeLoanSubmittedOnDateValidationTest() {
        try {
            // Set business date
            LocalDate businessDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(businessDate.toString());

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = createClient();

            // set repayment start date type as disbursement date
            final Integer repaymentStartDateType = 1;

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            // Loan Product creation with repayment start date type and down payment configuration with multi
            // disbursement
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
                    repaymentStartDateType, enableDownPayment, "25", enableAutoRepaymentForDownPayment);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(repaymentStartDateType, getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
            assertEquals("repaymentStartDateType.disbursementDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account with submitted date as business date (03 March 2023) and expected disbursement date
            // as future date (07 March 2023)
            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));
            // verify schedule is according to expected disbursement date
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // fourth installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));
            // first disbursement on a future date (7 March 2023)
            businessDate = LocalDate.of(2023, 3, 7);
            updateBusinessDate(businessDate.toString());
            disburseLoan(loanId, "07 March 2023", 500.0);

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify schedule is according to first disbursement date
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());

            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());

            // second disbursement backdated for 2 nd March 2023 before loan submission date
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> disburseLoan(loanId, "02 March 2023", 500.0));
            assertEquals(403, exception.getStatus());
            assertErrorGlobalisationCode(exception, "error.msg.loan.actualdisbursementdate.before.submittedDate");
            assertTrue(exception.getResponseBody().contains("Loan can't be disbursed before 2023-03-03"));

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void loanAccountBackDatedDisbursementForLoanProductWithDisableDownPaymentAndScheduleStartDateTypeAsDisbursementDate_DisbursementPeriodsOrderTest() {
        try {

            // Set business date
            LocalDate businessDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(businessDate.toString());

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = createClient();

            // set repayment start date type as disbursement date
            final Integer repaymentStartDateType = 1;

            // down-payment configuration
            Boolean enableDownPayment = false;
            Boolean enableAutoRepaymentForDownPayment = false;

            // Loan Product creation with repayment start date type and down payment configuration with multi
            // disbursement
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
                    repaymentStartDateType, enableDownPayment, null, enableAutoRepaymentForDownPayment);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(repaymentStartDateType, getLoanProductsProductResponse.getRepaymentStartDateType().getId().intValue());
            assertEquals("repaymentStartDateType.disbursementDate", getLoanProductsProductResponse.getRepaymentStartDateType().getCode());
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());

            // create loan account with submitted date as business date (03 March 2023) and expected disbursement date
            // as future date (07 March 2023)
            final Long loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));

            // verify schedule is according to expected disbursement date
            // first installment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(333.33,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(333.33,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(333.34,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));
            // first disbursement on a future date (7 March 2023)

            businessDate = LocalDate.of(2023, 3, 7);

            updateBusinessDate(businessDate.toString());

            disburseLoan(loanId, "07 March 2023", 500.0);

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to disbursement date after first disbursement
            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));

            // disbursement period
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(0).getDueDate());

            // first installment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(166.67,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(166.67,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(166.66,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));

            // make repayment on 7 March to pay installment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 March 2023").locale("en")
                            .transactionAmount(166.67));

            loanDetails = getLoanDetails(loanId);

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(500.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));
            assertEquals(333.33, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));

            // first installment
            // check installment gets paid
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(166.67,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));
            assertEquals(166.67, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(166.67,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 7), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(166.66,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));
            // set business date

            businessDate = LocalDate.of(2023, 3, 8);

            updateBusinessDate(businessDate.toString());

            // second disbursement backdated 5 March

            disburseLoan(loanId, "05 March 2023", 500.0);

            loanDetails = getLoanDetails(loanId);

            // verify loan schedule is according to backdated disbursement date after second disbursement

            assertNotNull(loanDetails);
            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify amounts
            assertEquals(1000.0, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalPrincipalExpected()));
            assertEquals(833.33, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getTotalOutstanding()));

            // verify disbursement period order
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(0).getDueDate());
            assertEquals(LocalDate.of(2023, 3, 7), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());

            // first installment for 5 March disbursal
            // check installment gets paid
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 4, 5), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(333.33,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));
            assertEquals(166.67, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalPaidForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(2).getComplete());

            // second installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 5), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 5, 5), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(333.33,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // third installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 5, 5), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 6, 5), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(333.34,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(4).getComplete());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private Long createLoanAccountMultipleRepaymentsDisbursement(final Long clientId, final Long loanProductId, final String externalId) {
        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, "03 March 2023", 1000.0, 3)//
                .expectedDisbursementDate("07 March 2023")//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .externalId(externalId);

        final Long loanId = loanHelper.applyForLoan(application).getLoanId();
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "03 March 2023"));
        return loanId;
    }

    private GetLoanProductsProductIdResponse createLoanProductWithRepaymentStartDateTypeConfigurationAndMultipleDisbursements(
            final Integer repaymentStartDateType, final Boolean enableDownPayment, final String disbursedAmountPercentageForDownPayment,
            final boolean enableAutoRepaymentForDownPayment) {
        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("3").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withRepaymentStartDateType(repaymentStartDateType)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .buildRequest(null));
        return retrieveLoanProduct(loanProductId);
    }

}
