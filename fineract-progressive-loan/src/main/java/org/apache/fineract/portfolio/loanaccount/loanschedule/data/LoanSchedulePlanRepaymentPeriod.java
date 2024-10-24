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
import lombok.Data;

@Data
public final class LoanSchedulePlanRepaymentPeriod implements LoanSchedulePlanPeriod {

    private final int periodNumber;
    private final LocalDate periodFromDate;
    private final LocalDate periodDueDate;
    private final BigDecimal principalAmount;
    private final BigDecimal interestAmount;
    private final BigDecimal feeAmount;
    private final BigDecimal penaltyAmount;
    private final BigDecimal totalDueAmount;
    private final BigDecimal outstandingLoanBalance;

    @Override
    public Integer periodNumber() {
        return periodNumber;
    }

    @Override
    public LocalDate periodFromDate() {
        return periodFromDate;
    }

    @Override
    public LocalDate periodDueDate() {
        return periodDueDate;
    }
}
