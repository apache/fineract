package org.mifosng.platform.staff.service;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.BulkLoanReassignmentCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

import java.util.ArrayList;
import java.util.List;

public class BulkLoanReassignmentCommandValidator {

    private final BulkLoanReassignmentCommand command;

    public BulkLoanReassignmentCommandValidator(BulkLoanReassignmentCommand command) {
        this.command = command;
    }

    public void validateLoanReassignment(){
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("staff.account.transfer");

        baseDataValidator.reset().parameter("fromLoanOfficerId").value(command.getFromLoanOfficerId()).
                notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("toLoanOfficerId").value(command.getToLoanOfficerId()).notNull().
                integerGreaterThanZero().notSameAsParameter("fromLoanOfficerId", command.getFromLoanOfficerId());

        baseDataValidator.reset().parameter("loans").value(command.getLoans()).arrayNotEmpty();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }
}
