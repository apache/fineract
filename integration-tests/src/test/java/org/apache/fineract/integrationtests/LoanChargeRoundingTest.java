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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargeData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanChargeRoundingTest extends FeignLoanTestBase {

    private Long clientId;

    private static final String DATE = "01 January 2026";
    private static final String LATER_DATE = "01 June 2026";

    @BeforeEach
    public void setup() {
        clientId = createClient();
    }

    /** FLAT CHARGE **/
    @Test
    public void shouldApplyRoundingRules_forFlatCharge() {
        runAt(DATE, () -> {

            Long productId = createLoanProduct(0, 1);

            Long loanId = applyAndApproveLoan(productId, 10000.0, 1);

            PostChargesResponse chargeResponse = createFlatCharge(19.8);

            assertNotNull(chargeResponse.getResourceId());
            addLoanCharge(loanId, chargeResponse.getResourceId(), DATE, 19.8);

            BigDecimal actualChargeAmount = getLoanChargeAmount(loanId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("19.8"), 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
        });
    }

    @Test
    public void shouldRoundUpFlatCharge_whenValueIsAboveHalf() {
        runAt(DATE, () -> {

            Long productId = createLoanProduct(0, 1);

            Long loanId = applyAndApproveLoan(productId, 10000.0, 1);

            PostChargesResponse chargeResponse = createFlatCharge(0.6);

            assertNotNull(chargeResponse.getResourceId());
            addLoanCharge(loanId, chargeResponse.getResourceId(), DATE, 0.6);

            BigDecimal actualChargeAmount = getLoanChargeAmount(loanId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = applyRoundingRules(new BigDecimal("0.6"), 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeAmount);
        });
    }

    @Test
    public void shouldFailToAddFlatCharge_whenAmountRoundsToZero() {
        runAt(DATE, () -> {

            Long productId = createLoanProduct(0, 1);

            Long loanId = applyAndApproveLoan(productId, 10000.0, 1);

            PostChargesResponse chargeResponse = createFlatCharge(0.5);
            assertNotNull(chargeResponse.getResourceId());

            PostLoansLoanIdChargesRequest request = buildLoanChargeRequest(chargeResponse.getResourceId(), DATE, 0.5);

            CallFailedRuntimeException exception = loanHelper.addLoanChargeExpectingError(loanId, request);

            assertEquals(403, exception.getStatus());
            assertErrorGlobalisationCode(exception, "error.msg.loanCharge.cannot.be.added.as.amount.rounded.to.zero");

            assertNoChargesPersisted(loanId, chargeResponse.getResourceId());
        });
    }

    /** PERCENTAGE OF AMOUNT CHARGE **/
    @Test
    public void shouldApplyRoundingRules_forPercentageOfAmountCharge() {
        runAt(DATE, () -> {

            Long productId = createLoanProduct(0, 3);

            Long loanId = applyAndApproveLoan(productId, 10000.0, 1);

            PostChargesResponse chargeResponse = createPercentageOfAmountCharge(2.5067);

            assertNotNull(chargeResponse.getResourceId());
            addLoanCharge(loanId, chargeResponse.getResourceId(), DATE, 2.5067);

            BigDecimal actualChargeAmount = getLoanChargeAmount(loanId, chargeResponse.getResourceId());

            BigDecimal principal = getLoanPrincipal(loanId);

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge(principal.toPlainString(), "0.025067", 0, 3);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);

        });
    }

    @Test
    public void shouldRoundUpPercentageOfAmountCharge_whenValueIsAboveHalf() {
        runAt(DATE, () -> {

            Long productId = createLoanProduct(0, 1);

            Long loanId = applyAndApproveLoan(productId, 10000.0, 1);

            PostChargesResponse chargeResponse = createPercentageOfAmountCharge(0.006);

            assertNotNull(chargeResponse.getResourceId());
            addLoanCharge(loanId, chargeResponse.getResourceId(), DATE, 0.006);

            BigDecimal actualChargeAmount = getLoanChargeAmount(loanId, chargeResponse.getResourceId());

            BigDecimal principal = getLoanPrincipal(loanId);

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge(principal.toPlainString(), "0.00006", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeAmount);
        });
    }

    @Test
    public void shouldFailToAddPercentageOfAmountCharge_whenRoundedToZero() {
        runAt(DATE, () -> {
            Long productId = createLoanProduct(0, 1);

            Long loanId = applyAndApproveLoan(productId, 10000.0, 1);

            PostChargesResponse chargeResponse = createPercentageOfAmountCharge(0.005);
            assertNotNull(chargeResponse.getResourceId());

            PostLoansLoanIdChargesRequest request = buildLoanChargeRequest(chargeResponse.getResourceId(), DATE, 0.005);

            CallFailedRuntimeException exception = loanHelper.addLoanChargeExpectingError(loanId, request);

            assertEquals(403, exception.getStatus());
            assertErrorGlobalisationCode(exception, "error.msg.loanCharge.cannot.be.added.as.amount.rounded.to.zero");

            assertNoChargesPersisted(loanId, chargeResponse.getResourceId());
        });
    }

    /** PERCENTAGE OF AMOUNT + INTEREST CHARGE **/
    @Test
    public void shouldApplyRoundingRules_forPercentageOfAmountPlusInterestCharge_beforeInterestAccrual() {
        runAt(DATE, () -> {

            Long productId = createLoanProduct(0, 1);

            Long loanId = applyAndApproveLoan(productId, 10000.0, 1);

            PostChargesResponse chargeResponse = createPercentageOfAmountPlusInterestCharge(2.536);

            assertNotNull(chargeResponse.getResourceId());
            addLoanCharge(loanId, chargeResponse.getResourceId(), DATE, 2.536);

            BigDecimal actualChargeAmount = getLoanChargeAmount(loanId, chargeResponse.getResourceId());

            BigDecimal principal = getLoanPrincipal(loanId);

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge(principal.toPlainString(), "0.02536", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
        });
    }

    @Test
    public void shouldApplyRoundingRules_forPercentageOfAmountPlusInterestCharge_afterInterestAccrualViaCOB() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();

        runAt(DATE, () -> {
            loanIdRef.set(createAndDisburseProgressiveLoan(10000.0, 0, 1));
        });

        runAt(LATER_DATE, () -> {

            Long loanId = loanIdRef.get();

            BigDecimal totalInterest = executeCobAndGetTotalInterest(loanId);

            PostChargesResponse chargeResponse = createPercentageOfAmountPlusInterestCharge(2.536);

            assertNotNull(chargeResponse.getResourceId());
            addLoanCharge(loanId, chargeResponse.getResourceId(), LATER_DATE, 2.536);

            BigDecimal actualChargeAmount = getLoanChargeAmount(loanId, chargeResponse.getResourceId());

            BigDecimal base = new BigDecimal("10000").add(totalInterest);

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge(base.toPlainString(), "0.02536", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
        });
    }

    @Test
    public void shouldRoundUpPercentageOfAmountPlusInterestCharge_whenValueIsAboveHalf() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();

        runAt(DATE, () -> {
            loanIdRef.set(createAndDisburseProgressiveLoan(100.0, 0, 1));
        });

        runAt(LATER_DATE, () -> {

            Long loanId = loanIdRef.get();

            BigDecimal totalInterest = executeCobAndGetTotalInterest(loanId);

            PostChargesResponse chargeResponse = createPercentageOfAmountPlusInterestCharge(0.56);

            assertNotNull(chargeResponse.getResourceId());
            addLoanCharge(loanId, chargeResponse.getResourceId(), LATER_DATE, 0.56);

            BigDecimal actualChargeAmount = getLoanChargeAmount(loanId, chargeResponse.getResourceId());

            BigDecimal base = new BigDecimal("100").add(totalInterest);

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge(base.toPlainString(), "0.0056", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeAmount);
        });
    }

    @Test
    public void shouldFailToAddPercentageOfAmountPlusInterestCharge_whenRoundedToZero() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();

        runAt(DATE, () -> {
            loanIdRef.set(createAndDisburseProgressiveLoan(100.0, 0, 1));
        });

        runAt(LATER_DATE, () -> {

            Long loanId = loanIdRef.get();

            executeCobAndGetTotalInterest(loanId);

            PostChargesResponse chargeResponse = createPercentageOfAmountPlusInterestCharge(0.38);
            assertNotNull(chargeResponse.getResourceId());

            PostLoansLoanIdChargesRequest request = buildLoanChargeRequest(chargeResponse.getResourceId(), DATE, 0.38);

            CallFailedRuntimeException exception = loanHelper.addLoanChargeExpectingError(loanId, request);

            assertEquals(403, exception.getStatus());
            assertErrorGlobalisationCode(exception, "error.msg.loanCharge.cannot.be.added.as.amount.rounded.to.zero");

            assertNoChargesPersisted(loanId, chargeResponse.getResourceId());
        });
    }

    /** PERCENTAGE OF INTEREST **/
    @Test
    public void shouldApplyRoundingRules_forPercentageOfInterestCharge() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();

        runAt(DATE, () -> {
            loanIdRef.set(createAndDisburseProgressiveLoan(10000.0, 0, 1));
        });

        runAt(LATER_DATE, () -> {

            Long loanId = loanIdRef.get();

            BigDecimal totalInterest = executeCobAndGetTotalInterest(loanId);

            PostChargesResponse chargeResponse = createPercentageOfInterestCharge(2.536);

            assertNotNull(chargeResponse.getResourceId());
            addLoanCharge(loanId, chargeResponse.getResourceId(), LATER_DATE, 2.536);

            BigDecimal actualChargeAmount = getLoanChargeAmount(loanId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge(totalInterest.toPlainString(), "0.02536", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
        });
    }

    @Test
    public void shouldRoundUpPercentageOfInterestCharge_whenValueIsAboveHalf() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();

        runAt(DATE, () -> {
            loanIdRef.set(createAndDisburseProgressiveLoan(100.0, 0, 1));
        });

        runAt(LATER_DATE, () -> {

            Long loanId = loanIdRef.get();

            BigDecimal totalInterest = executeCobAndGetTotalInterest(loanId);

            PostChargesResponse chargeResponse = createPercentageOfInterestCharge(2.068);

            assertNotNull(chargeResponse.getResourceId());
            addLoanCharge(loanId, chargeResponse.getResourceId(), LATER_DATE, 2.068);

            BigDecimal actualChargeAmount = getLoanChargeAmount(loanId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge(totalInterest.toPlainString(), "0.02068", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeAmount);
        });
    }

    @Test
    public void shouldFailToAddPercentageOfInterestCharge_whenRoundedToZero() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();

        runAt(DATE, () -> {
            loanIdRef.set(createAndDisburseProgressiveLoan(100.0, 0, 1));
        });

        runAt(LATER_DATE, () -> {

            Long loanId = loanIdRef.get();

            executeCobAndGetTotalInterest(loanId);

            PostChargesResponse chargeResponse = createPercentageOfInterestCharge(1.68);
            assertNotNull(chargeResponse.getResourceId());

            PostLoansLoanIdChargesRequest request = buildLoanChargeRequest(chargeResponse.getResourceId(), DATE, 1.68);

            CallFailedRuntimeException exception = loanHelper.addLoanChargeExpectingError(loanId, request);

            assertEquals(403, exception.getStatus());
            assertErrorGlobalisationCode(exception, "error.msg.loanCharge.cannot.be.added.as.amount.rounded.to.zero");

            assertNoChargesPersisted(loanId, chargeResponse.getResourceId());
        });
    }

    /** PERCENTAGE OF TRANCHE DISBURSEMENT */
    @Test
    public void shouldApplyRoundingRules_forPercentageOfTrancheDisbursementCharge() {
        runAt(DATE, () -> {

            Long productId = createMultiDisbursementLoanProduct(0, 1);

            List<PostLoansDisbursementData> disbursements = List.of(
                    new PostLoansDisbursementData().expectedDisbursementDate("01 January 2026").principal(new BigDecimal("615")),
                    new PostLoansDisbursementData().expectedDisbursementDate("01 February 2026").principal(new BigDecimal("385")));

            Long loanId = applyAndApproveMultiTrancheLoan(productId, disbursements);

            PostChargesResponse chargeResponse = createPercentageOfTrancheDisbursementCharge(0.5);
            assertNotNull(chargeResponse.getResourceId());

            addLoanCharge(loanId, chargeResponse.getResourceId(), DATE, 0.5);

            List<GetLoansLoanIdLoanChargeData> charges = getLoanChargeData(loanId);
            assertNotNull(charges);
            assertEquals(2, charges.size());

            List<BigDecimal> actualAmounts = extractAndSortAmounts(charges);
            assertTrue(actualAmounts.stream().allMatch(amount -> amount.compareTo(BigDecimal.ZERO) > 0));

            List<BigDecimal> expectedAmounts = calculateExpectedTrancheCharges(disbursements, "0.005", 0, 1);

            assertFalse(expectedAmounts.isEmpty());
            assertEquals(expectedAmounts.size(), actualAmounts.size());
            assertBigDecimalListEquals(expectedAmounts, actualAmounts);
        });
    }

    @Test
    public void shouldFailToAddPercentageOfTrancheDisbursementCharge_whenAnyTrancheRoundsToZero() {
        runAt(DATE, () -> {

            Long productId = createMultiDisbursementLoanProduct(0, 1);

            List<PostLoansDisbursementData> disbursements = List.of(
                    new PostLoansDisbursementData().expectedDisbursementDate("01 January 2026").principal(new BigDecimal("615")),
                    new PostLoansDisbursementData().expectedDisbursementDate("01 February 2026").principal(new BigDecimal("385")));

            Long loanId = applyAndApproveMultiTrancheLoan(productId, disbursements);

            PostChargesResponse chargeResponse = createPercentageOfTrancheDisbursementCharge(0.09);
            assertNotNull(chargeResponse.getResourceId());

            PostLoansLoanIdChargesRequest request = buildLoanChargeRequest(chargeResponse.getResourceId(), DATE, 0.09);

            CallFailedRuntimeException exception = loanHelper.addLoanChargeExpectingError(loanId, request);

            assertEquals(403, exception.getStatus());
            assertErrorGlobalisationCode(exception, "error.msg.loanCharge.cannot.be.added.as.amount.rounded.to.zero");

            List<GetLoansLoanIdLoanChargeData> charges = getLoanChargeData(loanId);
            assertTrue(charges == null || charges.isEmpty(), "Expected no charges since rounded amount becomes 0");

            assertNoChargesPersisted(loanId, chargeResponse.getResourceId());
        });
    }

    @Test
    public void shouldFailToAddAnyCharges_whenAllRoundedChargesBecomeZero_forPercentageOfTrancheDisbursementCharge() {
        runAt(DATE, () -> {

            Long productId = createMultiDisbursementLoanProduct(0, 1);

            List<PostLoansDisbursementData> disbursements = List.of(
                    new PostLoansDisbursementData().expectedDisbursementDate("01 January 2026").principal(new BigDecimal("615")),
                    new PostLoansDisbursementData().expectedDisbursementDate("01 February 2026").principal(new BigDecimal("385")));

            Long loanId = applyAndApproveMultiTrancheLoan(productId, disbursements);

            PostChargesResponse chargeResponse = createPercentageOfTrancheDisbursementCharge(0.04);
            assertNotNull(chargeResponse.getResourceId());

            PostLoansLoanIdChargesRequest request = buildLoanChargeRequest(chargeResponse.getResourceId(), DATE, 0.04);

            CallFailedRuntimeException exception = loanHelper.addLoanChargeExpectingError(loanId, request);

            assertEquals(403, exception.getStatus());
            assertErrorGlobalisationCode(exception, "error.msg.loanCharge.cannot.be.added.as.amount.rounded.to.zero");

            List<GetLoansLoanIdLoanChargeData> charges = getLoanChargeData(loanId);
            assertTrue(charges == null || charges.isEmpty(), "Expected no charges since rounded amount becomes 0");

            assertNoChargesPersisted(loanId, chargeResponse.getResourceId());
        });
    }

    /** PERCENTAGE OF DISBURSEMENT */
    @Test
    public void shouldApplyRoundingRules_forPercentageOfDisbursementCharge() {
        runAt(DATE, () -> {

            Long productId = createMultiDisbursementLoanProduct(0, 1);

            List<PostLoansDisbursementData> disbursements = List.of(
                    new PostLoansDisbursementData().expectedDisbursementDate("01 January 2026").principal(new BigDecimal("615")),
                    new PostLoansDisbursementData().expectedDisbursementDate("01 February 2026").principal(new BigDecimal("385")));

            Long loanId = applyAndApproveMultiTrancheLoan(productId, disbursements);

            PostChargesResponse chargeResponse = createPercentageOfDisbursementCharge(0.5);

            assertNotNull(chargeResponse.getResourceId());
            addLoanCharge(loanId, chargeResponse.getResourceId(), DATE, 0.5);

            List<GetLoansLoanIdLoanChargeData> charges = getLoanChargeData(loanId);

            assertNotNull(charges);
            assertEquals(1, charges.size());

            BigDecimal actualChargeAmount = getLoanChargeAmount(loanId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge("615", "0.005", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
        });
    }

    @Test
    public void shouldRoundUpPercentageOfDisbursementCharge_whenValueIsAboveHalf() {
        runAt(DATE, () -> {

            Long productId = createMultiDisbursementLoanProduct(0, 1);

            List<PostLoansDisbursementData> disbursements = List.of(
                    new PostLoansDisbursementData().expectedDisbursementDate("01 January 2026").principal(new BigDecimal("615")),
                    new PostLoansDisbursementData().expectedDisbursementDate("01 February 2026").principal(new BigDecimal("385")));

            Long loanId = applyAndApproveMultiTrancheLoan(productId, disbursements);

            PostChargesResponse chargeResponse = createPercentageOfDisbursementCharge(0.09);
            assertNotNull(chargeResponse.getResourceId());

            addLoanCharge(loanId, chargeResponse.getResourceId(), DATE, 0.09);

            List<GetLoansLoanIdLoanChargeData> charges = getLoanChargeData(loanId);

            assertNotNull(charges);
            assertEquals(1, charges.size());

            BigDecimal actualChargeAmount = getLoanChargeAmount(loanId, chargeResponse.getResourceId());

            BigDecimal expectedChargeAmount = calculateExpectedPercentageCharge("615", "0.0009", 0, 1);

            assertBigDecimalEquals(expectedChargeAmount, actualChargeAmount);
            assertBigDecimalEquals(BigDecimal.ONE, actualChargeAmount);
        });
    }

    @Test
    public void shouldFailToAddPercentageOfDisbursementCharge_whenRoundedToZero() {
        runAt(DATE, () -> {

            Long productId = createMultiDisbursementLoanProduct(0, 1);

            List<PostLoansDisbursementData> disbursements = List.of(
                    new PostLoansDisbursementData().expectedDisbursementDate("01 January 2026").principal(new BigDecimal("615")),
                    new PostLoansDisbursementData().expectedDisbursementDate("01 February 2026").principal(new BigDecimal("385")));

            Long loanId = applyAndApproveMultiTrancheLoan(productId, disbursements);

            PostChargesResponse chargeResponse = createPercentageOfDisbursementCharge(0.04);
            assertNotNull(chargeResponse.getResourceId());

            PostLoansLoanIdChargesRequest request = buildLoanChargeRequest(chargeResponse.getResourceId(), DATE, 0.04);

            CallFailedRuntimeException exception = loanHelper.addLoanChargeExpectingError(loanId, request);

            assertEquals(403, exception.getStatus());
            assertErrorGlobalisationCode(exception, "error.msg.loanCharge.cannot.be.added.as.amount.rounded.to.zero");

            assertNoChargesPersisted(loanId, chargeResponse.getResourceId());
        });
    }

    // -----------------------------
    // HELPERS
    // -----------------------------

    private Long createLoanProduct(int digitsAfterDecimal, int inMultiplesOf) {
        return createLoanProduct(baseLoanProductRequest().digitsAfterDecimal(digitsAfterDecimal).inMultiplesOf(inMultiplesOf));
    }

    private Long createMultiDisbursementLoanProduct(int digitsAfterDecimal, int inMultiplesOf) {
        return createLoanProduct(
                baseLoanProductRequestMultiRepayment().digitsAfterDecimal(digitsAfterDecimal).inMultiplesOf(inMultiplesOf));
    }

    private PostLoanProductsRequest baseLoanProductRequest() {
        return new PostLoanProductsRequest().name("LP-" + UUID.randomUUID()).shortName(randomShortName()).currencyCode("USD").locale("en")
                .dateFormat("dd MMMM yyyy").principal(10000.00).numberOfRepayments(1).repaymentEvery(1).repaymentFrequencyType(1L)
                .interestRatePerPeriod(1.0).interestRateFrequencyType(1).amortizationType(1).interestType(0)
                .interestCalculationPeriodType(1).transactionProcessingStrategyCode("mifos-standard-strategy").accountingRule(1)
                .isInterestRecalculationEnabled(false).daysInYearType(1).daysInMonthType(1);
    }

    private String randomShortName() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        return "L" + chars.charAt(ThreadLocalRandom.current().nextInt(chars.length()))
                + chars.charAt(ThreadLocalRandom.current().nextInt(chars.length()))
                + chars.charAt(ThreadLocalRandom.current().nextInt(chars.length()));
    }

    private PostLoanProductsRequest baseLoanProductRequestMultiRepayment() {
        return baseLoanProductRequest().interestCalculationPeriodType(0).multiDisburseLoan(true).maxTrancheCount(2)
                .outstandingLoanBalance(0.0).disallowExpectedDisbursements(false);
    }

    private Long applyAndApproveLoan(Long productId, double principal, int repayments) {
        Long loanId = applyForLoan(LoanRequestBuilders.applyLoanRequest(clientId, productId, DATE, principal, repayments));
        BigDecimal approvedPrincipal = getLoanPrincipal(loanId);
        assertNotNull(approvedPrincipal);
        approveLoan(loanId, approveLoanRequest(approvedPrincipal.doubleValue(), DATE));
        return loanId;
    }

    private BigDecimal getLoanPrincipal(Long loanId) {
        return getLoanDetails(loanId).getPrincipal();
    }

    private Long applyAndApproveMultiTrancheLoan(Long productId, List<PostLoansDisbursementData> disbursements) {
        double totalPrincipal = disbursements.stream().map(PostLoansDisbursementData::getPrincipal).mapToDouble(BigDecimal::doubleValue)
                .sum();

        PostLoansRequest request = LoanRequestBuilders.applyLoanRequest(clientId, productId, DATE, totalPrincipal, disbursements.size());
        request.interestCalculationPeriodType(0).setDisbursementData(disbursements);
        Long loanId = applyForLoan(request);
        approveLoan(loanId, approveLoanRequest(totalPrincipal, DATE));
        return loanId;
    }

    private PostChargesResponse createFlatCharge(double amount) {
        String uniqueChargeName = "Loan Flat Charge" + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper.createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(1).chargeTimeType(2)
                .chargeCalculationType(1).amount(amount).currencyCode("USD").locale("en").chargePaymentMode(0).active(true).penalty(false));
    }

    private PostChargesResponse createPercentageOfAmountCharge(double percentage) {
        String uniqueChargeName = "Loan Percentage of Amount Charge" + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper
                .createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(1).chargeTimeType(2).chargeCalculationType(2)
                        .amount(percentage).currencyCode("USD").locale("en").chargePaymentMode(0).active(true).penalty(false));
    }

    private PostChargesResponse createPercentageOfAmountPlusInterestCharge(double percentage) {
        String uniqueChargeName = "Loan Percentage of Amount Plus Interest Charge" + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper
                .createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(1).chargeTimeType(2).chargeCalculationType(3)
                        .amount(percentage).currencyCode("USD").locale("en").chargePaymentMode(0).active(true).penalty(false));
    }

    private PostChargesResponse createPercentageOfInterestCharge(double percentage) {
        String uniqueChargeName = "Loan Percentage of Interest Charge" + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper
                .createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(1).chargeTimeType(2).chargeCalculationType(4)
                        .amount(percentage).currencyCode("USD").locale("en").chargePaymentMode(0).active(true).penalty(false));
    }

    private PostChargesResponse createPercentageOfTrancheDisbursementCharge(double percentage) {
        String uniqueChargeName = "Loan Tranche Charge" + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper
                .createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(1).chargeTimeType(12).chargeCalculationType(5)
                        .amount(percentage).currencyCode("USD").locale("en").chargePaymentMode(0).active(true).penalty(false));
    }

    private PostChargesResponse createPercentageOfDisbursementCharge(double percentage) {
        String uniqueChargeName = "Loan Disbursement Charge" + UUID.randomUUID().toString().replace("-", "");
        return chargesHelper
                .createCharge(new ChargeRequest().name(uniqueChargeName).chargeAppliesTo(1).chargeTimeType(1).chargeCalculationType(2)
                        .amount(percentage).currencyCode("USD").locale("en").chargePaymentMode(0).active(true).penalty(false));
    }

    private PostLoansLoanIdChargesRequest buildLoanChargeRequest(Long chargeId, String dueDate, Double amount) {
        return new PostLoansLoanIdChargesRequest().chargeId(chargeId).amount(amount).dueDate(dueDate).dateFormat("dd MMMM yyyy")
                .locale("en");
    }

    private BigDecimal getLoanChargeAmount(Long loanId, Long chargeId) {
        List<GetLoansLoanIdLoanChargeData> charges = getLoanChargeData(loanId);
        assertNotNull(charges);
        GetLoansLoanIdLoanChargeData charge = charges.stream().filter(c -> Objects.equals(c.getChargeId(), chargeId)).findFirst()
                .orElseThrow(() -> new AssertionError("Loan charge not found: " + chargeId));

        BigDecimal amount = charge.getAmount();
        assertNotNull(amount);
        return amount;
    }

    private BigDecimal calculateExpectedPercentageCharge(String baseAmount, String percentageAsDecimal, int digitsAfterDecimal,
            int inMultiplesOf) {
        BigDecimal base = new BigDecimal(baseAmount);
        BigDecimal percentage = new BigDecimal(percentageAsDecimal);
        BigDecimal rawCharge = base.multiply(percentage);
        return applyRoundingRules(rawCharge, digitsAfterDecimal, inMultiplesOf);
    }

    private List<GetLoansLoanIdLoanChargeData> getLoanChargeData(Long loanId) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        return loanDetails.getCharges();
    }

    private List<BigDecimal> extractAndSortAmounts(List<GetLoansLoanIdLoanChargeData> charges) {
        return charges.stream().map(GetLoansLoanIdLoanChargeData::getAmount).sorted().toList();
    }

    private BigDecimal applyRoundingRules(BigDecimal amount, int digitsAfterDecimal, int inMultiplesOf) {
        BigDecimal scaled;

        if (digitsAfterDecimal == 0) {
            BigDecimal fractionPart = amount.remainder(BigDecimal.ONE);

            if (fractionPart.compareTo(new BigDecimal("0.5")) <= 0) {
                scaled = amount.setScale(0, RoundingMode.DOWN);
            } else {
                scaled = amount.setScale(0, RoundingMode.UP);
            }
        } else {
            scaled = amount.setScale(digitsAfterDecimal, RoundingMode.HALF_UP);
        }

        if (digitsAfterDecimal == 0 && inMultiplesOf > 0) {
            BigDecimal divisor = new BigDecimal(inMultiplesOf);
            BigDecimal remainder = scaled.remainder(divisor);

            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                scaled = scaled.add(divisor.subtract(remainder));
            }
        }
        return scaled;
    }

    private List<BigDecimal> calculateExpectedTrancheCharges(List<PostLoansDisbursementData> disbursements, String percentageAsDecimal,
            int digitsAfterDecimal, int inMultiplesOf) {
        BigDecimal percentage = new BigDecimal(percentageAsDecimal);
        List<BigDecimal> expectedAmounts = new ArrayList<>();
        for (PostLoansDisbursementData disbursement : disbursements) {
            BigDecimal rawCharge = disbursement.getPrincipal().multiply(percentage);
            BigDecimal rounded = applyRoundingRules(rawCharge, digitsAfterDecimal, inMultiplesOf);
            if (rounded.compareTo(BigDecimal.ZERO) != 0) {
                expectedAmounts.add(rounded);
            }
        }
        Collections.sort(expectedAmounts);
        return expectedAmounts;
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual));
    }

    private void assertBigDecimalListEquals(List<BigDecimal> expected, List<BigDecimal> actual) {
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(0, expected.get(i).compareTo(actual.get(i)));
        }
    }

    /// AMOUNT + INTEREST
    private Long createAndDisburseProgressiveLoan(double principal, int digitsAfterDecimal, int inMultiplesOf) {
        Long productId = createLoanProduct(
                create4IProgressive().numberOfRepayments(12).interestRatePerPeriod(1.0).isInterestRecalculationEnabled(false)
                        .currencyCode("USD").digitsAfterDecimal(digitsAfterDecimal).inMultiplesOf(inMultiplesOf));

        PostLoansRequest request = LoanRequestBuilders.applyLoanRequest(clientId, productId, DATE, principal, 12);

        request.setInterestRatePerPeriod(new BigDecimal("1.0"));
        request.setInterestRateFrequencyType(1);
        request.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");

        Long loanId = applyForLoan(request);

        approveLoan(loanId, approveLoanRequest(principal, DATE));

        disburseLoanWithAmount(loanId, DATE, principal);

        return loanId;
    }

    private BigDecimal executeCobAndGetTotalInterest(Long loanId) {
        executeInlineCOB(loanId);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

        BigDecimal totalInterest = loanDetails.getRepaymentSchedule().getPeriods().stream()
                .map(p -> p.getInterestDue() == null ? BigDecimal.ZERO : p.getInterestDue()).reduce(BigDecimal.ZERO, BigDecimal::add);

        assertTrue(totalInterest.compareTo(BigDecimal.ZERO) > 0);

        return totalInterest;
    }

    private void assertNoChargesPersisted(Long loanId, Long chargeId) {
        List<GetLoansLoanIdLoanChargeData> charges = getLoanChargeData(loanId);

        boolean chargeExists = charges != null && charges.stream().anyMatch(c -> Objects.equals(c.getChargeId(), chargeId));

        assertFalse(chargeExists, "Expected charge not to persist since rounded amount becomes 0");
    }
}
