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
package org.apache.fineract.portfolio.loanaccount.data;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class AccrualPeriodsData {

    private final MonetaryCurrency currency;
    private final List<AccrualPeriodData> periods = new ArrayList<>();

    public AccrualPeriodsData addPeriod(AccrualPeriodData period) {
        periods.add(period);
        return this;
    }

    public static AccrualPeriodsData create(@NotNull List<LoanRepaymentScheduleInstallment> installments, Integer firstInstallmentNumber,
            MonetaryCurrency currency) {
        AccrualPeriodsData accrualPeriods = new AccrualPeriodsData(currency);
        for (LoanRepaymentScheduleInstallment installment : installments) {
            Integer installmentNumber = installment.getInstallmentNumber();
            boolean isFirst = installmentNumber.equals(firstInstallmentNumber);
            accrualPeriods
                    .addPeriod(new AccrualPeriodData(installmentNumber, isFirst, installment.getFromDate(), installment.getDueDate()));
        }
        return accrualPeriods;
    }

    public AccrualPeriodData getPeriodByInstallmentNumber(Integer installmentNumber) {
        return installmentNumber == null ? null
                : periods.stream().filter(p -> installmentNumber.equals(p.getInstallmentNumber())).findFirst().orElse(null);
    }

    public Integer getFirstInstallmentNumber() {
        return periods.stream().filter(AccrualPeriodData::isFirstPeriod).map(AccrualPeriodData::getInstallmentNumber).findFirst()
                .orElse(null);
    }
}
