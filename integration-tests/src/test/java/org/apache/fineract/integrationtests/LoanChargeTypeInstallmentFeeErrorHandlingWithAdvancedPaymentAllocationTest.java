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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
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

public class LoanChargeTypeInstallmentFeeErrorHandlingWithAdvancedPaymentAllocationTest extends BaseLoanIntegrationTest {

    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER;
    private static ResponseSpecification RESPONSE_SPEC;
    private static RequestSpecification REQUEST_SPEC;
    private static ClientHelper CLIENT_HELPER;
    private static AccountHelper ACCOUNT_HELPER;

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

    /*
     * TODO: To be disabled when Installment Fee Charges are handled for Advanced Payment Allocation
     */
    @Test
    public void addingLoanChargeTypeInstallmentFeeForAdvancedPaymentAllocationGivesErrorTest() {
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

            final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
            final LoanTransactionHelper validationErrorHelper = new LoanTransactionHelper(REQUEST_SPEC, errorResponse);

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Integer clientId = CLIENT_HELPER.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            final Integer loanProductId = createLoanProduct(assetAccount, incomeAccount, expenseAccount, overpaymentAccount);

            final Integer loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

            // disburse principal amount
            LOAN_TRANSACTION_HELPER.disburseLoanWithTransactionAmount("15 February 2023", loanId, "1000");

            // add loan charge
            // apply Installment fee
            Integer installmentFeeCharge = ChargesHelper.createCharges(REQUEST_SPEC, RESPONSE_SPEC,
                    ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));

            List<HashMap<String, Object>> loanChargeErrorData = (List<HashMap<String, Object>>) validationErrorHelper
                    .addChargesForLoanWithError(loanId,
                            LoanTransactionHelper.getInstallmentChargesForLoanAsJSON(String.valueOf(installmentFeeCharge), "50"),
                            CommonConstants.RESPONSE_ERROR);
            assertNotNull(loanChargeErrorData);

            assertEquals(
                    "Charge with identifier %d cannot be applied: Installment fee charges are not supported for Advanced payment allocation strategy"
                            .formatted(installmentFeeCharge),
                    loanChargeErrorData.get(0).get("defaultUserMessage"));
            assertEquals("error.msg.charge.cannot.be.applied.toloan",
                    loanChargeErrorData.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

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
}
