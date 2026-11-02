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
package org.apache.fineract.portfolio.savings.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Undoing a payment on a recurring (weekly/monthly/annual) savings fee. A partial payment leaves the cycle open - pay()
 * only advances the due date once the outstanding reaches zero - so undoing it must give the amount back to that cycle
 * without resetting the outstanding to the full fee or rolling the due date back a cycle. Undoing the payment that
 * closed a cycle reopens that cycle with the undone amount outstanding, and a payment of a cycle that later payments
 * have moved past is an earlier-cycle payment the account refuses to undo on its own. The account decides what came
 * later by transaction id.
 */
class SavingsAccountChargeUndoPaymentTest {

    private static final BigDecimal FEE = new BigDecimal("45.00");
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 11, 20);

    private final MonetaryCurrency currency = new MonetaryCurrency("USD", 2, 0);

    private MockedStatic<MoneyHelper> moneyHelperStatic;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        moneyHelperStatic = Mockito.mockStatic(MoneyHelper.class, Mockito.CALLS_REAL_METHODS);
        moneyHelperStatic.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
        moneyHelperStatic.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterEach
    void tearDown() {
        moneyHelperStatic.close();
        ThreadLocalContextUtil.reset();
    }

    @Test
    void undoingOnePartialPaymentKeepsTheOtherAndTheDueDate() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("1.70"));
        charge.pay(currency, money("1.70"));

        charge.undoPayment(currency, money("1.70"));

        assertThat(amountPaid(charge)).isEqualByComparingTo("1.70");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo("43.30");
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE);
        assertThat(charge.isPaid()).isFalse();
    }

    @Test
    void undoingTheOnlyPartialPaymentRestoresTheFullFeeOnTheSameCycle() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("10.00"));

        charge.undoPayment(currency, money("10.00"));

        assertThat(amountPaid(charge)).isEqualByComparingTo("0");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo(FEE);
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE);
    }

    @Test
    void undoingAPartialPaymentAfterAnEarlierWaivedCycleStaysOnTheOpenCycle() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.waive(currency);
        charge.pay(currency, money("5.00"));

        charge.undoPayment(currency, money("5.00"));

        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo(FEE);
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE.plusWeeks(1));
    }

    @Test
    void undoingTheFullCyclePaymentStillRollsTheDueDateBack() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("45.00"));
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE.plusWeeks(1));

        charge.undoPayment(currency, money("45.00"));

        assertThat(amountPaid(charge)).isEqualByComparingTo("0");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo(FEE);
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE);
        assertThat(charge.isPaid()).isFalse();
    }

    @Test
    void undoingTheCycleClosingPaymentReopensThatCycleWithOnlyThatAmountOutstanding() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("40.00"));
        charge.pay(currency, money("5.00"));
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE.plusWeeks(1));

        charge.undoPayment(currency, money("5.00"), money("0"));

        assertThat(amountPaid(charge)).isEqualByComparingTo("40.00");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo("5.00");
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE);
    }

    @Test
    void aPaymentOfAClosedCycleFollowedByPaymentsOnTheOpenCycleIsAnEarlierCyclePayment() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("40.00"));
        charge.pay(currency, money("5.00"));
        charge.pay(currency, money("10.00"));

        assertThat(charge.isPaymentOfEarlierCycle(currency, money("10.00"))).isTrue();
        assertThat(charge.isPaymentOfEarlierCycle(currency, money("15.00"))).isTrue();
        assertThat(charge.isPaymentOfEarlierCycle(currency, money("0"))).isFalse();
    }

    @Test
    void aClosingPaymentFollowedByAWholeLaterCycleIsAnEarlierCyclePayment() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("45.00"));
        charge.pay(currency, money("45.00"));

        assertThat(charge.isPaymentOfEarlierCycle(currency, money("45.00"))).isTrue();
        assertThat(charge.isPaymentOfEarlierCycle(currency, money("0"))).isFalse();
    }

    @Test
    void undoingTheLatestPaymentOfTheOpenCycleLeavesTheClosedCycleAlone() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("40.00"));
        charge.pay(currency, money("5.00"));
        charge.pay(currency, money("10.00"));

        charge.undoPayment(currency, money("10.00"), money("0"));

        assertThat(amountPaid(charge)).isEqualByComparingTo("45.00");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo(FEE);
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE.plusWeeks(1));
    }

    @Test
    void undoingAnEarlierPartialPaymentOfTheOpenCycleKeepsTheLaterOne() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("3.00"));
        charge.pay(currency, money("7.00"));

        assertThat(charge.isPaymentOfEarlierCycle(currency, money("7.00"))).isFalse();
        charge.undoPayment(currency, money("3.00"), money("7.00"));

        assertThat(amountPaid(charge)).isEqualByComparingTo("7.00");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo("38.00");
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE);
    }

    @Test
    void undoingAWaiverThatClosedAPartlyPaidCycleReopensItWithTheWaivedAmountOutstanding() {
        final SavingsAccountCharge charge = weeklyFee();
        charge.pay(currency, money("10.00"));
        final Money waived = charge.waive(currency);
        assertThat(waived.getAmount()).isEqualByComparingTo("35.00");

        charge.undoWaiver(currency, waived);

        assertThat(amountPaid(charge)).isEqualByComparingTo("10.00");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo("35.00");
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE);
        assertThat(charge.isWaived()).isFalse();
    }

    @Test
    void accountRefusesToUndoAPaymentOfAnEarlierCycleAndLeavesTheChargeAlone() {
        final SavingsAccountCharge charge = weeklyFee();
        final SavingsAccount account = account();
        pay(account, charge, 1L, "40.00");
        final SavingsAccountTransaction closingPayment = pay(account, charge, 2L, "5.00");
        pay(account, charge, 3L, "10.00");

        assertThatThrownBy(() -> account.undoTransaction(closingPayment)).isInstanceOf(PlatformApiDataValidationException.class)
                .satisfies(e -> assertThat(((PlatformApiDataValidationException) e).getErrors().get(0).getUserMessageGlobalisationCode())
                        .endsWith("transaction.undo.recurring.charge.has.later.transactions"));

        assertThat(amountPaid(charge)).isEqualByComparingTo("55.00");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo("35.00");
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE.plusWeeks(1));
    }

    @Test
    void accountUndoesTheLatestPaymentsOneByOne() {
        final SavingsAccountCharge charge = weeklyFee();
        final SavingsAccount account = account();
        pay(account, charge, 1L, "40.00");
        final SavingsAccountTransaction closingPayment = pay(account, charge, 2L, "5.00");
        final SavingsAccountTransaction openCyclePayment = pay(account, charge, 3L, "10.00");

        account.undoTransaction(openCyclePayment);

        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo(FEE);
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE.plusWeeks(1));

        account.undoTransaction(closingPayment);

        assertThat(amountPaid(charge)).isEqualByComparingTo("40.00");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo("5.00");
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE);
    }

    @Test
    void accountCountsLaterPaymentsOfTheOpenCycleWhenUndoingAnEarlierOne() {
        final SavingsAccountCharge charge = weeklyFee();
        final SavingsAccount account = account();
        final SavingsAccountTransaction earlierPayment = pay(account, charge, 1L, "3.00");
        pay(account, charge, 2L, "7.00");

        account.undoTransaction(earlierPayment);

        assertThat(amountPaid(charge)).isEqualByComparingTo("7.00");
        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo("38.00");
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE);
    }

    @Test
    void accountRefusesToUndoAWaiverFollowedByAPayment() {
        final SavingsAccountCharge charge = weeklyFee();
        final SavingsAccount account = account();
        final SavingsAccountTransaction waiver = transaction(account, charge, 1L, charge.waive(currency), true);
        pay(account, charge, 2L, "5.00");

        assertThatThrownBy(() -> account.undoTransaction(waiver)).isInstanceOf(PlatformApiDataValidationException.class);

        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo("40.00");
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE.plusWeeks(1));
    }

    @Test
    void accountOrdersPaymentsByIdNotByTheirPositionInTheTransactions() {
        final SavingsAccountCharge charge = weeklyFee();
        final SavingsAccount account = account();
        final SavingsAccountTransaction firstCycle = pay(null, charge, 1L, "45.00");
        final SavingsAccountTransaction secondCycleBackdated = pay(account, charge, 2L, "45.00");
        account.transactions.add(firstCycle);

        assertThatThrownBy(() -> account.undoTransaction(firstCycle)).isInstanceOf(PlatformApiDataValidationException.class);

        account.undoTransaction(secondCycleBackdated);

        assertThat(charge.getAmountOutstanding(currency).getAmount()).isEqualByComparingTo(FEE);
        assertThat(charge.getDueDate()).isEqualTo(DUE_DATE.plusWeeks(1));
    }

    private SavingsAccount account() {
        final SavingsAccount account = new SavingsAccount();
        account.currency = currency;
        return account;
    }

    private SavingsAccountTransaction pay(final SavingsAccount account, final SavingsAccountCharge charge, final Long id,
            final String amount) {
        final Money paid = money(amount);
        charge.pay(currency, paid);
        return transaction(account, charge, id, paid, false);
    }

    // Added to the account's transactions unless account is null; reverse() marks it reversed like the entity does.
    private SavingsAccountTransaction transaction(final SavingsAccount account, final SavingsAccountCharge charge, final Long id,
            final Money amount, final boolean waiver) {
        final SavingsAccountChargePaidBy paidBy = mock(SavingsAccountChargePaidBy.class);
        when(paidBy.getSavingsAccountCharge()).thenReturn(charge);
        final SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        final AtomicBoolean reversed = new AtomicBoolean();
        when(transaction.getId()).thenReturn(id);
        when(transaction.getAmount(currency)).thenReturn(amount);
        when(transaction.isChargeTransaction()).thenReturn(!waiver);
        when(transaction.isWaiveCharge()).thenReturn(waiver);
        when(transaction.getSavingsAccountChargesPaid()).thenReturn(Set.of(paidBy));
        when(transaction.isPaymentForCurrentCharge(charge)).thenReturn(true);
        when(transaction.isReversed()).thenAnswer(invocation -> reversed.get());
        when(transaction.isNotReversed()).thenAnswer(invocation -> !reversed.get());
        doAnswer(invocation -> {
            reversed.set(true);
            return null;
        }).when(transaction).reverse();
        if (account != null) {
            account.transactions.add(transaction);
        }
        return transaction;
    }

    private SavingsAccountCharge weeklyFee() {
        final Charge chargeDefinition = mock(Charge.class);
        when(chargeDefinition.isPenalty()).thenReturn(false);
        when(chargeDefinition.getChargeTimeType()).thenReturn(ChargeTimeType.WEEKLY_FEE.getValue());
        when(chargeDefinition.getChargeCalculation()).thenReturn(ChargeCalculationType.FLAT.getValue());
        when(chargeDefinition.feeInterval()).thenReturn(1);
        final SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getCurrency()).thenReturn(currency);
        final JsonCommand command = mock(JsonCommand.class);
        when(command.bigDecimalValueOfParameterNamed(SavingsApiConstants.amountParamName)).thenReturn(FEE);
        when(command.localDateValueOfParameterNamed(SavingsApiConstants.dueAsOfDateParamName)).thenReturn(DUE_DATE);
        when(command.integerValueOfParameterNamed(SavingsApiConstants.feeIntervalParamName)).thenReturn(1);
        return SavingsAccountCharge.createNewFromJson(savingsAccount, chargeDefinition, command);
    }

    private Money money(final String amount) {
        return Money.of(currency, new BigDecimal(amount));
    }

    // amountPaid has no public accessor.
    private static BigDecimal amountPaid(final SavingsAccountCharge charge) {
        try {
            final Field field = SavingsAccountCharge.class.getDeclaredField("amountPaid");
            field.setAccessible(true);
            final BigDecimal value = (BigDecimal) field.get(charge);
            return value == null ? BigDecimal.ZERO : value;
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
