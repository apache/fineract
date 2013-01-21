package org.mifosplatform.accounting.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.accounting.api.commands.GLJournalEntryCommand;
import org.mifosplatform.accounting.api.commands.SingleDebitOrCreditEntryCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class GLJournalEntryCommandValidator {

    private final GLJournalEntryCommand command;

    public GLJournalEntryCommandValidator(GLJournalEntryCommand command) {
        this.command = command;
    }

    public void validateForCreate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLJournalEntry");

        baseDataValidator.reset().parameter("entryDate").value(command.getEntryDate()).notBlank();

        baseDataValidator.reset().parameter("officeId").value(command.getOfficeId()).notNull().integerGreaterThanZero();

        baseDataValidator.reset().parameter("comments").value(command.getComments()).ignoreIfNull().notExceedingLengthOf(500);

        baseDataValidator.reset().parameter("credits").value(command.getCredits()).notNull();

        baseDataValidator.reset().parameter("debits").value(command.getDebits()).notNull();

        // validation for credit array elements
        if (command.getCredits() != null) {
            if (command.getCredits().length == 0) {
                validateSingleDebitOrCredit(baseDataValidator, "credits", 0, new SingleDebitOrCreditEntryCommand(null, null, null, null));
            } else {
                int i = 0;
                for (SingleDebitOrCreditEntryCommand credit : command.getCredits()) {
                    validateSingleDebitOrCredit(baseDataValidator, "credits", i, credit);
                    i++;
                }
            }
        }

        // validation for debit array elements
        if (command.getDebits() != null) {
            if (command.getDebits().length == 0) {
                validateSingleDebitOrCredit(baseDataValidator, "credits", 0, new SingleDebitOrCreditEntryCommand(null, null, null, null));
            } else {
                int i = 0;
                for (SingleDebitOrCreditEntryCommand debit : command.getDebits()) {
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
}