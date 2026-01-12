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
package org.apache.fineract.portfolio.loanaccount.service.reamortization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@SuppressFBWarnings({ "VA_FORMAT_STRING_USES_NEWLINE" })
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanReAmortizationValidatorTest {

    private final LocalDate actualDate = LocalDate.now(Clock.systemUTC());

    @InjectMocks
    private LoanReAmortizationValidator underTest;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, actualDate)));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testValidateReAmortize_ShouldNotThrowException() {
        // given
        Loan loan = loan();
        JsonCommand command = jsonCommand();
        // when
        underTest.validateReAmortize(loan, command);
        // then no exception thrown
    }

    @Test
    public void testValidateReAmortize_ShouldThrowException_WhenExternalIdIsLongerThan100() {
        // given
        Loan loan = loan();
        JsonCommand command = jsonCommand(RandomStringUtils.randomAlphabetic(120));
        // when
        PlatformApiDataValidationException result = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateReAmortize(loan, command));
        // then
        assertThat(result).isNotNull();
    }

    @Test
    public void testValidateReAmortize_ShouldThrowException_WhenLoanIsAfterMaturity() {
        // given
        Loan loan = loan();
        given(loan.getMaturityDate()).willReturn(actualDate.minusDays(2));
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAmortize(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.reamortize.cannot.be.submitted.after.maturity");
    }

    @Test
    public void testValidateReAmortize_ShouldThrowException_WhenLoanIsOnCumulativeSchedule() {
        // given
        Loan loan = loan();
        given(loan.getLoanProductRelatedDetail().getLoanScheduleType()).willReturn(LoanScheduleType.CUMULATIVE);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAmortize(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode())
                .isEqualTo("error.msg.loan.reamortize.supported.only.for.progressive.loan.schedule.type");
    }

    @Test
    public void testValidateReAmortize_ShouldThrowException_WhenLoanIsNotOnAdvancedPaymentAllocation() {
        // given
        Loan loan = loan();
        given(loan.getTransactionProcessingStrategyCode())
                .willReturn(DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor.STRATEGY_CODE);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAmortize(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode())
                .isEqualTo("error.msg.loan.reamortize.supported.only.for.progressive.loan.schedule.type");
    }

    @Test
    public void testValidateReAmortize_ShouldThrowException_WhenLoanIsNotActive() {
        // given
        Loan loan = loan();
        given(loan.getStatus()).willReturn(LoanStatus.APPROVED);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAmortize(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.reamortize.supported.only.for.active.loans");
    }

    @Test
    public void testValidateReAmortize_ShouldThrowException_WhenLoanAlreadyHasReAmortizationForToday() {
        // given
        List<LoanTransaction> transactions = List.of(loanTransaction(LoanTransactionType.DISBURSEMENT, actualDate.minusDays(2)),
                loanTransaction(LoanTransactionType.REAMORTIZE, actualDate));
        Loan loan = loan();
        given(loan.getLoanTransactions()).willReturn(transactions);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAmortize(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode())
                .isEqualTo("error.msg.loan.reamortize.reamortize.transaction.already.present.for.today");
    }

    @Test
    public void testValidateReAmortize_ShouldThrowException_WhenNoOverdueInstallmentsExist() {
        // given
        Loan loan = loan(false);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAmortize(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.reamortize.no.overdue.amount");
    }

    @Test
    public void testValidateUndoReAmortize_ShouldThrowException_WhenLoanDoesntHaveReAmortization() {
        // given
        List<LoanTransaction> transactions = List.of(loanTransaction(LoanTransactionType.DISBURSEMENT, actualDate.minusDays(3)));
        Loan loan = loan();
        given(loan.getLoanTransactions()).willReturn(transactions);
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.findAndValidateReAmortizeTransactionForUndo(loan));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.reamortize.reamortization.transaction.missing");
    }

    @Test
    public void testValidateUndoReAmortize_ShouldNotThrowException() {
        // given
        List<LoanTransaction> transactions = List.of(loanTransaction(LoanTransactionType.DISBURSEMENT, actualDate.minusDays(2)),
                loanTransaction(LoanTransactionType.REAMORTIZE, actualDate.minusDays(1),
                        OffsetDateTime.of(actualDate, LocalTime.of(10, 0), ZoneOffset.UTC)),
                loanTransaction(LoanTransactionType.REPAYMENT, actualDate.minusDays(1),
                        OffsetDateTime.of(actualDate, LocalTime.of(9, 0), ZoneOffset.UTC)));
        Loan loan = loan();
        given(loan.getLoanTransactions()).willReturn(transactions);
        // when
        underTest.findAndValidateReAmortizeTransactionForUndo(loan);
        // then no exception thrown
    }

    private JsonCommand jsonCommand() {
        return jsonCommand("123456");
    }

    private JsonCommand jsonCommand(String externalId) {
        String json = """
                {
                    "externalId": "%s"
                }
                """.formatted(externalId);
        FromJsonHelper fromJsonHelper = new FromJsonHelper();
        return new JsonCommand(1L, fromJsonHelper.parse(json), fromJsonHelper);
    }

    private LoanTransaction loanTransaction(LoanTransactionType type, LocalDate txDate, OffsetDateTime creationTime) {
        LoanTransaction loanTransaction = loanTransaction(type, txDate);
        given(loanTransaction.getCreatedDate()).willReturn(Optional.of(creationTime));
        return loanTransaction;
    }

    private LoanTransaction loanTransaction(LoanTransactionType type, LocalDate txDate) {
        LoanTransaction loanTransaction = mock(LoanTransaction.class);
        given(loanTransaction.getTypeOf()).willReturn(type);
        given(loanTransaction.getTransactionDate()).willReturn(txDate);
        given(loanTransaction.getSubmittedOnDate()).willReturn(txDate);
        given(loanTransaction.isNotReversed()).willReturn(true);
        return loanTransaction;
    }

    private Loan loan() {
        return loan(true);
    }

    private Loan loan(boolean withOverdueInstallments) {
        Loan loan = mock(Loan.class);
        given(loan.getStatus()).willReturn(LoanStatus.ACTIVE);
        given(loan.getMaturityDate()).willReturn(actualDate.plusDays(30));
        given(loan.getTransactionProcessingStrategyCode())
                .willReturn(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
        LoanProductRelatedDetail loanProductRelatedDetail = mock(LoanProductRelatedDetail.class);
        given(loan.getLoanProductRelatedDetail()).willReturn(loanProductRelatedDetail);
        given(loanProductRelatedDetail.getLoanScheduleType()).willReturn(LoanScheduleType.PROGRESSIVE);
        given(loan.isInterestBearing()).willReturn(false);
        given(loan.getLoanTransactions()).willReturn(List.of());

        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, null);
        given(loan.getCurrency()).willReturn(currency);

        Money principalOutstanding = mock(Money.class);
        given(principalOutstanding.isGreaterThanZero()).willReturn(true);

        if (withOverdueInstallments) {
            LoanRepaymentScheduleInstallment overdueInstallment = mock(LoanRepaymentScheduleInstallment.class);
            given(overdueInstallment.getDueDate()).willReturn(actualDate.minusDays(5));
            given(overdueInstallment.getPrincipalOutstanding(currency)).willReturn(principalOutstanding);
            given(loan.getRepaymentScheduleInstallments()).willReturn(List.of(overdueInstallment));
        } else {
            LoanRepaymentScheduleInstallment futureInstallment = mock(LoanRepaymentScheduleInstallment.class);
            given(futureInstallment.getDueDate()).willReturn(actualDate.plusDays(10));
            given(futureInstallment.getPrincipalOutstanding(currency)).willReturn(principalOutstanding);
            given(loan.getRepaymentScheduleInstallments()).willReturn(List.of(futureInstallment));
        }
        return loan;
    }

}
