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
package org.apache.fineract.portfolio.charge.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.MonthDay;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.portfolio.charge.data.ChargeCreateRequest;
import org.apache.fineract.portfolio.charge.data.ChargeUpdateRequest;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class ChargeValidationTest {

    private static final String PREFIX = "{org.apache.fineract.portfolio.charge.";
    private static final String NAME_NOT_BLANK = PREFIX + "name.not-blank}";
    private static final String AMOUNT_POSITIVE = PREFIX + "amount.positive}";
    private static final String CHARGE_APPLIES_TO_NOT_NULL = PREFIX + "charge-applies-to.not-null}";
    private static final String CHARGE_APPLIES_TO_INVALID = PREFIX + "charge-applies-to.invalid}";
    private static final String CHARGE_TIME_TYPE_NOT_NULL = PREFIX + "charge-time-type.not-null}";
    private static final String CHARGE_TIME_TYPE_INVALID = PREFIX + "charge-time-type.invalid}";
    private static final String CHARGE_TIME_TYPE_NOT_ALLOWED_FOR_LOAN = PREFIX + "charge-time-type.not-allowed-for-loan}";
    private static final String CHARGE_CALCULATION_TYPE_INVALID = PREFIX + "charge-calculation-type.invalid}";
    private static final String CHARGE_CALCULATION_TYPE_PERCENTAGE_ONLY = PREFIX
            + "charge-calculation-type.percentage-only-for-withdrawal-or-no-activity}";
    private static final String CHARGE_CALCULATION_TYPE_INVALID_FOR_TRANCHE = PREFIX
            + "charge-calculation-type.invalid-for-tranche-disbursement}";
    private static final String CHARGE_PAYMENT_MODE_NOT_NULL = PREFIX + "charge-payment-mode.not-null}";
    private static final String FEE_ON_MONTH_DAY_NOT_NULL = PREFIX + "fee-on-month-day.not-null}";
    private static final String FEE_ON_MONTH_DAY_INVALID = PREFIX + "fee-on-month-day.invalid}";
    private static final String FEE_ON_MONTH_DAY_NOT_SUPPORTED = PREFIX + "fee-on-month-day.not-supported}";
    private static final String FEE_INTERVAL_NOT_NULL = PREFIX + "fee-interval.not-null}";
    private static final String FEE_INTERVAL_MONTHLY_RANGE = PREFIX + "fee-interval.monthly-range}";
    private static final String TAX_GROUP_MODIFICATION_NOT_SUPPORTED = PREFIX + "tax-group-id.modification-not-supported}";
    private static final String CHARGE_CALCULATION_TYPE_INVALID_FOR_DOWN_PAYMENT = PREFIX
            + "charge-calculation-type.invalid-for-down-payment}";
    private static final String PENALTY_NOT_SUPPORTED_FOR_DOWN_PAYMENT = PREFIX + "penalty.not-supported-for-down-payment}";
    private static final String CHARGE_PAYMENT_MODE_INVALID = PREFIX + "charge-payment-mode.invalid}";
    private static final String FEE_FREQUENCY_RANGE = PREFIX + "fee-frequency.range}";
    private static final String FREE_WITHDRAWAL_FREQUENCY_NOT_NULL = PREFIX + "free-withdrawal-frequency.not-null}";
    private static final String RESTART_COUNT_FREQUENCY_NOT_NULL = PREFIX + "restart-count-frequency.not-null}";
    private static final String COUNT_FREQUENCY_TYPE_NOT_NULL = PREFIX + "count-frequency-type.not-null}";

    // the format/value combination the platform is actually called with; it only parses leniently
    private static final String FEE_ON_MONTH_DAY = "04 March";
    private static final String MONTH_DAY_FORMAT = "dd MMM";

    private ChargeRepository repository;
    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    public void setUp() {
        repository = mock(ChargeRepository.class);
        validatorFactory = Validation.byDefaultProvider().configure().constraintValidatorFactory(new ConstraintValidatorFactory() {

            @Override
            @SuppressWarnings("unchecked")
            public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> key) {
                if (key.equals(ChargeUpdateValidator.class)) {
                    return (T) new ChargeUpdateValidator(repository);
                }
                try {
                    return key.getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public void releaseInstance(final ConstraintValidator<?, ?> instance) {}
        }).buildValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    public void tearDown() {
        validatorFactory.close();
    }

    // ---------------------------------------------------------------- create

    @Test
    public void createValidLoanChargeHasNoViolations() {
        assertThat(validate(loanChargeRequest())).isEmpty();
    }

    @Test
    public void createBlankNameFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setName("  ");

        assertThat(validate(request)).containsExactly(NAME_NOT_BLANK);
    }

    @Test
    public void createZeroAmountFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setAmount(0.0);

        assertThat(validate(request)).containsExactly(AMOUNT_POSITIVE);
    }

    @Test
    public void createWithoutChargeAppliesToFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setChargeAppliesTo(null);

        assertThat(validate(request)).contains(CHARGE_APPLIES_TO_NOT_NULL);
    }

    @Test
    public void createWithUnknownChargeAppliesToFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setChargeAppliesTo(99);

        assertThat(validate(request)).containsExactly(CHARGE_APPLIES_TO_INVALID);
    }

    @Test
    public void createWithoutChargeTimeTypeFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setChargeTimeType(null);

        assertThat(validate(request)).containsExactly(CHARGE_TIME_TYPE_NOT_NULL);
    }

    @Test
    public void createLoanChargeWithSavingsOnlyChargeTimeTypeFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setChargeTimeType(ChargeTimeType.MONTHLY_FEE.getValue());

        assertThat(validate(request)).containsExactly(CHARGE_TIME_TYPE_INVALID);
    }

    @Test
    public void createLoanChargeWithoutChargePaymentModeFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setChargePaymentMode(null);

        assertThat(validate(request)).containsExactly(CHARGE_PAYMENT_MODE_NOT_NULL);
    }

    @Test
    public void createTrancheDisbursementWithUnsupportedCalculationTypeFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setChargeTimeType(ChargeTimeType.TRANCHE_DISBURSEMENT.getValue());
        request.setChargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST.getValue());

        assertThat(validate(request)).containsExactly(CHARGE_CALCULATION_TYPE_INVALID_FOR_TRANCHE);
    }

    @Test
    public void createClientChargeWithPercentageCalculationTypeFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setChargeAppliesTo(ChargeAppliesTo.CLIENT.getValue());
        request.setChargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        request.setChargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue());
        request.setChargePaymentMode(null);

        assertThat(validate(request)).containsExactly(CHARGE_CALCULATION_TYPE_INVALID);
    }

    @Test
    public void createValidSavingsMonthlyFeeHasNoViolations() {
        assertThat(validate(savingsMonthlyFeeRequest())).isEmpty();
    }

    @Test
    public void createSavingsMonthlyFeeWithoutFeeOnMonthDayFails() {
        final ChargeCreateRequest request = savingsMonthlyFeeRequest();
        request.setFeeOnMonthDay(null);

        assertThat(validate(request)).containsExactly(FEE_ON_MONTH_DAY_NOT_NULL);
    }

    @Test
    public void createSavingsMonthlyFeeWithoutFeeIntervalFails() {
        final ChargeCreateRequest request = savingsMonthlyFeeRequest();
        request.setFeeInterval(null);

        assertThat(validate(request)).containsExactly(FEE_INTERVAL_NOT_NULL);
    }

    @Test
    public void createSavingsMonthlyFeeWithFeeIntervalAboveTwelveFails() {
        final ChargeCreateRequest request = savingsMonthlyFeeRequest();
        request.setFeeInterval(13);

        assertThat(validate(request)).containsExactly(FEE_INTERVAL_MONTHLY_RANGE);
    }

    @Test
    public void createSavingsWeeklyFeeWithFeeOnMonthDayFails() {
        final ChargeCreateRequest request = savingsMonthlyFeeRequest();
        request.setChargeTimeType(ChargeTimeType.WEEKLY_FEE.getValue());

        assertThat(validate(request)).containsExactly(FEE_ON_MONTH_DAY_NOT_SUPPORTED);
    }

    @Test
    public void createWithUnparseableFeeOnMonthDayFails() {
        final ChargeCreateRequest request = savingsMonthlyFeeRequest();
        request.setFeeOnMonthDay("not-a-date");

        assertThat(validate(request)).containsExactlyInAnyOrder(FEE_ON_MONTH_DAY_INVALID, FEE_ON_MONTH_DAY_NOT_NULL);
    }

    @Test
    public void createWithFeeFrequencyButNoFeeIntervalFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setFeeFrequency(2);

        assertThat(validate(request)).containsExactly(FEE_INTERVAL_NOT_NULL);
    }

    @Test
    public void createWithSemiMonthlyFeeFrequencyFails() {
        // SEMI_MONTHLY has no period length, so overdue fee schedules cannot step by it
        final ChargeCreateRequest request = loanChargeRequest();
        request.setFeeInterval(1);
        request.setFeeFrequency(PeriodFrequencyType.SEMI_MONTHLY.getValue());

        assertThat(validate(request)).containsExactly(FEE_FREQUENCY_RANGE);
    }

    @Test
    public void createFlatDownPaymentChargeHasNoViolations() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setChargeTimeType(ChargeTimeType.DOWN_PAYMENT.getValue());

        assertThat(validate(request)).isEmpty();
    }

    @Test
    public void createDownPaymentChargeWithPercentOfInterestFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setChargeTimeType(ChargeTimeType.DOWN_PAYMENT.getValue());
        request.setChargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST.getValue());

        assertThat(validate(request)).containsExactly(CHARGE_CALCULATION_TYPE_INVALID_FOR_DOWN_PAYMENT);
    }

    @Test
    public void createDownPaymentPenaltyFails() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setChargeTimeType(ChargeTimeType.DOWN_PAYMENT.getValue());
        request.setPenalty(true);

        assertThat(validate(request)).containsExactly(PENALTY_NOT_SUPPORTED_FOR_DOWN_PAYMENT);
    }

    @Test
    public void createSavingsDownPaymentChargeFails() {
        final ChargeCreateRequest request = savingsMonthlyFeeRequest();
        request.setChargeTimeType(ChargeTimeType.DOWN_PAYMENT.getValue());
        request.setFeeOnMonthDay(null);
        request.setMonthDayFormat(null);
        request.setFeeInterval(null);

        assertThat(validate(request)).containsExactly(CHARGE_TIME_TYPE_INVALID);
    }

    @Test
    public void createWorkingCapitalDisbursementChargeHasNoViolations() {
        assertThat(validate(workingCapitalChargeRequest())).isEmpty();
    }

    @Test
    public void createWorkingCapitalChargeWithoutPaymentModeHasNoViolations() {
        final ChargeCreateRequest request = workingCapitalChargeRequest();
        request.setChargePaymentMode(null);

        assertThat(validate(request)).isEmpty();
    }

    @Test
    public void createWorkingCapitalChargeWithAccountTransferPaymentModeFails() {
        final ChargeCreateRequest request = workingCapitalChargeRequest();
        request.setChargePaymentMode(ChargePaymentMode.ACCOUNT_TRANSFER.getValue());

        assertThat(validate(request)).containsExactly(CHARGE_PAYMENT_MODE_INVALID);
    }

    @Test
    public void createWorkingCapitalChargeWithPercentOfInterestFails() {
        final ChargeCreateRequest request = workingCapitalChargeRequest();
        request.setChargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST.getValue());

        assertThat(validate(request)).containsExactly(CHARGE_CALCULATION_TYPE_INVALID);
    }

    @Test
    public void createWorkingCapitalChargeWithInstalmentFeeFails() {
        final ChargeCreateRequest request = workingCapitalChargeRequest();
        request.setChargeTimeType(ChargeTimeType.INSTALMENT_FEE.getValue());

        // no calculation type is valid for a charge time type the working capital loan does not support
        assertThat(validate(request)).containsExactlyInAnyOrder(CHARGE_TIME_TYPE_INVALID, CHARGE_CALCULATION_TYPE_INVALID);
    }

    @Test
    public void createWithdrawalFeeWithFreeWithdrawalsHasNoViolations() {
        final ChargeCreateRequest request = withdrawalFeeRequest();
        request.setEnableFreeWithdrawalCharge(true);
        request.setFreeWithdrawalFrequency(3);
        request.setRestartCountFrequency(1);
        request.setCountFrequencyType(PeriodFrequencyType.MONTHS.getValue());

        assertThat(validate(request)).isEmpty();
    }

    @Test
    public void createWithFreeWithdrawalsEnabledButNoFrequenciesFails() {
        final ChargeCreateRequest request = withdrawalFeeRequest();
        request.setEnableFreeWithdrawalCharge(true);

        assertThat(validate(request)).containsExactlyInAnyOrder(FREE_WITHDRAWAL_FREQUENCY_NOT_NULL, RESTART_COUNT_FREQUENCY_NOT_NULL,
                COUNT_FREQUENCY_TYPE_NOT_NULL);
    }

    @Test
    public void createWithFreeWithdrawalsDisabledNeedsNoFrequencies() {
        final ChargeCreateRequest request = withdrawalFeeRequest();
        request.setEnableFreeWithdrawalCharge(false);

        assertThat(validate(request)).isEmpty();
    }

    // ---------------------------------------------------------------- update

    @Test
    public void updateWithoutIdIsSkipped() {
        // the resource-level @Valid pass runs before the path id is bound
        final ChargeUpdateRequest request = new ChargeUpdateRequest();
        request.setChargeTimeType(99);

        assertThat(validate(request)).isEmpty();
    }

    @Test
    public void updateOfUnknownChargeIsSkipped() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        final ChargeUpdateRequest request = new ChargeUpdateRequest();
        request.setId(1L);
        request.setChargeTimeType(ChargeTimeType.MONTHLY_FEE.getValue());

        assertThat(validate(request)).isEmpty();
    }

    @Test
    public void updateBlankNameFails() {
        givenCharge(loanCharge());
        final ChargeUpdateRequest request = updateRequest();
        request.setName("  ");

        assertThat(validate(request)).containsExactly(NAME_NOT_BLANK);
    }

    @Test
    public void updateLoanChargeToSavingsOnlyChargeTimeTypeFails() {
        givenCharge(loanCharge());
        final ChargeUpdateRequest request = updateRequest();
        request.setChargeTimeType(ChargeTimeType.MONTHLY_FEE.getValue());

        assertThat(validate(request)).containsExactly(CHARGE_TIME_TYPE_NOT_ALLOWED_FOR_LOAN);
    }

    @Test
    public void updateSavingsActivationFeeToPercentageCalculationFails() {
        givenCharge(savingsCharge(ChargeTimeType.SAVINGS_ACTIVATION, ChargeCalculationType.FLAT));
        final ChargeUpdateRequest request = updateRequest();
        request.setChargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue());

        assertThat(validate(request)).containsExactly(CHARGE_CALCULATION_TYPE_PERCENTAGE_ONLY);
    }

    @Test
    public void updateWithdrawalFeeToPercentageCalculationIsAllowed() {
        givenCharge(savingsCharge(ChargeTimeType.WITHDRAWAL_FEE, ChargeCalculationType.FLAT));
        final ChargeUpdateRequest request = updateRequest();
        request.setChargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue());

        assertThat(validate(request)).isEmpty();
    }

    @Test
    public void updateTaxGroupOfChargeThatAlreadyHasOneFails() {
        final Charge charge = loanCharge();
        charge.setTaxGroup(taxGroup(7L));
        givenCharge(charge);
        final ChargeUpdateRequest request = updateRequest();
        request.setTaxGroupId(8L);

        assertThat(validate(request)).containsExactly(TAX_GROUP_MODIFICATION_NOT_SUPPORTED);
    }

    @Test
    public void updateAmountOnlyHasNoViolations() {
        givenCharge(loanCharge());
        final ChargeUpdateRequest request = updateRequest();
        request.setAmount(25.0);

        assertThat(validate(request)).isEmpty();
    }

    @Test
    public void updateLoanChargeToDownPaymentPenaltyFails() {
        givenCharge(loanCharge());
        final ChargeUpdateRequest request = updateRequest();
        request.setChargeTimeType(ChargeTimeType.DOWN_PAYMENT.getValue());
        request.setPenalty(true);

        assertThat(validate(request)).containsExactly(PENALTY_NOT_SUPPORTED_FOR_DOWN_PAYMENT);
    }

    @Test
    public void updateWorkingCapitalChargeToAccountTransferPaymentModeFails() {
        givenCharge(workingCapitalCharge());
        final ChargeUpdateRequest request = updateRequest();
        request.setChargePaymentMode(ChargePaymentMode.ACCOUNT_TRANSFER.getValue());

        assertThat(validate(request)).containsExactly(CHARGE_PAYMENT_MODE_INVALID);
    }

    @Test
    public void updateWorkingCapitalChargeToPercentOfInterestFails() {
        givenCharge(workingCapitalCharge());
        final ChargeUpdateRequest request = updateRequest();
        request.setChargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST.getValue());

        assertThat(validate(request)).containsExactly(CHARGE_CALCULATION_TYPE_INVALID);
    }

    // ---------------------------------------------------------------- helpers

    private Set<String> validate(final Object request) {
        return validator.validate(request).stream().map(ConstraintViolation::getMessageTemplate).collect(Collectors.toSet());
    }

    private ChargeCreateRequest loanChargeRequest() {
        final ChargeCreateRequest request = new ChargeCreateRequest();
        request.setChargeAppliesTo(ChargeAppliesTo.LOAN.getValue());
        request.setName("Processing Fee");
        request.setCurrencyCode("USD");
        request.setChargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        request.setChargeCalculationType(ChargeCalculationType.FLAT.getValue());
        request.setChargePaymentMode(ChargePaymentMode.REGULAR.getValue());
        request.setAmount(10.0);
        request.setActive(true);
        request.setLocale("en");
        return request;
    }

    private ChargeCreateRequest savingsMonthlyFeeRequest() {
        final ChargeCreateRequest request = new ChargeCreateRequest();
        request.setChargeAppliesTo(ChargeAppliesTo.SAVINGS.getValue());
        request.setName("Monthly Fee");
        request.setCurrencyCode("USD");
        request.setChargeTimeType(ChargeTimeType.MONTHLY_FEE.getValue());
        request.setChargeCalculationType(ChargeCalculationType.FLAT.getValue());
        request.setAmount(10.0);
        request.setActive(true);
        request.setFeeOnMonthDay(FEE_ON_MONTH_DAY);
        request.setMonthDayFormat(MONTH_DAY_FORMAT);
        request.setFeeInterval(1);
        request.setLocale("en");
        return request;
    }

    private ChargeCreateRequest workingCapitalChargeRequest() {
        final ChargeCreateRequest request = loanChargeRequest();
        request.setChargeAppliesTo(ChargeAppliesTo.WORKING_CAPITAL_LOAN.getValue());
        request.setChargeTimeType(ChargeTimeType.DISBURSEMENT.getValue());
        return request;
    }

    private ChargeCreateRequest withdrawalFeeRequest() {
        final ChargeCreateRequest request = savingsMonthlyFeeRequest();
        request.setChargeTimeType(ChargeTimeType.WITHDRAWAL_FEE.getValue());
        request.setFeeOnMonthDay(null);
        request.setMonthDayFormat(null);
        request.setFeeInterval(null);
        return request;
    }

    private ChargeUpdateRequest updateRequest() {
        final ChargeUpdateRequest request = new ChargeUpdateRequest();
        request.setId(1L);
        request.setLocale("en");
        return request;
    }

    private void givenCharge(final Charge charge) {
        when(repository.findById(1L)).thenReturn(Optional.of(charge));
    }

    private Charge loanCharge() {
        return charge(ChargeAppliesTo.LOAN, ChargeTimeType.SPECIFIED_DUE_DATE, ChargeCalculationType.FLAT, ChargePaymentMode.REGULAR, null);
    }

    private Charge workingCapitalCharge() {
        return charge(ChargeAppliesTo.WORKING_CAPITAL_LOAN, ChargeTimeType.DISBURSEMENT, ChargeCalculationType.FLAT,
                ChargePaymentMode.REGULAR, null);
    }

    private Charge savingsCharge(final ChargeTimeType chargeTimeType, final ChargeCalculationType chargeCalculationType) {
        final MonthDay feeOnMonthDay = chargeTimeType.isMonthlyFee() || chargeTimeType.isAnnualFee() ? MonthDay.of(3, 4) : null;
        return charge(ChargeAppliesTo.SAVINGS, chargeTimeType, chargeCalculationType, null, feeOnMonthDay);
    }

    private Charge charge(final ChargeAppliesTo chargeAppliesTo, final ChargeTimeType chargeTimeType,
            final ChargeCalculationType chargeCalculationType, final ChargePaymentMode chargePaymentMode, final MonthDay feeOnMonthDay) {
        final Charge charge = new Charge("Charge", BigDecimal.TEN, "USD", chargeAppliesTo, chargeTimeType, chargeCalculationType, false,
                true, chargePaymentMode, feeOnMonthDay, null, null, null, null, false, null, null, null, null, null, false, null);
        ReflectionTestUtils.setField(charge, "id", 1L);
        return charge;
    }

    private TaxGroup taxGroup(final Long id) {
        final TaxGroup taxGroup = TaxGroup.createTaxGroup("Tax Group", new HashSet<>());
        ReflectionTestUtils.setField(taxGroup, "id", id);
        return taxGroup;
    }
}
