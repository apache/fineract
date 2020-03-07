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

import java.util.List;


public class JournalEntryData {

    private final Long officeId;
    private final String transactionDate;
    private final String comments;
    private final String referenceNumber;
    private final boolean useAccountingRule;
    private final String currencyCode;
    private final List<SingleDebitOrCreditEntryData> debits;
    private final List<SingleDebitOrCreditEntryData> credits;
    private final String officeName;

    public JournalEntryData(final Long officeId, final String transactionDate,
                        final String comments, final List<SingleDebitOrCreditEntryData> credits,
                        final List<SingleDebitOrCreditEntryData> debits,
                        final String referenceNumber,final  boolean useAccountingRule,
                        final String currencyCode,final String officeName) {
        this.officeId = officeId;
        this.transactionDate = transactionDate;
        this.comments = comments;
        this.credits = credits;
        this.debits = debits;
        this.referenceNumber = referenceNumber;
        this.useAccountingRule = useAccountingRule;
        this.currencyCode = currencyCode;
        this.officeName = officeName;
    }

    public Long getOfficeId() {return this.officeId;}

    public List<SingleDebitOrCreditEntryData> getCredits() {return this.credits;}

    public String getTransactionDate() {return this.transactionDate;}

    public String getComments() {return this.comments;}

    public String getReferenceNumber() {return this.referenceNumber;}

    public boolean isUseAccountingRule() {return this.useAccountingRule;}

    public String getCurrencyCode() {return this.currencyCode;}

    public List<SingleDebitOrCreditEntryData> getDebits() {return this.debits;}

    public String getOfficeName() {return this.officeName;}

}
