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
package org.apache.fineract.portfolio.loanaccount.service.reaging;

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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
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

@SuppressFBWarnings({ "VA_FORMAT_STRING_USES_NEWLINE" })
class LoanReAgingValidatorTest {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    private final LocalDate actualDate = LocalDate.now(Clock.systemUTC());
    private final LocalDate maturityDate = actualDate.plusDays(30);
    private final LocalDate businessDate = maturityDate.plusDays(1);
    private final LocalDate afterMaturity = maturityDate.plusDays(7);

    private LoanReAgingValidator underTest = new LoanReAgingValidator();

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, businessDate)));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testValidateReAge_ShouldNotThrowException() {
        // given
        Loan loan = loan();
        JsonCommand command = jsonCommand();
        // when
        underTest.validateReAge(loan, command);
        // then no exception thrown
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenExternalIdIsLongerThan100() {
        // given
        Loan loan = loan();
        JsonCommand command = jsonCommand(RandomStringUtils.randomAlphabetic(120));
        // when
        PlatformApiDataValidationException result = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("validation.msg.validation.errors.exist");
        assertThat(result.getErrors().get(0).getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg.loan.reAge.externalId.exceeds.max.length");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenStartDateIsMissing() {
        // given
        Loan loan = loan();
        JsonCommand command = makeJsonCommand("""
                {
                    "externalId": "12345",
                    "dateFormat": "%s",
                    "locale": "en",
                    "frequencyType": "MONTHS",
                    "frequencyNumber": 1,
                    "numberOfInstallments": 1
                }
                """.formatted(DATE_FORMAT));
        // when
        PlatformApiDataValidationException result = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("validation.msg.validation.errors.exist");
        assertThat(result.getErrors().get(0).getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg.loan.reAge.startDate.cannot.be.blank");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenFrequencyTypeIsMissing() {
        // given
        Loan loan = loan();
        JsonCommand command = makeJsonCommand("""
                {
                    "externalId": "12345",
                    "dateFormat": "%s",
                    "locale": "en",
                    "startDate": "%s",
                    "frequencyNumber": 1,
                    "numberOfInstallments": 1
                }
                """.formatted(DATE_FORMAT, formatDate(afterMaturity)));
        // when
        PlatformApiDataValidationException result = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("validation.msg.validation.errors.exist");
        assertThat(result.getErrors().get(0).getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg.loan.reAge.frequencyType.cannot.be.blank");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenFrequencyNumberIsMissing() {
        // given
        Loan loan = loan();
        JsonCommand command = makeJsonCommand("""
                {
                    "externalId": "12345",
                    "dateFormat": "%s",
                    "locale": "en",
                    "startDate": "%s",
                    "frequencyType": "MONTHS",
                    "numberOfInstallments": 1
                }
                """.formatted(DATE_FORMAT, formatDate(afterMaturity)));
        // when
        PlatformApiDataValidationException result = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("validation.msg.validation.errors.exist");
        assertThat(result.getErrors().get(0).getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg.loan.reAge.frequencyNumber.cannot.be.blank");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenFrequencyNumberIsZero() {
        // given
        Loan loan = loan();
        JsonCommand command = makeJsonCommand("""
                {
                    "externalId": "12345",
                    "dateFormat": "%s",
                    "locale": "en",
                    "startDate": "%s",
                    "frequencyType": "MONTHS",
                    "frequencyNumber": 0,
                    "numberOfInstallments": 1
                }
                """.formatted(DATE_FORMAT, formatDate(afterMaturity)));
        // when
        PlatformApiDataValidationException result = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("validation.msg.validation.errors.exist");
        assertThat(result.getErrors().get(0).getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg.loan.reAge.frequencyNumber.not.greater.than.zero");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenNumberOfInstallmentsIsMissing() {
        // given
        Loan loan = loan();
        JsonCommand command = makeJsonCommand("""
                {
                    "externalId": "12345",
                    "dateFormat": "%s",
                    "locale": "en",
                    "startDate": "%s",
                    "frequencyType": "MONTHS",
                    "frequencyNumber": 1
                }
                """.formatted(DATE_FORMAT, formatDate(afterMaturity)));
        // when
        PlatformApiDataValidationException result = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("validation.msg.validation.errors.exist");
        assertThat(result.getErrors().get(0).getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg.loan.reAge.numberOfInstallments.cannot.be.blank");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenNumberOfInstallmentsIsZero() {
        // given
        Loan loan = loan();
        JsonCommand command = makeJsonCommand("""
                {
                    "externalId": "12345",
                    "dateFormat": "%s",
                    "locale": "en",
                    "startDate": "%s",
                    "frequencyType": "MONTHS",
                    "frequencyNumber": 1,
                    "numberOfInstallments": 0
                }
                """.formatted(DATE_FORMAT, formatDate(afterMaturity)));
        // when
        PlatformApiDataValidationException result = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("validation.msg.validation.errors.exist");
        assertThat(result.getErrors().get(0).getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg.loan.reAge.numberOfInstallments.not.greater.than.zero");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenNumberOfInstallmentsIsNegative() {
        // given
        Loan loan = loan();
        JsonCommand command = makeJsonCommand("""
                {
                    "externalId": "12345",
                    "dateFormat": "%s",
                    "locale": "en",
                    "startDate": "%s",
                    "frequencyType": "MONTHS",
                    "frequencyNumber": 1,
                    "numberOfInstallments": -1
                }
                """.formatted(DATE_FORMAT, formatDate(afterMaturity)));
        // when
        PlatformApiDataValidationException result = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("validation.msg.validation.errors.exist");
        assertThat(result.getErrors().get(0).getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg.loan.reAge.numberOfInstallments.not.greater.than.zero");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenLoanIsBeforeMaturity() {
        // given
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, actualDate)));
        Loan loan = loan();
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.reage.cannot.be.submitted.before.maturity");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenStartDateIsBeforeMaturity() {
        // given
        Loan loan = loan();
        given(loan.getMaturityDate()).willReturn(maturityDate);
        String formattedDate = formatDate(maturityDate.minusDays(1));
        JsonCommand command = jsonCommand("123456", formattedDate);
        // when
        PlatformApiDataValidationException result = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("validation.msg.validation.errors.exist");
        assertThat(result.getErrors().get(0).getUserMessageGlobalisationCode())
                .isEqualTo("validation.msg.loan.reAge.startDate.is.less.than.date");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenLoanIsOnCumulativeSchedule() {
        // given
        Loan loan = loan();
        given(loan.getLoanProductRelatedDetail().getLoanScheduleType()).willReturn(LoanScheduleType.CUMULATIVE);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode())
                .isEqualTo("error.msg.loan.reage.supported.only.for.progressive.loan.schedule.type");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenLoanIsNotOnAdvancedPaymentAllocation() {
        // given
        Loan loan = loan();
        given(loan.getTransactionProcessingStrategyCode())
                .willReturn(DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor.STRATEGY_CODE);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode())
                .isEqualTo("error.msg.loan.reage.supported.only.for.progressive.loan.schedule.type");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenLoanIsInterestBearing() {
        // given
        Loan loan = loan();
        given(loan.isInterestBearing()).willReturn(true);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.reage.supported.only.for.non.interest.loans");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenLoanIsNotActive() {
        // given
        Loan loan = loan();
        given(loan.getStatus()).willReturn(LoanStatus.APPROVED);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.reage.supported.only.for.active.loans");
    }

    @Test
    public void testValidateReAge_ShouldThrowException_WhenLoanAlreadyHasReAgeForToday() {
        // given
        List<LoanTransaction> transactions = List.of(loanTransaction(LoanTransactionType.DISBURSEMENT, maturityDate.minusDays(2)),
                loanTransaction(LoanTransactionType.REAGE, businessDate));
        Loan loan = loan();
        given(loan.getLoanTransactions()).willReturn(transactions);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.reage.reage.transaction.already.present.for.today");
    }

    @Test
    public void testValidateUndoReAge_ShouldThrowException_WhenLoanDoesntHaveReAge() {
        // given
        List<LoanTransaction> transactions = List.of(loanTransaction(LoanTransactionType.DISBURSEMENT, actualDate.minusDays(3)));
        Loan loan = loan();
        given(loan.getLoanTransactions()).willReturn(transactions);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateUndoReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.reage.reaging.transaction.missing");
    }

    @Test
    public void testValidateUndoReAge_ShouldThrowException_WhenLoanAlreadyHasRepaymentAfterReAge() {
        // given
        List<LoanTransaction> transactions = List.of(loanTransaction(LoanTransactionType.DISBURSEMENT, actualDate.minusDays(3)),
                loanTransaction(LoanTransactionType.REAGE, actualDate.minusDays(2)),
                loanTransaction(LoanTransactionType.REPAYMENT, actualDate.minusDays(1)));
        Loan loan = loan();
        given(loan.getLoanTransactions()).willReturn(transactions);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateUndoReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.reage.repayment.exists.after.reaging");
    }

    @Test
    public void testValidateUndoReAge_ShouldThrowException_WhenLoanAlreadyHasRepaymentAfterReAge_SameDay() {
        // given
        List<LoanTransaction> transactions = List.of(loanTransaction(LoanTransactionType.DISBURSEMENT, actualDate.minusDays(2)),
                loanTransaction(LoanTransactionType.REAGE, actualDate.minusDays(1),
                        OffsetDateTime.of(actualDate, LocalTime.of(10, 0), ZoneOffset.UTC)),
                loanTransaction(LoanTransactionType.REPAYMENT, actualDate.minusDays(1),
                        OffsetDateTime.of(actualDate, LocalTime.of(11, 0), ZoneOffset.UTC)));
        Loan loan = loan();
        given(loan.getLoanTransactions()).willReturn(transactions);
        JsonCommand command = jsonCommand();
        // when
        GeneralPlatformDomainRuleException result = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.validateUndoReAge(loan, command));
        // then
        assertThat(result).isNotNull();
        assertThat(result.getGlobalisationMessageCode()).isEqualTo("error.msg.loan.reage.repayment.exists.after.reaging");
    }

    @Test
    public void testValidateUndoReAge_ShouldNotThrowException_WhenLoanAlreadyHasRepaymentAfterReAge_SameDay_RepaymentBeforeReAge() {
        // given
        List<LoanTransaction> transactions = List.of(loanTransaction(LoanTransactionType.DISBURSEMENT, actualDate.minusDays(2)),
                loanTransaction(LoanTransactionType.REAGE, actualDate.minusDays(1),
                        OffsetDateTime.of(actualDate, LocalTime.of(10, 0), ZoneOffset.UTC)),
                loanTransaction(LoanTransactionType.REPAYMENT, actualDate.minusDays(1),
                        OffsetDateTime.of(actualDate, LocalTime.of(9, 0), ZoneOffset.UTC)));
        Loan loan = loan();
        given(loan.getLoanTransactions()).willReturn(transactions);
        JsonCommand command = jsonCommand();
        // when
        underTest.validateUndoReAge(loan, command);
        // then no exception thrown
    }

    private JsonCommand jsonCommand() {
        return jsonCommand("123456");
    }

    private JsonCommand jsonCommand(String externalId) {
        return jsonCommand(externalId, formatDate(afterMaturity));
    }

    private String formatDate(LocalDate date) {
        return DateTimeFormatter.ofPattern(DATE_FORMAT).format(date);
    }

    private JsonCommand jsonCommand(String externalId, String startDate) {
        String json = """
                {
                    "externalId": "%s",
                    "dateFormat": "%s",
                    "locale": "en",
                    "frequencyType": "MONTHS",
                    "frequencyNumber": 1,
                    "startDate": "%s",
                    "numberOfInstallments": 1
                }
                """.formatted(externalId, DATE_FORMAT, startDate);
        return makeJsonCommand(json);
    }

    private JsonCommand makeJsonCommand(String json) {
        FromJsonHelper fromJsonHelper = new FromJsonHelper();
        return new JsonCommand(1L, fromJsonHelper.parse(json), fromJsonHelper);
    }

    private LoanTransaction loanTransaction(LoanTransactionType type, LocalDate txDate, OffsetDateTime creationTime) {
        LoanTransaction loanTransaction = loanTransaction(type, txDate);
        given(loanTransaction.getCreatedDateTime()).willReturn(creationTime);
        return loanTransaction;
    }

    private LoanTransaction loanTransaction(LoanTransactionType type, LocalDate txDate) {
        LoanTransaction loanTransaction = mock(LoanTransaction.class);
        given(loanTransaction.getTypeOf()).willReturn(type);
        given(loanTransaction.getTransactionDate()).willReturn(txDate);
        given(loanTransaction.getSubmittedOnDate()).willReturn(txDate);
        return loanTransaction;
    }

    private Loan loan() {
        Loan loan = mock(Loan.class);
        given(loan.getStatus()).willReturn(LoanStatus.ACTIVE);
        given(loan.getMaturityDate()).willReturn(maturityDate);
        given(loan.getTransactionProcessingStrategyCode())
                .willReturn(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
        LoanProductRelatedDetail loanProductRelatedDetail = mock(LoanProductRelatedDetail.class);
        given(loan.getLoanProductRelatedDetail()).willReturn(loanProductRelatedDetail);
        given(loanProductRelatedDetail.getLoanScheduleType()).willReturn(LoanScheduleType.PROGRESSIVE);
        given(loan.isInterestBearing()).willReturn(false);
        given(loan.getLoanTransactions()).willReturn(List.of());
        return loan;
    }

}
