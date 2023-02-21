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
import lombok.Data;

@Data
public class LoanRepaymentScheduleData {

    private final Integer id;
    private final String fromDate;
    private final String dueDate;
    private final Integer installmentNo;
    private final BigDecimal principalAmount;
    private final BigDecimal principalPaid;
    private final BigDecimal principalWrittenOff;

    private final BigDecimal interestAmount;
    private final BigDecimal interestPaid;
    private final BigDecimal interestWrittenOff;
    private final BigDecimal interestWaived;

    private final BigDecimal feeChargesAmount;
    private final BigDecimal feePaid;
    private final BigDecimal feeChargesWrittenOff;
    private final BigDecimal feeChargeWaived;

    private final BigDecimal penaltyChargesAmount;
    private final BigDecimal penaltyChargePaid;
    private final BigDecimal penaltyChargesWrittenOff;
    private final BigDecimal penaltyChargesWaived;
    private final Boolean completedDerived;
    private final String obligationMetOnDate;

    public LoanRepaymentScheduleData(Integer id, String fromDate, String dueDate, Integer installmentNo, BigDecimal principalAmount,
            BigDecimal principalPaid, BigDecimal principalWrittenOff, BigDecimal interestAmount, BigDecimal interestPaid,
            BigDecimal interestWrittenOff, BigDecimal interestWaived, BigDecimal feeChargesAmount, BigDecimal feePaid,
            BigDecimal feeChargesWrittenOff, BigDecimal feeChargeWaived, BigDecimal penaltyChargesAmount, BigDecimal penaltyChargePaid,
            BigDecimal penaltyChargesWrittenOff, BigDecimal penaltyChargesWaived, Boolean completedDerived, String obligationMetOnDate) {
        this.id = id;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.installmentNo = installmentNo;
        this.principalAmount = principalAmount;
        this.principalPaid = principalPaid;
        this.principalWrittenOff = principalWrittenOff;
        this.interestAmount = interestAmount;
        this.interestPaid = interestPaid;
        this.interestWrittenOff = interestWrittenOff;
        this.interestWaived = interestWaived;
        this.feeChargesAmount = feeChargesAmount;
        this.feePaid = feePaid;
        this.feeChargesWrittenOff = feeChargesWrittenOff;
        this.feeChargeWaived = feeChargeWaived;
        this.penaltyChargesAmount = penaltyChargesAmount;
        this.penaltyChargePaid = penaltyChargePaid;
        this.penaltyChargesWrittenOff = penaltyChargesWrittenOff;
        this.penaltyChargesWaived = penaltyChargesWaived;
        this.completedDerived = completedDerived;
        this.obligationMetOnDate = obligationMetOnDate;
    }
}
