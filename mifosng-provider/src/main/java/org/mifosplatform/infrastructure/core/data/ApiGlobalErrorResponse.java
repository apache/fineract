package org.mifosplatform.infrastructure.core.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 *
 */
public class ApiGlobalErrorResponse {

	/**
	 * A developer friendly plain English description of why the HTTP error response was returned from the API.
	 */
	private String developerMessage;
	
	/**
	 * A link to a URL which provides more information on the details of this type of error.
	 */
	private String developerDocLink;
	
	/**
	 * The HTTP status code of the response.
	 */
	private String httpStatusCode;
	
	/**
	 * A user friendly plain English description of why the HTTP error response was returned from the API that can be presented to end users.
	 */
	private String defaultUserMessage;
	
	/**
	 * A code that can be used for globalisation support by client applications of the API.
	 */
	private String userMessageGlobalisationCode;
	
	/**
	 * A list of zero or more of the actual reasons for the HTTP error should they be needed. Typically used to express data validation errors with parameters passed to API.
	 */
	private List<ApiParameterError> errors = new ArrayList<ApiParameterError>();

	public static ApiGlobalErrorResponse unAuthenticated() {
		
		ApiGlobalErrorResponse globalErrorResponse = new ApiGlobalErrorResponse();
		globalErrorResponse.setHttpStatusCode("401");
		globalErrorResponse.setDeveloperMessage("Invalid authentication details were passed in api request.");
		globalErrorResponse.setDeveloperDocLink("https://github.com/keithwoodlock/mifosx/wiki/HTTP-API-Error-codes");
		globalErrorResponse.setUserMessageGlobalisationCode("error.msg.not.authenticated");
		globalErrorResponse.setDefaultUserMessage("Unauthenticated. Please login.");
		
		return globalErrorResponse;
	}
	
	public static ApiGlobalErrorResponse invalidTenantIdentifier() {
		
		ApiGlobalErrorResponse globalErrorResponse = new ApiGlobalErrorResponse();
		globalErrorResponse.setHttpStatusCode("401");
		globalErrorResponse.setDeveloperMessage("Invalid tenant details were passed in api request.");
		globalErrorResponse.setDeveloperDocLink("https://github.com/keithwoodlock/mifosx/wiki/HTTP-API-Error-codes");
		globalErrorResponse.setUserMessageGlobalisationCode("error.msg.invalid.tenant.identifier");
		globalErrorResponse.setDefaultUserMessage("Invalide tenant identifier provided with request.");
		
		return globalErrorResponse;
	}

	public static ApiGlobalErrorResponse unAuthorized() {
		ApiGlobalErrorResponse globalErrorResponse = new ApiGlobalErrorResponse();
		globalErrorResponse.setHttpStatusCode("403");
		globalErrorResponse.setDeveloperMessage("The user associated with credentials passed on this request does not have sufficient privileges to perform this action.");
		globalErrorResponse.setDeveloperDocLink("https://github.com/keithwoodlock/mifosx/wiki/HTTP-API-Error-codes");
		globalErrorResponse.setUserMessageGlobalisationCode("error.msg.not.authorized");
		globalErrorResponse.setDefaultUserMessage("Insufficient privileges to perform this action.");
		
		List<ApiParameterError> errors = new ArrayList<ApiParameterError>();
		errors.add(ApiParameterError.generalError("error.msg.not.authorized", "Insufficient privileges to perform this action."));
		globalErrorResponse.setErrors(errors);
		
		return globalErrorResponse;
	}
	
	public static ApiGlobalErrorResponse domainRuleViolation(String globalisationMessageCode, String defaultUserMessage, Object... defaultUserMessageArgs) {
		ApiGlobalErrorResponse globalErrorResponse = new ApiGlobalErrorResponse();
		globalErrorResponse.setHttpStatusCode("403");
		globalErrorResponse.setDeveloperMessage("Request was understood but caused a domain rule violation.");
		globalErrorResponse.setDeveloperDocLink("https://github.com/keithwoodlock/mifosx/wiki/HTTP-API-Error-codes");
		globalErrorResponse.setUserMessageGlobalisationCode("validation.msg.domain.rule.violation");
		globalErrorResponse.setDefaultUserMessage("Errors contain reason for domain rule violation.");
		
		List<ApiParameterError> errors = new ArrayList<ApiParameterError>();
		errors.add(ApiParameterError.generalError(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs));
		globalErrorResponse.setErrors(errors);
		
		return globalErrorResponse;
	}
	

