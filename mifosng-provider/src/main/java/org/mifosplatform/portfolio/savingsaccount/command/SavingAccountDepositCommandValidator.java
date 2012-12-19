package org.mifosplatform.portfolio.savingsaccount.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class SavingAccountDepositCommandValidator {
	
	private final SavingAccountDepositCommand command;
	
	public SavingAccountDepositCommandValidator(final SavingAccountDepositCommand command) {
		this.command = command;
	}
	
	public void validate(){
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("saving.account.transaction.deposit");
        baseDataValidator.reset().parameter("accountId").value(command.getAccountId()).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("depositDate").value(command.getDepositDate()).notNull();
        baseDataValidator.reset().parameter("savingsDepositAmountPerPeriod").value(command.getSavingsDepostiAmountPerPeriod()).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("note").value(command.getNote()).notNull();
        
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
	}

}
