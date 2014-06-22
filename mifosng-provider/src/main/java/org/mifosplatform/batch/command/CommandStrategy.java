package org.mifosplatform.batch.command;

import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;

/**
 * An interface for various Command Strategies. It contains a single function
 * which returns appropriate response from a particular command strategy.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.command.internal.UnknownCommandStrategy
 */
public interface CommandStrategy {

	/**
	 * Returns an object of type {@link org.mifosplatform.batch.domain.BatchResponse}.
	 * This takes  {@link org.mifosplatform.batch.domain.BatchRequest} as it's single
	 * argument and provides appropriate response.
	 * 
	 * @param batchRequest
	 * @return BatchResponse
	 */
	public BatchResponse execute(BatchRequest batchRequest);
}
