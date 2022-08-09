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
package org.apache.fineract.accounting.journalentry.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

/**
 * Immutable object representing a General Ledger Account
 *
 * Note: no getter/setters required as google will produce json from fields of object.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class JournalEntryData {

    private Long id;
    private Long officeId;
    @SuppressWarnings("unused")
    private String officeName;
    @SuppressWarnings("unused")
    private String glAccountName;
    private Long glAccountId;
    @SuppressWarnings("unused")
    private String glAccountCode;
    private EnumOptionData glAccountType;
    @SuppressWarnings("unused")
    private LocalDate transactionDate;
    private EnumOptionData entryType;
    private BigDecimal amount;
    @SuppressWarnings("unused")
    private CurrencyData currency;
    private String transactionId;
    @SuppressWarnings("unused")
    private Boolean manualEntry;
    @SuppressWarnings("unused")
    private EnumOptionData entityType;
    @SuppressWarnings("unused")
    private Long entityId;
    @SuppressWarnings("unused")
    private Long createdByUserId;
    @SuppressWarnings("unused")
    private LocalDate createdDate;
    @SuppressWarnings("unused")
    private String createdByUserName;
    @SuppressWarnings("unused")
    private String comments;
    @SuppressWarnings("unused")
    private Boolean reversed;
    @SuppressWarnings("unused")
    private String referenceNumber;
    @SuppressWarnings("unused")
    private BigDecimal officeRunningBalance;
    @SuppressWarnings("unused")
    private BigDecimal organizationRunningBalance;
    @SuppressWarnings("unused")
    private Boolean runningBalanceComputed;

    @SuppressWarnings("unused")
    private TransactionDetailData transactionDetails;
    @SuppressWarnings("unused")
    private LocalDate submittedOnDate;

    // import fields
    private transient Integer rowIndex;
    private String dateFormat;
    private String locale;
    private List<CreditDebit> credits;
    private List<CreditDebit> debits;
    private Long paymentTypeId;
    private String currencyCode;
    private String accountNumber;
    private String checkNumber;
    private String routingCode;
    private String receiptNumber;
    private String bankNumber;
    private transient Long savingTransactionId;

    public static JournalEntryData importInstance(Long officeId, LocalDate transactionDate, String currencyCode, Long paymentTypeId,
            Integer rowIndex, List<CreditDebit> credits, List<CreditDebit> debits, String accountNumber, String checkNumber,
            String routingCode, String receiptNumber, String bankNumber, String comments, String locale, String dateFormat) {
        return new JournalEntryData().setOfficeId(officeId).setTransactionDate(transactionDate).setCurrencyCode(currencyCode)
                .setPaymentTypeId(paymentTypeId).setRowIndex(rowIndex).setCredits(credits).setDebits(debits).setAccountNumber(accountNumber)
                .setCheckNumber(checkNumber).setRoutingCode(routingCode).setReceiptNumber(receiptNumber).setBankNumber(bankNumber)
                .setComments(comments).setLocale(locale).setDateFormat(dateFormat);
    }

    public static JournalEntryData importInstance1(Long officeId, LocalDate transactionDate, String currencyCode, List<CreditDebit> credits,
            List<CreditDebit> debits, String locale, String dateFormat) {
        return new JournalEntryData().setOfficeId(officeId).setTransactionDate(transactionDate).setCurrencyCode(currencyCode)
                .setCredits(credits).setDebits(debits).setLocale(locale).setDateFormat(dateFormat);
    }

    public void addDebits(CreditDebit debit) {
        this.debits.add(debit);
    }

    public void addCredits(CreditDebit credit) {
        this.credits.add(credit);
    }

    public static JournalEntryData fromGLAccountData(final GLAccountData glAccountData) {

        final String glAccountName = glAccountData.getName();
        final Long glAccountId = glAccountData.getId();
        final String glAccountCode = glAccountData.getGlCode();
        final EnumOptionData glAccountClassification = glAccountData.getType();

        return new JournalEntryData().setGlAccountName(glAccountName).setGlAccountId(glAccountId).setGlAccountCode(glAccountCode)
                .setGlAccountType(glAccountClassification);
    }
}
