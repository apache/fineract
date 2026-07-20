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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestValidators;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

public class LoanRepaymentRescheduleAtDisbursementTest extends FeignLoanTestBase {

    @Test
    public void testLoanRepaymentRescheduleAtDisbursement() {
        final String approvalAmount = "10000";
        final String approveDate = "01 March 2015";
        final String expectedDisbursementDate = "01 March 2015";
        final String disbursementDate = "01 March 2015";
        final String adjustRepaymentDate = "16 March 2015";

        Long clientId = createClient("01 January 2014");

        Long loanProductId = createLoanProductFromJson(buildLoanProductJson());

        List<PostLoansDisbursementData> createTranches = List.of(LoanRequestBuilders.applyTrancheDetail("01 March 2015", 5000.0),
                LoanRequestBuilders.applyTrancheDetail("01 May 2015", 5000.0));

        List<PostLoansDisbursementData> approveTranches = List.of(LoanRequestBuilders.applyTrancheDetail("01 March 2015", 5000.0),
                LoanRequestBuilders.applyTrancheDetail("01 May 2015", 5000.0));

        Long loanId = applyForLoanFromJson(buildLoanApplicationJson(clientId, loanProductId, disbursementDate, createTranches));

        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoanFromJson(loanId, LoanRequestBuilders.approveLoanWithTranchesJson(Double.valueOf(approvalAmount), approveDate,
                expectedDisbursementDate, approveTranches));

        GetLoansLoanIdResponse approvedLoan = getLoanDetails(loanId);
        verifyLoanStatus(approvedLoan, LoanStatus.APPROVED);
        LoanTestValidators.verifyLoanStatus(approvedLoan, status -> Boolean.TRUE.equals(status.getWaitingForDisbursal()));

        disburseLoanWithRepaymentReschedule(loanId, disbursementDate, adjustRepaymentDate);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                .filter(period -> period.getPeriod() != null && period.getPeriod() == 1).findFirst().orElseThrow();

        LoanTestValidators.validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2015, 3, 16), 834.71, 0.0, 834.71, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 49.32, 0.0, 49.32, 0, 0);
        assertEquals(884.03, Utils.getDoubleValue(firstInstallment.getTotalDueForPeriod()));
    }

    private String buildLoanProductJson() {
        return new LoanProductTestBuilder().withPrincipal("10000.00").withNumberOfRepayments("12").withRepaymentAfterEvery("2")
                .withRepaymentTypeAsWeek().withinterestRatePerPeriod("2").withInterestRateFrequencyTypeAsMonths().withTranches(true)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withRepaymentStrategy(LoanProductTestBuilder.RBI_INDIA_STRATEGY)
                .withInterestTypeAsDecliningBalance()
                .withInterestRecalculationDetails(LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                        LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                        LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE)
                .withInterestRecalculationRestFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "0", null, null)
                .withInterestRecalculationCompoundingFrequencyDetails(null, null, null, null).build(null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private String buildLoanApplicationJson(Long clientId, Long loanProductId, String disbursementDate,
            List<PostLoansDisbursementData> tranches) {
        List<HashMap> trancheMaps = tranches.stream().map(tranche -> {
            HashMap map = new HashMap();
            map.put("expectedDisbursementDate", tranche.getExpectedDisbursementDate());
            map.put("principal", tranche.getPrincipal().toPlainString());
            return map;
        }).toList();

        return new LoanApplicationTestBuilder().withPrincipal("10000.00").withLoanTermFrequency("24").withLoanTermFrequencyAsWeeks()
                .withNumberOfRepayments("12").withRepaymentEveryAfter("2").withRepaymentFrequencyTypeAsWeeks()
                .withInterestRatePerPeriod("2").withAmortizationTypeAsEqualInstallments().withTranches(trancheMaps).withFixedEmiAmount("")
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeAsDays()
                .withExpectedDisbursementDate(disbursementDate).withSubmittedOnDate(disbursementDate)
                .withRepaymentStrategy(LoanApplicationTestBuilder.RBI_INDIA_STRATEGY).withCharges(new ArrayList<>())
                .build(clientId.toString(), loanProductId.toString(), null);
    }
}
