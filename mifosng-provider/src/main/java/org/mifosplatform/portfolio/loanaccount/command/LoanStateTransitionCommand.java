package org.mifosplatform.portfolio.loanaccount.command;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for any of the loan state transitions eg. reject, withdrawn
 * by client, approve, disburse
 */
public class LoanStateTransitionCommand {

    private final LocalDate eventDate;
    private final String note;
    private final LocalDate approvedOnDate;
    private final LocalDate rejectedOnDate;
    private final LocalDate withdrawnOnDate;
    private final LocalDate disbursedOnDate;

    public LoanStateTransitionCommand(final LocalDate eventDate, final String note, final LocalDate approvedOnDate,
            final LocalDate rejectedOnDate, final LocalDate withdrawnOnDate, final LocalDate disbursedOnDate) {
        this.eventDate = eventDate;
        this.note = note;
        this.approvedOnDate = approvedOnDate;
        this.rejectedOnDate = rejectedOnDate;
        this.withdrawnOnDate = withdrawnOnDate;
        this.disbursedOnDate = disbursedOnDate;
    }

    public LocalDate getApprovedOnDate() {
        LocalDate dateValue = this.approvedOnDate;
        if (this.approvedOnDate == null) {
            dateValue = eventDate;
        }
        return dateValue;
    }

    public LocalDate getRejectedOnDate() {
        LocalDate dateValue = this.rejectedOnDate;
        if (this.rejectedOnDate == null) {
            dateValue = eventDate;
        }
        return dateValue;
    }

    public LocalDate getWithdrawnOnDate() {
        LocalDate dateValue = this.withdrawnOnDate;
        if (this.withdrawnOnDate == null) {
            dateValue = eventDate;
        }
        return dateValue;
    }

    public LocalDate getDisbursedOnDate() {
        LocalDate dateValue = this.disbursedOnDate;
        if (this.disbursedOnDate == null) {
            dateValue = eventDate;
        }
        return dateValue;
    }

    public void validate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transition");

        baseDataValidator.reset().parameter("eventDate").value(this.eventDate).notNull();
        baseDataValidator.reset().parameter("note").value(this.note).notExceedingLengthOf(1000);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}