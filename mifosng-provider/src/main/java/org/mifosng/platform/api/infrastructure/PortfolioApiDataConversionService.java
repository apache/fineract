package org.mifosng.platform.api.infrastructure;

import org.mifosng.platform.accounting.api.commands.RolePermissionCommand;
import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.commands.BranchMoneyTransferCommand;
import org.mifosng.platform.api.commands.LoanReassignmentCommand;
import org.mifosng.platform.api.commands.ChargeCommand;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.commands.ClientIdentifierCommand;
import org.mifosng.platform.api.commands.CodeCommand;
import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.api.commands.DepositAccountWithdrawInterestCommand;
import org.mifosng.platform.api.commands.DepositAccountWithdrawalCommand;
import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.api.commands.DepositStateTransitionApprovalCommand;
import org.mifosng.platform.api.commands.DepositStateTransitionCommand;
import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.api.commands.GroupCommand;
import org.mifosng.platform.api.commands.GuarantorCommand;
import org.mifosng.platform.api.commands.LoanApplicationCommand;
import org.mifosng.platform.api.commands.LoanChargeCommand;
import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.commands.LoanStateTransitionCommand;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.commands.NoteCommand;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.mifosng.platform.api.commands.OrganisationCurrencyCommand;
import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.commands.SavingAccountCommand;
import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.api.commands.StaffCommand;
import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.api.data.ClientData;

public interface PortfolioApiDataConversionService {

    ChargeCommand convertJsonToChargeCommand(Long resourceIdentifier, String json);

    FundCommand convertJsonToFundCommand(Long resourceIdentifier, String json);

    OfficeCommand convertJsonToOfficeCommand(Long resourceIdentifier, String json);

    RoleCommand convertApiRequestJsonToRoleCommand(Long resourceIdentifier, String json);
    
    RolePermissionCommand convertApiRequestJsonToRolePermissionCommand(Long roleId, String jsonRequestBody);

    UserCommand convertApiRequestJsonToUserCommand(Long resourceIdentifier, String json);

    BranchMoneyTransferCommand convertJsonToBranchMoneyTransferCommand(String json);

    LoanProductCommand convertJsonToLoanProductCommand(Long resourceIdentifier, String json);

    SavingProductCommand convertJsonToSavingProductCommand(Long resourceIdentifier, String json);

    DepositProductCommand convertJsonToDepositProductCommand(Long resourceIdentifier, String json);

    ClientCommand convertApiRequestJsonToClientCommand(Long resourceIdentifier, String json);

    ClientCommand convertInternalJsonFormatToClientCommand(Long resourceIdentifier, String json, boolean checkerApproved);

    ClientData convertInternalJsonFormatToClientDataChange(Long clientId, String json);

    ClientCommand detectChanges(Long resourceId, String baseJson, String workingJson);

    GroupCommand convertJsonToGroupCommand(Long resourceIdentifier, String json);

    LoanApplicationCommand convertJsonToLoanApplicationCommand(Long resourceIdentifier, String json);

    LoanChargeCommand convertJsonToLoanChargeCommand(Long loanChargeId, Long loanId, String json);

    LoanStateTransitionCommand convertJsonToLoanStateTransitionCommand(Long resourceIdentifier, String json);

    LoanTransactionCommand convertJsonToLoanTransactionCommand(Long resourceIdentifier, String json);

    AdjustLoanTransactionCommand convertJsonToAdjustLoanTransactionCommand(Long loanId, Long transactionId, String json);

    OrganisationCurrencyCommand convertJsonToOrganisationCurrencyCommand(String json);

    NoteCommand convertJsonToNoteCommand(Long resourceIdentifier, Long clientId, String json);

    DepositAccountCommand convertJsonToDepositAccountCommand(Long resourceIdentifier, String json);

    DepositStateTransitionCommand convertJsonToDepositStateTransitionCommand(Long resourceIdentifier, String json);

    DepositStateTransitionApprovalCommand convertJsonToDepositStateTransitionApprovalCommand(Long resourceIdentifier, String json);

    StaffCommand convertJsonToStaffCommand(Long resourceIdentifier, String json);

    LoanReassignmentCommand convertJsonToLoanReassignmentCommand(Long resourceIdentifier, String json);

    LoanReassignmentCommand convertJsonToBulkLoanReassignmentCommand(String json);

    DepositAccountWithdrawalCommand convertJsonToDepositWithdrawalCommand(Long resourceIdentifier, String json);

    DepositAccountWithdrawInterestCommand convertJsonToDepositAccountWithdrawInterestCommand(Long resourceIdentifier, String json);

    ClientIdentifierCommand convertJsonToClientIdentifierCommand(Long resourceIdentifier, Long clientId, String json);

    CodeCommand convertJsonToCodeCommand(Long resourceIdentifier, String json);

    SavingAccountCommand convertJsonToSavingAccountCommand(Long resourceIdentifier, String json);

    GuarantorCommand convertJsonToGuarantorCommand(Long resourceIdentifier, Long loanId, String json);
}