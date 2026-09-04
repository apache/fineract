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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanFixedPrincipalPercentageAmortizationTest extends FeignLoanTestBase {

    private static final String ACCOUNTING_NONE = "1";
    private static final String PRINCIPAL = "100000.00";
    private static final String DISBURSEMENT_DATE = "20 September 2011";

    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void checkLoanCreateAndDisburseFlowWithFixedPrincipalPercentage() {
        final Long clientId = createVerifiedClient();
        final Long loanProductId = createLoanProduct(false);
        final Long loanId = applyForLoanApplication(clientId, loanProductId, 13, null);

        verifyLoanRepaymentScheduleForEqualPrincipal(getLoanDetails(loanId).getRepaymentSchedule().getPeriods());
    }

    @Test
    public void checkLoanCreateAndDisburseFlowWithFixedPrincipalPercentageWithPrincipalGrace() {
        final Long clientId = createVerifiedClient();
        final Long loanProductId = createLoanProduct(false);
        final Long loanId = applyForLoanApplication(clientId, loanProductId, 19, 6);

        verifyLoanRepaymentScheduleForEqualPrincipalWithPrincipalGrace(getLoanDetails(loanId).getRepaymentSchedule().getPeriods());
    }

    @Test
    public void checkLoanCreateAndDisburseFlowWithFixedPrincipalPercentageAndFlatInterest() {
        final Long clientId = createVerifiedClient();
        final Long loanProductId = createLoanProduct(true);
        final Long loanId = applyForLoanApplicationWithFlatInterest(clientId, loanProductId);

        verifyLoanRepaymentScheduleForEqualPrincipalAndFlatInterest(getLoanDetails(loanId).getRepaymentSchedule().getPeriods());
    }

    private Long createVerifiedClient() {
        final Long clientId = createClient();
        assertEquals(clientId, clientHelper.getClient(clientId).getId(), "ERROR IN CREATING THE CLIENT");
        return clientId;
    }

    private Long createLoanProduct(final boolean flatInterest) {
        final LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal(PRINCIPAL) //
                .withNumberOfRepayments("13") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestCalculationPeriodTypeAsDays().withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() // This is required to fix the principal
                .withPrinciplePercentagePerInstallment("5.00") // This fixes the principal at a fixed value till the
                                                               // second last EMI
                .withAccounting(ACCOUNTING_NONE, new Account[0]);

        return createLoanProduct(
                (flatInterest ? builder.withInterestTypeAsFlat() : builder.withInterestTypeAsDecliningBalance()).buildRequest(null));
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, final int numberOfRepayments,
            final Integer principalGrace) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);

        final PostLoansRequest application = baseApplication(clientId, loanProductId, numberOfRepayments)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .graceOnPrincipalPayment(principalGrace)//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));

        return loanHelper.applyForLoan(application).getLoanId();
    }

    private Long applyForLoanApplicationWithFlatInterest(final Long clientId, final Long loanProductId) {
        final PostLoansRequest application = baseApplication(clientId, loanProductId, 13)//
                .interestType(LoanTestData.InterestType.FLAT);

        return loanHelper.applyForLoan(application).getLoanId();
    }

    private PostLoansRequest baseApplication(final Long clientId, final Long loanProductId, final int numberOfRepayments) {
        return LoanRequestBuilders.applyLoan(clientId, loanProductId, DISBURSEMENT_DATE, Double.parseDouble(PRINCIPAL), numberOfRepayments)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL) // This is required to fix the
                                                                                 // principal
                .fixedPrincipalPercentagePerInstallment(new BigDecimal("5.00")) // This fixes the principal at a fixed
                                                                                // value till the second last EMI
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY);
    }

    private void verifyPeriod(final List<GetLoansLoanIdRepaymentPeriod> loanSchedule, final int period, final LocalDate dueDate,
            final String principalDue, final String interestDue, final String label) {
        assertEquals(dueDate, loanSchedule.get(period).getDueDate(), "Checking for Due Date for " + label);
        assertEquals(0, new BigDecimal(principalDue).compareTo(loanSchedule.get(period).getPrincipalDue()),
                "Checking for Principal Due for " + label);
        assertEquals(0, new BigDecimal(interestDue).compareTo(loanSchedule.get(period).getInterestOriginalDue()),
                "Checking for Interest Due for " + label);
    }

    private void verifyLoanRepaymentScheduleForEqualPrincipal(final List<GetLoansLoanIdRepaymentPeriod> loanSchedule) {
        verifyPeriod(loanSchedule, 1, LocalDate.of(2011, 10, 20), "5000", "1972.60", "1st Month");
        verifyPeriod(loanSchedule, 2, LocalDate.of(2011, 11, 20), "5000", "1936.44", "2nd Month");
        verifyPeriod(loanSchedule, 3, LocalDate.of(2011, 12, 20), "5000", "1775.34", "3rd Month");
        verifyPeriod(loanSchedule, 12, LocalDate.of(2012, 9, 20), "5000", "917.26", "12th Month");
        verifyPeriod(loanSchedule, 13, LocalDate.of(2012, 10, 20), "40000", "789.04", "13th Month - Last EMI");
    }

    private void verifyLoanRepaymentScheduleForEqualPrincipalWithPrincipalGrace(final List<GetLoansLoanIdRepaymentPeriod> loanSchedule) {
        verifyPeriod(loanSchedule, 1, LocalDate.of(2011, 10, 20), "0", "1972.60", "1st Month");
        verifyPeriod(loanSchedule, 6, LocalDate.of(2012, 3, 20), "0", "1906.85", "6th Month");
        verifyPeriod(loanSchedule, 7, LocalDate.of(2012, 4, 20), "5000", "2038.36", "7th Month");
        verifyPeriod(loanSchedule, 18, LocalDate.of(2013, 3, 20), "5000", "828.49", "18th Month");
        verifyPeriod(loanSchedule, 19, LocalDate.of(2013, 4, 20), "40000", "815.34", "19th Month - Last EMI");
    }

    private void verifyLoanRepaymentScheduleForEqualPrincipalAndFlatInterest(final List<GetLoansLoanIdRepaymentPeriod> loanSchedule) {
        verifyPeriod(loanSchedule, 1, LocalDate.of(2011, 10, 20), "5000", "2002.95", "1st Month");
        verifyPeriod(loanSchedule, 2, LocalDate.of(2011, 11, 20), "5000", "2002.95", "2nd Month");
        verifyPeriod(loanSchedule, 3, LocalDate.of(2011, 12, 20), "5000", "2002.95", "3rd Month");
        verifyPeriod(loanSchedule, 12, LocalDate.of(2012, 9, 20), "5000", "2002.95", "12th Month");
        verifyPeriod(loanSchedule, 13, LocalDate.of(2012, 10, 20), "40000", "2002.96", "13th Month - Last EMI");
    }
}
