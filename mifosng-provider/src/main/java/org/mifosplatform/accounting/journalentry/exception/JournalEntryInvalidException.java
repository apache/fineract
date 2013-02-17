/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.exception;

import java.util.Date;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a GL Journal Entry is Invalid
 */
public class JournalEntryInvalidException extends AbstractPlatformDomainRuleException {

    /*** enum of reasons for invalid Journal Entry **/
    public static enum GL_JOURNAL_ENTRY_INVALID_REASON {
        FUTURE_DATE, ACCOUNTING_CLOSED, NO_DEBITS_OR_CREDITS, DEBIT_CREDIT_SUM_MISMATCH, DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY, GL_ACCOUNT_DISABLED, GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED;

        public String errorMessage() {
            if (name().toString().equalsIgnoreCase("FUTURE_DATE")) {
                return "The journal entry cannot be made for a future date";
            } else if (name().toString().equalsIgnoreCase("ACCOUNTING_CLOSED")) {
                return "Journal entry cannot be made prior to last account closing date for the branch";
            } else if (name().toString().equalsIgnoreCase("NO_DEBITS_OR_CREDITS")) {
                return "Journal Entry must have atleast one Debit and one Credit";
            } else if (name().toString().equalsIgnoreCase("DEBIT_CREDIT_SUM_MISMATCH")) {
                return "Sum of All Debits must equal the sum of all Credits for a Journal Entry";
            } else if (name().toString().equalsIgnoreCase("DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY")) {
                return "Both account and amount must be specified for all Debits and Credits";
            } else if (name().toString().equalsIgnoreCase("GL_ACCOUNT_DISABLED")) {
                return "Target account has been disabled";
            } else if (name().toString().equalsIgnoreCase("GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED")) { return "Target account does not allow maual adjustments"; }
            return name().toString();
        }

        public String errorCode() {
            if (name().toString().equalsIgnoreCase("FUTURE_DATE")) {
                return "error.msg.glJournalEntry.invalid.future.date";
            } else if (name().toString().equalsIgnoreCase("ACCOUNTING_CLOSED")) {
                return "error.msg.glJournalEntry.invalid.accounting.closed";
            } else if (name().toString().equalsIgnoreCase("NO_DEBITS_OR_CREDITS")) {
                return "error.msg.glJournalEntry.invalid.no.debits.or.credits";
            } else if (name().toString().equalsIgnoreCase("DEBIT_CREDIT_SUM_MISMATCH")) {
                return "error.msg.glJournalEntry.invalid.mismatch.debits.credits";
            } else if (name().toString().equalsIgnoreCase("DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY")) {
                return "error.msg.glJournalEntry.invalid.empty.account.or.amount";
            } else if (name().toString().equalsIgnoreCase("GL_ACCOUNT_DISABLED")) {
                return "error.msg.glJournalEntry.invalid.account.disabled";
            } else if (name().toString().equalsIgnoreCase("GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED")) { return "error.msg.glJournalEntry.invalid.account.manual.adjustments.not.permitted"; }
            return name().toString();
        }
    }

    public JournalEntryInvalidException(final GL_JOURNAL_ENTRY_INVALID_REASON reason, final Date date, final String accountName,
            final String accountGLCode) {
        super(reason.errorCode(), reason.errorMessage(), date, accountName, accountGLCode);
    }
}