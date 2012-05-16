package org.mifosng.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.mifosng.data.ClientList;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.ErrorResponseList;
import org.mifosng.data.LoanProductData;
import org.mifosng.data.LoanProductList;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.data.command.UndoLoanApprovalCommand;
import org.mifosng.data.command.UndoLoanDisbursalCommand;
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
}