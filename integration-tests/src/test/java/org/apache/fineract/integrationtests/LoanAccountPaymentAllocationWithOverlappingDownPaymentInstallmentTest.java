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
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanAccountPaymentAllocationWithOverlappingDownPaymentInstallmentTest {

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
            LocalDate disbursementDate = LocalDate.of(2023, 03, 3);

            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
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

            // first period [2023-03-03 to 2023-03-03] down payment installment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second period [2023-03-03 to 2023-04-03] regular installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(166.67, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalPaidForPeriod());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(2).getComplete());

            // third period [2023-04-03 to 2023-05-03] regular installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 05, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(208.33, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // second disbursement with overlapping installment i.e same due date as regular repayment due date

            disbursementDate = LocalDate.of(2023, 04, 3);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 April 2023", loanId, "1000");

            checkDownPaymentTransaction(disbursementDate, 234.85f, 0.0f, 5.15f, 10.0f, loanId);

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify that overlapping down payment installment is created after regular installment

            // and verify regular repayment type installment gets paid before down payment installment according to
            // payment

            // allocation strategy [DEFAULT]

            // first period [2023-03-03 to 2023-03-03] first down payment installment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second period [2023-03-03 to 2023-04-03] regular installment, penalty and fee charges gets paid first
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(500.00, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalPaidForPeriod());
            assertEquals(265.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalOutstandingForPeriod());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesDue());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesPaid());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesDue());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesPaid());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // third period [2023-04-03 to 2023-04-03] overlapping down payment installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(4).getComplete());

            // fourth period [2023-04-03 to 2023-05-03] regular installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(5).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getFromDate());
            assertEquals(LocalDate.of(2023, 05, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getDueDate());
            assertEquals(625.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(5).getComplete());

            // make repayment for fully paying and verify that regular installment gets fully paid on 3rd april

            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(265.15));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify regular installment gets paid off

            // first period [2023-03-03 to 2023-03-03] first down payment installment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second period [2023-03-03 to 2023-04-03] regular installment, penalty and fee charges gets paid first
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(500.00, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());
            assertEquals(515.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalPaidForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalOutstandingForPeriod());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesDue());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesPaid());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesDue());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesPaid());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // third period [2023-04-03 to 2023-04-03] overlapping down payment installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(4).getComplete());

            // fourth period [2023-04-03 to 2023-05-03] regular installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(5).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getFromDate());
            assertEquals(LocalDate.of(2023, 05, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getDueDate());
            assertEquals(625.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(5).getComplete());

            // make another repayment of 250 and verify downpayment installment gets paid off on 3rd april
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(250.00));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify down payment installment gets paid off

            // first period [2023-03-03 to 2023-03-03] first down payment installment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second period [2023-03-03 to 2023-04-03] regular installment, penalty and fee charges gets paid first and
            // then principal
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(500.00, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());
            assertEquals(515.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalPaidForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalOutstandingForPeriod());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesDue());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesPaid());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesDue());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesPaid());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // third period [2023-04-03 to 2023-04-03] down payment installment gets paid
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalPaidForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(4).getComplete());

            // fourth period [2023-04-03 to 2023-05-03] regular installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(5).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getFromDate());
            assertEquals(LocalDate.of(2023, 05, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getDueDate());
            assertEquals(625.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(5).getComplete());

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
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
            LocalDate disbursementDate = LocalDate.of(2023, 03, 3);

            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
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

            // first period [2023-03-03 to 2023-03-03] down payment installment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second period [2023-03-03 to 2023-04-03] regular installment
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(166.67, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalPaidForPeriod());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(2).getComplete());

            // third period [2023-04-03 to 2023-05-03] regular installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 05, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(208.33, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // second disbursement with overlapping installment i.e same due date as regular repayment due date

            disbursementDate = LocalDate.of(2023, 04, 3);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 April 2023", loanId, "1000");

            // make repayment on 3rd April
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(250.0));

            // verify loan schedule

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify that overlapping down payment installment is created after regular installment

            // and verify regular repayment type installment gets paid before down payment installment according to
            // payment

            // allocation strategy

            // first period [2023-03-03 to 2023-03-03] first down payment installment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second period [2023-03-03 to 2023-04-03] regular installment, principal gets paid first
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(500.00, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalPaidForPeriod());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPrincipalPaid());
            assertEquals(265.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalOutstandingForPeriod());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesPaid());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesPaid());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // third period [2023-04-03 to 2023-04-03] overlapping down payment installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(4).getComplete());

            // fourth period [2023-04-03 to 2023-05-03] regular installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(5).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getFromDate());
            assertEquals(LocalDate.of(2023, 05, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getDueDate());
            assertEquals(625.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(5).getComplete());

            // make repayment for fully paying and verify that regular installment gets fully paid on 3rd april

            final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(265.15));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify regular installment gets paid off

            // first period [2023-03-03 to 2023-03-03] first down payment installment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second period [2023-03-03 to 2023-04-03] regular installment, principal gets paid first
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(500.00, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());
            assertEquals(515.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalPaidForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalOutstandingForPeriod());
            assertEquals(500.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPrincipalPaid());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesDue());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesPaid());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesDue());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesPaid());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // third period [2023-04-03 to 2023-04-03] overlapping down payment installment
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(4).getComplete());

            // fourth period [2023-04-03 to 2023-05-03] regular installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(5).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getFromDate());
            assertEquals(LocalDate.of(2023, 05, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getDueDate());
            assertEquals(625.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(5).getComplete());

            // make another repayment of 250 and verify downpayment installment gets paid off on 3rd april
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_3 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("3 April 2023").locale("en")
                            .transactionAmount(250.00));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails.getRepaymentSchedule());

            // verify down payment installment gets paid off

            // first period [2023-03-03 to 2023-03-03] first down payment installment
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getFromDate());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(125.00, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalPaidForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getComplete());

            // second period [2023-03-03 to 2023-04-03] regular installment, principal gets paid first
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 03, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(500.00, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());
            assertEquals(515.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalPaidForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalOutstandingForPeriod());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesDue());
            assertEquals(5.15, loanDetails.getRepaymentSchedule().getPeriods().get(3).getFeeChargesPaid());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesDue());
            assertEquals(10.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesPaid());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(3).getComplete());

            // third period [2023-04-03 to 2023-04-03] down payment installment gets paid
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getFromDate());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalPaidForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(4).getComplete());

            // fourth period [2023-04-03 to 2023-05-03] regular installment
            assertEquals(4, loanDetails.getRepaymentSchedule().getPeriods().get(5).getPeriod());
            assertEquals(LocalDate.of(2023, 04, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getFromDate());
            assertEquals(LocalDate.of(2023, 05, 3), loanDetails.getRepaymentSchedule().getPeriods().get(5).getDueDate());
            assertEquals(625.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalInstallmentAmountForPeriod());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(5).getTotalPaidForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(5).getComplete());

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }

    }

    private Integer createLoanAccountMultipleRepaymentsDisbursement(final Integer clientID, final Long loanProductID,
            final String externalId, final String repaymentStartegy) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("3")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("3").withRepaymentEveryAfter("1")
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
                .withRepaymentAfterEvery("1").withNumberOfRepayments("3").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .build(null);
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

                if (transactionDate.isEqual(downPaymentEntryDate)) {
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
