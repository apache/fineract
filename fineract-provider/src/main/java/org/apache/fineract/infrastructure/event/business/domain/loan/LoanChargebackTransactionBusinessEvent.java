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
package org.apache.fineract.infrastructure.event.business.domain.loan;

import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;

public class LoanChargebackTransactionBusinessEvent extends LoanTransactionBusinessEvent {

    public static final String LOAN_CHARGEBACK_TRANSACTION_BUSINESS_EVENT = "LoanChargebackTransactionBusinessEvent";
    public static final String LOAN_CHARGEBACK_TRANSACTION_PERMISSION = "LOAN_CHARGEBACK";
    public static final String LOAN_CHARGEBACK_TRANSACTION_OBJECT_TYPE = "loanTransaction";
    public static final String LOAN_CHARGEBACK_TRANSACTION_EVENT_TYPE = "loanTransactionChargeback";
    public static final String LOAN_CHARGEBACK_TRANSACTION_NOTIFICATION = "Loan Transaction has been chargeback";

    public LoanChargebackTransactionBusinessEvent(LoanTransaction transactionToChargeback) {
        super(transactionToChargeback);
    }

    @Override
    public String getType() {
        return LOAN_CHARGEBACK_TRANSACTION_BUSINESS_EVENT;
    }
}
