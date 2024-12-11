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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class AccrualPeriodData {

    private final Integer installmentNumber;
    private final boolean isFirstPeriod;
    private final LocalDate startDate;
    private final LocalDate dueDate;
    private Money interestAmount;
    private Money interestAccruable;
    private Money interestAccrued;
    private Money unrecognizedWaive;
    private Money transactionAccrued;
    private final List<AccrualChargeData> charges = new ArrayList<>();

    public AccrualPeriodData addCharge(AccrualChargeData charge) {
        charges.add(charge);
        return this;
    }

    public Money getChargeAmount() {
        return charges.stream().map(AccrualChargeData::getChargeAmount).reduce(null, MathUtil::plus);
    }

    public Money getFeeAmount() {
        return charges.stream().filter(charge -> !charge.isPenalty()).map(AccrualChargeData::getChargeAmount).reduce(null, MathUtil::plus);
    }

    public Money getPenaltyAmount() {
        return charges.stream().filter(AccrualChargeData::isPenalty).map(AccrualChargeData::getChargeAmount).reduce(null, MathUtil::plus);
    }

    public Money getChargeAccrued() {
        return charges.stream().map(AccrualChargeData::getChargeAccrued).reduce(null, MathUtil::plus);
    }

    public Money getFeeAccrued() {
        return charges.stream().filter(charge -> !charge.isPenalty()).map(AccrualChargeData::getChargeAccrued).reduce(null, MathUtil::plus);
    }

    public Money getFeeTransactionAccrued() {
        return charges.stream().filter(charge -> !charge.isPenalty()).map(AccrualChargeData::getTransactionAccrued).reduce(null,
                MathUtil::plus);
    }

    public Money getPenaltyAccrued() {
        return charges.stream().filter(AccrualChargeData::isPenalty).map(AccrualChargeData::getChargeAccrued).reduce(null, MathUtil::plus);
    }

    public Money getPenaltyTransactionAccrued() {
        return charges.stream().filter(AccrualChargeData::isPenalty).map(AccrualChargeData::getTransactionAccrued).reduce(null,
                MathUtil::plus);
    }

    public Money getChargeAccruable() {
        return charges.stream().map(AccrualChargeData::getChargeAccruable).reduce(null, MathUtil::plus);
    }

    public Money getFeeAccruable() {
        return charges.stream().filter(charge -> !charge.isPenalty()).map(AccrualChargeData::getChargeAccruable).reduce(null,
                MathUtil::plus);
    }

    public Money getPenaltyAccruable() {
        return charges.stream().filter(AccrualChargeData::isPenalty).map(AccrualChargeData::getChargeAccruable).reduce(null,
                MathUtil::plus);
    }
}
