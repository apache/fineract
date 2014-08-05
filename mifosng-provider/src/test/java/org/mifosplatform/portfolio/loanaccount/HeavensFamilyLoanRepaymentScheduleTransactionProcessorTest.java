/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl.HeavensFamilyLoanRepaymentScheduleTransactionProcessor;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("unused")
@RunWith(MockitoJUnitRunner.class)
public class HeavensFamilyLoanRepaymentScheduleTransactionProcessorTest {

    // class under test
    private HeavensFamilyLoanRepaymentScheduleTransactionProcessor processor;

    //
    private final LocalDate july2nd = new LocalDate(2012, 7, 2);
    private final MonetaryCurrency usDollars = new MonetaryCurrencyBuilder().withCode("USD").withDigitsAfterDecimal(2).build();
    private List<LoanRepaymentScheduleInstallment> installments;

    @Before
    public void setUpForEachTestCase() {

        this.installments = LoanScheduleTestDataHelper.createSimpleLoanSchedule(this.july2nd, this.usDollars);

        this.processor = new HeavensFamilyLoanRepaymentScheduleTransactionProcessor();
    }

    /**
     * Scenario 1: Single transaction which is less than the interest component
     * of the first installment.
     * 
     * Expectation: - First installment shows interest completed equal to that
     * of transactions, zero principal completed. (payment order interest,
     * principal) - transaction has interest portion equal to transaction
     * amount, and zero principal portion
     */
    @Test
    public void givenSingleOnTimeLoanTransactionShouldPayoffInterestComponentFirst() {

        // // setup
        // LoanRepaymentScheduleInstallment singleInstallment =
        // this.installments.get(0);
        //
        // Money oneHundredDollars = new
        // MoneyBuilder().with(usDollars).with("100.00").build();
        // LoanTransaction onTimePartialPayment = new
        // LoanTransactionBuilder().repayment().with(july2nd).with(oneHundredDollars).build();
        //
        // // execute test
        // processor.handleTransaction(onTimePartialPayment, usDollars,
        // installments);
        //
        // // verification of derived fields on installments
        // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("0.00",
        // "100.00", singleInstallment);
        // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("0.00",
        // "100.00", onTimePartialPayment);
    }

