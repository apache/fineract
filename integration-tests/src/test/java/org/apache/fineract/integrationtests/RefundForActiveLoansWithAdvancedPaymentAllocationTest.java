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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
@Slf4j
public class RefundForActiveLoansWithAdvancedPaymentAllocationTest {

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern(DATETIME_PATTERN).toFormatter();
    private static RequestSpecification requestSpec;
    private static ResponseSpecification responseSpec;
    private static LoanTransactionHelper loanTransactionHelper;
    private static PostClientsResponse client;
    private static BusinessDateHelper businessDateHelper;
    private static AccountHelper accountHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        businessDateHelper = new BusinessDateHelper();
        accountHelper = new AccountHelper(requestSpec, responseSpec);
    }

    @Test
    public void refundForActiveLoanWithDefaultPaymentAllocationProcessingVertically() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.02.15").dateFormat("yyyy.MM.dd").locale("en"));

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            Integer loanProductId = createLoanProduct("1000", "30", "4", LoanScheduleProcessingType.VERTICAL, assetAccount, incomeAccount,
                    expenseAccount, overpaymentAccount);

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), loanProductId, 1000L, 90, 30, 3,
                    BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            int loanId = loanResponse.getLoanId().intValue();

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(1000.00)).locale("en"));

            final float feePortion = 50.0f;
            final float penaltyPortion = 100.0f;

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                    .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(feePortion), false));

            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                    .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(penaltyPortion), true));

            final String firstInstallmentChargeAddedDate = DATE_FORMATTER.format(LocalDate.of(2023, 1, 3));
            loanTransactionHelper.addChargesForLoan(loanId, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                    String.valueOf(fee), firstInstallmentChargeAddedDate, String.valueOf(feePortion)));

            loanTransactionHelper.addChargesForLoan(loanId, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                    String.valueOf(penalty), firstInstallmentChargeAddedDate, String.valueOf(penaltyPortion)));

            final String secondInstallmentChargeAddedDate = DATE_FORMATTER.format(LocalDate.of(2023, 2, 3));
            loanTransactionHelper.addChargesForLoan(loanId, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                    String.valueOf(fee), secondInstallmentChargeAddedDate, String.valueOf(feePortion)));

            loanTransactionHelper.addChargesForLoan(loanId, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                    String.valueOf(penalty), secondInstallmentChargeAddedDate, String.valueOf(penaltyPortion)));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            GetLoansLoanIdRepaymentPeriod firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            GetLoansLoanIdRepaymentPeriod secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            GetLoansLoanIdRepaymentPeriod thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, firstRepaymentInstallment.getFeeChargesDue());
            assertEquals(feePortion, firstRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, firstRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(penaltyPortion, firstRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, firstRepaymentInstallment.getPrincipalDue());
            assertEquals(250.0f, firstRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, firstRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(400.0f, firstRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, secondRepaymentInstallment.getFeeChargesDue());
            assertEquals(feePortion, secondRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, secondRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(penaltyPortion, secondRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(400.0f, secondRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(400.0f, secondRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.03.01").dateFormat("yyyy.MM.dd").locale("en"));
            loanTransactionHelper.makeRepayment("01 March 2023", 810.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, firstRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, firstRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, firstRepaymentInstallment.getPrincipalDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, firstRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(0.0f, firstRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, secondRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, secondRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, secondRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, secondRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(400.0f, secondRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(0.0f, secondRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(240.0f, thirdRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            loanTransactionHelper.makeRefundByCash("01 March 2023", 15.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, firstRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, firstRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, firstRepaymentInstallment.getPrincipalDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, firstRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(0.0f, firstRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, secondRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, secondRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, secondRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, secondRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, secondRepaymentInstallment.getPrincipalDue());
            assertEquals(5.0f, secondRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, secondRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(5.0f, secondRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            loanTransactionHelper.makeRefundByCash("01 March 2023", 265.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, firstRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, firstRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, firstRepaymentInstallment.getPrincipalDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, firstRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(0.0f, firstRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, secondRepaymentInstallment.getFeeChargesDue());
            assertEquals(20.0f, secondRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, secondRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, secondRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, secondRepaymentInstallment.getPrincipalDue());
            assertEquals(250.0f, secondRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, secondRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(270.0f, secondRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    @Test
    public void refundForActiveLoanWithDefaultPaymentAllocationProcessingHorizontally() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.02.15").dateFormat("yyyy.MM.dd").locale("en"));

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            Integer loanProductId = createLoanProduct("1000", "30", "4", LoanScheduleProcessingType.HORIZONTAL, assetAccount, incomeAccount,
                    expenseAccount, overpaymentAccount);

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), loanProductId, 1000L, 90, 30, 3,
                    BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            int loanId = loanResponse.getLoanId().intValue();

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(1000.00)).locale("en"));

            final float feePortion = 50.0f;
            final float penaltyPortion = 100.0f;

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                    .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(feePortion), false));

            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                    .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(penaltyPortion), true));

            final String firstInstallmentChargeAddedDate = DATE_FORMATTER.format(LocalDate.of(2023, 1, 3));
            loanTransactionHelper.addChargesForLoan(loanId, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                    String.valueOf(fee), firstInstallmentChargeAddedDate, String.valueOf(feePortion)));

            loanTransactionHelper.addChargesForLoan(loanId, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                    String.valueOf(penalty), firstInstallmentChargeAddedDate, String.valueOf(penaltyPortion)));

            final String secondInstallmentChargeAddedDate = DATE_FORMATTER.format(LocalDate.of(2023, 2, 3));
            loanTransactionHelper.addChargesForLoan(loanId, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                    String.valueOf(fee), secondInstallmentChargeAddedDate, String.valueOf(feePortion)));

            loanTransactionHelper.addChargesForLoan(loanId, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                    String.valueOf(penalty), secondInstallmentChargeAddedDate, String.valueOf(penaltyPortion)));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            GetLoansLoanIdRepaymentPeriod firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            GetLoansLoanIdRepaymentPeriod secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            GetLoansLoanIdRepaymentPeriod thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, firstRepaymentInstallment.getFeeChargesDue());
            assertEquals(feePortion, firstRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, firstRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(penaltyPortion, firstRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, firstRepaymentInstallment.getPrincipalDue());
            assertEquals(250.0f, firstRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, firstRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(400.0f, firstRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, secondRepaymentInstallment.getFeeChargesDue());
            assertEquals(feePortion, secondRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, secondRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(penaltyPortion, secondRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(400.0f, secondRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(400.0f, secondRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.03.01").dateFormat("yyyy.MM.dd").locale("en"));
            loanTransactionHelper.makeRepayment("28 January 2023", 810.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, firstRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, firstRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, firstRepaymentInstallment.getPrincipalDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, firstRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(0.0f, firstRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, secondRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, secondRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, secondRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, secondRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(400.0f, secondRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(0.0f, secondRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(240.0f, thirdRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            loanTransactionHelper.makeRefundByCash("28 January 2023", 15.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, firstRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, firstRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, firstRepaymentInstallment.getPrincipalDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, firstRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(0.0f, firstRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, secondRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, secondRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, secondRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, secondRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, secondRepaymentInstallment.getPrincipalDue());
            assertEquals(5.0f, secondRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, secondRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(5.0f, secondRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            // fully unpaying the second installment
            loanTransactionHelper.makeRefundByCash("28 January 2023", 395.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, firstRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, firstRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, firstRepaymentInstallment.getPrincipalDue());
            assertEquals(0.0f, firstRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, firstRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(0.0f, firstRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, secondRepaymentInstallment.getFeeChargesDue());
            assertEquals(feePortion, secondRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(penaltyPortion, secondRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(penaltyPortion, secondRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, secondRepaymentInstallment.getPrincipalDue());
            assertEquals(250.0f, secondRepaymentInstallment.getPrincipalOutstanding());
            assertEquals(400.0f, secondRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(400.0f, secondRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getFeeChargesOutstanding());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesDue());
            assertEquals(0.0f, thirdRepaymentInstallment.getPenaltyChargesOutstanding());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalDueForPeriod());
            assertEquals(250.0f, thirdRepaymentInstallment.getTotalOutstandingForPeriod());
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    private Integer createLoanProduct(final String principal, final String repaymentAfterEvery, final String numberOfRepayments,
            LoanScheduleProcessingType loanScheduleProcessingType, final Account... accounts) {
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        log.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withMinPrincipal(principal).withPrincipal(principal)
                .withRepaymentTypeAsDays().withRepaymentAfterEvery(repaymentAfterEvery).withNumberOfRepayments(numberOfRepayments)
                .withEnableDownPayment(true, "25", true).withinterestRatePerPeriod("0").withInterestRateFrequencyTypeAsMonths()
                .withRepaymentStrategy(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE).withLoanScheduleProcessingType(loanScheduleProcessingType)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .addAdvancedPaymentAllocation(defaultAllocation).withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").build(null);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private static AdvancedPaymentData createDefaultPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_INTEREST, PaymentAllocationType.PAST_DUE_PRINCIPAL,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_INTEREST,
                PaymentAllocationType.DUE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_INTEREST, PaymentAllocationType.IN_ADVANCE_PRINCIPAL);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private static List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }

    private static PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final Long principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate) {
        log.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return loanTransactionHelper.applyLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId.longValue())
                .expectedDisbursementDate(expectedDisbursementDate).dateFormat(DATETIME_PATTERN)
                .transactionProcessingStrategyCode(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .locale("en").submittedOnDate(submittedOnDate).amortizationType(1).interestRatePerPeriod(interestRate)
                .interestCalculationPeriodType(1).interestType(0).repaymentFrequencyType(0).repaymentEvery(repaymentAfterEvery)
                .repaymentFrequencyType(0).numberOfRepayments(numberOfRepayments).loanTermFrequency(loanTermFrequency)
                .loanTermFrequencyType(0).principal(BigDecimal.valueOf(principal)).loanType("individual"));
    }
}
