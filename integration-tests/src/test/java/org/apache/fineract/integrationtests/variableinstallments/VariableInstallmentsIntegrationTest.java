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
package org.apache.fineract.integrationtests.variableinstallments;

import static org.apache.fineract.integrationtests.client.feign.modules.VariableInstallmentsRequestBuilders.addedByInstallmentAmount;
import static org.apache.fineract.integrationtests.client.feign.modules.VariableInstallmentsRequestBuilders.addedByPrincipal;
import static org.apache.fineract.integrationtests.client.feign.modules.VariableInstallmentsRequestBuilders.deleted;
import static org.apache.fineract.integrationtests.client.feign.modules.VariableInstallmentsRequestBuilders.deletedInstallments;
import static org.apache.fineract.integrationtests.client.feign.modules.VariableInstallmentsRequestBuilders.modifiedByInstallmentAmount;
import static org.apache.fineract.integrationtests.client.feign.modules.VariableInstallmentsRequestBuilders.modifiedByPrincipal;
import static org.apache.fineract.integrationtests.client.feign.modules.VariableInstallmentsRequestBuilders.movedByInstallmentAmount;
import static org.apache.fineract.integrationtests.client.feign.modules.VariableInstallmentsRequestBuilders.movedByPrincipal;
import static org.apache.fineract.integrationtests.client.feign.modules.VariableInstallmentsRequestBuilders.variations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.PostLoansLoanIdScheduleExceptions;
import org.apache.fineract.client.models.PostLoansLoanIdSchedulePeriod;
import org.apache.fineract.client.models.PostLoansLoanIdScheduleRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignVariableInstallmentsHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

public class VariableInstallmentsIntegrationTest extends FeignLoanTestBase {

    private static final String NONE = "1";
    private static final String PRINCIPAL = "1,00,000.00";
    private static final String APPLICATION_DATE = "20 September 2011";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

    private final FeignVariableInstallmentsHelper variationsHelper = new FeignVariableInstallmentsHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testVariableLoanProductCreation() {
        final Long loanProductId = createDecliningBalanceProduct();
        final GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        assertTrue(loanProduct.getAllowVariableInstallments());
        assertEquals(5, loanProduct.getMinimumGap());
        assertEquals(90, loanProduct.getMaximumGap());
    }

