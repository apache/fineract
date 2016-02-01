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
package org.apache.fineract.portfolio.collectionsheet.command;

import org.joda.time.LocalDate;

/**
 * Immutable command for loan bulk repayment.
 */
public class CollectionSheetBulkRepaymentCommand {

    private final String note;
    private final LocalDate transactionDate;
    private final SingleRepaymentCommand[] repaymentTransactions;

    public CollectionSheetBulkRepaymentCommand(final String note, final LocalDate transactionDate,
            final SingleRepaymentCommand[] repaymentTransactions) {
        this.note = note;
        this.transactionDate = transactionDate;
        this.repaymentTransactions = repaymentTransactions;
    }

    public String getNote() {
        return this.note;
    }

    public SingleRepaymentCommand[] getLoanTransactions() {
        return this.repaymentTransactions;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

}