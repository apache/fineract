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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanOverAppliedCalculationType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.springframework.stereotype.Component;

@Component
public final class LoanMaximumAmountCalculator {

    public BigDecimal getOverAppliedMax(Loan loan) {
        final LoanProduct loanProduct = loan.getLoanProduct();
        if (LoanOverAppliedCalculationType.valueOf(loanProduct.getOverAppliedCalculationType().toUpperCase()).isPercentage()) {
            BigDecimal overAppliedNumber = BigDecimal.valueOf(loanProduct.getOverAppliedNumber());
            BigDecimal totalPercentage = BigDecimal.valueOf(1).add(overAppliedNumber.divide(BigDecimal.valueOf(100)));
            return loan.getProposedPrincipal().multiply(totalPercentage);
        } else {
            return loan.getProposedPrincipal().add(BigDecimal.valueOf(loanProduct.getOverAppliedNumber()));
        }
    }
}
