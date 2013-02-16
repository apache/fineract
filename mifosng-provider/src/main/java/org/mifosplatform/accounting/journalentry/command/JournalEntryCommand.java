/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.command;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for adding an accounting closure
 */
public class JournalEntryCommand {

    private final Long officeId;
    private final LocalDate entryDate;
    private final String comments;

    private final SingleDebitOrCreditEntryCommand[] credits;
    private final SingleDebitOrCreditEntryCommand[] debits;

    public JournalEntryCommand(Long officeId, LocalDate entryDate, String comments, SingleDebitOrCreditEntryCommand[] credits,
            SingleDebitOrCreditEntryCommand[] debits) {
        this.officeId = officeId;
        this.entryDate = entryDate;
        this.comments = comments;
        this.credits = credits;
        this.debits = debits;
    }

    public void validateForCreate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLJournalEntry");

        baseDataValidator.reset().parameter("entryDate").value(this.entryDate).notBlank();

        baseDataValidator.reset().parameter("officeId").value(this.officeId).notNull().integerGreaterThanZero();

        baseDataValidator.reset().parameter("comments").value(this.comments).ignoreIfNull().notExceedingLengthOf(500);

        baseDataValidator.reset().parameter("credits").value(this.credits).notNull();

        baseDataValidator.reset().parameter("debits").value(this.debits).notNull();

        // validation for credit array elements
        if (this.credits != null) {
            if (this.credits.length == 0) {
                validateSingleDebitOrCredit(baseDataValidator, "credits", 0, new SingleDebitOrCreditEntryCommand(null, null, null, null));
            } else {
                int i = 0;
                for (SingleDebitOrCreditEntryCommand credit : this.credits) {
                    validateSingleDebitOrCredit(baseDataValidator, "credits", i, credit);
                    i++;
                }
            }
        }

        // validation for debit array elements
        if (this.debits != null) {
            if (this.debits.length == 0) {
                validateSingleDebitOrCredit(baseDataValidator, "credits", 0, new SingleDebitOrCreditEntryCommand(null, null, null, null));
            } else {
                int i = 0;
                for (SingleDebitOrCreditEntryCommand debit : this.debits) {
                    validateSingleDebitOrCredit(baseDataValidator, "debits", i, debit);
                    i++;
                }
            }
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    /**
     * @param baseDataValidator
     * @param i
     * @param credit
     */
    private void validateSingleDebitOrCredit(DataValidatorBuilder baseDataValidator, String paramSuffix, int arrayPos,
            SingleDebitOrCreditEntryCommand credit) {
        baseDataValidator.reset().parameter(paramSuffix + "[" + arrayPos + "].glAccountId").value(credit.getGlAccountId()).notNull()
                .integerGreaterThanZero();
        baseDataValidator.reset().parameter(paramSuffix + "[" + arrayPos + "].amount").value(credit.getAmount()).notNull()
                .zeroOrPositiveAmount();
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public LocalDate getEntryDate() {
        return this.entryDate;
    }

    public String getComments() {
        return this.comments;
    }

    public SingleDebitOrCreditEntryCommand[] getCredits() {
        return this.credits;
    }

    public SingleDebitOrCreditEntryCommand[] getDebits() {
        return this.debits;
    }

}