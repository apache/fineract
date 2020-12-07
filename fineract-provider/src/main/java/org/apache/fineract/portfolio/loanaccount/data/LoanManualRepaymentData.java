/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fineract.portfolio.loanaccount.data;

import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;

import java.math.BigDecimal;

public class LoanManualRepaymentData {

    private final Money principalPortion;
    private final Money interestPortion;
    private final Money feeChargesPortion;
    private final Money penaltyChargesPortion;

    public LoanManualRepaymentData(MonetaryCurrency currency, BigDecimal principalPortion, BigDecimal interestPortion,
            BigDecimal feeChargesPortion, BigDecimal penaltyChargesPortion) {
        this.principalPortion = Money.of(currency, principalPortion);
        this.interestPortion = Money.of(currency, interestPortion);
        this.feeChargesPortion = Money.of(currency, feeChargesPortion);
        this.penaltyChargesPortion = Money.of(currency, penaltyChargesPortion);
    }

    public Money getPenaltyChargesPortion() {
        return penaltyChargesPortion;
    }

    public Money getFeeChargesPortion() {
        return feeChargesPortion;
    }

    public Money getInterestPortion() {
        return interestPortion;
    }

    public Money getPrincipalPortion() {
        return principalPortion;
    }
}
