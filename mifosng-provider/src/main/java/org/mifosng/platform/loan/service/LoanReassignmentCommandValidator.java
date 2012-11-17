package org.mifosng.platform.loan.service;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.LoanReassignmentCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

import java.util.ArrayList;
import java.util.List;

public class LoanReassignmentCommandValidator {

    private final LoanReassignmentCommand command;

    public LoanReassignmentCommandValidator(LoanReassignmentCommand command) {
        this.command = command;
    }

    public void validateForBulkLoanReassignment(){
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loans.reassignment");

        baseDataValidator.reset().parameter("fromLoanOfficerId").value(command.getFromLoanOfficerId()).
                notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("toLoanOfficerId").value(command.getToLoanOfficerId()).notNull().
                integerGreaterThanZero().notSameAsParameter("fromLoanOfficerId", command.getFromLoanOfficerId());

        baseDataValidator.reset().parameter("assignmentDate").value(command.getAssignmentDate()).notNull();

        baseDataValidator.reset().parameter("loans").value(command.getLoans()).arrayNotEmpty();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }

    public void validateForLoanReassignment(){
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loans.reassignment");

        baseDataValidator.reset().parameter("toLoanOfficerId").value(command.getToLoanOfficerId()).notNull().
                integerGreaterThanZero().notSameAsParameter("fromLoanOfficerId", command.getFromLoanOfficerId());

        baseDataValidator.reset().parameter("assignmentDate").value(command.getAssignmentDate()).notNull();

        baseDataValidator.reset().parameter("loanId").value(command.getLoanId()).notNull();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }
}
