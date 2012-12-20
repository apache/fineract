package org.mifosplatform.portfolio.loanaccount.command;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for creating and updating loan charges.
 */
public class LoanChargeCommand implements Comparable<LoanChargeCommand> {

    private final Long chargeId;
    private final BigDecimal amount;
    private final Integer chargeTimeType;
    private final LocalDate specifiedDueDate;
    private final Integer chargeCalculationType;

    public LoanChargeCommand(final Long chargeId, final BigDecimal amount, final Integer chargeTimeType,
            final Integer chargeCalculationType, final LocalDate specifiedDueDate) {
        this.chargeId = chargeId;
        this.amount = amount;
        this.chargeTimeType = chargeTimeType;
        this.chargeCalculationType = chargeCalculationType;
        this.specifiedDueDate = specifiedDueDate;
    }

    @Override
    public int compareTo(final LoanChargeCommand o) {
        int comparison = this.chargeId.compareTo(o.chargeId);
        if (comparison == 0) {
            comparison = this.amount.compareTo(o.amount);
        }
        return comparison;
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        baseDataValidator.reset().parameter("chargeId").value(this.chargeId).notNull().longGreaterThanZero();
        baseDataValidator.reset().parameter("amount").value(this.amount).notNull().positiveAmount();
        // baseDataValidator.reset().parameter("chargeTimeType").value(command.getChargeTimeType()).ignoreIfNull().inMinMaxRange(1,
        // 2);
        //
        // if (command.getChargeTimeType().equals(Integer.valueOf(2))) {
        // // date must be provided
        // baseDataValidator.reset().parameter("specifiedDueDate").value(command.getSpecifiedDueDate()).notNull();
        // }
        //
        // baseDataValidator.reset().parameter("chargeCalculationType").value(command.getChargeCalculationType()).ignoreIfNull().inMinMaxRange(1,
        // 4);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        baseDataValidator.reset().parameter("amount").value(this.amount).ignoreIfNull().positiveAmount();
        baseDataValidator.reset().parameter("chargeTimeType").value(this.chargeTimeType).ignoreIfNull().inMinMaxRange(1, 2);
        baseDataValidator.reset().parameter("chargeCalculationType").value(this.chargeCalculationType).ignoreIfNull().inMinMaxRange(1, 4);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}