package org.mifosplatform.batch.service;

import java.util.List;

import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;

/**
 * Provides an interface for service class, that implements the method to 
 * handle separate Batch Requests.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.domain.BatchRequest
 * @see org.mifosplatform.batch.domain.BatchResponse
 * @see BatchApiServiceImpl
 */
public interface BatchApiService {
	
	/**
	 * Returns a list of {@link org.mifosplatform.batch.domain.BatchResponse}s by getting
	 * the appropriate CommandStrategy for every {@link org.mifosplatform.batch.domain.BatchRequest}.
	 * It will be used when the Query Parameter "enclosingTransaction "is set to 'false'.
	 * 
	 * @param requestList
	 * @return List<BatchResponse>
	 */	
	List<BatchResponse> handleBatchRequestsWithoutEnclosingTransaction(List<BatchRequest> requestList);	
	
	/**
	 * returns a list of {@link org.mifosplatform.batch.domain.BatchResponse}s by getting
	 * the appropriate CommandStrategy for every {@link org.mifosplatform.batch.domain.BatchRequest}.
	 * It will be used when the Query Parameter "enclosingTransaction "is set to 'true'. If one or
	 * more of the requests are not completed properly then whole of the transaction will be rolled back
	 * properly. 
	 * 
	 * @param requestList
	 * @return List<BatchResponse>
	 */	
	List<BatchResponse> handleBatchRequestsWithEnclosingTransaction(List<BatchRequest> requestList);
}
