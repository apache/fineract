package org.mifosplatform.accounting.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.accounting.api.commands.GLJournalEntryCommand;
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

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}