    @Test
    public void testLoanProductCreation() {
        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder()//
                .withPrincipal(PRINCIPAL)//
                .withNumberOfRepayments("4")//
                .withRepaymentAfterEvery("1")//
                .withRepaymentTypeAsMonth()//
                .withinterestRatePerPeriod("1")//
                .withInterestRateFrequencyTypeAsMonths()//
                .withAmortizationTypeAsEqualInstallments()//
                .withInterestTypeAsDecliningBalance()//
                .withTranches(false)//
                .withAccounting(NONE, new Account[0])//
                .buildRequest(null));
        assertFalse(retrieveLoanProduct(loanProductId).getAllowVariableInstallments());
    }

    @Test
    public void testDeleteInstallmentsWithDecliningBalanceEqualInstallments() {
        final Long loanId = createDecliningBalanceLoan();
        final String firstInstallmentDueDate = dueDateOf(loanId, 1);

        final PostLoansLoanIdScheduleRequest request = variations(deleted(firstInstallmentDueDate));

        verifyCalculated(variationsHelper.validateVariations(loanId, request),
                new String[] { "20 November 2011", "20 December 2011", "20 January 2012" },
                new String[] { "34675.47", "34675.47", "36756.26" });
        variationsHelper.submitVariations(loanId, request);
        verifyPersisted(loanId, new String[] { "20 November 2011", "20 December 2011", "20 January 2012" },
                new String[] { "34675.47", "34675.47", "36756.26" });
    }

    @Test
    public void testAddInstallmentsWithDecliningBalanceEqualInstallments() {
        final Long loanId = createDecliningBalanceLoan();

        final PostLoansLoanIdScheduleRequest request = variations(
                new PostLoansLoanIdScheduleExceptions().newinstallments(List.of(addedByInstallmentAmount("31 October 2011", 5000))));

        final String[] dates = { "20 October 2011", "31 October 2011", "20 November 2011", "20 December 2011", "20 January 2012" };
        final String[] amounts = { "21215.84", "5000.0", "26477.31", "26477.31", "25947.7" };
        verifyCalculated(variationsHelper.validateVariations(loanId, request), dates, amounts);
        variationsHelper.submitVariations(loanId, request);
        verifyPersisted(loanId, dates, amounts);
    }

    @Test
    public void testModifyInstallmentWithDecliningBalanceEqualInstallments() {
        final Long loanId = createDecliningBalanceLoan();
        final String firstInstallmentDueDate = dueDateOf(loanId, 1);

        final PostLoansLoanIdScheduleRequest request = variations(new PostLoansLoanIdScheduleExceptions()
                .modifiedinstallments(List.of(modifiedByInstallmentAmount(firstInstallmentDueDate, 30000))));

        final String[] dates = { "20 October 2011", "20 November 2011", "20 December 2011", "20 January 2012" };
        final String[] amounts = { "30000.0", "24966.34", "24966.34", "24966.33" };
        verifyCalculated(variationsHelper.validateVariations(loanId, request), dates, amounts);
        variationsHelper.submitVariations(loanId, request);
        verifyPersisted(loanId, dates, amounts);
    }

    @Test
    public void testAllVariationsDecliningBalancewithEqualInstallments() {
        final Long loanId = createDecliningBalanceLoan();

        final PostLoansLoanIdScheduleRequest request = variations(new PostLoansLoanIdScheduleExceptions()
                .modifiedinstallments(List.of(modifiedByInstallmentAmount("20 November 2011", 30000)))
                .newinstallments(List.of(addedByInstallmentAmount("25 December 2011", 5000)))
                .deletedinstallments(deletedInstallments("20 December 2011")));

        final String[] dates = { "20 October 2011", "20 November 2011", "25 December 2011", "20 January 2012" };
        final String[] amounts = { "26262.38", "30000.0", "5000.0", "44077.0" };
        verifyCalculated(variationsHelper.validateVariations(loanId, request), dates, amounts);
        variationsHelper.submitVariations(loanId, request);
        verifyPersisted(loanId, dates, amounts);
    }

    @Test
    public void testAllVariationsDecliningBalancewithEqualPrincipal() {
        final Long loanProductId = createLoanProduct(
                variableProductBuilder().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                        .withInterestRateFrequencyTypeAsMonths().withVariableInstallmentsConfig(Boolean.TRUE, 5, 90).buildRequest(null));
        final Long loanId = applyForLoanApplication(loanProductId, LoanTestData.AmortizationType.EQUAL_PRINCIPAL,
                LoanTestData.InterestType.DECLINING_BALANCE);

        final PostLoansLoanIdScheduleRequest request = variations(
                new PostLoansLoanIdScheduleExceptions().modifiedinstallments(List.of(modifiedByPrincipal("20 November 2011", 30000)))
                        .newinstallments(List.of(addedByPrincipal("25 December 2011", 5000)))
                        .deletedinstallments(deletedInstallments("20 December 2011")));

        final String[] dates = { "20 October 2011", "20 November 2011", "25 December 2011", "20 January 2012" };
        final String[] amounts = { "27000.0", "31500.0", "6045.16", "40670.97" };
        verifyCalculated(variationsHelper.validateVariations(loanId, request), dates, amounts);
        variationsHelper.submitVariations(loanId, request);
        verifyPersisted(loanId, dates, amounts);
    }

    @Test
    public void testModifyDatesWithDecliningBalanceEqualInstallments() {
        final Long loanId = createDecliningBalanceLoan();

        final PostLoansLoanIdScheduleRequest request = variations(new PostLoansLoanIdScheduleExceptions()
                .modifiedinstallments(List.of(movedByInstallmentAmount("20 December 2011", "04 January 2012", 20000.0),
                        movedByInstallmentAmount("20 January 2012", "08 February 2012", null))));

        final String[] dates = { "20 October 2011", "20 November 2011", "04 January 2012", "08 February 2012" };
        final String[] amounts = { "26262.38", "26262.38", "20000.0", "33242.97" };
        verifyCalculated(variationsHelper.validateVariations(loanId, request), dates, amounts);
        variationsHelper.submitVariations(loanId, request);
        verifyPersisted(loanId, dates, amounts);
    }

    @Test
    public void testDeleteInstallmentsWithInterestTypeFlat() {
        final Long loanId = createFlatLoan();
        final String firstInstallmentDueDate = dueDateOf(loanId, 1);

        final PostLoansLoanIdScheduleRequest request = variations(deleted(firstInstallmentDueDate));

        final String[] dates = { "20 November 2011", "20 December 2011", "20 January 2012" };
        final String[] amounts = { "36000.0", "36000.0", "36000.0" };
        verifyCalculated(variationsHelper.validateVariations(loanId, request), dates, amounts);
        variationsHelper.submitVariations(loanId, request);
        verifyPersisted(loanId, dates, amounts);
    }

    @Test
    public void testAddInstallmentsWithInterestTypeFlat() {
        final Long loanId = createFlatLoan();

        final PostLoansLoanIdScheduleRequest request = variations(
                new PostLoansLoanIdScheduleExceptions().newinstallments(List.of(addedByPrincipal("31 October 2011", 5000))));

        final String[] dates = { "20 October 2011", "31 October 2011", "20 November 2011", "20 December 2011", "20 January 2012" };
        final String[] amounts = { "21600.0", "6600.0", "26600.0", "26600.0", "26600.0" };
        verifyCalculated(variationsHelper.validateVariations(loanId, request), dates, amounts);
        variationsHelper.submitVariations(loanId, request);
        verifyPersisted(loanId, dates, amounts);
    }

    @Test
    public void testModifyInstallmentsWithInterestTypeisFlat() {
        final Long loanId = createFlatLoan();
        final String firstInstallmentDueDate = dueDateOf(loanId, 1);

        final PostLoansLoanIdScheduleRequest request = variations(
                new PostLoansLoanIdScheduleExceptions().modifiedinstallments(List.of(modifiedByPrincipal(firstInstallmentDueDate, 30000))));

        final String[] dates = { "20 October 2011", "20 November 2011", "20 December 2011", "20 January 2012" };
        final String[] amounts = { "32000.0", "25333.33", "25333.33", "25333.34" };
        verifyCalculated(variationsHelper.validateVariations(loanId, request), dates, amounts);
        variationsHelper.submitVariations(loanId, request);
        verifyPersisted(loanId, dates, amounts);
    }

    @Test
    public void testAllVariationsWithInterestTypeFlat() {
        final Long loanId = createFlatLoan();

        final PostLoansLoanIdScheduleRequest request = variations(
                new PostLoansLoanIdScheduleExceptions().modifiedinstallments(List.of(modifiedByPrincipal("20 November 2011", 30000)))
                        .newinstallments(List.of(addedByPrincipal("25 December 2011", 5000)))
                        .deletedinstallments(deletedInstallments("20 December 2011")));

        final String[] dates = { "20 October 2011", "20 November 2011", "25 December 2011", "20 January 2012" };
        final String[] amounts = { "27000.0", "32000.0", "7000.0", "42000.0" };
        verifyCalculated(variationsHelper.validateVariations(loanId, request), dates, amounts);
        variationsHelper.submitVariations(loanId, request);
        verifyPersisted(loanId, dates, amounts);
    }

    @Test
    public void testModifyDatesWithInterestTypeFlat() {
        final Long loanId = createFlatLoan();

        final PostLoansLoanIdScheduleRequest request = variations(new PostLoansLoanIdScheduleExceptions()
                .modifiedinstallments(List.of(movedByPrincipal("20 December 2011", "04 January 2012", 20000.0),
                        movedByPrincipal("20 January 2012", "08 February 2012", null))));

        final String[] dates = { "20 October 2011", "20 November 2011", "04 January 2012", "08 February 2012" };
        final String[] amounts = { "27306.45", "27306.45", "22306.45", "32306.46" };
        verifyCalculated(variationsHelper.validateVariations(loanId, request), dates, amounts);
        variationsHelper.submitVariations(loanId, request);
        verifyPersisted(loanId, dates, amounts);
    }

    private LoanProductTestBuilder variableProductBuilder() {
        return new LoanProductTestBuilder()//
                .withPrincipal(PRINCIPAL)//
                .withNumberOfRepayments("4")//
                .withRepaymentAfterEvery("1")//
                .withRepaymentTypeAsMonth()//
                .withinterestRatePerPeriod("1")//
                .withTranches(false)//
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true)//
                .withAccounting(NONE, new Account[0]);
    }

    private Long createDecliningBalanceProduct() {
        return createLoanProduct(variableProductBuilder()//
                .withInterestRateFrequencyTypeAsMonths()//
                .withAmortizationTypeAsEqualInstallments()//
                .withInterestTypeAsDecliningBalance()//
                .withVariableInstallmentsConfig(Boolean.TRUE, 5, 90)//
                .buildRequest(null));
    }

    private Long createFlatProduct() {
        return createLoanProduct(variableProductBuilder()//
                .withAmortizationTypeAsEqualPrincipalPayment()//
                .withInterestTypeAsFlat()//
                .withVariableInstallmentsConfig(Boolean.TRUE, 5, 90)//
                .buildRequest(null));
    }

    private Long createDecliningBalanceLoan() {
        return applyForLoanApplication(createDecliningBalanceProduct(), LoanTestData.AmortizationType.EQUAL_INSTALLMENTS,
                LoanTestData.InterestType.DECLINING_BALANCE);
    }

    private Long createFlatLoan() {
        return applyForLoanApplication(createFlatProduct(), LoanTestData.AmortizationType.EQUAL_PRINCIPAL, LoanTestData.InterestType.FLAT);
    }

    private Long applyForLoanApplication(final Long loanProductId, final Integer amortizationType, final Integer interestType) {
        final Long clientId = createClient();
        assertEquals(clientId, clientHelper.getClient(clientId).getId(), "ERROR IN CREATING THE CLIENT");

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);

        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, APPLICATION_DATE, 100000.00, 4)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .amortizationType(amortizationType)//
                .interestType(interestType)//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));

        final Long loanId = loanHelper.applyForLoan(application).getLoanId();
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        return loanId;
    }

    /** The due date of a period of the loan's current schedule, formatted the way a variation request expects it. */
    private String dueDateOf(final Long loanId, final int period) {
        return dateFormatter.format(persistedPeriods(loanId).get(period).getDueDate());
    }

    private List<GetLoansLoanIdRepaymentPeriod> persistedPeriods(final Long loanId) {
        return getLoanDetails(loanId).getRepaymentSchedule().getPeriods();
    }

    /**
     * The expected amounts are compared by value, not by {@code toString()}: the server states them with its own scale
     * ("30000.00"), which need not match the scale the expectation is written with ("30000.0").
     */
    private void verifyCalculated(final List<PostLoansLoanIdSchedulePeriod> periods, final String[] dueDates,
            final String[] installmentAmounts) {
        assertEquals(dueDates.length, periods.size());
        for (int i = 0; i < periods.size(); i++) {
            assertEquals(dueDates[i], dateFormatter.format(periods.get(i).getDueDate()));
            assertEquals(0, new BigDecimal(installmentAmounts[i]).compareTo(periods.get(i).getTotalOutstandingForPeriod()),
                    "installment " + i);
        }
    }

    private void verifyPersisted(final Long loanId, final String[] dueDates, final String[] installmentAmounts) {
        // the persisted schedule leads with the disbursement period, which the calculated one does not carry
        final List<GetLoansLoanIdRepaymentPeriod> periods = persistedPeriods(loanId).subList(1, persistedPeriods(loanId).size());
        assertEquals(dueDates.length, periods.size());
        for (int i = 0; i < periods.size(); i++) {
            assertEquals(dueDates[i], dateFormatter.format(periods.get(i).getDueDate()));
            assertEquals(0, new BigDecimal(installmentAmounts[i]).compareTo(periods.get(i).getTotalOutstandingForPeriod()),
                    "installment " + i);
        }
    }
}
