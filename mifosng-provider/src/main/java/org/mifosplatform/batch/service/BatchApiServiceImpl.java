package org.mifosplatform.batch.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.batch.command.CommandContext;
import org.mifosplatform.batch.command.CommandStrategy;
import org.mifosplatform.batch.command.CommandStrategyProvider;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	
	/**
	 * Constructs a 'BatchApiServiceImpl' with an argument of 
	 * {@link org.mifosplatform.batch.command.CommandStrategyProvider} type.
	 * 
	 * @param strategyProvider
	 */
	@Autowired
	public BatchApiServiceImpl(final CommandStrategyProvider strategyProvider) {
		this.strategyProvider = strategyProvider;
	}

	private List<BatchResponse> handleBatchRequests(final List<BatchRequest> requestList) {

		final  List<BatchResponse> responseList = new ArrayList<>(requestList.size());
		
		for(final BatchRequest br: requestList) {
			
			final CommandStrategy commandStrategy = this.strategyProvider.getCommandStrategy(CommandContext.
					resource(br.getRelativeUrl()).method(br.getMethod()).build());
			
			final BatchResponse response = commandStrategy.execute(br);
			
			responseList.add(response);
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
