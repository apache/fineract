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
package org.apache.fineract.accounting.closure.data;

import java.math.BigDecimal;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.joda.time.LocalDate;


public class IncomeAndExpenseJournalEntryData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long accountId;
    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final LocalDate entryDate;
    @SuppressWarnings("unused")
    private final boolean reversed;
    @SuppressWarnings("unused")
    private final boolean isRunningBalanceCalculated;
    @SuppressWarnings("unused")
    private final String comments;
    @SuppressWarnings("unused")
    private final BigDecimal officeRunningBalance;
    @SuppressWarnings("unused")
    private final BigDecimal organizationRunningBalance;
    @SuppressWarnings("unused")
    private final int accountTypeId;
    @SuppressWarnings("unused")
    private final int entryTypeId;
    @SuppressWarnings("unused")
    private final String glAccountName;
    @SuppressWarnings("unused")
    private final String officeName;


    public IncomeAndExpenseJournalEntryData(final Long id, final Long accountId, final Long officeId, final LocalDate entryDate, final boolean reversed,
            final boolean isRunningBalanceCalculated, final String comments, final BigDecimal officeRunningBalance,
            final BigDecimal organizationRunningBalance, final int accountTypeId, final int entryTypeId,
            final String glAccountName, final String officeName) {
        this.id = id;
        this.accountId = accountId;
        this.officeId = officeId;
        this.entryDate = entryDate;
        this.reversed = reversed;
        this.isRunningBalanceCalculated = isRunningBalanceCalculated;
        this.comments = comments;
        this.officeRunningBalance = officeRunningBalance;
        this.organizationRunningBalance = organizationRunningBalance;
        this.accountTypeId = accountTypeId;
        this.entryTypeId = entryTypeId;
        this.glAccountName = glAccountName;
        this.officeName = officeName;
    }

    public Long getId() {
        return this.id;
    }

    public int getEntryTypeId() {return this.entryTypeId;}

    public Long getAccountId() {
        return this.accountId;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public LocalDate getEntryDate() {
        return this.entryDate;
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public boolean isRunningBalanceCalculated() {
        return this.isRunningBalanceCalculated;
    }

    public String getComments() {
        return this.comments;
    }

    public BigDecimal getOfficeRunningBalance() {return this.officeRunningBalance;}

    public BigDecimal getOrganizationRunningBalance() {
        return this.organizationRunningBalance;
    }

    public int getAccountTypeId() {
        return this.accountTypeId;
    }

    public boolean isIncomeAccountType(){
       return  (this.accountTypeId == (GLAccountType.INCOME.getValue()));
    }

    public boolean isExpenseAccountType(){
        return (this.accountTypeId == GLAccountType.EXPENSE.getValue());
    }

    public String getGlAccountName() {return this.glAccountName;}

    public String getOfficeName() {return this.officeName;}

}
