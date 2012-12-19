package org.mifosplatform.portfolio.loanaccount.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for loan transactions.
 */
public class LoanTransactionCommand {

    private final LocalDate transactionDate;
    private final BigDecimal transactionAmount;
    private final String note;

    public LoanTransactionCommand(final LocalDate paymentDate, final BigDecimal paymentAmount, final String note) {
        this.transactionDate = paymentDate;
        this.transactionAmount = paymentAmount;
        this.note = note;
    }

    public void validate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        baseDataValidator.reset().parameter("transactionDate").value(this.transactionDate).notNull();
        baseDataValidator.reset().parameter("transactionAmount").value(this.transactionAmount).notNull().positiveAmount();
        baseDataValidator.reset().parameter("note").value(this.note).notExceedingLengthOf(1000);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateNonMonetaryTransaction() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");

        baseDataValidator.reset().parameter("transactionDate").value(this.transactionDate).notNull();
        baseDataValidator.reset().parameter("note").value(this.note).notExceedingLengthOf(1000);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}