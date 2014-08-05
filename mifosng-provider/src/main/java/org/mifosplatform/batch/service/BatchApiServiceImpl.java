/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.batch.command.CommandContext;
import org.mifosplatform.batch.command.CommandStrategy;
import org.mifosplatform.batch.command.CommandStrategyProvider;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.mifosplatform.batch.exception.ErrorHandler;
import org.mifosplatform.batch.exception.ErrorInfo;
import org.mifosplatform.batch.service.ResolutionHelper.BatchRequestNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.gson.Gson;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Implementation for {@link BatchApiService} to iterate through all the
 * incoming requests and obtain the appropriate CommandStrategy from
 * CommandStrategyProvider.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.domain.BatchRequest
 * @see org.mifosplatform.batch.domain.BatchResponse
 * @see org.mifosplatform.batch.command.CommandStrategyProvider
 */
@Service
public class BatchApiServiceImpl implements BatchApiService {

    private final CommandStrategyProvider strategyProvider;
    private final ResolutionHelper resolutionHelper;
    private final TransactionTemplate transactionTemplate;
    private List<BatchResponse> checkList = new ArrayList<>();

    /**
     * Constructs a 'BatchApiServiceImpl' with an argument of
     * {@link org.mifosplatform.batch.command.CommandStrategyProvider} type.
     * 
     * @param strategyProvider
     * @param resolutionHelper
     * @param transactionTemplate
     */
    @Autowired
    public BatchApiServiceImpl(final CommandStrategyProvider strategyProvider, final ResolutionHelper resolutionHelper,
            final TransactionTemplate transactionTemplate) {
        this.strategyProvider = strategyProvider;
        this.resolutionHelper = resolutionHelper;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Returns the response list by getting a proper
     * {@link org.mifosplatform.batch.command.CommandStrategy}. execute() method
     * of acquired commandStrategy is then provided with the separate Request.
     * 
     * @param requestList
     * @param uriInfo
     * @return List<BatchResponse>
     */
    private List<BatchResponse> handleBatchRequests(final List<BatchRequest> requestList, final UriInfo uriInfo) {

        final List<BatchResponse> responseList = new ArrayList<>(requestList.size());

        final List<BatchRequestNode> batchRequestNodes = this.resolutionHelper.getDependingRequests(requestList);
        checkList.clear();

        for (BatchRequestNode rootNode : batchRequestNodes) {
            final BatchRequest rootRequest = rootNode.getRequest();
            final CommandStrategy commandStrategy = this.strategyProvider.getCommandStrategy(CommandContext
                    .resource(rootRequest.getRelativeUrl()).method(rootRequest.getMethod()).build());
            final BatchResponse rootResponse = commandStrategy.execute(rootRequest, uriInfo);

            responseList.add(rootResponse);
            responseList.addAll(this.processChildRequests(rootNode, rootResponse, uriInfo));
        }

        Collections.sort(responseList, new Comparator<BatchResponse>() {

            @Override
            public int compare(BatchResponse source, BatchResponse testee) {
                return source.getRequestId().compareTo(testee.getRequestId());
            }
        });

        checkList = responseList;
        return responseList;

    }

    private List<BatchResponse> processChildRequests(final BatchRequestNode rootRequest, BatchResponse rootResponse, UriInfo uriInfo) {

        final List<BatchResponse> childResponses = new ArrayList<>();
        if (rootRequest.getChildRequests().size() > 0) {

            for (BatchRequestNode childNode : rootRequest.getChildRequests()) {

                BatchRequest childRequest = childNode.getRequest();
                BatchResponse childResponse;

                try {

                    if (rootResponse.getStatusCode().equals(200)) {
                        childRequest = this.resolutionHelper.resoluteRequest(childRequest, rootResponse);
                        final CommandStrategy commandStrategy = this.strategyProvider.getCommandStrategy(CommandContext
                                .resource(childRequest.getRelativeUrl()).method(childRequest.getMethod()).build());

                        childResponse = commandStrategy.execute(childRequest, uriInfo);

                    } else {
                        // Something went wrong with the parent request, create
                        // a response with status code 409
                        childResponse = new BatchResponse();
                        childResponse.setRequestId(childRequest.getRequestId());
                        childResponse.setStatusCode(Status.CONFLICT.getStatusCode());

                        // Some detail information about the error
                        final ErrorInfo conflictError = new ErrorInfo(Status.CONFLICT.getStatusCode(), 8001, "Parent request with id "
                                + rootResponse.getRequestId() + " was erroneous!");
                        childResponse.setBody(conflictError.getMessage());
                    }
                    childResponses.addAll(this.processChildRequests(childNode, childResponse, uriInfo));

                } catch (Throwable ex) {

                    childResponse = new BatchResponse();
                    childResponse.setRequestId(childRequest.getRequestId());
                    childResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                    childResponse.setBody(ex.getMessage());
                }

                childResponses.add(childResponse);
            }
        }

        return childResponses;
    }

    @Override
    public List<BatchResponse> handleBatchRequestsWithoutEnclosingTransaction(final List<BatchRequest> requestList, UriInfo uriInfo) {

        return handleBatchRequests(requestList, uriInfo);
    }

    @Override
    public List<BatchResponse> handleBatchRequestsWithEnclosingTransaction(final List<BatchRequest> requestList, final UriInfo uriInfo) {

        try {
            return this.transactionTemplate.execute(new TransactionCallback<List<BatchResponse>>() {

                @Override
                public List<BatchResponse> doInTransaction(TransactionStatus status) {
                    try {
                        return handleBatchRequests(requestList, uriInfo);
                    } catch (RuntimeException ex) {

                        ErrorInfo e = ErrorHandler.handler(ex);
                        BatchResponse errResponse = new BatchResponse();
                        errResponse.setStatusCode(e.getStatusCode());
                        errResponse.setBody(e.getMessage());

                        List<BatchResponse> errResponseList = new ArrayList<>();
                        errResponseList.add(errResponse);

                        status.setRollbackOnly();
                        return errResponseList;
                    }
                }

            });
        } catch (TransactionException ex) {
            ErrorInfo e = ErrorHandler.handler(ex);
            BatchResponse errResponse = new BatchResponse();
            errResponse.setStatusCode(e.getStatusCode());

            for (BatchResponse res : checkList) {
                if (!res.getStatusCode().equals(200)) {
                    errResponse.setBody("Transaction is being rolled back. First erroneous request: \n" + new Gson().toJson(res));
                    break;
                }
            }

            checkList.clear();
            List<BatchResponse> errResponseList = new ArrayList<>();
            errResponseList.add(errResponse);

            return errResponseList;
        }

    }
}
