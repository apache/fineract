package org.mifosplatform.infrastructure.core.api;

import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.api.commands.DepositAccountWithdrawInterestCommand;
import org.mifosng.platform.api.commands.DepositAccountWithdrawalCommand;
import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.api.commands.DepositStateTransitionApprovalCommand;
import org.mifosng.platform.api.commands.DepositStateTransitionCommand;
import org.mifosng.platform.api.commands.GroupCommand;
import org.mifosng.platform.api.commands.GuarantorCommand;
import org.mifosng.platform.api.commands.SavingAccountCommand;
import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.configuration.command.CurrencyCommand;
import org.mifosplatform.infrastructure.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.infrastructure.office.command.OfficeCommand;
import org.mifosplatform.infrastructure.staff.command.BulkTransferLoanOfficerCommand;
import org.mifosplatform.infrastructure.staff.command.StaffCommand;
import org.mifosplatform.infrastructure.user.command.PermissionsCommand;
import org.mifosplatform.infrastructure.user.command.RoleCommand;
import org.mifosplatform.infrastructure.user.command.RolePermissionCommand;
import org.mifosplatform.infrastructure.user.command.UserCommand;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.client.command.NoteCommand;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.fund.command.FundCommand;
import org.mifosplatform.portfolio.loanaccount.command.AdjustLoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanApplicationCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanStateTransitionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanTransactionCommand;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;

public interface PortfolioApiDataConversionService {

    ChargeDefinitionCommand convertApiRequestJsonToChargeDefinitionCommand(Long resourceIdentifier, String json);

    FundCommand convertApiRequestJsonToFundCommand(Long resourceIdentifier, String json);

    OfficeCommand convertApiRequestJsonToOfficeCommand(Long resourceIdentifier, String json);

    RoleCommand convertApiRequestJsonToRoleCommand(Long resourceIdentifier, String json);

    PermissionsCommand convertApiRequestJsonToPermissionsCommand(String jsonRequestBody);

    RolePermissionCommand convertApiRequestJsonToRolePermissionCommand(Long roleId, String jsonRequestBody);

    UserCommand convertApiRequestJsonToUserCommand(Long resourceIdentifier, String json);

    BranchMoneyTransferCommand convertApiRequestJsonToBranchMoneyTransferCommand(String json);

    LoanProductCommand convertApiRequestJsonToLoanProductCommand(Long resourceIdentifier, String json);

    SavingProductCommand convertJsonToSavingProductCommand(Long resourceIdentifier, String json);

    DepositProductCommand convertJsonToDepositProductCommand(Long resourceIdentifier, String json);

    ClientCommand convertApiRequestJsonToClientCommand(Long resourceIdentifier, String json);

    ClientData convertInternalJsonFormatToClientDataChange(Long clientId, String json);

    ClientCommand detectChanges(Long resourceId, String baseJson, String workingJson);

    GroupCommand convertJsonToGroupCommand(Long resourceIdentifier, String json);

    LoanApplicationCommand convertApiRequestJsonToLoanApplicationCommand(Long resourceIdentifier, String json);

    LoanChargeCommand convertJsonToLoanChargeCommand(Long loanChargeId, Long loanId, String json);

    LoanStateTransitionCommand convertJsonToLoanStateTransitionCommand(Long resourceIdentifier, String json);

    LoanTransactionCommand convertJsonToLoanTransactionCommand(Long resourceIdentifier, String json);

    AdjustLoanTransactionCommand convertJsonToAdjustLoanTransactionCommand(Long loanId, Long transactionId, String json);

    CurrencyCommand convertApiRequestJsonToCurrencyCommand(String json);

    NoteCommand convertJsonToNoteCommand(Long resourceIdentifier, Long clientId, String json);

    DepositAccountCommand convertJsonToDepositAccountCommand(Long resourceIdentifier, String json);

    DepositStateTransitionCommand convertJsonToDepositStateTransitionCommand(Long resourceIdentifier, String json);

    DepositStateTransitionApprovalCommand convertJsonToDepositStateTransitionApprovalCommand(Long resourceIdentifier, String json);

    StaffCommand convertApiRequestJsonToStaffCommand(Long resourceIdentifier, String json);

    BulkTransferLoanOfficerCommand convertJsonToLoanReassignmentCommand(Long resourceIdentifier, String json);

    BulkTransferLoanOfficerCommand convertJsonToBulkLoanReassignmentCommand(String json);

    DepositAccountWithdrawalCommand convertJsonToDepositWithdrawalCommand(Long resourceIdentifier, String json);

    DepositAccountWithdrawInterestCommand convertJsonToDepositAccountWithdrawInterestCommand(Long resourceIdentifier, String json);

    ClientIdentifierCommand convertApiRequestJsonToClientIdentifierCommand(Long resourceIdentifier, Long clientId, String json);

    CodeCommand convertApiRequestJsonToCodeCommand(Long resourceIdentifier, String json);

    SavingAccountCommand convertJsonToSavingAccountCommand(Long resourceIdentifier, String json);

    GuarantorCommand convertJsonToGuarantorCommand(Long resourceIdentifier, Long loanId, String json);
}