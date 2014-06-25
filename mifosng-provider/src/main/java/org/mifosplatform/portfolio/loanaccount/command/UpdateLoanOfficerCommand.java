/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.command;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable data object for updating relationship between loan officer and a
 * loan.
 */
public class UpdateLoanOfficerCommand {

    private final Long fromLoanOfficerId;
    private final Long toLoanOfficerId;
    private final LocalDate assignmentDate;

    private final String[] loans;

    public UpdateLoanOfficerCommand(final Long fromLoanOfficerId, final Long toLoanOfficerId, final LocalDate assignmentDate) {
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.toLoanOfficerId = toLoanOfficerId;
        this.assignmentDate = assignmentDate;
        this.loans = null;
    }

    public UpdateLoanOfficerCommand(final Long fromLoanOfficerId, final Long toLoanOfficerId, final LocalDate assignmentDate,
            final String[] loans) {
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.toLoanOfficerId = toLoanOfficerId;
        this.assignmentDate = assignmentDate;
        this.loans = loans;
    }

    public void validateForBulkLoanReassignment() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loans.reassignment");

        baseDataValidator.reset().parameter("fromLoanOfficerId").value(this.fromLoanOfficerId).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("toLoanOfficerId").value(this.toLoanOfficerId).notNull().integerGreaterThanZero()
                .notSameAsParameter("fromLoanOfficerId", this.fromLoanOfficerId);

        baseDataValidator.reset().parameter("assignmentDate").value(this.assignmentDate).notNull();

        baseDataValidator.reset().parameter("loans").value(this.loans).arrayNotEmpty();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForLoanReassignment() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loans.reassignment");

        baseDataValidator.reset().parameter("toLoanOfficerId").value(this.toLoanOfficerId).notNull().integerGreaterThanZero()
                .notSameAsParameter("fromLoanOfficerId", this.fromLoanOfficerId);

        baseDataValidator.reset().parameter("assignmentDate").value(this.assignmentDate).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}