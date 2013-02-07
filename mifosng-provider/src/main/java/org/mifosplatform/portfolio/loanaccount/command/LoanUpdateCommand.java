package org.mifosplatform.portfolio.loanaccount.command;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for loan transactions.
 */
public class LoanUpdateCommand {

    private final LocalDate unassignedDate;

    public LoanUpdateCommand(final LocalDate unassignDate) {
        this.unassignedDate = unassignDate;
    }

    public void validate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        baseDataValidator.reset().parameter("unassignedDate").value(this.unassignedDate).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}