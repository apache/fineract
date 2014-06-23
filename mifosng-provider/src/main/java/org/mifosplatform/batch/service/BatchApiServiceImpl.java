package org.mifosplatform.batch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response.Status;

import org.mifosplatform.batch.command.CommandContext;
import org.mifosplatform.batch.command.CommandStrategy;
import org.mifosplatform.batch.command.CommandStrategyProvider;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.mifosplatform.batch.exception.ErrorInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;

/**
 * Implementation for {@link BatchApiService} to iterate through all the incoming
 * requests and obtain the appropriate CommandStrategy from CommandStrategyProvider. 
 * 
 * @author Rishabh Shukla
 *
 * @see org.mifosplatform.batch.domain.BatchRequest
 * @see org.mifosplatform.batch.domain.BatchResponse
 * @see org.mifosplatform.batch.command.CommandStrategyProvider
 */
@Service
public class BatchApiServiceImpl implements BatchApiService{
	
	private final CommandStrategyProvider strategyProvider;
	private final ResolutionHelper resolutionHelper;
	
	/**
	 * Constructs a 'BatchApiServiceImpl' with an argument of 
	 * {@link org.mifosplatform.batch.command.CommandStrategyProvider} type.
	 * 
	 * @param strategyProvider
	 */
	@Autowired
	public BatchApiServiceImpl(final CommandStrategyProvider strategyProvider,
			final ResolutionHelper resolutionHelper) {
		this.strategyProvider = strategyProvider;
		this.resolutionHelper = resolutionHelper;
	}

	/**
	 * Returns the response list by getting a proper {@link org.mifosplatform.batch.command.CommandStrategy}.
	 * execute() method of acquired commandStrategy is then provided with the separate Request.
	 * 
	 * @param requestList
	 * @return List<BatchResponse>
	 */
	private List<BatchResponse> handleBatchRequests(final List<BatchRequest> requestList) {

		final List<BatchResponse> responseList = new ArrayList<>();
		
		final ConcurrentHashMap<Long, ArrayList<BatchRequest>> requestMap = this.resolutionHelper.getDependencyMap(requestList);
				
		for(ConcurrentHashMap.Entry<Long, ArrayList<BatchRequest>> entry : requestMap.entrySet()) {
			
			final ArrayList<BatchRequest> mappedList = entry.getValue();
			
			for(BatchRequest br: mappedList) {
				
				//If this request is dependent on any other request in the list
				if(br.getReference() != null) {
					
					BatchResponse parentResponse = new BatchResponse();
					
					//Fetch the response of the request on which it is dependent from responseList
					for(final BatchResponse res: responseList) {
						
						if(res.getRequestId().equals(br.getReference())) {								
							parentResponse = res;
						}
					}
					
					//If parentResponse has returned a successful request
					if(parentResponse.getStatusCode() == 200) {
						br = this.resolutionHelper.resoluteRequest(br, parentResponse);
					} else {
						//Something went wrong with the parent request, create a response with status code 409
						final BatchResponse response = new BatchResponse();
						response.setRequestId(br.getRequestId());
						response.setStatusCode(Status.CONFLICT.getStatusCode());
						
						//Some detail information about the error
						final ErrorInfo conflictError = new ErrorInfo(Status.CONFLICT.getStatusCode(), 8001,
								"Parent request with id " + parentResponse.getRequestId() + " was erroneous!");
						response.setBody(new Gson().toJson(conflictError));
						responseList.add(response);
						
						//Proceed with next list entry
						continue;
					}
				}
				
				final CommandStrategy commandStrategy = this.strategyProvider.getCommandStrategy(CommandContext.
						resource(br.getRelativeUrl()).method(br.getMethod()).build());
				
				final BatchResponse response = commandStrategy.execute(br);
								
				responseList.add(response);
			}				
			
		}

		return responseList;
	}
	
	@Override
	public List<BatchResponse> handleBatchRequestsWithoutEnclosingTransaction(final
			List<BatchRequest> requestList) {
		
		return handleBatchRequests(requestList);
	}

	@Transactional
	@Override
	public List<BatchResponse> handleBatchRequestsWithEnclosingTransaction( final
			List<BatchRequest> requestList) {
		
		return handleBatchRequests(requestList);
	}
}
