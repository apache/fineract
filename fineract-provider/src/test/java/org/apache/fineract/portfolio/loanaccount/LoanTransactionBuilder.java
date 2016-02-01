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
package org.apache.fineract.portfolio.loanaccount;

import org.apache.fineract.organisation.monetary.domain.Money;
import org.joda.time.LocalDate;

public class LoanTransactionBuilder {

    @SuppressWarnings("unused")
    private Money transactionAmount = new MoneyBuilder().build();
    @SuppressWarnings("unused")
    private LocalDate transactionDate = LocalDate.now();
    @SuppressWarnings("unused")
    private boolean repayment = false;

    /**
     * public LoanTransaction build() {
     * 
     * LoanTransaction transaction = null;
     * 
     * if (repayment) { transaction =
     * LoanTransaction.repayment(transactionAmount, transactionDate); }
     * 
     * return transaction; }
     **/

    public LoanTransactionBuilder with(final Money newAmount) {
        this.transactionAmount = newAmount;
        return this;
    }

    public LoanTransactionBuilder with(final LocalDate withTransactionDate) {
        this.transactionDate = withTransactionDate;
        return this;
    }

    public LoanTransactionBuilder repayment() {
        this.repayment = true;
        return this;
    }
}
