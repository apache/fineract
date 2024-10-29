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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.PayableDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreClosureInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PrepaymentCalculationTest {

    private static final MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);
    private static final MathContext mc = new MathContext(12, RoundingMode.HALF_EVEN);
    private static final MonetaryCurrency monetaryCurrency = MonetaryCurrency
            .fromApplicationCurrency(new ApplicationCurrency("USD", "USD", 2, 1, "USD", "$"));

    @Mock
    private LoanProductRelatedDetail loanProductRelatedDetail;

    @Mock
    private LoanApplicationTerms loanApplicationTerms;

    @Mock
    private AdvancedPaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor;

    @Mock
    private EMICalculator emiCalculator;

    @InjectMocks
    private ProgressiveLoanScheduleGenerator progressiveLoanScheduleGenerator;

    private Loan loan;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.UP));
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.UP);

        loan = Mockito.mock(Loan.class);
        when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);
        when(loan.getDisbursementDate()).thenReturn(LocalDate.of(2022, 9, 7));
        when(loan.getRepaymentScheduleInstallments()).thenReturn(createRepaymentScheduleInstallments());

        when(loanApplicationTerms.getPreClosureInterestCalculationStrategy())
                .thenReturn(LoanPreClosureInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE);

        when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(BigDecimal.valueOf(7.0));

        List<RepaymentPeriod> repaymentPeriods = createMockRepaymentPeriods();
        ProgressiveLoanInterestScheduleModel scheduleModel = new ProgressiveLoanInterestScheduleModel(repaymentPeriods,
                loanProductRelatedDetail, 100, mc);

        when(loanRepaymentScheduleTransactionProcessor.reprocessProgressiveLoanTransactions(Mockito.any(), Mockito.anyList(), Mockito.any(),
                Mockito.anyList(), Mockito.anySet())).thenReturn(org.apache.commons.lang3.tuple.Pair.of(null, scheduleModel));

        PayableDetails payableDetails = new PayableDetails(Money.of(monetaryCurrency, BigDecimal.valueOf(200)),
                Money.of(monetaryCurrency, BigDecimal.valueOf(500)), Money.of(monetaryCurrency, BigDecimal.valueOf(0)),
                Money.of(monetaryCurrency, BigDecimal.valueOf(1000)));

        when(emiCalculator.getPayableDetails(Mockito.any(ProgressiveLoanInterestScheduleModel.class), Mockito.any(LocalDate.class),
                Mockito.any(LocalDate.class))).thenReturn(payableDetails);
    }

    @AfterAll
    public static void tearDown() {
        moneyHelper.close();
    }

    @Test
    public void testCalculatePrepaymentAmount() {
        LocalDate prepaymentDate = LocalDate.of(2023, 6, 1);

        OutstandingAmountsDTO result = progressiveLoanScheduleGenerator.calculatePrepaymentAmount(monetaryCurrency, prepaymentDate,
                loanApplicationTerms, mc, loan, null, loanRepaymentScheduleTransactionProcessor);

        assertEquals("1000.00", result.principal().getAmount().toString());
        assertEquals("15.00", result.interest().getAmount().toString());
    }

    private List<LoanRepaymentScheduleInstallment> createRepaymentScheduleInstallments() {
        LoanRepaymentScheduleInstallment installment1 = new LoanRepaymentScheduleInstallment(loan, 1, LocalDate.of(2022, 10, 1),
                LocalDate.of(2022, 11, 1), BigDecimal.valueOf(500), BigDecimal.valueOf(10), BigDecimal.ZERO, BigDecimal.ZERO, false, null);

        LoanRepaymentScheduleInstallment installment2 = new LoanRepaymentScheduleInstallment(loan, 2, LocalDate.of(2022, 11, 2),
                LocalDate.of(2022, 12, 1), BigDecimal.valueOf(500), BigDecimal.valueOf(5), BigDecimal.ZERO, BigDecimal.ZERO, false, null);

        return List.of(installment1, installment2);
    }

    private List<RepaymentPeriod> createMockRepaymentPeriods() {
        RepaymentPeriod period1 = Mockito.mock(RepaymentPeriod.class);
        when(period1.getFromDate()).thenReturn(LocalDate.of(2022, 10, 1));
        when(period1.getDueDate()).thenReturn(LocalDate.of(2022, 11, 1));

        RepaymentPeriod period2 = Mockito.mock(RepaymentPeriod.class);
        when(period2.getFromDate()).thenReturn(LocalDate.of(2022, 11, 2));
        when(period2.getDueDate()).thenReturn(LocalDate.of(2022, 12, 1));

        return List.of(period1, period2);
    }
}
