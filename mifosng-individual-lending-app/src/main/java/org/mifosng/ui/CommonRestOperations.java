package org.mifosng.ui;

import java.util.Collection;
import java.util.Map;

import org.mifosng.data.AppUserData;
import org.mifosng.data.ClientData;
import org.mifosng.data.ClientDataWithAccountsData;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.EnumOptionReadModel;
import org.mifosng.data.LoanAccountData;
import org.mifosng.data.LoanProductData;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.data.NoteData;
import org.mifosng.data.PermissionData;
import org.mifosng.data.RoleData;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.ChangePasswordCommand;
import org.mifosng.data.command.EnrollClientCommand;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.NoteCommand;
import org.mifosng.data.command.RoleCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.data.command.UndoLoanApprovalCommand;
import org.mifosng.data.command.UndoLoanDisbursalCommand;
import org.mifosng.data.command.UserCommand;
import org.mifosng.data.reports.GenericResultset;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;

public interface CommonRestOperations {

	void logout(String accessToken);

	void updateProtectedResource(
			ProtectedResourceDetails loadProtectedResourceDetailsById);

	Collection<AppUserData> retrieveAllUsers();

	AppUserData retrieveNewUserDetails();

	AppUserData retrieveUser(Long userId);

	AppUserData retrieveCurrentUser();

	EntityIdentifier createUser(UserCommand command);

	EntityIdentifier updateUser(UserCommand command);

	EntityIdentifier updateCurrentUserPassword(ChangePasswordCommand command);

	void deleteUser(Long userId);

	Collection<RoleData> retrieveAllRoles();

	RoleData retrieveRole(Long roleId);

	RoleData retrieveNewRoleDetails();

	EntityIdentifier updateRole(RoleCommand command);

	Collection<PermissionData> retrieveAllPermissions();

	Collection<EnumOptionReadModel> retrieveAllPermissionGroups();

	EntityIdentifier createRole(RoleCommand command);

	Collection<ClientData> retrieveAllIndividualClients();

	Collection<LoanProductData> retrieveAllLoanProducts();

	LoanSchedule calculateLoanSchedule(CalculateLoanScheduleCommand command);

	NewLoanWorkflowStepOneData retrieveNewLoanApplicationStepOneDetails(
			Long clientId);

	Long submitLoanApplication(SubmitLoanApplicationCommand command);

	EntityIdentifier deleteLoan(Long loanId);

	EntityIdentifier approveLoan(LoanStateTransitionCommand command);

	EntityIdentifier undoLoanApproval(UndoLoanApprovalCommand command);

	EntityIdentifier rejectLoan(LoanStateTransitionCommand command);

	EntityIdentifier withdrawLoan(LoanStateTransitionCommand command);

	EntityIdentifier disburseLoan(LoanStateTransitionCommand command);

	EntityIdentifier undloLoanDisbursal(UndoLoanDisbursalCommand command);

	LoanRepaymentData retrieveNewLoanRepaymentDetails(Long loanId);

	LoanRepaymentData retrieveLoanRepaymentDetails(Long loanId, Long repaymentId);

	EntityIdentifier makeLoanRepayment(LoanTransactionCommand command);

	EntityIdentifier adjustLoanRepayment(AdjustLoanTransactionCommand command);

	LoanRepaymentData retrieveNewLoanWaiverDetails(Long loanId);

	EntityIdentifier waiveLoanAmount(LoanTransactionCommand command);

	EntityIdentifier updateCurrentUserDetails(UserCommand command);

	ClientData retrieveNewIndividualClient();

	ClientData retrieveClientDetails(Long clientId);

	EntityIdentifier enrollClient(EnrollClientCommand command);

	EntityIdentifier addNote(NoteCommand command);

	EntityIdentifier updateNote(NoteCommand command);

	NoteData retrieveClientNote(Long clientId, Long noteId);

	Collection<NoteData> retrieveClientNotes(Long clientId);

	ClientDataWithAccountsData retrieveClientAccount(Long clientId);

	LoanAccountData retrieveLoanAccount(Long loanId);

	GenericResultset retrieveReportingData(String rptDB, String name, String type, Map<String, String> extractedQueryParams);

	NewLoanWorkflowStepOneData retrieveNewLoanApplicationDetails(Long clientId, Long productId);
}