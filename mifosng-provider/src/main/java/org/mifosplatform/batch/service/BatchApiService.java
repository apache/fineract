/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch.service;

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;

/**
 * Provides an interface for service class, that implements the method to handle
 * separate Batch Requests.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.domain.BatchRequest
 * @see org.mifosplatform.batch.domain.BatchResponse
 * @see BatchApiServiceImpl
 */
public interface BatchApiService {

    /**
     * Returns a list of {@link org.mifosplatform.batch.domain.BatchResponse}s
     * by getting the appropriate CommandStrategy for every
     * {@link org.mifosplatform.batch.domain.BatchRequest}. It will be used when
     * the Query Parameter "enclosingTransaction "is set to 'false'.
     * 
     * @param requestList
     * @param uriInfo
     * @return List<BatchResponse>
     */
    List<BatchResponse> handleBatchRequestsWithoutEnclosingTransaction(List<BatchRequest> requestList, UriInfo uriInfo);

    /**
     * returns a list of {@link org.mifosplatform.batch.domain.BatchResponse}s
     * by getting the appropriate CommandStrategy for every
     * {@link org.mifosplatform.batch.domain.BatchRequest}. It will be used when
     * the Query Parameter "enclosingTransaction "is set to 'true'. If one or
     * more of the requests are not completed properly then whole of the
     * transaction will be rolled back properly.
     * 
     * @param requestList
     * @param uriInfo
     * @return List<BatchResponse>
     */
    List<BatchResponse> handleBatchRequestsWithEnclosingTransaction(List<BatchRequest> requestList, UriInfo uriInfo);
}