	public static ApiGlobalErrorResponse notFound(String globalisationMessageCode, String defaultUserMessage, Object... defaultUserMessageArgs) {
		
		ApiGlobalErrorResponse globalErrorResponse = new ApiGlobalErrorResponse();
		globalErrorResponse.setHttpStatusCode("404");
		globalErrorResponse.setDeveloperMessage("The requested resource is not available.");
		globalErrorResponse.setDeveloperDocLink("https://github.com/keithwoodlock/mifosx/wiki/HTTP-API-Error-codes");
		globalErrorResponse.setUserMessageGlobalisationCode("error.msg.resource.not.found");
		globalErrorResponse.setDefaultUserMessage("The requested resource is not available.");
		
		List<ApiParameterError> errors = new ArrayList<ApiParameterError>();
		errors.add(ApiParameterError.resourceIdentifierNotFound(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs));
		globalErrorResponse.setErrors(errors);
		
		return globalErrorResponse;
	}
	
	public static ApiGlobalErrorResponse dataIntegrityError(final String globalisationMessageCode, final String defaultUserMessage, final String parameterName, final Object... defaultUserMessageArgs) {
		
		ApiGlobalErrorResponse globalErrorResponse = new ApiGlobalErrorResponse();
		globalErrorResponse.setHttpStatusCode("403");
		globalErrorResponse.setDeveloperMessage("The request caused a data integrity issue to be fired by the database.");
		globalErrorResponse.setDeveloperDocLink("https://github.com/keithwoodlock/mifosx/wiki/HTTP-API-Error-codes");
		globalErrorResponse.setUserMessageGlobalisationCode(globalisationMessageCode);
		globalErrorResponse.setDefaultUserMessage(defaultUserMessage);
		
		List<ApiParameterError> errors = new ArrayList<ApiParameterError>();
		errors.add(ApiParameterError.parameterError(globalisationMessageCode, defaultUserMessage, parameterName, defaultUserMessageArgs));
		globalErrorResponse.setErrors(errors);
		
		return globalErrorResponse;
	}
	
	public static ApiGlobalErrorResponse badClientRequest(final String globalisationMessageCode, final String defaultUserMessage, 
			List<ApiParameterError> errors) {
		
		ApiGlobalErrorResponse globalErrorResponse = new ApiGlobalErrorResponse();
		globalErrorResponse.setHttpStatusCode("400");
		globalErrorResponse.setDeveloperMessage("The request was invalid. This typically will happen due to validation errors which are provided.");
		globalErrorResponse.setDeveloperDocLink("https://github.com/keithwoodlock/mifosx/wiki/HTTP-API-Error-codes");
		globalErrorResponse.setUserMessageGlobalisationCode(globalisationMessageCode);
		globalErrorResponse.setDefaultUserMessage(defaultUserMessage);
		
		globalErrorResponse.setErrors(errors);
		
		return globalErrorResponse;
	}
	
	public static ApiGlobalErrorResponse serverSideError(String globalisationMessageCode, String defaultUserMessage, Object... defaultUserMessageArgs) {
		
		ApiGlobalErrorResponse globalErrorResponse = new ApiGlobalErrorResponse();
		globalErrorResponse.setHttpStatusCode("500");
		globalErrorResponse.setDeveloperMessage("An unexpected error occured on the platform server.");
		globalErrorResponse.setDeveloperDocLink("https://github.com/keithwoodlock/mifosx/wiki/HTTP-API-Error-codes");
		globalErrorResponse.setUserMessageGlobalisationCode("error.msg.platform.server.side.error");
		globalErrorResponse.setDefaultUserMessage("An unexpected error occured on the platform server.");
		
		List<ApiParameterError> errors = new ArrayList<ApiParameterError>();
		errors.add(ApiParameterError.generalError(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs));
		globalErrorResponse.setErrors(errors);
		
		return globalErrorResponse;
	}
	
	protected ApiGlobalErrorResponse() {
		//
	}

	public ApiGlobalErrorResponse(final List<ApiParameterError> errors) {
		this.errors = errors;
	}

	@XmlElementWrapper(name="errors")
	@XmlElement(name="errorResponse")
	public List<ApiParameterError> getErrors() {
		return errors;
	}

	public void setErrors(List<ApiParameterError> errors) {
		this.errors = errors;
	}

	public String getDeveloperMessage() {
		return developerMessage;
	}

	public void setDeveloperMessage(String developerMessage) {
		this.developerMessage = developerMessage;
	}

	public String getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(String httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	public String getDefaultUserMessage() {
		return defaultUserMessage;
	}

	public void setDefaultUserMessage(String defaultUserMessage) {
		this.defaultUserMessage = defaultUserMessage;
	}

	public String getUserMessageGlobalisationCode() {
		return userMessageGlobalisationCode;
	}

	public void setUserMessageGlobalisationCode(String userMessageGlobalisationCode) {
		this.userMessageGlobalisationCode = userMessageGlobalisationCode;
	}

	public String getDeveloperDocLink() {
		return developerDocLink;
	}

	public void setDeveloperDocLink(String developerDocLink) {
		this.developerDocLink = developerDocLink;
	}
}