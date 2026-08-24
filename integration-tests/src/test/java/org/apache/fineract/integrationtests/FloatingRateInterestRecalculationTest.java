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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.FloatingRateCreateRequest;
import org.apache.fineract.client.models.FloatingRateCreateResponse;
import org.apache.fineract.client.models.FloatingRatePeriodRequest;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

/**
 * Integration test for Inconsistent Interest Recalculation between exact repayment and over-payment for Cumulative Loan
 * with Floating Rates.
 *
 * @see org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms#updateAnnualNominalInterestRate
 */
public class FloatingRateInterestRecalculationTest extends FeignLoanTestBase {

    private static final BigDecimal INITIAL_INTEREST_RATE = new BigDecimal("12");
    private static final BigDecimal CHANGED_INTEREST_RATE = new BigDecimal("6");

    @Test
    public void testExactRepaymentRecalculatesEmiOnFloatingRateChange() {
        runFloatingRateRecalculationScenario(false);
    }

    @Test
    public void testOverPaymentRecalculatesEmiOnFloatingRateChange() {
        runFloatingRateRecalculationScenario(true);
    }

    private void runFloatingRateRecalculationScenario(boolean overPayment) {
        runAt("01 February 2024", () -> {
            Long floatingRateId = createFloatingRate();

            final Account assetAccount = accountHelper.createAssetAccount("floatingRateAsset");
            final Account incomeAccount = accountHelper.createIncomeAccount("floatingRateIncome");
            final Account expenseAccount = accountHelper.createExpenseAccount("floatingRateExpense");
            final Account overpaymentAccount = accountHelper.createLiabilityAccount("floatingRateOverpayment");

            Integer loanProductId = createCumulativeFloatingRateLoanProduct(floatingRateId, assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);
            assertNotNull(loanProductId);

            final Long clientId = createClient();

            updateBusinessDate("15 March 2024");
            final Long loanId = createAndDisburseLoan(clientId, loanProductId, "15 March 2024");
            assertNotNull(loanId);

            GetLoansLoanIdResponse initialLoan = getLoanDetails(loanId);
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
            final BigDecimal capturedInitialEmi = initialEmi;

            updateBusinessDate("10 April 2024");
            String repaymentDate = "10 April 2024";
            float repaymentAmount = overPayment ? capturedInitialEmi.floatValue() + 0.01f : capturedInitialEmi.floatValue();
            makeRepayment(repaymentDate, repaymentAmount, loanId);

            GetLoansLoanIdResponse updatedLoan = getLoanDetails(loanId);
            assertNotNull(updatedLoan.getRepaymentSchedule());
            List<GetLoansLoanIdRepaymentPeriod> updatedPeriods = updatedLoan.getRepaymentSchedule().getPeriods();

            boolean foundRecalculatedPeriod = false;
            for (GetLoansLoanIdRepaymentPeriod period : updatedPeriods) {
                if (period.getPeriod() != null && period.getPeriod() > 1) {
                    BigDecimal updatedEmi = period.getTotalDueForPeriod();
                    assertNotNull(updatedEmi, "EMI for period " + period.getPeriod() + " should not be null");
                    assertTrue(updatedEmi.compareTo(capturedInitialEmi) < 0,
                            "Period " + period.getPeriod() + " EMI (" + updatedEmi + ") should be lower than initial EMI ("
                                    + capturedInitialEmi + ") after rate drop from " + INITIAL_INTEREST_RATE + "% to "
                                    + CHANGED_INTEREST_RATE + "%");
                    foundRecalculatedPeriod = true;
                }
            }
            assertTrue(foundRecalculatedPeriod, "Should have found at least one recalculated period after the rate change");
        });
    }

    private Long createFloatingRate() {
        FloatingRatePeriodRequest initialPeriod = new FloatingRatePeriodRequest().fromDate("01 March 2024")
                .interestRate(INITIAL_INTEREST_RATE).isDifferentialToBaseLendingRate(false).locale("en").dateFormat("dd MMMM yyyy");

        FloatingRatePeriodRequest changedPeriod = new FloatingRatePeriodRequest().fromDate("01 April 2024")
                .interestRate(CHANGED_INTEREST_RATE).isDifferentialToBaseLendingRate(false).locale("en").dateFormat("dd MMMM yyyy");

        FloatingRateCreateRequest floatingRateRequest = new FloatingRateCreateRequest()
                .name(Utils.uniqueRandomStringGenerator("FLOAT_RATE_", 6)).isBaseLendingRate(false).isActive(true)
                .ratePeriods(List.of(initialPeriod, changedPeriod));

        FloatingRateCreateResponse response = ok(() -> fineractClient().floatingRates().createFloatingRate(floatingRateRequest));
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

        String responseJson = FeignRawHttpHelper.post("/loanproducts", Utils.convertToJson(loanProductMap));
        return JsonParser.parseString(responseJson).getAsJsonObject().get("resourceId").getAsInt();
    }

    private Long createAndDisburseLoan(Long clientId, Integer loanProductId, String disburseDateStr) {
        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("10000").withLoanTermFrequency("12")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("12").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withInterestTypeAsDecliningBalance()
                .withExpectedDisbursementDate(disburseDateStr).withSubmittedOnDate(disburseDateStr).withLoanType("individual")
                .build(clientId.toString(), loanProductId.toString(), null);

        JsonObject jsonObject = JsonParser.parseString(loanApplicationJSON).getAsJsonObject();
        jsonObject.remove("interestRatePerPeriod");
        jsonObject.addProperty("interestRateDifferential", "0");
        jsonObject.addProperty("isFloatingInterestRate", true);
        loanApplicationJSON = jsonObject.toString();

        final Long loanId = applyForLoanFromJson(loanApplicationJSON);
        approveLoan(loanId, approveLoanRequest(10000.0, disburseDateStr));
        disburseLoanWithNetDisbursalAmount(loanId, disburseDateStr, "10000");
        return loanId;
    }
}