    // /**
    // * Scenario 2: Two transactions which is less than the interest component
    // of the first installment.
    // *
    // * Expectation:
    // * - First installment shows interest completed equal to that of
    // transactions, zero principal completed. (payment order interest,
    // principal)
    // * - transactions has interest portion equal to transaction amount, and
    // zero principal portion
    // */
    // @Test
    // public void
    // givenMultipleOnTimeLoanTransactionShouldPayoffInterestComponentFirst() {
    //
    // // setup
    // LoanRepaymentScheduleInstallment singleInstallment =
    // this.installments.get(0);
    //
    // Money oneHundredDollars = new
    // MoneyBuilder().with(usDollars).with("100.00").build();
    // LoanTransaction onTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(oneHundredDollars).build();
    //
    // Money fiftyDollars = new
    // MoneyBuilder().with(usDollars).with("50.00").build();
    // LoanTransaction secondOnTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(fiftyDollars).build();
    //
    //
    // // execute test
    // processor.handleTransaction(onTimePartialPayment, usDollars,
    // installments);
    // processor.handleTransaction(secondOnTimePartialPayment, usDollars,
    // installments);
    //
    // // verification of derived fields on installments
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("0.00",
    // "150.00", singleInstallment);
    //
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("0.00",
    // "100.00", onTimePartialPayment);
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("0.00",
    // "50.00", secondOnTimePartialPayment);
    // }
    //
    // /**
    // * Scenario 3: Single transaction which is greater than the interest
    // component of the first installment but less than the total due for
    // installment.
    // *
    // * Expectation:
    // * - First installment shows interest completed equal to that interest
    // due, remainder is against principal completed. (payment order interest,
    // principal)
    // * - transaction has interest portion equal to interest expected and
    // principal portion of remainder.
    // */
    // @Test
    // public void
    // givenSingleOnTimeLoanTransactionShouldPayoffInterestThenPrincipalComponents()
    // {
    //
    // // setup
    // LoanRepaymentScheduleInstallment singleInstallment =
    // this.installments.get(0);
    //
    // Money amount = new MoneyBuilder().with(usDollars).with("300.00").build();
    // LoanTransaction onTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(amount).build();
    //
    // // execute test
    // processor.handleTransaction(onTimePartialPayment, usDollars,
    // installments);
    //
    // // verification of derived fields on installments
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("100.00",
    // "200.00", singleInstallment);
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("100.00",
    // "200.00", onTimePartialPayment);
    // }
    //
    // /**
    // * Scenario 4: Single transaction which is greater than the interest
    // component of the first installment but less than the total due for
    // installment.
    // *
    // * Expectation:
    // * - First installment shows interest completed equal to that interest
    // due, remainder is against principal completed. (payment order interest,
    // principal)
    // * - transaction has interest portion equal to interest expected and
    // principal portion of remainder.
    // */
    // @Test
    // public void
    // givenMultipleOnTimeLoanTransactionShouldPayoffInterestThenPrincipalComponents()
    // {
    //
    // // setup
    // LoanRepaymentScheduleInstallment singleInstallment =
    // this.installments.get(0);
    //
    // Money amount = new MoneyBuilder().with(usDollars).with("300.00").build();
    // LoanTransaction onTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(amount).build();
    //
    // Money secondTransactionAmount = new
    // MoneyBuilder().with(usDollars).with("50.00").build();
    // LoanTransaction secondOnTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(secondTransactionAmount).build();
    //
    // // execute test
    // processor.handleTransaction(onTimePartialPayment, usDollars,
    // installments);
    // processor.handleTransaction(secondOnTimePartialPayment, usDollars,
    // installments);
    //
    // // verification of derived fields on installments
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("150.00",
    // "200.00", singleInstallment);
    //
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("100.00",
    // "200.00", onTimePartialPayment);
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("50.00",
    // "0.00", secondOnTimePartialPayment);
    // }
    //
    // /**
    // * Scenario 5:Single transaction which is equal to the total due for
    // installment.
    // *
    // * Expectation:
    // * - First installment shows interest completed and principal completed
    // equal to their due amounts. (payment order interest, principal)
    // * - transaction has interest portion and principal portions
    // */
    // @Test
    // public void
    // givenSingleFullOnTimeLoanTransactionShouldPayoffInterestThenPrincipalComponentInFull()
    // {
    //
    // // setup
    // LoanRepaymentScheduleInstallment singleInstallment =
    // this.installments.get(0);
    //
    // Money amount = new
    // MoneyBuilder().with(usDollars).with("1200.00").build();
    // LoanTransaction onTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(amount).build();
    //
    // // execute test
    // processor.handleTransaction(onTimePartialPayment, usDollars,
    // installments);
    //
    // // verification of derived fields on installments
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("1000.00",
    // "200.00", singleInstallment);
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("1000.00",
    // "200.00", onTimePartialPayment);
    // }
    //
    // /**
    // * Scenario 6: Multiple transactions which together equal to the total due
    // for installment.
    // *
    // * Expectation:
    // * - First installment shows interest completed and principal completed
    // equal to their due amounts. (payment order interest, principal)
    // * - transaction has interest portion and principal portions
    // */
    // @Test
    // public void
    // givenMuliptleOnTimeLoanTransactionsThatInFullShouldPayoffInterestThenPrincipalComponentInFull()
    // {
    //
    // // setup
    // LoanRepaymentScheduleInstallment singleInstallment =
    // this.installments.get(0);
    //
    // Money amount = new MoneyBuilder().with(usDollars).with("600.00").build();
    // LoanTransaction onTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(amount).build();
    //
    // Money secondTransactionAmount = new
    // MoneyBuilder().with(usDollars).with("600.00").build();
    // LoanTransaction secondOnTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(secondTransactionAmount).build();
    //
    // // execute test
    // processor.handleTransaction(onTimePartialPayment, usDollars,
    // installments);
    // processor.handleTransaction(secondOnTimePartialPayment, usDollars,
    // installments);
    //
    // // verification of derived fields on installments
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("1000.00",
    // "200.00", singleInstallment);
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("400.00",
    // "200.00", onTimePartialPayment);
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("600.00",
    // "0.00", secondOnTimePartialPayment);
    // }
    //
    // /**
    // * Scenario 7: Single transaction which is greater than total due for
    // first installment.
    // *
    // * Expectation:
    // * - First installment shows interest completed and principal completed
    // equal to their due amounts. (payment order interest, principal)
    // * - Second installment shows principal completed equal to the remaining
    // amount
    // * - transaction has interest portion and principal portions, the
    // principal portion is the sum of principal completed for first and second
    // installments
    // */
    // @Test
    // public void
    // givenSingleOnTimeLoanTransactionThatOverpaysInstallmentShouldPayoffInterestThenPrincipalComponentInFullOfFirstInstallmentWithRemainingOverpaymentAgainstPrincipalOfNextInstallment()
    // {
    //
    // // setup
    // Money amount = new
    // MoneyBuilder().with(usDollars).with("1300.00").build();
    // LoanTransaction onTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(amount).build();
    //
    // // execute test
    // processor.handleTransaction(onTimePartialPayment, usDollars,
    // installments);
    //
    // // verification of derived fields on installments
    // LoanRepaymentScheduleInstallment firstInstallment =
    // this.installments.get(0);
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("1000.00",
    // "200.00", firstInstallment);
    //
    // LoanRepaymentScheduleInstallment secondInstallment =
    // this.installments.get(1);
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("100.00",
    // "0.00", secondInstallment);
    //
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("1100.00",
    // "200.00", onTimePartialPayment);
    // }
    //
    // /**
    // * Scenario 8: Multiple transactions which together are greater than total
    // due for first installment.
    // *
    // * Expectation:
    // * - First installment shows interest completed and principal completed
    // equal to their due amounts. (payment order interest, principal)
    // * - Second installment shows principal completed equal to the remaining
    // amount
    // */
    // @Test
    // public void
    // givenMultipleOnTimeLoanTransactionThatOverpaysInstallmentShouldPayoffInterestThenPrincipalComponentInFullOfFirstInstallmentWithRemainingOverpaymentAgainstPrincipalOfNextInstallment()
    // {
    //
    // // setup
    // Money amount = new MoneyBuilder().with(usDollars).with("600.00").build();
    // LoanTransaction onTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(amount).build();
    //
    // Money secondTransactionAmount = new
    // MoneyBuilder().with(usDollars).with("700.00").build();
    // LoanTransaction secondOnTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(secondTransactionAmount).build();
    //
    // // execute test
    // processor.handleTransaction(onTimePartialPayment, usDollars,
    // installments);
    // processor.handleTransaction(secondOnTimePartialPayment, usDollars,
    // installments);
    //
    // // verification of derived fields on installments
    // LoanRepaymentScheduleInstallment singleInstallment =
    // this.installments.get(0);
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("1000.00",
    // "200.00", singleInstallment);
    //
    // LoanRepaymentScheduleInstallment secondInstallment =
    // this.installments.get(1);
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("100.00",
    // "0.00", secondInstallment);
    //
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("400.00",
    // "200.00", onTimePartialPayment);
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("700.00",
    // "0.00", secondOnTimePartialPayment);
    // }
    //
    // /**
    // * Scenario 9: Single transaction which overpays first installment and
    // second installment with outstanding amount been account as principal in
    // third installment.
    // *
    // * Where an installments principal is paid for fully in advance - the
    // interest component is waived.
    // */
    // @Test
    // public void
    // givenOverPaymentShouldPayoffInterestThenPrincipalComponentsOfFirstInstallmentWithRemainingOverpaymentAgainstPrincipalOfAllSubsequentInstallments()
    // {
    // // setup
    // Money amount = new
    // MoneyBuilder().with(usDollars).with("3000.00").build();
    // LoanTransaction onTimePartialPayment = new
    // LoanTransactionBuilder().repayment().with(july2nd).with(amount).build();
    //
    // // execute test
    // processor.handleTransaction(onTimePartialPayment, usDollars,
    // installments);
    //
    // // verification of derived fields on installments
    // LoanRepaymentScheduleInstallment firstInstallment =
    // this.installments.get(0);
    // assertThatLoanInstallmentHasDerivedFieldsOf(principalOf("1000.00"),
    // interestOf("200.00"), interestWaivedOf("0.00"), firstInstallment,
    // usDollars);
    //
    // LoanRepaymentScheduleInstallment secondInstallment =
    // this.installments.get(1);
    // assertThatLoanInstallmentHasDerivedFieldsOf(principalOf("1000.00"),
    // interestOf("0.00"), interestWaivedOf("200.00"), secondInstallment,
    // usDollars);
    //
    // LoanRepaymentScheduleInstallment thirdInstallment =
    // this.installments.get(2);
    // assertThatLoanInstallmentHasDerivedFieldsOf(principalOf("800.00"),
    // interestOf("0.00"), interestWaivedOf("0.00"), thirdInstallment,
    // usDollars);
    //
    // assertThatLoanTransactionHasDerivedFieldsOf(principalOf("2800.00"),
    // interestOf("200.00"), onTimePartialPayment, usDollars);
    // }
    //
    // /**
    // * Scenario 10: Single transaction which is a repayment in advance
    // */
    // @Test
    // public void
    // givenRepaymentOfInstallmentInAdvanceShouldPayoffOnlyPrincipalComponent()
    // {
    // // setup
    // installments =
    // LoanScheduleTestDataHelper.createSimpleLoanScheduleWithFirstInstallmentFullyPaid(july2nd,
    // usDollars);
    // LoanRepaymentScheduleInstallment firstInstallment =
    // this.installments.get(0);
    // LocalDate paymentInAdvanceDate =
    // firstInstallment.getDueDate().minusDays(1);
    //
    // Money amount = new MoneyBuilder().with(usDollars).with("900.00").build();
    // LoanTransaction paymentInAdvance = new
    // LoanTransactionBuilder().repayment().with(paymentInAdvanceDate).with(amount).build();
    //
    // // execute test
    // processor.handleTransaction(paymentInAdvance, usDollars, installments);
    //
    // // verification of derived fields on installments
    // LoanRepaymentScheduleInstallment secondInstallment =
    // this.installments.get(1);
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("900.00",
    // "0.00", secondInstallment);
    //
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("900.00",
    // "0.00", paymentInAdvance);
    // }
    //
    // /**
    // * Scenario 11: Single transaction which is a repayment many days early
    // but still does not qualify for 'in advance'.
    // */
    // @Test
    // public void
    // givenRepaymentOfInstallmentBeforeDueDateButNotInAdvanceShouldPayoffInterestAndPrincipalComponents()
    // {
    // // setup
    // installments =
    // LoanScheduleTestDataHelper.createSimpleLoanScheduleWithFirstInstallmentFullyPaid(july2nd,
    // usDollars);
    // LoanRepaymentScheduleInstallment firstInstallment =
    // this.installments.get(0);
    // LocalDate paymentBeforeDueDateButNotInAdvanceDate =
    // firstInstallment.getDueDate().plusDays(1);
    //
    // Money amount = new MoneyBuilder().with(usDollars).with("900.00").build();
    // LoanTransaction paymentBeforeDueDateButNotInAdvance = new
    // LoanTransactionBuilder().repayment().with(paymentBeforeDueDateButNotInAdvanceDate).with(amount).build();
    //
    // // execute test
    // processor.handleTransaction(paymentBeforeDueDateButNotInAdvance,
    // usDollars, installments);
    //
    // // verification of derived fields on installments
    // LoanRepaymentScheduleInstallment secondInstallment =
    // this.installments.get(1);
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf("700.00",
    // "200.00", secondInstallment);
    //
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf("700.00",
    // "200.00", paymentBeforeDueDateButNotInAdvance);
    // }
    //
    //
    // /**
    // * Scenario 12: Single transaction which is a repayment many days early
    // but still does not qualify for 'in advance'.
    // */
    // @Test
    // public void
    // givenLateRepaymentOfInstallmentShouldPayoffInterestAndPrincipalComponentsOfOverdueInstallmentsWithNoWaiveOfInterest()
    // {
    // // setup
    // installments =
    // LoanScheduleTestDataHelper.createSimpleLoanSchedule(july2nd, usDollars);
    // LoanRepaymentScheduleInstallment firstInstallment =
    // this.installments.get(0);
    // LocalDate latePaymentDate = firstInstallment.getDueDate().plusMonths(5);
    //
    // Money amount = new
    // MoneyBuilder().with(usDollars).with("3600.00").build();
    // LoanTransaction lateRepaymentTransaction = new
    // LoanTransactionBuilder().repayment().with(latePaymentDate).with(amount).build();
    //
    // // execute test
    // processor.handleTransaction(lateRepaymentTransaction, usDollars,
    // installments);
    //
    // // verification of derived fields on installments
    // assertThatLoanInstallmentHasDerivedFieldsOf(principalOf("1000.00"),
    // interestOf("200.00"), interestWaivedOf("0.00"), firstInstallment,
    // usDollars);
    //
    // LoanRepaymentScheduleInstallment secondInstallment =
    // this.installments.get(1);
    // assertThatLoanInstallmentHasDerivedFieldsOf(principalOf("1000.00"),
    // interestOf("200.00"), interestWaivedOf("0.00"), secondInstallment,
    // usDollars);
    //
    // LoanRepaymentScheduleInstallment thirdInstallment =
    // this.installments.get(2);
    // assertThatLoanInstallmentHasDerivedFieldsOf(principalOf("1000.00"),
    // interestOf("200.00"), interestWaivedOf("0.00"), thirdInstallment,
    // usDollars);
    //
    // assertThatLoanTransactionHasDerivedFieldsOf(principalOf("3000.00"),
    // interestOf("600.00"), lateRepaymentTransaction, usDollars);
    // }
    //
    //
    // private void
    // assertThatLoanTransactionHasDerivedFieldsForPrincipalInterestOf(
    // final String principal, final String interest, final LoanTransaction
    // transaction) {
    //
    // Money expectedInterestPortion = new
    // MoneyBuilder().with(usDollars).with(interest).build();
    // assertThat(transaction.getInterestPortion(usDollars).toString(),
    // is(expectedInterestPortion.toString()));
    //
    // Money expectedPrincipalPortion = new
    // MoneyBuilder().with(usDollars).with(principal).build();
    // assertThat(transaction.getPrincipalPortion(usDollars).toString(),
    // is(expectedPrincipalPortion.toString()));
    // }
    //
    // private void
    // assertThatLoanInstallmentHasDerivedFieldsForPrincipalInterestOf(final
    // String principal, final String interest,
    // final LoanRepaymentScheduleInstallment installment) {
    // Money expectedInterestCompleted = new
    // MoneyBuilder().with(usDollars).with(interest).build();
    // assertThat(installment.getInterestCompleted(usDollars).toString(),
    // is(expectedInterestCompleted.toString()));
    //
    // Money expectedPrincipalCompleted = new
    // MoneyBuilder().with(usDollars).with(principal).build();
    // assertThat(installment.getPrincipalCompleted(usDollars).toString(),
    // is(expectedPrincipalCompleted.toString()));
    // }
    //
    // private void assertThatLoanTransactionHasDerivedFieldsOf(
    // final String principal,
    // final String interest,
    // final LoanTransaction transaction, final MonetaryCurrency currency) {
    //
    // Money expectedPrincipalPortion = new
    // MoneyBuilder().with(currency).with(principal).build();
    // assertThat("Principal portion of transaction not as expected: ",
    // transaction.getPrincipalPortion(currency).toString(),
    // is(expectedPrincipalPortion.toString()));
    //
    // Money expectedInterestPortion = new
    // MoneyBuilder().with(currency).with(interest).build();
    // assertThat("Interest portion of transaction not as expected: ",
    // transaction.getInterestPortion(currency).toString(),
    // is(expectedInterestPortion.toString()));
    // }
    //
    // private void assertThatLoanInstallmentHasDerivedFieldsOf(
    // final String principal,
    // final String interest,
    // final String interestWaived,
    // final LoanRepaymentScheduleInstallment installment, final
    // MonetaryCurrency currency) {
    //
    // Money expectedPrincipalCompleted = new
    // MoneyBuilder().with(currency).with(principal).build();
    // assertThat("principal not as expected",
    // installment.getPrincipalCompleted(currency).toString(),
    // is(expectedPrincipalCompleted.toString()));
    //
    // Money expectedInterestCompleted = new
    // MoneyBuilder().with(currency).with(interest).build();
    // assertThat("interest completed not as expected: ",installment.getInterestCompleted(currency).toString(),
    // is(expectedInterestCompleted.toString()));
    //
    // Money expectedInterestWaived = new
    // MoneyBuilder().with(currency).with(interestWaived).build();
    // assertThat("interest waived not as expected: ",
    // installment.getInterestWaived(currency).toString(),
    // is(expectedInterestWaived.toString()));
    // }
    //
    // private String principalOf(final String value) {
    // return value;
    // }
    //
    // private String interestOf(final String value) {
    // return value;
    // }
    //
    // private String interestWaivedOf(final String value) {
    // return value;
    // }
}