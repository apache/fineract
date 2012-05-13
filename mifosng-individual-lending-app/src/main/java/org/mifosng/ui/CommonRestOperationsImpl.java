package org.mifosng.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.mifosng.data.AppUserData;
import org.mifosng.data.ClientData;
import org.mifosng.data.ClientDataWithAccountsData;
import org.mifosng.data.ClientList;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.EnumOptionList;
import org.mifosng.data.EnumOptionReadModel;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.ErrorResponseList;
import org.mifosng.data.LoanAccountData;
import org.mifosng.data.LoanProductData;
import org.mifosng.data.LoanProductList;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.data.NoteData;
import org.mifosng.data.NoteDataList;
import org.mifosng.data.PermissionData;
import org.mifosng.data.PermissionList;
import org.mifosng.data.RoleData;
import org.mifosng.data.RoleList;
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
import org.mifosng.ui.loanproduct.ClientValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;
import org.springframework.security.oauth.consumer.client.OAuthRestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import com.thoughtworks.xstream.XStream;

@Service(value = "commonRestOperations")
public class CommonRestOperationsImpl implements CommonRestOperations {

	private OAuthRestTemplate oauthRestServiceTemplate;
	private final ApplicationConfigurationService applicationConfigurationService;

	@Autowired
	public CommonRestOperationsImpl(
			final OAuthRestTemplate oauthRestServiceTemplate,
			final ApplicationConfigurationService applicationConfigurationService) {
		this.oauthRestServiceTemplate = oauthRestServiceTemplate;
		this.applicationConfigurationService = applicationConfigurationService;
	}

	private String getBaseServerUrl() {
		return this.applicationConfigurationService
				.retrieveOAuthProviderDetails().getProviderBaseUrl();
	}

	@Override
	public void logout(String accessToken) {
		URI restUri = URI.create(getBaseServerUrl()
				.concat("api/protected/user/").concat(accessToken)
				.concat("/signout"));

		oauthRestServiceTemplate.exchange(restUri, HttpMethod.GET,
				emptyRequest(), null);
	}

	@Override
	public void updateProtectedResource(ProtectedResourceDetails resource) {

		this.oauthRestServiceTemplate = new OAuthRestTemplate(resource);
	}

	private HttpEntity<ClientList> emptyRequest() {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		HttpEntity<ClientList> requestEntity = new HttpEntity<ClientList>(
				requestHeaders);
		return requestEntity;
	}

	@Override
	public Collection<ClientData> retrieveAllIndividualClients() {
		URI restUri = URI.create(getBaseServerUrl().concat(
				"api/protected/client/all"));

		ResponseEntity<ClientList> s = this.oauthRestServiceTemplate.exchange(
				restUri, HttpMethod.GET, emptyRequest(), ClientList.class);

		return s.getBody().getClients();
	}

	@Override
	public Collection<LoanProductData> retrieveAllLoanProducts() {
		URI restUri = URI.create(getBaseServerUrl().concat(
				"api/protected/product/loan/all"));

		ResponseEntity<LoanProductList> s = this.oauthRestServiceTemplate
				.exchange(restUri, HttpMethod.GET, emptyRequest(),
						LoanProductList.class);

		return s.getBody().getProducts();
	}

