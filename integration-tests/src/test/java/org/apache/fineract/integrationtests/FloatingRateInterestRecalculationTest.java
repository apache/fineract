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
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.FloatingRatePeriodRequest;
import org.apache.fineract.client.models.FloatingRateRequest;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostFloatingRatesResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for Inconsistent Interest Recalculation between exact repayment and over-payment for Cumulative Loan
 * with Floating Rates.
 *
 * @see org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms#updateAnnualNominalInterestRate
 */
public class FloatingRateInterestRecalculationTest extends BaseLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ClientHelper clientHelper;
    private AccountHelper accountHelper;
    private final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    private static final BigDecimal INITIAL_INTEREST_RATE = new BigDecimal("12");
    private static final BigDecimal CHANGED_INTEREST_RATE = new BigDecimal("6");

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);

        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    @Test
    public void testExactRepaymentRecalculatesEmiOnFloatingRateChange() {
        runFloatingRateRecalculationScenario(false);
    }

    @Test
    public void testOverPaymentRecalculatesEmiOnFloatingRateChange() {
        runFloatingRateRecalculationScenario(true);
    }

    private void runFloatingRateRecalculationScenario(boolean overPayment) {
        LocalDate setupDate = LocalDate.of(2024, 2, 1);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, setupDate);

        Long floatingRateId = createFloatingRate();

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        Integer loanProductId = createCumulativeFloatingRateLoanProduct(floatingRateId, assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        assertNotNull(loanProductId);

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        LocalDate disbursementDate = LocalDate.of(2024, 3, 15);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

        final Integer loanId = createAndDisburseLoan(clientId, loanProductId, disbursementDate);
        assertNotNull(loanId);

        GetLoansLoanIdResponse initialLoan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(initialLoan.getRepaymentSchedule());
        List<GetLoansLoanIdRepaymentPeriod> initialPeriods = initialLoan.getRepaymentSchedule().getPeriods();

        BigDecimal initialEmi = null;
        for (GetLoansLoanIdRepaymentPeriod period : initialPeriods) {
            if (period.getPeriod() != null && period.getPeriod() == 1) {
                initialEmi = period.getTotalDueForPeriod();
                break;
            }
        }
        assertNotNull(initialEmi, "Could not find initial EMI for period 1");
        assertTrue(initialEmi.compareTo(BigDecimal.ZERO) > 0, "Initial EMI should be greater than zero");

        LocalDate postRateChangeDate = LocalDate.of(2024, 4, 10);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, postRateChangeDate);

        String repaymentDate = dateFormatter.format(postRateChangeDate);
        float repaymentAmount = overPayment ? initialEmi.floatValue() + 0.01f : initialEmi.floatValue();
        loanTransactionHelper.makeRepayment(repaymentDate, repaymentAmount, loanId);

        GetLoansLoanIdResponse updatedLoan = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(updatedLoan.getRepaymentSchedule());
        List<GetLoansLoanIdRepaymentPeriod> updatedPeriods = updatedLoan.getRepaymentSchedule().getPeriods();

        boolean foundRecalculatedPeriod = false;
        for (GetLoansLoanIdRepaymentPeriod period : updatedPeriods) {
            if (period.getPeriod() != null && period.getPeriod() > 1) {
                BigDecimal updatedEmi = period.getTotalDueForPeriod();
                assertNotNull(updatedEmi, "EMI for period " + period.getPeriod() + " should not be null");
                assertTrue(updatedEmi.compareTo(initialEmi) < 0,
                        "Period " + period.getPeriod() + " EMI (" + updatedEmi + ") should be lower than initial EMI (" + initialEmi
                                + ") after rate drop from " + INITIAL_INTEREST_RATE + "% to " + CHANGED_INTEREST_RATE + "%");
                foundRecalculatedPeriod = true;
            }
        }
        assertTrue(foundRecalculatedPeriod, "Should have found at least one recalculated period after the rate change");
    }

    private Long createFloatingRate() {
        FloatingRatePeriodRequest initialPeriod = new FloatingRatePeriodRequest().fromDate("01 March 2024")
                .interestRate(INITIAL_INTEREST_RATE).isDifferentialToBaseLendingRate(false).locale("en").dateFormat("dd MMMM yyyy");

        FloatingRatePeriodRequest changedPeriod = new FloatingRatePeriodRequest().fromDate("01 April 2024")
                .interestRate(CHANGED_INTEREST_RATE).isDifferentialToBaseLendingRate(false).locale("en").dateFormat("dd MMMM yyyy");

        FloatingRateRequest floatingRateRequest = new FloatingRateRequest().name(Utils.uniqueRandomStringGenerator("FLOAT_RATE_", 6))
                .isBaseLendingRate(false).isActive(true).ratePeriods(List.of(initialPeriod, changedPeriod));

        PostFloatingRatesResponse response = Calls.ok(fineractClient().floatingRates.createFloatingRate(floatingRateRequest));
        assertNotNull(response);
        assertNotNull(response.getResourceId());
        return response.getResourceId();
    }

    private Integer createCumulativeFloatingRateLoanProduct(Long floatingRateId, Account... accounts) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().withPrincipal("10000").withNumberOfRepayments("12")
                .withRepaymentTypeAsMonth().withRepaymentAfterEvery("1").withInterestTypeAsDecliningBalance()
                .withAmortizationTypeAsEqualInstallments().withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withInterestRecalculationDetails(LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                        LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_EMI_AMOUN,
                        LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE)
                .withInterestRecalculationRestFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "1", null, null)
                .withDaysInMonth("30").withDaysInYear("360").withAccountingRulePeriodicAccrual(accounts).build(null, null);

        loanProductMap.remove("interestRatePerPeriod");
        loanProductMap.remove("interestRateFrequencyType");

        loanProductMap.put("isLinkedToFloatingInterestRates", true);
        loanProductMap.put("floatingRatesId", floatingRateId);
        loanProductMap.put("interestRateDifferential", "0");
        loanProductMap.put("isFloatingInterestRateCalculationAllowed", true);
        loanProductMap.put("minDifferentialLendingRate", "0");
        loanProductMap.put("defaultDifferentialLendingRate", "0");
        loanProductMap.put("maxDifferentialLendingRate", "50");

        return loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
    }

    private Integer createAndDisburseLoan(Integer clientId, Integer loanProductId, LocalDate disbursementDate) {
        String disburseDateStr = dateFormatter.format(disbursementDate);

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("10000").withLoanTermFrequency("12")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("12").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withInterestTypeAsDecliningBalance()
                .withExpectedDisbursementDate(disburseDateStr).withSubmittedOnDate(disburseDateStr).withLoanType("individual")
                .build(clientId.toString(), loanProductId.toString(), null);

        com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(loanApplicationJSON).getAsJsonObject();
        jsonObject.remove("interestRatePerPeriod");
        jsonObject.addProperty("interestRateDifferential", "0");
        jsonObject.addProperty("isFloatingInterestRate", true);
        loanApplicationJSON = jsonObject.toString();

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan(disburseDateStr, "10000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount(disburseDateStr, loanId, "10000");
        return loanId;
    }
}
