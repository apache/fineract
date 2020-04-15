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

package org.apache.fineract.accounting.closure.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.office.domain.Office;


@Entity
@Table(name = "acc_income_and_expense_bookings",uniqueConstraints = { @UniqueConstraint(columnNames = { "journal_entry_transaction_id" }, name = "journal_entry_transaction_id") })
public class IncomeAndExpenseBooking extends AbstractPersistableCustom<Long>  {

    @ManyToOne
    @JoinColumn(name = "gl_closure_id", nullable = false)
    private GLClosure glClosure;
    @Column(name = "journal_entry_transaction_id",nullable = false)
    private String transactionId;
    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;
    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;


    public IncomeAndExpenseBooking(final GLClosure glClosure, final String transactionId,
                                   final Office office, final boolean reversed) {
        this.glClosure = glClosure;
        this.transactionId = transactionId;
        this.office = office;
        this.reversed = reversed;
    }

    protected IncomeAndExpenseBooking() {
    }

    public static IncomeAndExpenseBooking createNew(final GLClosure glClosure, final String transactionId, final Office office, final boolean reversed){
        return new IncomeAndExpenseBooking(glClosure,transactionId,office,reversed);
    }

    public GLClosure getGlClosure() {return this.glClosure;}

    public String getTransactionId() {return this.transactionId;}

    public Office getOffice() {return this.office;}

    public boolean isReversed() {return this.reversed;}

    public void updateReversed(boolean reversed) {this.reversed = reversed;}
}