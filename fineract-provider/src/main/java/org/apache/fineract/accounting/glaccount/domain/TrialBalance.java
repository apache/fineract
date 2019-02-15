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
 
package org.apache.fineract.accounting.glaccount.domain;


import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;


import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "m_trial_balance")
public class TrialBalance extends AbstractPersistableCustom<Long> {

    @Column(name = "office_id", nullable = false)
    private Long officeId;

    @Column(name = "account_id", nullable = false)
    private Long glAccountId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "entry_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date entryDate;

    @Column(name = "created_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date transactionDate;

    @Column(name = "closing_balance", nullable = false)
    private BigDecimal closingBalance;

    public static TrialBalance getInstance(final Long officeId, final Long glAccountId,
                                           final BigDecimal amount, final Date entryDate, final Date transactionDate) {
        return new TrialBalance(officeId, glAccountId, amount, entryDate, transactionDate);
    }

    private TrialBalance(final Long officeId, final Long glAccountId,
                         final BigDecimal amount, final Date entryDate, final Date transactionDate) {
        this.officeId = officeId;
        this.glAccountId = glAccountId;
        this.amount = amount;
        this.entryDate = entryDate;
        this.transactionDate = transactionDate;
    }

    protected TrialBalance() {

    }

    public Long getOfficeId() {
        return officeId;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setClosingBalance(final BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public Long getGlAccountId() {
        return glAccountId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(getClass())) return false;
        TrialBalance trialBalance = (TrialBalance) obj;
        return trialBalance.getOfficeId().equals(this.getOfficeId())
                && trialBalance.getGlAccountId().equals(this.getGlAccountId())
                && trialBalance.getEntryDate().equals(this.getEntryDate())
                && trialBalance.getTransactionDate().equals(this.getTransactionDate());
    }



}