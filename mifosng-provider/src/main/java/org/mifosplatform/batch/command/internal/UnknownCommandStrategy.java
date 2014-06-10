package org.mifosplatform.batch.command.internal;

import org.mifosplatform.batch.command.CommandStrategy;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;

/**
 * Provides a default CommandStrategy by implementing
 * {@link org.mifosplatform.batch.command.CommandStrategy} 
 * in case there is no appropriate command strategy with requested
 * 'method' and 'resoureUrl'.
 * 
 * @author Rishabh Shukla
 */
public class UnknownCommandStrategy implements CommandStrategy {

	@Override
	public BatchResponse execute(BatchRequest batchRequest) {
		
		final BatchResponse batchResponse = new BatchResponse();
		
		batchResponse.setRequestId(batchRequest.getRequestId());
		batchResponse.setStatusCode(501);
		
		return batchResponse;
	}

	
}
