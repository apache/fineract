package org.mifosplatform.infrastructure.core.api;

import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.organisation.monetary.command.CurrencyCommand;
import org.mifosplatform.organisation.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.organisation.office.command.OfficeCommand;
import org.mifosplatform.organisation.staff.command.BulkTransferLoanOfficerCommand;
import org.mifosplatform.organisation.staff.command.StaffCommand;
import org.mifosplatform.portfolio.charge.command.ChargeDefinitionCommand;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.client.command.NoteCommand;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.fund.command.FundCommand;
import org.mifosplatform.portfolio.group.command.GroupCommand;
import org.mifosplatform.portfolio.loanaccount.command.AdjustLoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanApplicationCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanStateTransitionCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanTransactionCommand;
import org.mifosplatform.portfolio.loanaccount.gaurantor.command.GuarantorCommand;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountCommand;
import org.mifosplatform.portfolio.savingsaccountproduct.command.SavingProductCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountWithdrawInterestCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountWithdrawalCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositStateTransitionApprovalCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositStateTransitionCommand;
import org.mifosplatform.portfolio.savingsdepositproduct.command.DepositProductCommand;
import org.mifosplatform.useradministration.command.PermissionsCommand;
import org.mifosplatform.useradministration.command.RoleCommand;
import org.mifosplatform.useradministration.command.RolePermissionCommand;
import org.mifosplatform.useradministration.command.UserCommand;

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