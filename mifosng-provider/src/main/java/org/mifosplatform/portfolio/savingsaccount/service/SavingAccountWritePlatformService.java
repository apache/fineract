package org.mifosplatform.portfolio.savingsaccount.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.loanaccount.command.UndoStateTransitionCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountApprovalCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountDepositCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountWithdrawalCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingStateTransitionsCommand;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountForLookup;
import org.springframework.security.access.prepost.PreAuthorize;

public interface SavingAccountWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_SAVINGSACCOUNT')")
    CommandProcessingResult createSavingAccount(SavingAccountCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'UPDATE_SAVINGSACCOUNT')")
    CommandProcessingResult updateSavingAccount(SavingAccountCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER')")
    CommandProcessingResult rejectSavingApplication(SavingStateTransitionsCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER')")
    CommandProcessingResult withdrawSavingApplication(SavingStateTransitionsCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER')")
    CommandProcessingResult undoSavingAccountApproval(UndoStateTransitionCommand undoCommand);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER')")
    CommandProcessingResult approveSavingAccount(SavingAccountApprovalCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER')")
    CommandProcessingResult depositMoney(SavingAccountDepositCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER')")
    CommandProcessingResult withdrawSavingAmount(SavingAccountWithdrawalCommand command);
    
    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER')")
    CommandProcessingResult deleteSavingAccount(Long accountId);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER')")
	CommandProcessingResult postInterest(Collection<SavingAccountForLookup> savingAccounts);
}