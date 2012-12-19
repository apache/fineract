package org.mifosplatform.portfolio.savingsaccount.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class SavingAccountApprovalCommandValidator {
	
	private final SavingAccountApprovalCommand command;
	
	public SavingAccountApprovalCommandValidator(final SavingAccountApprovalCommand command) {
		this.command = command ;
	}

	public void validate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("saving.account.transition.approve");
        baseDataValidator.reset().parameter("accountId").value(command.getAccountId()).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("commencementDate").value(command.getApprovalDate()).notNull();
        baseDataValidator.reset().parameter("savingsDepositAmountPerPeriod").value(command.getDepositAmountPerPeriod()).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("minimumBalanceForWithdrawal").value(command.getMinimumBalanceForWithdrawal()).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("recurringInterestRate").value(command.getRecurringInterestRate()).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("savingInterestRate").value(command.getSavingInterestRate()).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("interestType").value(command.getInterestType()).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("tenure").value(command.getTenure()).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("tenureType").value(command.getTenureType()).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("frequency").value(command.getDepositFrequencyType()).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("payEvery").value(command.getDepositEvery()).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("note").value(command.getNote()).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
	}

}
