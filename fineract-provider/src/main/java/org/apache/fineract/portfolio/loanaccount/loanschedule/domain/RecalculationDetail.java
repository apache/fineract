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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.joda.time.LocalDate;

public class RecalculationDetail {

    private LocalDate transactionDate;
    private boolean isProcessed;
    private LoanTransaction transaction;

    public RecalculationDetail(final LocalDate transactionDate, final LoanTransaction transaction) {
        this.transactionDate = transactionDate;
        this.transaction = transaction;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

    public LoanTransaction getTransaction() {
        return this.transaction;
    }

    public boolean isProcessed() {
        return this.isProcessed;
    }

    public void setProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }
}
