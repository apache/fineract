package org.mifosng.platform;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mifosng.data.AppUserData;
import org.mifosng.data.ClientData;
import org.mifosng.data.ClientDataWithAccountsData;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.EnumOptionReadModel;
import org.mifosng.data.LoanAccountData;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.data.NoteData;
import org.mifosng.data.OrganisationReadModel;
import org.mifosng.data.PermissionData;
import org.mifosng.data.RoleData;
import org.mifosng.data.reports.GenericResultset;

public interface ReadPlatformService {

	Collection<OrganisationReadModel> retrieveAll();

	Collection<ClientData> retrieveAllIndividualClients();

	ClientData retrieveIndividualClient(Long clientId);

	ClientDataWithAccountsData retrieveClientAccountDetails(Long clientId);

	NewLoanWorkflowStepOneData retrieveClientAndProductDetails(Long clientId, Long productId);

	LoanAccountData retrieveLoanAccountDetails(Long loanId);

	LoanRepaymentData retrieveNewLoanRepaymentDetails(Long loanId);

	LoanRepaymentData retrieveNewLoanWaiverDetails(Long loanId);

	LoanRepaymentData retrieveLoanRepaymentDetails(Long loanId, Long repaymentId);

	List<CurrencyData> retrieveAllowedCurrencies();

	List<CurrencyData> retrieveAllPlatformCurrencies();

	Collection<AppUserData> retrieveAllUsers();

	AppUserData retrieveNewUserDetails();

	AppUserData retrieveUser(Long userId);

	AppUserData retrieveCurrentUser();

	Collection<RoleData> retrieveAllRoles();

	RoleData retrieveRole(Long roleId);

	Collection<EnumOptionReadModel> retrieveAllPermissionGroups();

	Collection<PermissionData> retrieveAllPermissions();

	//@PreAuthorize(value = "hasAnyRole('REPORTING_SUPER_USER_ROLE')")
	GenericResultset retrieveGenericResultset(String rptDB, String name,
			String type, Map<String, String> extractedQueryParams);

	ClientData retrieveNewClientDetails();

	Collection<NoteData> retrieveAllClientNotes(Long clientId);

	NoteData retrieveClientNote(Long clientId, Long noteId);

}