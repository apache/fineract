package org.mifosplatform.infrastructure.core.api;

import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.group.command.GroupCommand;
import org.mifosplatform.portfolio.loanaccount.gaurantor.command.GuarantorCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountApprovalCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountDepositCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountWithdrawalCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingStateTransitionsCommand;
import org.mifosplatform.portfolio.savingsaccountproduct.command.SavingProductCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountWithdrawInterestCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountWithdrawalCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositStateTransitionApprovalCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositStateTransitionCommand;
import org.mifosplatform.portfolio.savingsdepositproduct.command.DepositProductCommand;

public interface PortfolioApiDataConversionService {

    SavingProductCommand convertJsonToSavingProductCommand(Long resourceIdentifier, String json);

    DepositProductCommand convertJsonToDepositProductCommand(Long resourceIdentifier, String json);

    ClientData convertInternalJsonFormatToClientDataChange(Long clientId, String json);

    GroupCommand convertJsonToGroupCommand(Long resourceIdentifier, String json);

    DepositAccountCommand convertJsonToDepositAccountCommand(Long resourceIdentifier, String json);

    DepositStateTransitionCommand convertJsonToDepositStateTransitionCommand(Long resourceIdentifier, String json);

    DepositStateTransitionApprovalCommand convertJsonToDepositStateTransitionApprovalCommand(Long resourceIdentifier, String json);

    DepositAccountWithdrawalCommand convertJsonToDepositWithdrawalCommand(Long resourceIdentifier, String json);

    DepositAccountWithdrawInterestCommand convertJsonToDepositAccountWithdrawInterestCommand(Long resourceIdentifier, String json);

    SavingAccountCommand convertJsonToSavingAccountCommand(Long resourceIdentifier, String json);

    GuarantorCommand convertJsonToGuarantorCommand(Long resourceIdentifier, Long loanId, String json);
    
    SavingStateTransitionsCommand convertJsonToSavingStateTransitionCommand(Long accountId, String json);
    
    SavingAccountApprovalCommand convertJsonToSavingApprovalCommand(Long accountId, String json);

	SavingAccountDepositCommand convertJsonToSavingAccountDepositCommand(Long accountId, String json);

	SavingAccountWithdrawalCommand convertJsonToSavingAccountWithdrawalCommand(Long accountId, String jsonRequestBody);
}