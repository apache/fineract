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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Covers the prepayment (payoff) template: the amount a caller must repay to close a Working Capital loan, and the
 * per-bucket breakdown behind it.
 */
@ExtendWith(MockitoExtension.class)
public class WorkingCapitalLoanPrePaymentTemplateTest {

    private static final Long LOAN_ID = 7L;
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2026, 1, 20);
    private static final String PREPAY = "prepayLoan";

    @Mock
    private WorkingCapitalLoanRepository workingCapitalLoanRepository;

    @Mock
    private PaymentTypeReadService paymentTypeReadPlatformService;

    @Mock
    private CodeValueReadPlatformService codeValueReadPlatformService;

    @InjectMocks
    private WorkingCapitalLoanTransactionReadPlatformServiceImpl service;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());
    }

    @AfterEach
    public void tearDown() {
        MoneyHelper.clearCacheForTenant("default");
        ThreadLocalContextUtil.reset();
    }

    @Test
    @DisplayName("The payoff amount is outstanding principal + fee + penalty, and each portion is reported separately")
    public void quotesTheOutstandingBucketsAndTheirTotal() {
        givenLoanWithBalance(amount("9000"), amount("2000"), amount("120"), amount("20"), amount("300"), amount("100"));

        final WorkingCapitalLoanTransactionTemplateData template = service.retrieveTransactionTemplate(LOAN_ID, PREPAY, TRANSACTION_DATE);

        assertThat(template.getPrincipalPortion()).isEqualByComparingTo("7000");
        assertThat(template.getFeeChargesPortion()).isEqualByComparingTo("100");
        assertThat(template.getPenaltyChargesPortion()).isEqualByComparingTo("200");
        assertThat(template.getTransactionAmount()).isEqualByComparingTo("7300");
    }

    @Test
    @DisplayName("The template is typed as a Repayment, so the caller posts it through the repayment endpoint")
    public void quotesARepaymentTransactionType() {
        givenLoanWithBalance(amount("9000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        final WorkingCapitalLoanTransactionTemplateData template = service.retrieveTransactionTemplate(LOAN_ID, PREPAY, TRANSACTION_DATE);

        assertThat(template.getType().getId()).isEqualTo(LoanTransactionType.REPAYMENT.getValue().longValue());
        assertThat(template.getWcLoanId()).isEqualTo(LOAN_ID);
        assertThat(template.getTransactionDate()).isEqualTo(TRANSACTION_DATE);
        assertThat(template.getCurrency().getCode()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("A fully repaid loan is quoted at zero, not at a negative amount")
    public void quotesZeroForAFullyRepaidLoan() {
        givenLoanWithBalance(amount("9000"), amount("9000"), amount("120"), amount("120"), amount("300"), amount("300"));

        final WorkingCapitalLoanTransactionTemplateData template = service.retrieveTransactionTemplate(LOAN_ID, PREPAY, TRANSACTION_DATE);

        assertThat(template.getTransactionAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(template.getPrincipalPortion()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(template.getFeeChargesPortion()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(template.getPenaltyChargesPortion()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("The quote is unaffected by the transaction date: a Working Capital loan accrues nothing over time")
    public void quotesTheSameAmountForAnyTransactionDate() {
        givenLoanWithBalance(amount("9000"), amount("2000"), amount("120"), amount("20"), amount("300"), amount("100"));

        final WorkingCapitalLoanTransactionTemplateData backdated = service.retrieveTransactionTemplate(LOAN_ID, PREPAY,
                TRANSACTION_DATE.minusDays(10));
        final WorkingCapitalLoanTransactionTemplateData today = service.retrieveTransactionTemplate(LOAN_ID, PREPAY, TRANSACTION_DATE);

        assertThat(backdated.getTransactionAmount()).isEqualByComparingTo(today.getTransactionAmount());
        assertThat(backdated.getTransactionDate()).isEqualTo(TRANSACTION_DATE.minusDays(10));
    }

    @Test
    @DisplayName("An unknown loan id is reported as a not-found loan, not as a lazy-loading failure")
    public void rejectsAnUnknownLoanId() {
        when(workingCapitalLoanRepository.findByIdWithFullDetails(LOAN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.retrieveTransactionTemplate(LOAN_ID, PREPAY, TRANSACTION_DATE))
                .isInstanceOf(WorkingCapitalLoanNotFoundException.class);
    }

    @Test
    @DisplayName("The template carries the payment type and classification options, so one call renders the whole screen")
    public void quotesTheDropdownOptionsAlongsideTheAmounts() {
        givenLoanWithBalance(amount("9000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        final PaymentTypeData paymentType = PaymentTypeData.builder().id(1L).name("Money Transfer").build();
        final CodeValueData classification = CodeValueData.instance(7L, "D00");
        when(paymentTypeReadPlatformService.retrieveAllPaymentTypes()).thenReturn(List.of(paymentType));
        when(codeValueReadPlatformService.retrieveCodeValuesByCode("working_capital_loan_repayment_classification"))
                .thenReturn(List.of(classification));

        final WorkingCapitalLoanTransactionTemplateData template = service.retrieveTransactionTemplate(LOAN_ID, PREPAY, TRANSACTION_DATE);

        assertThat(template.getPaymentTypeOptions()).containsExactly(paymentType);
        assertThat(template.getClassificationOptions()).containsExactly(classification);
    }

    @Test
    @DisplayName("A loan with no balance row is quoted with absent amounts, not a payoff of zero")
    public void quotesAbsentAmountsWhenTheBalanceRowIsMissing() {
        final WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        final WorkingCapitalLoanProduct product = mock(WorkingCapitalLoanProduct.class);
        when(product.getCurrency()).thenReturn(new MonetaryCurrency("EUR", 2, null));
        when(loan.getLoanProduct()).thenReturn(product);
        when(loan.getBalance()).thenReturn(null);
        when(workingCapitalLoanRepository.findByIdWithFullDetails(LOAN_ID)).thenReturn(Optional.of(loan));

        final WorkingCapitalLoanTransactionTemplateData template = service.retrieveTransactionTemplate(LOAN_ID, PREPAY, TRANSACTION_DATE);

        assertThat(template.getTransactionAmount()).isNull();
        assertThat(template.getPrincipalPortion()).isNull();
        assertThat(template.getFeeChargesPortion()).isNull();
        assertThat(template.getPenaltyChargesPortion()).isNull();
        // The rest of the template still resolves, so the caller gets a usable response.
        assertThat(template.getWcLoanId()).isEqualTo(LOAN_ID);
        assertThat(template.getCurrency().getCode()).isEqualTo("EUR");
    }

    private void givenLoanWithBalance(final BigDecimal principal, final BigDecimal principalPaid, final BigDecimal fee,
            final BigDecimal feePaid, final BigDecimal penalty, final BigDecimal penaltyPaid) {
        final WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        final WorkingCapitalLoanBalance balance = WorkingCapitalLoanBalance.createFor(loan);
        balance.setPrincipal(principal);
        balance.setPrincipalPaid(principalPaid);
        balance.setFee(fee);
        balance.setFeePaid(feePaid);
        balance.setPenalty(penalty);
        balance.setPenaltyPaid(penaltyPaid);

        final WorkingCapitalLoanProduct product = mock(WorkingCapitalLoanProduct.class);
        when(product.getCurrency()).thenReturn(new MonetaryCurrency("EUR", 2, null));
        when(loan.getLoanProduct()).thenReturn(product);
        when(loan.getBalance()).thenReturn(balance);
        when(workingCapitalLoanRepository.findByIdWithFullDetails(LOAN_ID)).thenReturn(Optional.of(loan));
    }

    private static BigDecimal amount(final String value) {
        return new BigDecimal(value);
    }
}
