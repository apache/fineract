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
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.BusinessDateUpdateRequest;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class RefundForActiveLoansWithAdvancedPaymentAllocationTest extends BaseLoanIntegrationTest {

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
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
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

            final double feePortion = 50.00;
            final double penaltyPortion = 100.00;

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
            assertEquals(feePortion, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesDue()));
            assertEquals(feePortion, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalDue()));
            assertEquals(250.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(400.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesDue()));
            assertEquals(feePortion, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(400.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("2023.03.01").dateFormat("yyyy.MM.dd").locale("en"));
            loanTransactionHelper.makeRepayment("01 March 2023", 810.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(0.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(240.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            loanTransactionHelper.makeRefundByCash("01 March 2023", 15.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(secondRepaymentInstallment.getPrincipalDue()));
            assertEquals(5.00, Utils.getDoubleValue(secondRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(5.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            loanTransactionHelper.makeRefundByCash("01 March 2023", 265.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesDue()));
            assertEquals(20.00, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(secondRepaymentInstallment.getPrincipalDue()));
            assertEquals(250.00, Utils.getDoubleValue(secondRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(270.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void refundForActiveLoanWithDefaultPaymentAllocationProcessingHorizontally() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
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

            final double feePortion = 50.00;
            final double penaltyPortion = 100.00;

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
            assertEquals(feePortion, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesDue()));
            assertEquals(feePortion, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalDue()));
            assertEquals(250.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(400.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesDue()));
            assertEquals(feePortion, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(400.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            businessDateHelper.updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE)
                    .date("2023.03.01").dateFormat("yyyy.MM.dd").locale("en"));
            loanTransactionHelper.makeRepayment("28 January 2023", 810.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesDue()));
            assertEquals(50.00, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(100.00, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(150.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(90.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            loanTransactionHelper.makeRefundByCash("28 January 2023", 15.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesDue()));
            assertEquals(50.00, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(100.00, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(secondRepaymentInstallment.getPrincipalDue()));
            assertEquals(0.00, Utils.getDoubleValue(secondRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(150.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(105.0, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());

            // fully unpaying the second installment
            loanTransactionHelper.makeRefundByCash("28 January 2023", 395.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

            firstRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(2);
            secondRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(3);
            thirdRepaymentInstallment = loanDetails.getRepaymentSchedule().getPeriods().get(4);

            assertEquals(5, loanDetails.getRepaymentSchedule().getPeriods().size());
            assertEquals(feePortion, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalDue()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(0.00, Utils.getDoubleValue(firstRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 1, 31), firstRepaymentInstallment.getDueDate());

            assertEquals(feePortion, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesDue()));
            assertEquals(feePortion, Utils.getDoubleValue(secondRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(penaltyPortion, Utils.getDoubleValue(secondRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(secondRepaymentInstallment.getPrincipalDue()));
            assertEquals(250.00, Utils.getDoubleValue(secondRepaymentInstallment.getPrincipalOutstanding()));
            assertEquals(400.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(400.00, Utils.getDoubleValue(secondRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 3, 2), secondRepaymentInstallment.getDueDate());

            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getFeeChargesOutstanding()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesDue()));
            assertEquals(0.00, Utils.getDoubleValue(thirdRepaymentInstallment.getPenaltyChargesOutstanding()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalDueForPeriod()));
            assertEquals(250.00, Utils.getDoubleValue(thirdRepaymentInstallment.getTotalOutstandingForPeriod()));
            assertEquals(LocalDate.of(2023, 4, 1), thirdRepaymentInstallment.getDueDate());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
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
                .addAdvancedPaymentAllocation(defaultAllocation).withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL)
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").build(null);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
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
