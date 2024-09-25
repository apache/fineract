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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanAccountPaymentAllocationWithOverlappingDownPaymentInstallmentTest extends BaseLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ClientHelper clientHelper;
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void loanAccountWithEnableDownPaymentAndEnableAutoRepaymentForDownPaymentWithOverlappingInstallmentPaymentAllocationTest() {
        try {

            // Test with
            // Enable Down Payment
            // Enable Auto Repayment For Down Payment
            // Payment Strategy DEFAULT payment allocation strategy "Penalties, Fees, Interest, Principal order"
            // Overlapping down payment and regular installment

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursements(
                    loanTransactionHelper, enableDownPayment, "25", enableAutoRepaymentForDownPayment);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account with DEFAULT payment allocation strategy "Penalties, Fees, Interest, Principal order"

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr, LoanProductTestBuilder.DEFAULT_STRATEGY);

            // add charge PENALTY with due date as overlapping installment due date

            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            LocalDate targetDate = LocalDate.of(2023, 4, 3);
            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            assertNotNull(penalty1LoanChargeId);

            // add charge FEE with due date as overlapping installment due date
            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            targetDate = LocalDate.of(2023, 4, 3);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), feeCharge1AddedDate, "5.15"));

            assertNotNull(feeLoanChargeId);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 March 2023", loanId, "500");

            // verify down-payment transaction created
            checkDownPaymentTransaction(disbursementDate, 125.0f, 0.0f, 0.0f, 0.0f, loanId);

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 187.5, 0.0, 0.0, 202.65, 5.15, 0.0, 10.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 3, 187.5, 0.0, 0.0, 187.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // second disbursement with overlapping installment i.e same due date as regular repayment due date

            disbursementDate = LocalDate.of(2023, 4, 3);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 April 2023", loanId, "1000");

            checkDownPaymentTransaction(disbursementDate, 250.0f, 0.0f, 0.0f, 0.0f, loanId);

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 1000.0);
            // down payment period [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 2, 250.0, 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 562.5, 0.0, 0.0, 577.65, 5.15, 0.0, 10.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 562.5, 0.0, 0.0, 562.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // make repayment for fully paying and verify that regular installment gets fully paid on 3rd april

            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(577.65));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 1000.0);
            // down payment period [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 2, 250.0, 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 562.5, 577.65, 562.5, 0.0, 5.15, 5.15, 10.0,
                    10.0, true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 562.5, 0.0, 0.0, 562.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    @Test
    public void loanAccountWithEnableDownPaymentAndDisableAutoRepaymentForDownPaymentWithOverlappingInstallmentPaymentAllocationTest() {
        try {

            // Test with
            // Enable Down Payment
            // Disable Auto Repayment For Down Payment
            // Payment Strategy INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY =
            // "interest-principal-penalties-fees-order-strategy"
            // Overlapping down payment and regular installment

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursements(
                    loanTransactionHelper, enableDownPayment, "25", enableAutoRepaymentForDownPayment);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account with "interest-principal-penalties-fees-order-strategy"

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr, LoanProductTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY);

            // add charge PENALTY with due date as overlapping installment due date

            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            LocalDate targetDate = LocalDate.of(2023, 4, 3);
            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            assertNotNull(penalty1LoanChargeId);

            // add charge FEE with due date as overlapping installment due date
            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            targetDate = LocalDate.of(2023, 4, 3);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), feeCharge1AddedDate, "5.15"));

            assertNotNull(feeLoanChargeId);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 March 2023", loanId, "500");

            // make repayment on 3rd March
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 March 2023").locale("en")
                            .transactionAmount(125.0));

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 187.5, 0.0, 0.0, 202.65, 5.15, 0.0, 10.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 3, 187.5, 0.0, 0.0, 187.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // second disbursement with overlapping installment i.e same due date as regular repayment due date

            disbursementDate = LocalDate.of(2023, 4, 3);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 April 2023", loanId, "1000");

            // make repayment on 3rd April
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(250.0));

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 1000.0);
            // down payment period [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 2, 250.0, 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 562.5, 0.0, 0.0, 577.65, 5.15, 0.0, 10.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 562.5, 0.0, 0.0, 562.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // make repayment for fully paying and verify that regular installment gets fully paid on 3rd april

            final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(577.65));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 500.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 125.0, 125.0, 125.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 1000.0);
            // down payment period [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 2, 250.0, 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 562.5, 577.65, 562.5, 0.0, 5.15, 5.15, 10.0,
                    10.0, true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 562.5, 0.0, 0.0, 562.5, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    @Test
    public void loanAccountWithEnableDownPaymentAndDisableAutoRepaymentForDownPaymentWithOverlappingInstallmentForMultipleDisbursementsOnSameDayTest() {
        try {

            // Test with
            // Enable Down Payment
            // Disable Auto Repayment For Down Payment
            // Overlapping down payment and regular installment with multiple disbursements on same day

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursements(
                    loanTransactionHelper, enableDownPayment, "25", enableAutoRepaymentForDownPayment);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr, LoanProductTestBuilder.DEFAULT_STRATEGY);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 March 2023", loanId, "200");

            // make repayment on 3rd March
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 March 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 75.0, 0.0, 0.0, 75.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 3, 75.0, 0.0, 0.0, 75.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // second disbursement with overlapping installment i.e same due date as regular repayment due date

            disbursementDate = LocalDate.of(2023, 4, 3);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 April 2023", loanId, "200");

            // make repayment on 3rd April
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 2, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 150.0, 0.0, 0.0, 150.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 150.0, 0.0, 0.0, 150.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // same day third disbursement with overlapping installment i.e same due date as regular repayment due date
            // 3-April
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 April 2023", loanId, "200");

            // make repayment on 3rd April
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(8, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 200.0);
            // disbursement period [3]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(3), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 2, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // down payment period [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 3, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [6]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(6), 4, 225.0, 0.0, 0.0, 225.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [7]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(7), 5, 225.0, 0.0, 0.0, 225.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // make repayment for fully paying and verify that regular installment gets fully paid on 3rd april
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_3 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(225.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(8, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // disbursement period [2]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(2), LocalDate.of(2023, 4, 3), 200.0);
            // disbursement period [3]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(3), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 2, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // down payment period [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 3, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [6]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(6), 4, 225.0, 225.0, 225.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    true, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [7]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(7), 5, 225.0, 0.0, 0.0, 225.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    @Test
    public void loanAccountWithEnableDownPaymentWithAdvancedPaymentAllocationWithProgressiveScheduleGenerationMultipleDisbursementsOnSameDayTest() {
        try {

            // Test with
            // Enable Down Payment
            // Disable Auto Repayment For Down Payment
            // Overlapping down payment and regular installment with multiple disbursements on same day
            // Progressive Schedule generation with Advanced Payment Allocation

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
            AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

            // Loan Product creation with down-payment configuration and progressive schedule
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithAdvancedPaymentStrategyAndProgressiveLoanSchedule(
                    loanTransactionHelper, enableDownPayment, "25", enableAutoRepaymentForDownPayment, defaultAllocation);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            // create loan account

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr, "advanced-payment-allocation-strategy");

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 March 2023", loanId, "200");

            // make repayment on 3rd March
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 March 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 75.0, 0.0, 0.0, 75.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // regular installment [3]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(3), 3, 75.0, 0.0, 0.0, 75.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // second disbursement with overlapping installment i.e same due date as regular repayment due date

            disbursementDate = LocalDate.of(2023, 4, 3);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 April 2023", loanId, "200");

            // make repayment on 3rd April
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(6, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 75.0, 50.0, 50.0, 25.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // disbursement period [3]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(3), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [4]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(4), 3, 50.0, 0.0, 0.0, 50.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 4, 225.0, 0.0, 0.0, 225.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // same day third disbursement with overlapping installment i.e same due date as regular repayment due date
            // 3-April
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 April 2023", loanId, "200");

            // make repayment on 3rd April
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(50.0));

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(8, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 75.0, 75.0, 75.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // disbursement period [3]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(3), LocalDate.of(2023, 4, 3), 200.0);
            // disbursement period [4]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(4), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 3, 50.0, 25.0, 25.0, 25.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // down payment period [6]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(6), 4, 50.0, 0.0, 0.0, 50.0, 0.0, 0.0, 0.0, 0.0, false,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [7]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(7), 5, 375.0, 0.0, 0.0, 375.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

            // make repayment for fully paying and verify that regular installment gets fully paid on 3rd april
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_3 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(225.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // periods
            assertEquals(8, loanDetails.getRepaymentSchedule().getPeriods().size());
            // disbursement period [0]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(0), LocalDate.of(2023, 3, 3), 200.0);
            // down payment period [1]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(1), 1, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 3, 3));
            // regular installment [2]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(2), 2, 75.0, 75.0, 75.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 3, 3), LocalDate.of(2023, 4, 3));
            // disbursement period [3]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(3), LocalDate.of(2023, 4, 3), 200.0);
            // disbursement period [4]
            verifyDisbursementPeriod(loanDetails.getRepaymentSchedule().getPeriods().get(4), LocalDate.of(2023, 4, 3), 200.0);
            // down payment period [5]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(5), 3, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // down payment period [6]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(6), 4, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true,
                    LocalDate.of(2023, 4, 3), LocalDate.of(2023, 4, 3));
            // regular installment [7]
            verifyPeriodDetails(loanDetails.getRepaymentSchedule().getPeriods().get(7), 5, 375.0, 150.0, 150.0, 225.0, 0.0, 0.0, 0.0, 0.0,
                    false, LocalDate.of(2023, 4, 3), LocalDate.of(2023, 5, 3));

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    private void verifyDisbursementPeriod(GetLoansLoanIdRepaymentPeriod period, LocalDate disbursementDate, double disbursedAmount) {
        assertEquals(disbursementDate, period.getDueDate());
        assertEquals(disbursedAmount, period.getPrincipalLoanBalanceOutstanding());
    }

    private void verifyPeriodDetails(GetLoansLoanIdRepaymentPeriod period, Integer periodNumber, double periodAmount,
            double periodAmountPaid, double principalPaid, double outstandingAmount, double feeDue, double feePaid, double penaltyDue,
            double penaltyPaid, boolean isComplete, LocalDate periodFromDate, LocalDate periodDueDate) {
        assertEquals(periodNumber, period.getPeriod());
        assertEquals(periodFromDate, period.getFromDate());
        assertEquals(periodDueDate, period.getDueDate());
        assertEquals(periodAmount, period.getTotalInstallmentAmountForPeriod());
        assertEquals(periodAmountPaid, period.getTotalPaidForPeriod());
        assertEquals(principalPaid, period.getPrincipalPaid());
        assertEquals(outstandingAmount, period.getTotalOutstandingForPeriod());
        assertEquals(feeDue, period.getFeeChargesDue());
        assertEquals(feePaid, period.getFeeChargesPaid());
        assertEquals(penaltyDue, period.getPenaltyChargesDue());
        assertEquals(penaltyPaid, period.getPenaltyChargesPaid());
        assertEquals(isComplete, period.getComplete());
    }

    private Integer createLoanAccountMultipleRepaymentsDisbursement(final Integer clientID, final Long loanProductID,
            final String externalId, final String repaymentStartegy) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("2")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("2").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 March 2023").withSubmittedOnDate("03 March 2023").withLoanType("individual")
                .withExternalId(externalId).withRepaymentStrategy(repaymentStartegy)
                .build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("03 March 2023", "1000", loanId, null);
        return loanId;
    }

    private GetLoanProductsProductIdResponse createLoanProductWithEnableDownPaymentAndMultipleDisbursements(
            LoanTransactionHelper loanTransactionHelper, Boolean enableDownPayment, String disbursedAmountPercentageForDownPayment,
            boolean enableAutoRepaymentForDownPayment) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("2").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .build(null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private GetLoanProductsProductIdResponse createLoanProductWithAdvancedPaymentStrategyAndProgressiveLoanSchedule(
            LoanTransactionHelper loanTransactionHelper, Boolean enableDownPayment, String disbursedAmountPercentageForDownPayment,
            boolean enableAutoRepaymentForDownPayment, AdvancedPaymentData... advancedPaymentData) {

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("2").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .addAdvancedPaymentAllocation(advancedPaymentData).withLoanScheduleType(LoanScheduleType.PROGRESSIVE).build(null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private void checkDownPaymentTransaction(final LocalDate transactionDate, final Float principalPortion, final Float interestPortion,
            final Float feePortion, final Float penaltyPortion, final Integer loanID) {
        ArrayList<HashMap> transactions = (ArrayList<HashMap>) loanTransactionHelper.getLoanTransactions(this.requestSpec,
                this.responseSpec, loanID);
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isDownPaymentTransaction = (Boolean) transactionType.get("downPayment");

            if (isDownPaymentTransaction) {
                ArrayList<Integer> downPaymentDateAsArray = (ArrayList<Integer>) transactions.get(i).get("date");
                LocalDate downPaymentEntryDate = LocalDate.of(downPaymentDateAsArray.get(0), downPaymentDateAsArray.get(1),
                        downPaymentDateAsArray.get(2));

                if (DateUtils.isEqual(transactionDate, downPaymentEntryDate)) {
                    isTransactionFound = true;
                    assertEquals(principalPortion, Float.valueOf(String.valueOf(transactions.get(i).get("principalPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(interestPortion, Float.valueOf(String.valueOf(transactions.get(i).get("interestPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(feePortion, Float.valueOf(String.valueOf(transactions.get(i).get("feeChargesPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(penaltyPortion, Float.valueOf(String.valueOf(transactions.get(i).get("penaltyChargesPortion"))),
                            "Mismatch in transaction amounts");
                    break;
                }
            }
        }
        assertTrue(isTransactionFound, "No Down Payment entries are posted");
    }
}
