package org.mifosplatform.infrastructure.staff.command;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

import java.util.ArrayList;
import java.util.List;

public class BulkTransferLoanOfficerCommandValidator {

    private final BulkTransferLoanOfficerCommand command;

    public BulkTransferLoanOfficerCommandValidator(BulkTransferLoanOfficerCommand command) {
        this.command = command;
    }

    public void validateForBulkLoanReassignment() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loans.reassignment");

        baseDataValidator.reset().parameter("fromLoanOfficerId").value(command.getFromLoanOfficerId()).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("toLoanOfficerId").value(command.getToLoanOfficerId()).notNull().integerGreaterThanZero()
                .notSameAsParameter("fromLoanOfficerId", command.getFromLoanOfficerId());

        baseDataValidator.reset().parameter("assignmentDate").value(command.getAssignmentDate()).notNull();

        baseDataValidator.reset().parameter("loans").value(command.getLoans()).arrayNotEmpty();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForLoanReassignment() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loans.reassignment");

        baseDataValidator.reset().parameter("toLoanOfficerId").value(command.getToLoanOfficerId()).notNull().integerGreaterThanZero()
                .notSameAsParameter("fromLoanOfficerId", command.getFromLoanOfficerId());

        baseDataValidator.reset().parameter("assignmentDate").value(command.getAssignmentDate()).notNull();

        baseDataValidator.reset().parameter("loanId").value(command.getLoanId()).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}
