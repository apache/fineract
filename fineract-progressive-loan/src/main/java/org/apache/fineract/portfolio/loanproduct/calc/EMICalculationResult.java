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
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.domain.Money;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class EMICalculationResult {

    @Getter
    private final Money equalMonthlyInstallmentValue;
    private final List<BigDecimal> repaymentPeriodRateFactorMinus1List;

    private int counter = 0;

    public BigDecimal getNextRepaymentPeriodRateFactorMinus1() {
        return counter < repaymentPeriodRateFactorMinus1List.size() ? repaymentPeriodRateFactorMinus1List.get(counter++) : BigDecimal.ZERO;
    }

    public void reset() {
        counter = 0;
    }
}
