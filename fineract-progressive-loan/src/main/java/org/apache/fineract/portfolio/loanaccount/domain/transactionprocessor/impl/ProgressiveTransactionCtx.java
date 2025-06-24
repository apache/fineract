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
package org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;

@Getter
public class ProgressiveTransactionCtx extends TransactionCtx {

    private final ProgressiveLoanInterestScheduleModel model;
    private List<LoanTransaction> alreadyProcessedTransactions = new ArrayList<>();
    @Setter
    private Money sumOfInterestRefundAmount;
    @Setter
    private boolean isChargedOff = false;
    @Setter
    private boolean isContractTerminated = false;
    @Setter
    private boolean isPrepayAttempt = false;
    private List<LoanRepaymentScheduleInstallment> skipRepaymentScheduleInstallments = new ArrayList<>();

    public ProgressiveTransactionCtx(MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments,
            Set<LoanCharge> charges, MoneyHolder overpaymentHolder, ChangedTransactionDetail changedTransactionDetail,
            ProgressiveLoanInterestScheduleModel model) {
        this(currency, installments, charges, overpaymentHolder, changedTransactionDetail, model, Money.zero(currency));
    }

    public ProgressiveTransactionCtx(MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments,
            Set<LoanCharge> charges, MoneyHolder overpaymentHolder, ChangedTransactionDetail changedTransactionDetail,
            ProgressiveLoanInterestScheduleModel model, Money sumOfInterestRefundAmount) {
        super(currency, installments, charges, overpaymentHolder, changedTransactionDetail);
        this.sumOfInterestRefundAmount = sumOfInterestRefundAmount;
        this.model = model;
    }
}
