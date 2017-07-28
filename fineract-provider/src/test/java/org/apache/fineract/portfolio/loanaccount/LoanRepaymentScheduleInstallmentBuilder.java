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
package org.apache.fineract.portfolio.loanaccount;

import java.util.Set;

import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.joda.time.LocalDate;

public class LoanRepaymentScheduleInstallmentBuilder {

    private final Loan loan = null;
    private Integer installmentNumber = Integer.valueOf(1);
    private final LocalDate fromDate = LocalDate.now();
    private LocalDate dueDate = LocalDate.now();
    private final LocalDate latestTransactionDate = LocalDate.now();
    private MonetaryCurrency currencyDetail = new MonetaryCurrencyBuilder().build();
    private Money principal = new MoneyBuilder().build();
    private Money interest = new MoneyBuilder().build();
    private final Money feeCharges = new MoneyBuilder().build();
    private final Money penaltyCharges = new MoneyBuilder().build();
    private boolean completed = false;
    private boolean recalculatedInterestComponent = false;

    public LoanRepaymentScheduleInstallmentBuilder(final MonetaryCurrency currencyDetail) {
        this.currencyDetail = currencyDetail;
        this.principal = new MoneyBuilder().with(currencyDetail).build();
        this.interest = new MoneyBuilder().with(currencyDetail).build();
    }

    public LoanRepaymentScheduleInstallment build() {
        final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = null;
        final LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(this.loan, this.installmentNumber,
                this.fromDate, this.dueDate, this.principal.getAmount(), this.interest.getAmount(), this.feeCharges.getAmount(),
                this.penaltyCharges.getAmount(), this.recalculatedInterestComponent, compoundingDetails);
        if (this.completed) {
            installment.payPrincipalComponent(this.latestTransactionDate, this.principal);
            installment.payInterestComponent(this.latestTransactionDate, this.interest);
        }
        return installment;
    }

    public LoanRepaymentScheduleInstallmentBuilder withPrincipal(final String withPrincipal) {
        this.principal = new MoneyBuilder().with(this.currencyDetail).with(withPrincipal).build();
        return this;
    }

    public LoanRepaymentScheduleInstallmentBuilder withInterest(final String withInterest) {
        this.interest = new MoneyBuilder().with(this.currencyDetail).with(withInterest).build();
        return this;
    }

    public LoanRepaymentScheduleInstallmentBuilder withDueDate(final LocalDate withDueDate) {
        this.dueDate = withDueDate;
        return this;
    }

    public LoanRepaymentScheduleInstallmentBuilder completed() {
        this.completed = true;
        return this;
    }

    public LoanRepaymentScheduleInstallmentBuilder withInstallmentNumber(final int withInstallmentNumber) {
        this.installmentNumber = withInstallmentNumber;
        return this;
    }
}