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

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;

@Data
@Accessors(chain = true, fluent = true)
public class OutstandingAmountsDTO {

    private Money principal;
    private Money interest;
    private Money feeCharges;
    private Money penaltyCharges;

    public OutstandingAmountsDTO(MonetaryCurrency currency) {
        this.principal = Money.zero(currency);
        this.interest = Money.zero(currency);
        this.feeCharges = Money.zero(currency);
        this.penaltyCharges = Money.zero(currency);
    }

    public Money getTotalOutstanding() {
        return principal() //
                .plus(interest()) //
                .plus(feeCharges()) //
                .plus(penaltyCharges());
    }

    public void plusPrincipal(Money principal) {
        this.principal = this.principal.plus(principal);
    }

    public void plusInterest(Money interest) {
        this.interest = this.interest.plus(interest);
    }

    public void plusFeeCharges(Money feeCharges) {
        this.feeCharges = this.feeCharges.plus(feeCharges);
    }

    public void plusPenaltyCharges(Money penaltyCharges) {
        this.penaltyCharges = this.penaltyCharges.plus(penaltyCharges);
    }

}
