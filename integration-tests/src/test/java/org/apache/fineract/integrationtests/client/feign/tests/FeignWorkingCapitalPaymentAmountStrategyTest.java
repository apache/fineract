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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.assertEqualBigDecimal;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.errorCodesOf;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.assertNoNegativeAmounts;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.paymentByNo;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.totalExpectedAmortization;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalAmortizationScheduleValidators.validatePayment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.integrationtests.client.feign.FeignWorkingCapitalTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FeignWorkingCapitalPaymentAmountStrategyTest extends FeignWorkingCapitalTestBase {

    private static final String BUSINESS_DATE = "2026-01-01";
    private static final String DISBURSE_DATE = "01 January 2026";
    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal DISCOUNT_FEE = new BigDecimal("1000");
    private static final BigDecimal PAYMENT_AMOUNT = new BigDecimal("47.22");
    private static final int NPV_DAY_COUNT = 360;
    private static final int TOTAL_PAYMENT_DAYS = 212;
    private static final String FINAL_PAYMENT = "36.58";
    private static final String CALCULATED_ANNUAL_EIR = "43.756245";
    /**
     * The 6-decimal annual percentage spread back over the 360-day count: the rate the schedule actually discounts on.
     */
    private static final String DAILY_RATE_12DP = "0.001008699885";

    private static final BigDecimal EVEN_PAYMENT_AMOUNT = new BigDecimal("50.00");
    private static final int EVEN_TOTAL_PAYMENT_DAYS = 200;

    private static final BigDecimal TPV_RATE = new BigDecimal("18");

    private static final String STRATEGY_TPV = "TPV";
    private static final String STRATEGY_ANNUAL_EIR = "ANNUAL_EIR";
    private static final String STRATEGY_PAYMENT_AMOUNT = "PAYMENT_AMOUNT";

    private static final String WCLP = "validation.msg.WORKINGCAPITALLOANPRODUCT.";
    private static final String WCL = "validation.msg.WORKINGCAPITALLOAN.";

    private static final String PRODUCT_TEMPLATE_PATH = "/working-capital-loan-products/template";

    private final List<Long> createdLoanIds = new ArrayList<>();

    @AfterAll
    void cleanupPaymentAmountLoans() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
    }

    @Test
    @DisplayName("anchor: net 9000 + fee 1000 with payment 47.22 gives 211 x 47.22 + 36.58 and an annual EIR of 43.756245")
    void workedExample_derivesScheduleAndAnnualEirFromTheProvidedPaymentAmount() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createPaymentAmountProduct("PaAnchor", PAYMENT_AMOUNT, DISCOUNT_FEE, NPV_DAY_COUNT);
            final Long loanId = createApproveAndDisbursePaymentAmountLoan(clientId, productId, null);

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEqualBigDecimal(new BigDecimal("1000.00"), schedule.getDiscountFeeAmount(), "schedule discountFeeAmount");
            assertEqualBigDecimal(new BigDecimal("9000.00"), schedule.getNetDisbursementAmount(), "schedule netDisbursementAmount");
            assertEqualBigDecimal(PAYMENT_AMOUNT, schedule.getExpectedPaymentAmount(), "schedule expectedPaymentAmount");
            assertEquals(TOTAL_PAYMENT_DAYS, schedule.getOriginalPaymentNumber(), "211 regular payments plus the short final one");
            assertEqualBigDecimal(new BigDecimal(DAILY_RATE_12DP), round(schedule.getEffectiveInterestRate(), 12),
                    "schedule effectiveInterestRate (daily rate spread back from the 6-decimal annual EIR)");

            assertEqualBigDecimal(PAYMENT_AMOUNT, paymentByNo(schedule, 211).getExpectedPaymentAmount(),
                    "payment 211 must still be a full regular payment");
            assertEqualBigDecimal(new BigDecimal(FINAL_PAYMENT), paymentByNo(schedule, TOTAL_PAYMENT_DAYS).getExpectedPaymentAmount(),
                    "the final payment is the remainder, lower than the configured payment amount");
            assertEqualBigDecimal(BigDecimal.ZERO, paymentByNo(schedule, TOTAL_PAYMENT_DAYS).getExpectedBalance(),
                    "the schedule must close on a zero balance");

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEqualBigDecimal(PAYMENT_AMOUNT, loan.getPeriodPaymentAmount(), "loan periodPaymentAmount");
            assertEquals(TOTAL_PAYMENT_DAYS, loan.getNumberOfRepayments(), "loan numberOfRepayments");
            assertEqualBigDecimal(NET_DISBURSEMENT, loan.getNetDisbursalAmount(), "loan netDisbursalAmount");
            assertEqualBigDecimal(new BigDecimal(CALCULATED_ANNUAL_EIR), round(loan.getCalculatedAnnualEir(), 6),
                    "loan calculatedAnnualEir (percentage, ^npvDayCount convention)");

            validatePayment(schedule, 1, "47.22", "8961.86", "9.08", "990.92");
            validatePayment(schedule, 2, "47.22", "8923.68", "9.04", "981.88");
            validatePayment(schedule, 3, "47.22", "8885.46", "9.00", "972.88");
            validatePayment(schedule, 210, "47.22", "83.68", "0.13", "0.12");
            validatePayment(schedule, 211, "47.22", "36.54", "0.08", "0.04");
            validatePayment(schedule, 212, "36.58", "0.00", "0.04", "0.00");

            assertEqualBigDecimal(new BigDecimal("1000.00"), totalExpectedAmortization(schedule),
                    "the schedule must earn exactly the discount fee — feature:35 'expected amortization sums to the discount fee'");
            assertNoNegativeAmounts(schedule);
        });
    }

    @Test
    @DisplayName("10000 / 50.00 divides evenly, so the last payment is a full 50.00 and there are 200 of them")
    void evenlyDividingSchedule_lastPaymentIsAFullPayment() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createPaymentAmountProduct("PaEven", EVEN_PAYMENT_AMOUNT, DISCOUNT_FEE, NPV_DAY_COUNT);
            final Long loanId = createApproveAndDisbursePaymentAmountLoan(clientId, productId, null);

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEqualBigDecimal(EVEN_PAYMENT_AMOUNT, schedule.getExpectedPaymentAmount(), "schedule expectedPaymentAmount");
            assertEquals(EVEN_TOTAL_PAYMENT_DAYS, schedule.getOriginalPaymentNumber(),
                    "10000 / 50.00 = 200 exactly, so the term is 200 days and not 201");
            assertEqualBigDecimal(EVEN_PAYMENT_AMOUNT, paymentByNo(schedule, EVEN_TOTAL_PAYMENT_DAYS).getExpectedPaymentAmount(),
                    "no extra closing flow is emitted when the final payment is 0");
            assertEqualBigDecimal(BigDecimal.ZERO, paymentByNo(schedule, EVEN_TOTAL_PAYMENT_DAYS).getExpectedBalance(),
                    "the schedule must close on a zero balance");
        });
    }

    @Test
    @DisplayName("a zero or negative paymentAmount is a parameter validation error, not a server error")
    void paymentAmountMustBeGreaterThanZero() {
        assertProductRejected(paymentAmountProductRequest("PaZero", BigDecimal.ZERO, DISCOUNT_FEE, NPV_DAY_COUNT),
                List.of(WCLP + "paymentAmount.not.greater.than.zero"), "a zero paymentAmount must be rejected");

        assertProductRejected(paymentAmountProductRequest("PaNeg", new BigDecimal("-1"), DISCOUNT_FEE, NPV_DAY_COUNT),
                List.of(WCLP + "paymentAmount.not.greater.than.zero"), "a negative paymentAmount must be rejected");
    }

    @Test
    @DisplayName("paymentAmount precision follows the currency: 2 dp rejects 47.225 and accepts 47.2, JPY (0 dp) rejects 47.22 and accepts 47")
    void paymentAmountPrecisionIsDrivenByTheCurrency() {
        assertProductRejected(paymentAmountProductRequest("PaScale", new BigDecimal("47.225"), DISCOUNT_FEE, NPV_DAY_COUNT),
                List.of(WCLP + "paymentAmount.scale.is.greater.than.2"),
                "the payment amount precision comes from the currency, enforced here on a 2-dp product");

        final Long productId = createProduct(paymentAmountProductRequest("PaScaleOk", new BigDecimal("47.2"), DISCOUNT_FEE, NPV_DAY_COUNT));
        final GetWorkingCapitalLoanProductsProductIdResponse product = productHelper.retrieveWorkingCapitalLoanProductById(productId);
        assertEqualBigDecimal(new BigDecimal("47.2"), product.getPaymentAmount(),
                "a paymentAmount below the currency scale must be accepted and stored as given");
        assertEquals("PAYMENT_AMOUNT", product.getPaymentAmountCalculationStrategy().getId(),
                "the product discloses the strategy its paymentAmount belongs to");
        assertNull(product.getAnnualEir(), "a PAYMENT_AMOUNT product carries no annual EIR");

        final PostWorkingCapitalLoanProductsRequest jpyRequest = paymentAmountProductRequest("PaJpy", PAYMENT_AMOUNT, DISCOUNT_FEE,
                NPV_DAY_COUNT).currencyCode("JPY").digitsAfterDecimal(0);
        assertProductRejected(jpyRequest, List.of(WCLP + "paymentAmount.scale.is.greater.than.0"),
                "the payment amount precision comes from the currency, so on a 0-dp currency 47.22 is too fine");

        final Long jpyProductId = createProduct(paymentAmountProductRequest("PaJpyOk", new BigDecimal("47"), DISCOUNT_FEE, NPV_DAY_COUNT)
                .currencyCode("JPY").digitsAfterDecimal(0).minPaymentAmount(new BigDecimal("10")).maxPaymentAmount(new BigDecimal("100")));
        assertEqualBigDecimal(new BigDecimal("47"), productHelper.retrieveWorkingCapitalLoanProductById(jpyProductId).getPaymentAmount(),
                "a whole paymentAmount and whole bounds must be accepted on a 0-dp currency");
    }

    @Test
    @DisplayName("a PAYMENT_AMOUNT product needs a discount fee greater than zero")
    void discountFeeMustBeGreaterThanZeroUnderPaymentAmountStrategy() {
        assertProductRejected(paymentAmountProductRequest("PaZeroDisc", PAYMENT_AMOUNT, BigDecimal.ZERO, NPV_DAY_COUNT),
                List.of(WCLP + "discount.must.be.greater.than.zero.for.payment.amount.strategy"), "a zero discount fee must be rejected");

        assertProductRejected(paymentAmountProductRequest("PaNoDisc", PAYMENT_AMOUNT, null, NPV_DAY_COUNT),
                List.of(WCLP + "discount.must.be.greater.than.zero.for.payment.amount.strategy"),
                "an omitted discount fee must be rejected too");
    }

    @Test
    @DisplayName("under PAYMENT_AMOUNT the % payment rate and annual EIR parameters are all rejected, one per request")
    void paymentAmountStrategyRejectsTheOtherStrategiesParameters() {
        assertProductParameterRejected("periodPaymentRate", p -> p.periodPaymentRate(new BigDecimal("18")));
        assertProductParameterRejected("minPeriodPaymentRate", p -> p.minPeriodPaymentRate(new BigDecimal("5")));
        assertProductParameterRejected("maxPeriodPaymentRate", p -> p.maxPeriodPaymentRate(new BigDecimal("25")));
        assertProductParameterRejected("annualEir", p -> p.annualEir(new BigDecimal("43.7562")));
        assertProductParameterRejected("minAnnualEir", p -> p.minAnnualEir(new BigDecimal("10")));
        assertProductParameterRejected("maxAnnualEir", p -> p.maxAnnualEir(new BigDecimal("90")));
    }

    private void assertProductParameterRejected(final String parameter, final Consumer<PostWorkingCapitalLoanProductsRequest> mutator) {
        final PostWorkingCapitalLoanProductsRequest request = paymentAmountProductRequest("PaRejects", PAYMENT_AMOUNT, DISCOUNT_FEE,
                NPV_DAY_COUNT);
        mutator.accept(request);
        assertProductRejected(request, List.of(WCLP + parameter + ".not.allowed.for.payment.amount.strategy"),
                "PAYMENT_AMOUNT offers no option to set TPV, a % payment rate or an annual EIR — '" + parameter + "'");
    }

    @Test
    @DisplayName("paymentAmount is rejected on TPV and ANNUAL_EIR products, and a manually provided annualEir on a TPV product")
    void otherStrategiesRejectAManuallySetPaymentAmount() {
        assertProductRejected(tpvProductRequest("PaOnTpv").paymentAmount(PAYMENT_AMOUNT),
                List.of(WCLP + "paymentAmount.not.allowed.for.tpv.strategy"),
                "a TPV product offers no option to manually set the payment amount");

        assertProductRejected(annualEirProductRequest("PaOnEir", new BigDecimal("43.7562")).paymentAmount(PAYMENT_AMOUNT),
                List.of(WCLP + "paymentAmount.not.allowed.for.annual.eir.strategy"),
                "ANNUAL_EIR derives the payment amount, so providing one must be rejected");

        assertProductRejected(tpvProductRequest("PaTpvEir").annualEir(new BigDecimal("43.7562")),
                List.of(WCLP + "annualEir.not.allowed.for.tpv.strategy"), "on a TPV product the annual EIR is calculated, not provided");
    }

    @Test
    @DisplayName("a loan-level paymentAmount of 50.00 overrides the product's 47.22 and reschedules to 200 days")
    void loanLevelPaymentAmountOverridesTheProductDefault() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createPaymentAmountProduct("PaOverride", PAYMENT_AMOUNT, DISCOUNT_FEE, NPV_DAY_COUNT);
            final Long loanId = createApproveAndDisbursePaymentAmountLoan(clientId, productId, EVEN_PAYMENT_AMOUNT);

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEqualBigDecimal(EVEN_PAYMENT_AMOUNT, loan.getPeriodPaymentAmount(),
                    "the loan-level paymentAmount must win over the product default");
            assertEquals(EVEN_TOTAL_PAYMENT_DAYS, loan.getNumberOfRepayments(), "10000 / 50.00 = 200 days, not the product default's 212");
        });
    }

    @Test
    @DisplayName("a % payment rate change is rejected on a PAYMENT_AMOUNT loan, as it already is on an ANNUAL_EIR one")
    void rateChangeIsRejectedOnAPaymentAmountLoan() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createPaymentAmountProduct("PaRateChange", PAYMENT_AMOUNT, DISCOUNT_FEE, NPV_DAY_COUNT);
            final Long loanId = createApproveAndDisbursePaymentAmountLoan(clientId, productId, null);

            final CallFailedRuntimeException failure = wcLoanHelper.updateRateExpectingError(loanId,
                    WorkingCapitalLoanRequestBuilders.updateRate(new BigDecimal("17"), DISBURSE_DATE));
            assertEquals(List.of(WCL + "periodPaymentRate.rate.change.not.allowed.for.payment.amount.strategy"), errorCodesOf(failure),
                    "a PAYMENT_AMOUNT loan has no % payment rate to change — mirrors rate.change.not.allowed.for.annual.eir.strategy — "
                            + failure.getResponseBody());
        });
    }

    @Test
    @DisplayName("the product template lists PAYMENT_AMOUNT alongside TPV and ANNUAL_EIR")
    void productTemplateOffersThePaymentAmountStrategy() {
        // Raw GET: the generated template response DTO does not declare paymentAmountCalculationStrategyOptions.
        final String body = FeignRawHttpHelper.get(PRODUCT_TEMPLATE_PATH);
        final JsonObject template = JsonParser.parseString(body).getAsJsonObject();

        final JsonElement options = template.get("paymentAmountCalculationStrategyOptions");
        assertNotNull(options, "the product template must expose paymentAmountCalculationStrategyOptions — " + body);
        final JsonArray strategies = options.getAsJsonArray();
        assertEquals(3, strategies.size(), "the three strategies are TPV, ANNUAL_EIR and PAYMENT_AMOUNT — " + strategies);

        final JsonObject paymentAmount = findStrategy(strategies, STRATEGY_PAYMENT_AMOUNT);
        assertNotNull(paymentAmount, "no PAYMENT_AMOUNT entry in " + strategies);
        assertEquals(STRATEGY_PAYMENT_AMOUNT, paymentAmount.get("code").getAsString(), "strategy code");
        assertEquals("Payment Amount", paymentAmount.get("value").getAsString(), "strategy display value");
    }

    private static JsonObject findStrategy(final JsonArray strategies, final String id) {
        for (final JsonElement element : strategies) {
            final JsonObject option = element.getAsJsonObject();
            if (option.has("id") && id.equals(option.get("id").getAsString())) {
                return option;
            }
        }
        return null;
    }

    @Test
    @DisplayName("a loan application on a PAYMENT_AMOUNT product rejects totalPaymentVolume, periodPaymentRate and annualEir")
    void paymentAmountLoanApplicationRejectsTheOtherStrategiesParameters() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final Long productId = createPaymentAmountProduct("PaAppRejects", PAYMENT_AMOUNT, DISCOUNT_FEE, NPV_DAY_COUNT);

            assertApplicationRejected(paymentAmountApplication(clientId, productId, null).totalPaymentVolume(new BigDecimal("10500")),
                    List.of(WCL + "totalPaymentVolume.not.allowed.for.payment.amount.strategy"),
                    "a PAYMENT_AMOUNT loan application offers no option to set TPV");

            assertApplicationRejected(paymentAmountApplication(clientId, productId, null).periodPaymentRate(new BigDecimal("18")),
                    List.of(WCL + "periodPaymentRate.not.allowed.for.payment.amount.strategy"),
                    "a PAYMENT_AMOUNT loan application offers no option to set a % payment rate");

            assertApplicationRejected(paymentAmountApplication(clientId, productId, null).annualEir(new BigDecimal("43.7562")),
                    List.of(WCL + "annualEir.not.allowed.for.payment.amount.strategy"),
                    "a PAYMENT_AMOUNT loan application offers no option to manually set the annual EIR");
        });
    }

    @Test
    @DisplayName("a loan-level paymentAmount is rejected under TPV and ANNUAL_EIR, and must be positive and currency-scaled")
    void loanApplicationPaymentAmountIsStrategyGatedAndValueChecked() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);

            final Long tpvProductId = createProduct(baseProductBuilder("PaAppTpv").build());
            assertApplicationRejected(
                    WorkingCapitalLoanRequestBuilders
                            .submitApplication(clientId, tpvProductId, NET_DISBURSEMENT, TPV_RATE, DISBURSE_DATE, DISBURSE_DATE)
                            .discount(DISCOUNT_FEE).paymentAmount(PAYMENT_AMOUNT),
                    List.of(WCL + "paymentAmount.not.allowed.for.tpv.strategy"),
                    "a TPV loan application offers no option to manually set the payment amount");

            final Long annualEirProductId = createProduct(annualEirProductRequest("PaAppEir", new BigDecimal("43.7562")));
            assertApplicationRejected(
                    WorkingCapitalLoanRequestBuilders.submitPaymentAmountApplication(clientId, annualEirProductId, NET_DISBURSEMENT,
                            DISCOUNT_FEE, PAYMENT_AMOUNT, DISBURSE_DATE, DISBURSE_DATE),
                    List.of(WCL + "paymentAmount.not.allowed.for.annual.eir.strategy"),
                    "an ANNUAL_EIR loan derives its payment amount, so providing one must be rejected");

            final Long productId = createPaymentAmountProduct("PaAppValue", PAYMENT_AMOUNT, DISCOUNT_FEE, NPV_DAY_COUNT);
            assertApplicationRejected(paymentAmountApplication(clientId, productId, BigDecimal.ZERO),
                    List.of(WCL + "paymentAmount.not.greater.than.zero"), "a zero paymentAmount must be rejected on the loan application");
            assertApplicationRejected(paymentAmountApplication(clientId, productId, new BigDecimal("47.225")),
                    List.of(WCL + "paymentAmount.scale.is.greater.than.2"),
                    "the payment amount precision comes from the currency, enforced on the loan application too");
        });
    }

    @Test
    @DisplayName("a product paymentAmount outside [minPaymentAmount, maxPaymentAmount] is rejected, inside it is accepted")
    void productPaymentAmountMustLieInsideTheConfiguredRange() {
        final BigDecimal min = new BigDecimal("40.00");
        final BigDecimal max = new BigDecimal("60.00");

        assertProductRejected(
                paymentAmountProductBuilder("PaBelowMin", new BigDecimal("39.99"), DISCOUNT_FEE, NPV_DAY_COUNT).withMinPaymentAmount(min)
                        .withMaxPaymentAmount(max).build(),
                List.of(WCLP + "paymentAmount.must.be.greater.than.or.equal.to.min"), "a paymentAmount below minPaymentAmount");

        assertProductRejected(
                paymentAmountProductBuilder("PaAboveMax", new BigDecimal("60.01"), DISCOUNT_FEE, NPV_DAY_COUNT).withMinPaymentAmount(min)
                        .withMaxPaymentAmount(max).build(),
                List.of(WCLP + "paymentAmount.must.be.less.than.or.equal.to.max"), "a paymentAmount above maxPaymentAmount");

        assertProductRejected(
                paymentAmountProductBuilder("PaMinOverMax", PAYMENT_AMOUNT, DISCOUNT_FEE, NPV_DAY_COUNT).withMinPaymentAmount(max)
                        .withMaxPaymentAmount(min).build(),
                List.of(WCLP + "minPaymentAmount.must.be.less.than.or.equal.to.max",
                        WCLP + "paymentAmount.must.be.greater.than.or.equal.to.min",
                        WCLP + "paymentAmount.must.be.less.than.or.equal.to.max"),
                "minPaymentAmount above maxPaymentAmount");

        assertProductRejected(
                paymentAmountProductBuilder("PaBoundScale", PAYMENT_AMOUNT, DISCOUNT_FEE, NPV_DAY_COUNT)
                        .withMinPaymentAmount(new BigDecimal("40.005")).withMaxPaymentAmount(new BigDecimal("60.0001")).build(),
                List.of(WCLP + "minPaymentAmount.scale.is.greater.than.2", WCLP + "maxPaymentAmount.scale.is.greater.than.2"),
                "payment amount bounds finer than the currency");

        final Long productId = createProduct(paymentAmountProductBuilder("PaInRange", PAYMENT_AMOUNT, DISCOUNT_FEE, NPV_DAY_COUNT)
                .withMinPaymentAmount(min).withMaxPaymentAmount(max).build());
        assertEqualBigDecimal(PAYMENT_AMOUNT, productHelper.retrieveWorkingCapitalLoanProductById(productId).getPaymentAmount(),
                "47.22 lies inside [40.00, 60.00] and must be accepted");
    }

    @Test
    @DisplayName("a loan-level paymentAmount outside the product's range is rejected, inside it is accepted")
    void loanPaymentAmountMustLieInsideTheProductRange() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(DISBURSE_DATE);
            final BigDecimal min = new BigDecimal("40.00");
            final BigDecimal max = new BigDecimal("60.00");
            final Long productId = createProduct(paymentAmountProductBuilder("PaLoanRange", PAYMENT_AMOUNT, DISCOUNT_FEE, NPV_DAY_COUNT)
                    .withMinPaymentAmount(min).withMaxPaymentAmount(max).build());

            assertApplicationRejected(paymentAmountApplication(clientId, productId, new BigDecimal("39.99")),
                    List.of(WCL + "paymentAmount.must.be.greater.than.or.equal.to.min"),
                    "a loan-level paymentAmount below the product's minPaymentAmount");
            assertApplicationRejected(paymentAmountApplication(clientId, productId, new BigDecimal("60.01")),
                    List.of(WCL + "paymentAmount.must.be.less.than.or.equal.to.max"),
                    "a loan-level paymentAmount above the product's maxPaymentAmount");

            final Long loanId = wcLoanHelper.submitApplication(paymentAmountApplication(clientId, productId, EVEN_PAYMENT_AMOUNT));
            createdLoanIds.add(loanId);
            assertEqualBigDecimal(EVEN_PAYMENT_AMOUNT, wcLoanHelper.getLoanDetails(loanId).getPaymentAmount(),
                    "50.00 lies inside [40.00, 60.00] and must be accepted and kept on the loan");
        });
    }

    private static WorkingCapitalLoanProductTestBuilder baseProductBuilder(final String label) {
        return new WorkingCapitalLoanProductTestBuilder().withName("WCL " + label + " " + Utils.uniqueRandomStringGenerator("", 8))
                .withShortName(Utils.uniqueRandomStringGenerator("", 4))
                .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE));
    }

    private static PostWorkingCapitalLoanProductsRequest paymentAmountProductRequest(final String label, final BigDecimal paymentAmount,
            final BigDecimal discount, final Integer npvDayCount) {
        return paymentAmountProductBuilder(label, paymentAmount, discount, npvDayCount).build();
    }

    private static WorkingCapitalLoanProductTestBuilder paymentAmountProductBuilder(final String label, final BigDecimal paymentAmount,
            final BigDecimal discount, final Integer npvDayCount) {
        return baseProductBuilder(label).withPeriodPaymentRate(null).withPaymentAmountCalculationStrategy(STRATEGY_PAYMENT_AMOUNT)
                .withPaymentAmount(paymentAmount).withDiscount(discount).withNpvDayCount(npvDayCount);
    }

    private static PostWorkingCapitalLoanProductsRequest tpvProductRequest(final String label) {
        return baseProductBuilder(label).withPaymentAmountCalculationStrategy(STRATEGY_TPV).withPeriodPaymentRate(new BigDecimal("18"))
                .withDiscount(DISCOUNT_FEE).build();
    }

    private static PostWorkingCapitalLoanProductsRequest annualEirProductRequest(final String label, final BigDecimal annualEir) {
        return baseProductBuilder(label).withPeriodPaymentRate(null).withPaymentAmountCalculationStrategy(STRATEGY_ANNUAL_EIR)
                .withAnnualEir(annualEir).withDiscount(DISCOUNT_FEE).build();
    }

    private static PostWorkingCapitalLoansRequest paymentAmountApplication(final Long clientId, final Long productId,
            final BigDecimal paymentAmount) {
        return WorkingCapitalLoanRequestBuilders.submitPaymentAmountApplication(clientId, productId, NET_DISBURSEMENT, DISCOUNT_FEE,
                paymentAmount, DISBURSE_DATE, DISBURSE_DATE);
    }

    private Long createProduct(final PostWorkingCapitalLoanProductsRequest request) {
        return productHelper.createWorkingCapitalLoanProduct(request).getResourceId();
    }

    private void assertProductRejected(final PostWorkingCapitalLoanProductsRequest request, final List<String> expectedCodes,
            final String because) {
        final CallFailedRuntimeException failure = productHelper.createWorkingCapitalLoanProductExpectingFailure(request);
        assertEquals(expectedCodes, errorCodesOf(failure), because + " — " + failure.getResponseBody());
    }

    private void assertApplicationRejected(final PostWorkingCapitalLoansRequest request, final List<String> expectedCodes,
            final String because) {
        final CallFailedRuntimeException failure = wcLoanHelper.submitApplicationExpectingFailure(request);
        assertEquals(expectedCodes, errorCodesOf(failure), because + " — " + failure.getResponseBody());
    }

    private Long createPaymentAmountProduct(final String label, final BigDecimal paymentAmount, final BigDecimal discount,
            final int npvDayCount) {
        return createProduct(paymentAmountProductRequest(label, paymentAmount, discount, npvDayCount));
    }

    private Long createApproveAndDisbursePaymentAmountLoan(final Long clientId, final Long productId, final BigDecimal loanPaymentAmount) {
        final Long loanId = wcLoanHelper.submitApplication(paymentAmountApplication(clientId, productId, loanPaymentAmount));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId,
                WorkingCapitalLoanRequestBuilders.approveWithDiscount(DISBURSE_DATE, NET_DISBURSEMENT, DISBURSE_DATE, DISCOUNT_FEE));
        wcLoanHelper.disburse(loanId,
                WorkingCapitalLoanRequestBuilders.disburseWithDiscount(DISBURSE_DATE, NET_DISBURSEMENT, DISCOUNT_FEE));
        return loanId;
    }

    private static BigDecimal round(final BigDecimal value, final int scale) {
        return value == null ? null : value.setScale(scale, RoundingMode.HALF_EVEN);
    }
}
