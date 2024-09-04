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
package org.apache.fineract.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Data
public class ProgressiveLoanInterestRepaymentInterestPeriod implements Comparable<ProgressiveLoanInterestRepaymentInterestPeriod> {

    private LocalDate fromDate;
    private LocalDate dueDate;

    private BigDecimal rateFactorMinus1;

    private Money disbursedAmount;
    private Money correctionAmount;
    private Money interestDue;

    public ProgressiveLoanInterestRepaymentInterestPeriod(final ProgressiveLoanInterestRepaymentInterestPeriod period) {
        this(period.fromDate, period.dueDate, period.rateFactorMinus1, period.disbursedAmount, period.correctionAmount, period.interestDue);
    }

    @Override
    public int compareTo(@NotNull ProgressiveLoanInterestRepaymentInterestPeriod o) {
        return dueDate.compareTo(o.dueDate);
    }

    public void addDisbursedAmount(final Money outstandingBalance) {
        if (outstandingBalance != null && !outstandingBalance.isZero()) {
            this.disbursedAmount = this.disbursedAmount.add(outstandingBalance);
        }
    }

    public void addCorrectionAmount(final Money correctionAmount) {
        if (correctionAmount != null && !correctionAmount.isZero()) {
            this.correctionAmount = this.correctionAmount.add(correctionAmount);
        }
    }
}
