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
package org.apache.fineract.portfolio.loanaccount.guarantor.data;

import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.savings.data.DepositAccountOnHoldTransactionData;

public class GuarantorTransactionData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final DepositAccountOnHoldTransactionData onHoldTransactionData;
    @SuppressWarnings("unused")
    private final LoanTransactionData loanTransactionData;
    @SuppressWarnings("unused")
    private final boolean reversed;

    private GuarantorTransactionData(final Long id, final DepositAccountOnHoldTransactionData onHoldTransactionData,
            final LoanTransactionData loanTransactionData, final boolean reversed) {

        this.id = id;
        this.onHoldTransactionData = onHoldTransactionData;
        this.loanTransactionData = loanTransactionData;
        this.reversed = reversed;
    }

    public static GuarantorTransactionData instance(final Long id, final DepositAccountOnHoldTransactionData onHoldTransactionData,
            final LoanTransactionData loanTransactionData, final boolean reversed) {
        return new GuarantorTransactionData(id, onHoldTransactionData, loanTransactionData, reversed);
    }

}
