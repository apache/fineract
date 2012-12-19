package org.mifosplatform.portfolio.loanaccount.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.organisation.staff.command.BulkTransferLoanOfficerCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LoanWritePlatformService {

    EntityIdentifier submitLoanApplication(JsonCommand command);

    EntityIdentifier modifyLoanApplication(Long loanId, JsonCommand command);

    EntityIdentifier deleteLoan(Long loanId);

    EntityIdentifier approveLoanApplication(Long loanId, JsonCommand command);

    EntityIdentifier undoLoanApplicationApproval(Long loanId, JsonCommand command);

    EntityIdentifier rejectLoanApplication(Long loanId, JsonCommand command);

    EntityIdentifier applicantWithdrawsFromLoanApplication(Long loanId, JsonCommand command);

    EntityIdentifier disburseLoan(Long loanId, JsonCommand command);

    EntityIdentifier undoLoanDisbursal(Long loanId, JsonCommand command);

    EntityIdentifier makeLoanRepayment(Long loanId, JsonCommand command);

    EntityIdentifier adjustLoanTransaction(Long loanId, Long transactionId, JsonCommand command);

    EntityIdentifier waiveInterestOnLoan(Long loanId, JsonCommand command);

    EntityIdentifier writeOff(Long loanId, JsonCommand command);

    EntityIdentifier closeLoan(Long loanId, JsonCommand command);

    EntityIdentifier closeAsRescheduled(Long loanId, JsonCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_LOANCHARGE')")
    EntityIdentifier addLoanCharge(LoanChargeCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_LOANCHARGE')")
    EntityIdentifier updateLoanCharge(LoanChargeCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_LOANCHARGE')")
    EntityIdentifier deleteLoanCharge(final Long loanId, final Long loanChargeId);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'WAIVE_LOANCHARGE')")
    EntityIdentifier waiveLoanCharge(LoanChargeCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'BULKREASSIGN_LOAN')")
    EntityIdentifier bulkLoanReassignment(final BulkTransferLoanOfficerCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER')")
    EntityIdentifier loanReassignment(final BulkTransferLoanOfficerCommand command);
}