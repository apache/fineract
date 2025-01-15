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
package org.apache.fineract.portfolio.loanproduct.calc;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.fineract.organisation.monetary.domain.Money;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmiChangeOperation {

    public enum Action {
        DISBURSEMENT, INTEREST_RATE_CHANGE
    }

    private final Action action;
    private final LocalDate submittedOnDate;

    private final Money amount;
    private final BigDecimal interestRate;

    public static EmiChangeOperation disburse(final LocalDate disbursementDueDate, final Money disbursedAmount) {
        return new EmiChangeOperation(EmiChangeOperation.Action.DISBURSEMENT, disbursementDueDate, disbursedAmount, null);
    }

    public static EmiChangeOperation changeInterestRate(final LocalDate newInterestSubmittedOnDate, final BigDecimal newInterestRate) {
        return new EmiChangeOperation(EmiChangeOperation.Action.INTEREST_RATE_CHANGE, newInterestSubmittedOnDate, null, newInterestRate);
    }
}
