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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoanTransactionReprocessForAdvancedPaymentAllocationTest extends BaseLoanIntegrationTest {

    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER;
    private static ResponseSpecification RESPONSE_SPEC;
    private static RequestSpecification REQUEST_SPEC;
    private static ClientHelper CLIENT_HELPER;
    private static AccountHelper ACCOUNT_HELPER;
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @BeforeAll
    public static void setupTests() {
        Utils.initializeRESTAssured();
        REQUEST_SPEC = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        REQUEST_SPEC.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        RESPONSE_SPEC = new ResponseSpecBuilder().expectStatusCode(200).build();
        LOAN_TRANSACTION_HELPER = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC);
        CLIENT_HELPER = new ClientHelper(REQUEST_SPEC, RESPONSE_SPEC);
        ACCOUNT_HELPER = new AccountHelper(REQUEST_SPEC, RESPONSE_SPEC);
    }

    @Test
    public void loanTransactionReprocessForAddChargeTest() {
        try {
            // Set business date
            LocalDate businessDate = LocalDate.of(2023, 3, 15);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(REQUEST_SPEC, RESPONSE_SPEC, BusinessDateType.BUSINESS_DATE, businessDate);

            // Accounts oof periodic accrual
            final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
            final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
            final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
            final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Integer clientId = CLIENT_HELPER.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            final Integer loanProductId = createLoanProduct(assetAccount, incomeAccount, expenseAccount, overpaymentAccount);

            final Integer loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

            // disburse principal amount
            LOAN_TRANSACTION_HELPER.disburseLoanWithTransactionAmount("15 February 2023", loanId, "1000");

            // add loan charge
            // apply fee
            Integer feeCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));

            LocalDate targetDate = LocalDate.of(2023, 2, 22);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            Integer feeLoanChargeId = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "50"));

            // Set Loan transaction externalId for transaction getting reversed and replayed
            String loanTransactionExternalIdStr = UUID.randomUUID().toString();

            // make repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = LOAN_TRANSACTION_HELPER.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("20 February 2023").locale("en")
                            .transactionAmount(50.0).externalId(loanTransactionExternalIdStr));

            // verify transaction amounts
            verifyTransaction(LocalDate.of(2023, 2, 20), 50.0f, 0.0f, 0.0f, 50.0f, 0.0f, loanId, "repayment");

            // add loan charge for a date later than repayment date
            // apply penalty

            targetDate = LocalDate.of(2023, 2, 22);

            Integer penalty = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Integer penalty1LoanChargeId = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            // verify no reverse replay
            GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = LOAN_TRANSACTION_HELPER
                    .getLoanTransactionDetails((long) loanId, loanTransactionExternalIdStr);
            assertNotNull(getLoansTransactionResponse);
            assertEquals(0, getLoansTransactionResponse.getTransactionRelations().size());

            // add loan charge for a date earlier than repayment date
            targetDate = LocalDate.of(2023, 2, 18);

            Integer penalty_1 = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            final String penaltyCharge1AddedDate_1 = DATE_FORMATTER.format(targetDate);

            Integer penalty1LoanChargeId_1 = LOAN_TRANSACTION_HELPER.addChargesForLoan(loanId, LoanTransactionHelper
                    .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty_1), penaltyCharge1AddedDate_1, "10"));

            // verify reverse replay
            getLoansTransactionResponse = LOAN_TRANSACTION_HELPER.getLoanTransactionDetails((long) loanId, loanTransactionExternalIdStr);
            assertNotNull(getLoansTransactionResponse);
            assertEquals(1, getLoansTransactionResponse.getTransactionRelations().size());

            // verify transaction amounts
            verifyTransaction(LocalDate.of(2023, 2, 20), 50.0f, 40.0f, 0.0f, 0.0f, 10.0f, loanId, "repayment");

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private Integer createLoanProduct(final Account... accounts) {
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);
        String loanProductCreateJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(accounts).withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .addAdvancedPaymentAllocation(defaultAllocation).withLoanScheduleType(LoanScheduleType.PROGRESSIVE).withMultiDisburse()
                .withDisallowExpectedDisbursements(true).build();
        return LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductCreateJSON);

    }

    private Integer createLoanAccount(final Integer clientID, final Integer loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("60")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("4").withRepaymentEveryAfter("15").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("15 February 2023")
                .withSubmittedOnDate("15 February 2023").withLoanType("individual").withExternalId(externalId)
                .withRepaymentStrategy("advanced-payment-allocation-strategy").build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);
        LOAN_TRANSACTION_HELPER.approveLoan("15 February 2023", "1000", loanId, null);
        return loanId;
    }

    private void verifyTransaction(final LocalDate transactionDate, final Float transactionAmount, final Float principalPortion,
            final Float interestPortion, final Float feePortion, final Float penaltyPortion, final Integer loanID,
            final String transactionOfType) {
        ArrayList<HashMap> transactions = (ArrayList<HashMap>) LOAN_TRANSACTION_HELPER.getLoanTransactions(REQUEST_SPEC, RESPONSE_SPEC,
                loanID);
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isTransaction = (Boolean) transactionType.get(transactionOfType);

            if (isTransaction) {
                ArrayList<Integer> transactionDateAsArray = (ArrayList<Integer>) transactions.get(i).get("date");
                LocalDate transactionEntryDate = LocalDate.of(transactionDateAsArray.get(0), transactionDateAsArray.get(1),
                        transactionDateAsArray.get(2));

                if (transactionDate.isEqual(transactionEntryDate)) {
                    isTransactionFound = true;
                    assertEquals(transactionAmount, Float.valueOf(String.valueOf(transactions.get(i).get("amount"))),
                            "Mismatch in transaction amounts");
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
        assertTrue(isTransactionFound, "No Transaction entries are posted");
    }

}
