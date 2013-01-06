package org.mifosplatform.portfolio.savingsdepositaccount.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.loanaccount.command.UndoStateTransitionCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountWithdrawInterestCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountWithdrawalCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositStateTransitionApprovalCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositStateTransitionCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountsForLookup;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DepositAccountWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_DEPOSITACCOUNT')")
    CommandProcessingResult createDepositAccount(DepositAccountCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_DEPOSITACCOUNT')")
    CommandProcessingResult updateDepositAccount(DepositAccountCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_DEPOSITACCOUNT')")
    CommandProcessingResult deleteDepositAccount(Long productId);

    // NOTE - took out permissions relating to doing things with deposit
    // accounts in the past as doesn't appear to be code for it
    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'APPROVE_DEPOSITACCOUNT')")
    CommandProcessingResult approveDepositApplication(DepositStateTransitionApprovalCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'REJECT_DEPOSITACCOUNT')")
    CommandProcessingResult rejectDepositApplication(DepositStateTransitionCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'WITHDRAW_DEPOSITACCOUNT')")
    CommandProcessingResult withdrawDepositApplication(DepositStateTransitionCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'APPROVALUNDO_DEPOSITACCOUNT')")
    CommandProcessingResult undoDepositApproval(UndoStateTransitionCommand undoCommand);

    /*
     * @PreAuthorize(value =
     * "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', '')" )
     * EntityIdentifier matureDepositApplication(DepositStateTransitionCommand
     * command);
     */

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'WITHDRAWAL_DEPOSITACCOUNT')")
    CommandProcessingResult withdrawDepositAccountMoney(DepositAccountWithdrawalCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'INTEREST_DEPOSITACCOUNT')")
    CommandProcessingResult withdrawDepositAccountInterestMoney(DepositAccountWithdrawInterestCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'RENEW_DEPOSITACCOUNT')")
    CommandProcessingResult renewDepositAccount(DepositAccountCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER')")
    CommandProcessingResult postInterestToDepositAccount(Collection<DepositAccountsForLookup> accounts);

}