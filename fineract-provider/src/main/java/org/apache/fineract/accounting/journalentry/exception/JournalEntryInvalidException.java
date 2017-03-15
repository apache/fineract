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
package org.apache.fineract.accounting.journalentry.exception;

import java.util.Date;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a GL Journal Entry is Invalid
 */
public class JournalEntryInvalidException extends AbstractPlatformDomainRuleException {

    /*** enum of reasons for invalid Journal Entry **/
    public static enum GL_JOURNAL_ENTRY_INVALID_REASON {
        FUTURE_DATE, ACCOUNTING_CLOSED, NO_DEBITS_OR_CREDITS, DEBIT_CREDIT_SUM_MISMATCH_WITH_AMOUNT, DEBIT_CREDIT_SUM_MISMATCH, DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY, GL_ACCOUNT_DISABLED, GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED, INVALID_DEBIT_OR_CREDIT_ACCOUNTS;

        public String errorMessage() {
            if ("FUTURE_DATE".equalsIgnoreCase(name())) {
                return "The journal entry cannot be made for a future date";
            } else if ("ACCOUNTING_CLOSED".equalsIgnoreCase(name())) {
                return "Journal entry cannot be made prior to last account closing date for the branch";
            } else if ("NO_DEBITS_OR_CREDITS".equalsIgnoreCase(name())) {
                return "Journal Entry must have atleast one Debit and one Credit";
            } else if ("DEBIT_CREDIT_SUM_MISMATCH_WITH_AMOUNT".equalsIgnoreCase(name())) {
                return "Sum of All Debits OR Credits must equal the Amount for a Journal Entry";
            } else if ("DEBIT_CREDIT_SUM_MISMATCH".equalsIgnoreCase(name())) {
                return "Sum of All Debits must equal the sum of all Credits for a Journal Entry";
            } else if ("DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY".equalsIgnoreCase(name())) {
                return "Both account and amount must be specified for all Debits and Credits";
            } else if ("GL_ACCOUNT_DISABLED".equalsIgnoreCase(name())) {
                return "Target account has been disabled";
            } else if ("INVALID_DEBIT_OR_CREDIT_ACCOUNTS".equalsIgnoreCase(name())) {
                return "Invalid debit or credit accounts are passed";
			} else if ("GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED".equalsIgnoreCase(name())) {
				return "Target account does not allow maual adjustments";
			}
			return name();
        }

        public String errorCode() {
            if ("FUTURE_DATE".equalsIgnoreCase(name())) {
                return "error.msg.glJournalEntry.invalid.future.date";
            } else if ("ACCOUNTING_CLOSED".equalsIgnoreCase(name())) {
                return "error.msg.glJournalEntry.invalid.accounting.closed";
            } else if ("NO_DEBITS_OR_CREDITS".equalsIgnoreCase(name())) {
                return "error.msg.glJournalEntry.invalid.no.debits.or.credits";
            } else if ("DEBIT_CREDIT_SUM_MISMATCH".equalsIgnoreCase(name())) {
                return "error.msg.glJournalEntry.invalid.mismatch.debits.credits";
            } else if ("DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY".equalsIgnoreCase(name())) {
                return "error.msg.glJournalEntry.invalid.empty.account.or.amount";
            } else if ("GL_ACCOUNT_DISABLED".equalsIgnoreCase(name())) {
                return "error.msg.glJournalEntry.invalid.account.disabled";
            } else if ("INVALID_DEBIT_OR_CREDIT_ACCOUNTS".equalsIgnoreCase(name())) {
                return "error.msg.glJournalEntry.invalid.debit.or.credit.accounts";
			} else if ("GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED".equalsIgnoreCase(name())) {
				return "error.msg.glJournalEntry.invalid.account.manual.adjustments.not.permitted";
			}
			return name();
        }
    }

    public JournalEntryInvalidException(final GL_JOURNAL_ENTRY_INVALID_REASON reason, final Date date, final String accountName,
            final String accountGLCode) {
        super(reason.errorCode(), reason.errorMessage(), date, accountName, accountGLCode);
    }
}