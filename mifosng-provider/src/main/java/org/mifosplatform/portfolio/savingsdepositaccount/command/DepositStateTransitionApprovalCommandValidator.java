package org.mifosplatform.portfolio.savingsdepositaccount.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class DepositStateTransitionApprovalCommandValidator {

    private final DepositStateTransitionApprovalCommand command;

    public DepositStateTransitionApprovalCommandValidator(final DepositStateTransitionApprovalCommand command) {
        this.command = command;
    }

    public void validate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.transition");
        baseDataValidator.reset().parameter("accountId").value(command.getAccountId()).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("commencementDate").value(command.getEventDate()).notNull();

        baseDataValidator.reset().parameter("deposit").value(command.getDepositAmount()).ignoreIfNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("maturityInterestRate").value(command.getMaturityInterestRate()).ignoreIfNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("tenureInMonths").value(command.getTenureInMonths()).ignoreIfNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(command.getInterestCompoundedEveryPeriodType())
                .ignoreIfNull().inMinMaxRange(2, 2);

        baseDataValidator.reset().parameter("note").value(command.getNote()).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}