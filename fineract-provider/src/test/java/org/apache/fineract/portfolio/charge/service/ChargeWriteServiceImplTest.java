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
package org.apache.fineract.portfolio.charge.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.portfolio.charge.data.ChargeCreateRequest;
import org.apache.fineract.portfolio.charge.data.ChargeUpdateRequest;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationIncomeType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationPeriodType;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeUpdatedException;
import org.apache.fineract.portfolio.charge.exception.ChargeDueAtDisbursementCannotBePenaltyException;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChargeWriteServiceImplTest {

    private static final Long CHARGE_ID = 1L;
    private static final String LOCALE = "en";

    @Mock
    private Validator validator;
    @Mock
    private ChargeRepository chargeRepository;
    @Mock
    private LoanProductRepository loanProductRepository;
    @Mock
    private FineractEntityAccessUtil fineractEntityAccessUtil;
    @Mock
    private GLAccountRepositoryWrapper glAccountRepository;
    @Mock
    private TaxGroupRepositoryWrapper taxGroupRepository;
    @Mock
    private PaymentTypeRepository paymentTypeRepository;

    @InjectMocks
    private ChargeWriteServiceImpl underTest;

    @BeforeEach
    void setUp() {
        // the bean validation constraints are covered by ChargeValidationTest
        lenient().doReturn(Set.of()).when(validator).validate(any());
    }

    // ------------------------------------------------------------- create

    @Test
    void createLinksTheIncomeAccount() {
        final GLAccount incomeAccount = mock(GLAccount.class);
        when(glAccountRepository.findOneWithNotFoundDetection(7L)).thenReturn(incomeAccount);

        final Charge charge = create(withIncomeAccount(clientFee(), 7L));

        assertSame(incomeAccount, charge.getAccount());
    }

    @Test
    void createKeepsTheFreeWithdrawalSettings() {
        final Charge charge = create(withFreeWithdrawals(createRequest(ChargeAppliesTo.SAVINGS, ChargeTimeType.WITHDRAWAL_FEE, false), 3, 1,
                PeriodFrequencyType.MONTHS));

        assertTrue(charge.isEnableFreeWithdrawal());
        assertEquals(3, charge.getFreeWithdrawalFrequency());
        assertEquals(1, charge.getRestartFrequency());
        assertEquals(PeriodFrequencyType.MONTHS.getValue(), charge.getRestartFrequencyEnum());
    }

    @Test
    void createWithoutAmortizationFields_defaultsToNone() {
        final Charge charge = create(loanFee(ChargeTimeType.DISBURSEMENT));

        assertEquals(ChargeAmortizationPeriodType.NONE, charge.getAmortizationPeriodType());
        assertFalse(charge.isAmortized());
        assertNull(charge.getAmortizationDays());
        assertNull(charge.getAmortizationCalculationType());
        assertNull(charge.getAmortizationIncomeType());
    }

    @Test
    void createFullLoanTerm_defaultsToFlatFee() {
        final Charge charge = create(amortized(loanFee(ChargeTimeType.DISBURSEMENT), "FULL_LOAN_TERM", null, null, null));

        assertEquals(ChargeAmortizationPeriodType.FULL_LOAN_TERM, charge.getAmortizationPeriodType());
        assertNull(charge.getAmortizationDays());
        assertEquals(ChargeAmortizationCalculationType.FLAT, charge.getAmortizationCalculationType());
        assertEquals(ChargeAmortizationIncomeType.FEE, charge.getAmortizationIncomeType());
    }

    @Test
    void createNumberOfDays_keepsTheGivenConfiguration() {
        final Charge charge = create(amortized(loanFee(ChargeTimeType.SPECIFIED_DUE_DATE), "NUMBER_OF_DAYS", 30, "FLAT", "INTEREST"));

        assertEquals(ChargeAmortizationPeriodType.NUMBER_OF_DAYS, charge.getAmortizationPeriodType());
        assertEquals(30, charge.getAmortizationDays());
        assertEquals(ChargeAmortizationCalculationType.FLAT, charge.getAmortizationCalculationType());
        assertEquals(ChargeAmortizationIncomeType.INTEREST, charge.getAmortizationIncomeType());
    }

    @Test
    void createFullLoanTermEffectiveRate_isAccepted() {
        final Charge charge = create(
                amortized(loanFee(ChargeTimeType.TRANCHE_DISBURSEMENT), "FULL_LOAN_TERM", null, "EFFECTIVE_RATE", null));

        assertEquals(ChargeAmortizationCalculationType.EFFECTIVE_RATE, charge.getAmortizationCalculationType());
    }

    @Test
    void createEnumValues_areReadIgnoringCase() {
        final Charge charge = create(amortized(loanFee(ChargeTimeType.DISBURSEMENT), "full_loan_term", null, null, "interest"));

        assertEquals(ChargeAmortizationPeriodType.FULL_LOAN_TERM, charge.getAmortizationPeriodType());
        assertEquals(ChargeAmortizationIncomeType.INTEREST, charge.getAmortizationIncomeType());
    }

    @Test
    void createNumberOfDaysWithoutDays_isRejected() {
        assertCreateRejected(List.of("validation.msg.charge.amortizationDays.cannot.be.blank"),
                amortized(loanFee(ChargeTimeType.DISBURSEMENT), "NUMBER_OF_DAYS", null, null, null));
    }

    @Test
    void createNumberOfDaysWithZeroDays_isRejected() {
        assertCreateRejected(List.of("validation.msg.charge.amortizationDays.not.greater.than.zero"),
                amortized(loanFee(ChargeTimeType.DISBURSEMENT), "NUMBER_OF_DAYS", 0, null, null));
    }

    @Test
    void createWithUnknownPeriodType_isRejected() {
        assertCreateRejected(List.of("validation.msg.charge.amortizationPeriodType.is.not.one.of.expected.enumerations"),
                amortized(loanFee(ChargeTimeType.DISBURSEMENT), "WEEKLY", null, null, null));
    }

    @Test
    void createAmortizedPenalty_isRejected() {
        assertCreateRejected(List.of("validation.msg.charge.amortizationPeriodType.not.supported.for.penalty"),
                amortized(loanCharge(ChargeTimeType.SPECIFIED_DUE_DATE, true), "FULL_LOAN_TERM", null, null, null));
    }

    @ParameterizedTest
    @EnumSource(value = ChargeTimeType.class, names = { "INSTALMENT_FEE", "DOWN_PAYMENT" })
    void createAmortizedFeeWithNotAllowedChargeTime_isRejected(final ChargeTimeType chargeTimeType) {
        assertCreateRejected(List.of("validation.msg.charge.amortizationPeriodType.not.supported.for.charge.time.type"),
                amortized(loanFee(chargeTimeType), "FULL_LOAN_TERM", null, null, null));
    }

    @ParameterizedTest
    @EnumSource(value = ChargeTimeType.class, names = { "DISBURSEMENT", "TRANCHE_DISBURSEMENT", "SPECIFIED_DUE_DATE" })
    void createAmortizedFeeWithAllowedChargeTime_isAccepted(final ChargeTimeType chargeTimeType) {
        final Charge charge = create(amortized(loanFee(chargeTimeType), "FULL_LOAN_TERM", null, null, null));

        assertEquals(ChargeAmortizationPeriodType.FULL_LOAN_TERM, charge.getAmortizationPeriodType());
    }

    @Test
    void createAmortizedOverduePenalty_isRejectedForPenaltyAndChargeTime() {
        assertCreateRejected(
                List.of("validation.msg.charge.amortizationPeriodType.not.supported.for.penalty",
                        "validation.msg.charge.amortizationPeriodType.not.supported.for.charge.time.type"),
                amortized(loanCharge(ChargeTimeType.OVERDUE_INSTALLMENT, true), "FULL_LOAN_TERM", null, null, null));
    }

    @Test
    void createAmortizedSavingsCharge_isRejected() {
        assertCreateRejected(List.of("validation.msg.charge.amortizationPeriodType.supported.only.for.loan.charge"), amortized(
                createRequest(ChargeAppliesTo.SAVINGS, ChargeTimeType.SPECIFIED_DUE_DATE, false), "FULL_LOAN_TERM", null, null, null));
    }

    @Test
    void createEffectiveRateWithNumberOfDays_isRejected() {
        assertCreateRejected(List.of("validation.msg.charge.amortizationCalculationType.effective.rate.supported.only.for.full.loan.term"),
                amortized(loanFee(ChargeTimeType.DISBURSEMENT), "NUMBER_OF_DAYS", 30, "EFFECTIVE_RATE", null));
    }

    @Test
    void createNoneWithCalculationAndIncomeType_isRejected() {
        assertCreateRejected(
                List.of("validation.msg.charge.amortizationCalculationType.supported.only.for.amortized.charge",
                        "validation.msg.charge.amortizationIncomeType.supported.only.for.amortized.charge"),
                amortized(loanFee(ChargeTimeType.DISBURSEMENT), "NONE", null, "FLAT", "FEE"));
    }

    @Test
    void createFullLoanTermWithDays_isRejected() {
        assertCreateRejected(List.of("validation.msg.charge.amortizationDays.supported.only.for.number.of.days.period.type"),
                amortized(loanFee(ChargeTimeType.DISBURSEMENT), "FULL_LOAN_TERM", 30, null, null));
    }

    // ------------------------------------------------------------- update: amortization

    @Test
    void updateNoneToFullLoanTerm_appliesTheDefaultsAndReportsThem() {
        final Charge charge = givenCharge(create(loanFee(ChargeTimeType.DISBURSEMENT)));

        final Map<String, Object> changes = update(updateRequest(r -> r.setAmortizationPeriodType("FULL_LOAN_TERM")));

        assertEquals(
                Map.of("amortizationPeriodType", "FULL_LOAN_TERM", "amortizationCalculationType", "FLAT", "amortizationIncomeType", "FEE"),
                changes);
        assertEquals(ChargeAmortizationCalculationType.FLAT, charge.getAmortizationCalculationType());
        assertEquals(ChargeAmortizationIncomeType.FEE, charge.getAmortizationIncomeType());
    }

    @Test
    void updateNumberOfDaysToNone_clearsTheConfiguration() {
        final Charge charge = givenCharge(numberOfDaysInterestFee());

        final Map<String, Object> changes = update(updateRequest(r -> r.setAmortizationPeriodType("NONE")));

        assertEquals("NONE", changes.get("amortizationPeriodType"));
        assertEquals(ChargeAmortizationPeriodType.NONE, charge.getAmortizationPeriodType());
        assertNull(charge.getAmortizationDays());
        assertNull(charge.getAmortizationCalculationType());
        assertNull(charge.getAmortizationIncomeType());
    }

    @Test
    void updateNumberOfDaysToFullLoanTerm_clearsTheDaysAndKeepsTheRest() {
        final Charge charge = givenCharge(numberOfDaysInterestFee());

        final Map<String, Object> changes = update(updateRequest(r -> r.setAmortizationPeriodType("FULL_LOAN_TERM")));

        assertEquals("FULL_LOAN_TERM", changes.get("amortizationPeriodType"));
        assertNull(charge.getAmortizationDays());
        assertEquals(ChargeAmortizationCalculationType.FLAT, charge.getAmortizationCalculationType());
        assertEquals(ChargeAmortizationIncomeType.INTEREST, charge.getAmortizationIncomeType());
    }

    @Test
    void updateDaysOnly_isReported() {
        final Charge charge = givenCharge(numberOfDaysInterestFee());

        final Map<String, Object> changes = update(updateRequest(r -> r.setAmortizationDays(60)));

        assertEquals(Map.of("amortizationDays", 60), changes);
        assertEquals(60, charge.getAmortizationDays());
    }

    @Test
    void updateDaysOnFullLoanTermCharge_isRejected() {
        givenCharge(create(amortized(loanFee(ChargeTimeType.DISBURSEMENT), "FULL_LOAN_TERM", null, null, null)));

        assertUpdateRejected(List.of("validation.msg.charge.amortizationDays.supported.only.for.number.of.days.period.type"),
                updateRequest(r -> r.setAmortizationDays(60)));
    }

    @Test
    void updateAmortizedFeeToPenalty_isRejected() {
        givenCharge(numberOfDaysInterestFee());

        assertUpdateRejected(List.of("validation.msg.charge.amortizationPeriodType.not.supported.for.penalty"),
                updateRequest(r -> r.setPenalty(true)));
    }

    @Test
    void updateAmortizedFeeToInstalmentFee_isRejected() {
        givenCharge(numberOfDaysInterestFee());

        assertUpdateRejected(List.of("validation.msg.charge.amortizationPeriodType.not.supported.for.charge.time.type"),
                updateRequest(r -> r.setChargeTimeType(ChargeTimeType.INSTALMENT_FEE.getValue())));
    }

    @Test
    void updateOtherFieldsOfAnAmortizedCharge_leavesTheConfigurationAlone() {
        final Charge charge = givenCharge(numberOfDaysInterestFee());

        final Map<String, Object> changes = update(updateRequest(r -> r.setName("renamed")));

        assertEquals(Map.of("name", "renamed"), changes);
        assertEquals(ChargeAmortizationPeriodType.NUMBER_OF_DAYS, charge.getAmortizationPeriodType());
        assertEquals(30, charge.getAmortizationDays());
    }

    // ------------------------------------------------------------- update: payment mode and penalty

    @Test
    void updateOnWorkingCapitalCharge_explicitRegularPaymentMode_isNoChange() {
        final Charge charge = givenCharge(workingCapitalDisbursementCharge(false));

        final Map<String, Object> changes = update(updateRequest(r -> r.setChargePaymentMode(ChargePaymentMode.REGULAR.getValue())));

        assertNull(changes, "an update that changes nothing reports no changes");
        assertEquals(ChargePaymentMode.REGULAR.getValue(), charge.getChargePaymentMode());
        verify(chargeRepository, never()).save(any());
    }

    @Test
    void updateOnWorkingCapitalDisbursementPenalty_amountOnly_succeeds() {
        givenCharge(workingCapitalDisbursementCharge(true));

        final Map<String, Object> changes = update(updateRequest(r -> r.setAmount(30.0)));

        assertEquals(0, new BigDecimal("30").compareTo((BigDecimal) changes.get("amount")));
    }

    @Test
    void updateOnClientPenaltyCharge_toDisbursementTimeType_isStillRejected() {
        givenCharge(charge(ChargeAppliesTo.CLIENT, ChargeTimeType.SPECIFIED_DUE_DATE, true, null));

        assertThrows(ChargeDueAtDisbursementCannotBePenaltyException.class,
                () -> update(updateRequest(r -> r.setChargeTimeType(ChargeTimeType.DISBURSEMENT.getValue()))));
    }

    @Test
    void updateOnLoanCharge_paymentModeChange_isPersistedAndReported() {
        final Charge charge = givenCharge(charge(ChargeAppliesTo.LOAN, ChargeTimeType.DISBURSEMENT, false, ChargePaymentMode.REGULAR));

        final Map<String, Object> changes = update(
                updateRequest(r -> r.setChargePaymentMode(ChargePaymentMode.ACCOUNT_TRANSFER.getValue())));

        assertEquals(ChargePaymentMode.ACCOUNT_TRANSFER.getValue(), changes.get("chargePaymentMode"));
        assertEquals(ChargePaymentMode.ACCOUNT_TRANSFER.getValue(), charge.getChargePaymentMode());
    }

    // ------------------------------------------------------------- update: down payment

    @Test
    void changingChargeToDownPaymentIsRejectedWhenAttachedToProductWithoutDownPaymentSupport() {
        givenCharge(charge(ChargeAppliesTo.LOAN, ChargeTimeType.SPECIFIED_DUE_DATE, false, ChargePaymentMode.REGULAR));
        final List<LoanProduct> products = List.of(product(true), product(false));
        when(loanProductRepository.retrieveLoanProductsByChargeId(CHARGE_ID)).thenReturn(products);

        assertThrows(ChargeCannotBeUpdatedException.class, () -> update(toDownPayment()));
        verify(chargeRepository, never()).save(any());
    }

    @Test
    void changingChargeToDownPaymentIsAllowedWhenAllProductsSupportDownPayment() {
        final Charge charge = givenCharge(
                charge(ChargeAppliesTo.LOAN, ChargeTimeType.SPECIFIED_DUE_DATE, false, ChargePaymentMode.REGULAR));
        final List<LoanProduct> products = List.of(product(true));
        when(loanProductRepository.retrieveLoanProductsByChargeId(CHARGE_ID)).thenReturn(products);

        assertDoesNotThrow(() -> update(toDownPayment()));
        verify(chargeRepository).save(charge);
    }

    @Test
    void productsAreNotCheckedWhenChargeTimeTypeIsNotChangedToDownPayment() {
        givenCharge(charge(ChargeAppliesTo.LOAN, ChargeTimeType.SPECIFIED_DUE_DATE, false, ChargePaymentMode.REGULAR));

        assertDoesNotThrow(() -> update(updateRequest(r -> r.setChargeTimeType(ChargeTimeType.DISBURSEMENT.getValue()))));
        verify(loanProductRepository, never()).retrieveLoanProductsByChargeId(any());
    }

    // ------------------------------------------------------------- helpers

    private Charge create(final ChargeCreateRequest request) {
        underTest.create(request);
        final ArgumentCaptor<Charge> saved = ArgumentCaptor.forClass(Charge.class);
        verify(chargeRepository).saveAndFlush(saved.capture());
        return saved.getValue();
    }

    private Map<String, Object> update(final ChargeUpdateRequest request) {
        request.setId(CHARGE_ID);
        return underTest.update(request).getChanges();
    }

    private Charge givenCharge(final Charge charge) {
        ReflectionTestUtils.setField(charge, "id", CHARGE_ID);
        when(chargeRepository.findById(CHARGE_ID)).thenReturn(Optional.of(charge));
        return charge;
    }

    private Charge numberOfDaysInterestFee() {
        return create(amortized(loanFee(ChargeTimeType.SPECIFIED_DUE_DATE), "NUMBER_OF_DAYS", 30, null, "INTEREST"));
    }

    private void assertCreateRejected(final List<String> expectedCodes, final ChargeCreateRequest request) {
        assertRejected(expectedCodes, assertThrows(PlatformApiDataValidationException.class, () -> underTest.create(request)));
        verify(chargeRepository, never()).saveAndFlush(any());
    }

    private void assertUpdateRejected(final List<String> expectedCodes, final ChargeUpdateRequest request) {
        assertRejected(expectedCodes, assertThrows(PlatformApiDataValidationException.class, () -> update(request)));
    }

    private static void assertRejected(final List<String> expectedCodes, final PlatformApiDataValidationException rejection) {
        assertEquals(expectedCodes, rejection.getErrors().stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList());
    }

    private static ChargeUpdateRequest toDownPayment() {
        return updateRequest(r -> r.setChargeTimeType(ChargeTimeType.DOWN_PAYMENT.getValue()));
    }

    private static ChargeCreateRequest loanFee(final ChargeTimeType chargeTimeType) {
        return loanCharge(chargeTimeType, false);
    }

    private static ChargeCreateRequest loanCharge(final ChargeTimeType chargeTimeType, final boolean penalty) {
        final ChargeCreateRequest request = createRequest(ChargeAppliesTo.LOAN, chargeTimeType, penalty);
        request.setChargePaymentMode(ChargePaymentMode.REGULAR.getValue());
        return request;
    }

    private static ChargeCreateRequest clientFee() {
        return createRequest(ChargeAppliesTo.CLIENT, ChargeTimeType.SPECIFIED_DUE_DATE, false);
    }

    private static ChargeCreateRequest createRequest(final ChargeAppliesTo chargeAppliesTo, final ChargeTimeType chargeTimeType,
            final boolean penalty) {
        final ChargeCreateRequest request = new ChargeCreateRequest();
        request.setChargeAppliesTo(chargeAppliesTo.getValue());
        request.setChargeTimeType(chargeTimeType.getValue());
        request.setChargeCalculationType(ChargeCalculationType.FLAT.getValue());
        request.setName("charge");
        request.setAmount(20.0);
        request.setCurrencyCode("USD");
        request.setPenalty(penalty);
        request.setActive(true);
        request.setLocale(LOCALE);
        return request;
    }

    private static ChargeCreateRequest amortized(final ChargeCreateRequest request, final String periodType, final Integer days,
            final String calculationType, final String incomeType) {
        request.setAmortizationPeriodType(periodType);
        request.setAmortizationDays(days);
        request.setAmortizationCalculationType(calculationType);
        request.setAmortizationIncomeType(incomeType);
        return request;
    }

    private static ChargeCreateRequest withIncomeAccount(final ChargeCreateRequest request, final Long incomeAccountId) {
        request.setIncomeAccountId(incomeAccountId);
        return request;
    }

    private static ChargeCreateRequest withFreeWithdrawals(final ChargeCreateRequest request, final Integer frequency,
            final Integer restartFrequency, final PeriodFrequencyType restartFrequencyType) {
        request.setEnableFreeWithdrawalCharge(true);
        request.setFreeWithdrawalFrequency(frequency);
        request.setRestartCountFrequency(restartFrequency);
        request.setCountFrequencyType(restartFrequencyType.getValue());
        return request;
    }

    private static ChargeUpdateRequest updateRequest(final Consumer<ChargeUpdateRequest> changes) {
        final ChargeUpdateRequest request = new ChargeUpdateRequest();
        request.setLocale(LOCALE);
        changes.accept(request);
        return request;
    }

    private static Charge workingCapitalDisbursementCharge(final boolean penalty) {
        return charge(ChargeAppliesTo.WORKING_CAPITAL_LOAN, ChargeTimeType.DISBURSEMENT, penalty, ChargePaymentMode.REGULAR);
    }

    private static Charge charge(final ChargeAppliesTo chargeAppliesTo, final ChargeTimeType chargeTimeType, final boolean penalty,
            final ChargePaymentMode paymentMode) {
        return new Charge("charge", new BigDecimal("20"), "USD", chargeAppliesTo, chargeTimeType, ChargeCalculationType.FLAT, penalty, true,
                paymentMode, null, null, null, null, null, false, null, null, null, null, null, false, null);
    }

    private static LoanProduct product(final boolean supportsDownPaymentCharges) {
        final LoanProduct product = mock(LoanProduct.class);
        when(product.supportsDownPaymentCharges()).thenReturn(supportsDownPaymentCharges);
        return product;
    }
}