	private ErrorResponseList parseErrors(HttpStatusCodeException e) {

		XStream xstream = new XStream();
		xstream.alias("errorResponseList", ErrorResponseList.class);
		xstream.alias("errorResponse", ErrorResponse.class);

		ErrorResponseList errorList = new ErrorResponseList();
		if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
			errorList = (ErrorResponseList) xstream.fromXML(e
					.getResponseBodyAsString());
		} else if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {

			List<ErrorResponse> errors = new ArrayList<ErrorResponse>();
			errors.add(new ErrorResponse(e.getMessage(), "error", e
					.getMessage()));

			errorList = new ErrorResponseList(errors);
		} else if (HttpStatus.UNAUTHORIZED.equals(e.getStatusCode())) {
			throw new AccessDeniedException(e.getMessage());
		} else {
			throw e;
		}
		return errorList;
	}

	@Override
	public LoanSchedule calculateLoanSchedule(
			final CalculateLoanScheduleCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/calculate"));

			ResponseEntity<LoanSchedule> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.POST,
							calculateLoanRepaymentScheduleRequest(command),
							LoanSchedule.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public NewLoanWorkflowStepOneData retrieveNewLoanApplicationStepOneDetails(
			final Long clientId) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/new/" + clientId.toString()
							+ "/workflow/one"));

			ResponseEntity<NewLoanWorkflowStepOneData> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							NewLoanWorkflowStepOneData.class);

			// for now ensure user must select product on step one (even if
			// there is only one product!)
			NewLoanWorkflowStepOneData data = s.getBody();
			data.setProductId(null);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public NewLoanWorkflowStepOneData retrieveNewLoanApplicationDetails(
			Long clientId, Long productId) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/new/" + clientId.toString()
							+ "/product/" + productId.toString()));

			ResponseEntity<NewLoanWorkflowStepOneData> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							NewLoanWorkflowStepOneData.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public Long submitLoanApplication(final SubmitLoanApplicationCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/new"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri,
							submitLoanApplicationRequest(command),
							EntityIdentifier.class);

			return s.getBody().getEntityId();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier deleteLoan(Long loanId) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/").concat(loanId.toString()));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.DELETE, null,
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier approveLoan(final LoanStateTransitionCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/approve"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri,
							loanStateTransitionApplicationRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier undoLoanApproval(
			final UndoLoanApprovalCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/undoapproval"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri, undoLoanApprovalRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier rejectLoan(final LoanStateTransitionCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/reject"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri,
							loanStateTransitionApplicationRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier withdrawLoan(
			final LoanStateTransitionCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/withdraw"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri,
							loanStateTransitionApplicationRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier disburseLoan(
			final LoanStateTransitionCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/disburse"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri,
							loanStateTransitionApplicationRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier undloLoanDisbursal(
			final UndoLoanDisbursalCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/undodisbursal"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri, undoLoanDisbursalRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public LoanRepaymentData retrieveNewLoanRepaymentDetails(Long loanId) {
		try {
			URI restUri = URI.create(getBaseServerUrl()
					.concat("api/protected/loan/").concat(loanId.toString())
					.concat("/repayment/"));

			ResponseEntity<LoanRepaymentData> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							LoanRepaymentData.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public LoanRepaymentData retrieveLoanRepaymentDetails(Long loanId,
			Long repaymentId) {
		try {
			URI restUri = URI.create(getBaseServerUrl()
					.concat("api/protected/loan/").concat(loanId.toString())
					.concat("/repayment/").concat(repaymentId.toString()));

			ResponseEntity<LoanRepaymentData> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							LoanRepaymentData.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier makeLoanRepayment(
			final LoanTransactionCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/repayment"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri, loanTransactionRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier adjustLoanRepayment(
			AdjustLoanTransactionCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/repayment/adjust"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri,
							adjustLoanRepaymentRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public LoanRepaymentData retrieveNewLoanWaiverDetails(Long loanId) {
		try {
			URI restUri = URI.create(getBaseServerUrl()
					.concat("api/protected/loan/").concat(loanId.toString())
					.concat("/waive/"));

			ResponseEntity<LoanRepaymentData> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							LoanRepaymentData.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier waiveLoanAmount(LoanTransactionCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/waive"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri, loanTransactionRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	private HttpEntity<LoanTransactionCommand> loanTransactionRequest(
			final LoanTransactionCommand command) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		return new HttpEntity<LoanTransactionCommand>(command, requestHeaders);
	}

	private HttpEntity<AdjustLoanTransactionCommand> adjustLoanRepaymentRequest(
			final AdjustLoanTransactionCommand command) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		return new HttpEntity<AdjustLoanTransactionCommand>(command,
				requestHeaders);
	}

	private HttpEntity<UndoLoanDisbursalCommand> undoLoanDisbursalRequest(
			final UndoLoanDisbursalCommand command) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		return new HttpEntity<UndoLoanDisbursalCommand>(command, requestHeaders);
	}

	private HttpEntity<UndoLoanApprovalCommand> undoLoanApprovalRequest(
			final UndoLoanApprovalCommand command) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		return new HttpEntity<UndoLoanApprovalCommand>(command, requestHeaders);
	}

	private HttpEntity<LoanStateTransitionCommand> loanStateTransitionApplicationRequest(
			final LoanStateTransitionCommand command) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		return new HttpEntity<LoanStateTransitionCommand>(command,
				requestHeaders);
	}

	private HttpEntity<SubmitLoanApplicationCommand> submitLoanApplicationRequest(
			final SubmitLoanApplicationCommand command) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		return new HttpEntity<SubmitLoanApplicationCommand>(command,
				requestHeaders);
	}

	private HttpEntity<CalculateLoanScheduleCommand> calculateLoanRepaymentScheduleRequest(
			final CalculateLoanScheduleCommand command) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		return new HttpEntity<CalculateLoanScheduleCommand>(command,
				requestHeaders);
	}

	@Override
	public EntityIdentifier updateCurrentUserDetails(UserCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/admin/user/current"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri, createUserRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier updateCurrentUserPassword(
			ChangePasswordCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/admin/user/current/password"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri,
							createUpdateCurrentUserPasswordRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	private HttpEntity<UserCommand> createUserRequest(final UserCommand command) {

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		HttpEntity<UserCommand> requestEntity = new HttpEntity<UserCommand>(
				command, requestHeaders);
		return requestEntity;
	}

	private HttpEntity<ChangePasswordCommand> createUpdateCurrentUserPasswordRequest(
			final ChangePasswordCommand command) {

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		HttpEntity<ChangePasswordCommand> requestEntity = new HttpEntity<ChangePasswordCommand>(
				command, requestHeaders);
		return requestEntity;
	}

	private HttpEntity<RoleCommand> roleRequest(final RoleCommand command) {

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		HttpEntity<RoleCommand> requestEntity = new HttpEntity<RoleCommand>(
				command, requestHeaders);
		return requestEntity;
	}

	@Override
	public AppUserData retrieveCurrentUser() {
		URI restUri = URI.create(getBaseServerUrl().concat(
				"api/protected/admin/user/current"));

		ResponseEntity<AppUserData> s = this.oauthRestServiceTemplate.exchange(
				restUri, HttpMethod.GET, emptyRequest(), AppUserData.class);

		return s.getBody();
	}

	@Override
	public Collection<RoleData> retrieveAllRoles() {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/admin/role/all"));

			ResponseEntity<RoleList> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							RoleList.class);

			return s.getBody().getRoles();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public RoleData retrieveRole(final Long roleId) {

		URI restUri = URI.create(getBaseServerUrl().concat(
				"api/protected/admin/role/").concat(roleId.toString()));

		ResponseEntity<RoleData> s = this.oauthRestServiceTemplate.exchange(
				restUri, HttpMethod.GET, emptyRequest(), RoleData.class);

		return s.getBody();
	}

	@Override
	public RoleData retrieveNewRoleDetails() {
		URI restUri = URI.create(getBaseServerUrl().concat(
				"api/protected/admin/role/new"));

		ResponseEntity<RoleData> s = this.oauthRestServiceTemplate.exchange(
				restUri, HttpMethod.GET, emptyRequest(), RoleData.class);

		return s.getBody();
	}

	@Override
	public EntityIdentifier createRole(final RoleCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/admin/role/new"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri, roleRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier updateRole(RoleCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/admin/role/").concat(
					command.getId().toString()));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri, roleRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public Collection<PermissionData> retrieveAllPermissions() {

		URI restUri = URI.create(getBaseServerUrl().concat(
				"api/protected/admin/permissions/all"));

		ResponseEntity<PermissionList> s = this.oauthRestServiceTemplate
				.exchange(restUri, HttpMethod.GET, emptyRequest(),
						PermissionList.class);

		return s.getBody().getPermissions();
	}

	@Override
	public Collection<EnumOptionReadModel> retrieveAllPermissionGroups() {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/admin/permissiongroup/all"));

			ResponseEntity<EnumOptionList> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							EnumOptionList.class);

			return s.getBody().getOptions();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public ClientData retrieveNewIndividualClient() {
		URI restUri = URI.create(getBaseServerUrl().concat(
				"api/protected/client/new"));

		ResponseEntity<ClientData> s = this.oauthRestServiceTemplate.exchange(
				restUri, HttpMethod.GET, emptyRequest(), ClientData.class);

		return s.getBody();
	}

	@Override
	public ClientData retrieveClientDetails(Long clientId) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/client/").concat(clientId.toString()));

			ResponseEntity<ClientData> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							ClientData.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier enrollClient(EnrollClientCommand command) {
		try {

			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/client/new"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri, enrollClientRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	private HttpEntity<EnrollClientCommand> enrollClientRequest(
			final EnrollClientCommand command) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		return new HttpEntity<EnrollClientCommand>(command, requestHeaders);
	}

	@Override
	public EntityIdentifier addNote(NoteCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl()
					.concat("api/protected/client/")
					.concat(command.getClientId().toString())
					.concat("/note/new"));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri, noteRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public EntityIdentifier updateNote(NoteCommand command) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/note/").concat(command.getId().toString()));

			ResponseEntity<EntityIdentifier> s = this.oauthRestServiceTemplate
					.postForEntity(restUri, noteRequest(command),
							EntityIdentifier.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public NoteData retrieveClientNote(Long clientId, Long noteId) {
		try {
			URI restUri = URI.create(getBaseServerUrl()
					.concat("api/protected/client/")
					.concat(clientId.toString()).concat("/note/")
					.concat(noteId.toString()));

			ResponseEntity<NoteData> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							NoteData.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public Collection<NoteData> retrieveClientNotes(Long clientId) {
		try {
			URI restUri = URI.create(getBaseServerUrl()
					.concat("api/protected/client/")
					.concat(clientId.toString()).concat("/note/all"));

			ResponseEntity<NoteDataList> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							NoteDataList.class);

			return s.getBody().getNotes();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	private HttpEntity<NoteCommand> noteRequest(final NoteCommand command) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		return new HttpEntity<NoteCommand>(command, requestHeaders);
	}

	@Override
	public ClientDataWithAccountsData retrieveClientAccount(Long clientId) {
		try {
			URI restUri = URI.create(getBaseServerUrl()
					.concat("api/protected/client/")
					.concat(clientId.toString()).concat("/withaccounts"));

			ResponseEntity<ClientDataWithAccountsData> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							ClientDataWithAccountsData.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

	@Override
	public LoanAccountData retrieveLoanAccount(Long loanId) {
		try {
			URI restUri = URI.create(getBaseServerUrl().concat(
					"api/protected/loan/" + loanId));

			ResponseEntity<LoanAccountData> s = this.oauthRestServiceTemplate
					.exchange(restUri, HttpMethod.GET, emptyRequest(),
							LoanAccountData.class);

			return s.getBody();
		} catch (HttpStatusCodeException e) {
			ErrorResponseList errorList = parseErrors(e);
			throw new ClientValidationException(errorList.getErrors());
		}
	}

}