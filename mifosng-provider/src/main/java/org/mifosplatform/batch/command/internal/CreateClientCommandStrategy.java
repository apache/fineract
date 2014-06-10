package org.mifosplatform.batch.command.internal;

import org.mifosplatform.batch.command.CommandStrategy;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.exception.PlatformInternalServerException;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.mifosplatform.portfolio.client.api.ClientsApiResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements {@link org.mifosplatform.batch.command.CommandStrategy} to handle
 * creation of a new client. It passes the contents of the body from the BatchRequest
 * to {@link org.mifosplatform.portfolio.client.api.ClientsApiResource} and gets back
 * the response. This class will also catch any errors raised by 
 * {@link org.mifosplatform.portfolio.client.api.ClientsApiResource} and map those errors
 * to appropriate status codes in BatchResponse.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.command.CommandStrategy
 * @see org.mifosplatform.batch.domain.BatchRequest
 * @see org.mifosplatform.batch.domain.BatchResponse
 */
public class CreateClientCommandStrategy implements CommandStrategy{

	private final ClientsApiResource clientsApiResource;
	
	@Autowired
	public CreateClientCommandStrategy(final ClientsApiResource clientsApiResource) {
		this.clientsApiResource = clientsApiResource;
	}
	
	@Override
	public BatchResponse execute(final BatchRequest request) {
		
		final BatchResponse response = new BatchResponse();	
		final String responseBody;
		
		// try-catch blocks to map exceptions to appropriate status codes
		try {
			
			//calls 'create' function from 'ClientsApiResource' to create a new client
			responseBody = clientsApiResource.create(request.getBody());
			
			response.setRequestId(request.getRequestId());
			response.setHeaders(request.getHeaders());
			response.setStatusCode(200);
			//sets the body of the response after the successful creation of the client
			response.setBody(responseBody);
			
		} catch (AbstractPlatformResourceNotFoundException e) {
			
			response.setStatusCode(404);
			response.setBody("error : " + e.toString());
			
		} catch (UnsupportedParameterException e) {
			
			response.setStatusCode(400);
			response.setBody("error : " + e.toString());
			
		} catch(PlatformApiDataValidationException e) {
			
			response.setStatusCode(400);
			response.setBody("error : " + e.toString());
			
		} catch (PlatformDataIntegrityException e) {
			
			response.setStatusCode(403);
			response.setBody("error : " + e.toString());
			
		} catch (PlatformInternalServerException e) {
			
			response.setStatusCode(500);
			response.setBody("error : " + e.toString());			
		}		
		
		return response;		
	}

}
