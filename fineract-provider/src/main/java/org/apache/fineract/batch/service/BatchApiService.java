/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.batch.service;

import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;

/**
 * Provides an interface for service class, that implements the method to handle
 * separate Batch Requests.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.apache.fineract.batch.domain.BatchRequest
 * @see org.apache.fineract.batch.domain.BatchResponse
 * @see BatchApiServiceImpl
 */
public interface BatchApiService {

    /**
     * Returns a list of {@link org.apache.fineract.batch.domain.BatchResponse}s
     * by getting the appropriate CommandStrategy for every
     * {@link org.apache.fineract.batch.domain.BatchRequest}. It will be used when
     * the Query Parameter "enclosingTransaction "is set to 'false'.
     * 
     * @param requestList
     * @param uriInfo
     * @return List&lt;BatchResponse&gt;
     */
    List<BatchResponse> handleBatchRequestsWithoutEnclosingTransaction(List<BatchRequest> requestList, UriInfo uriInfo);

    /**
     * returns a list of {@link org.apache.fineract.batch.domain.BatchResponse}s
     * by getting the appropriate CommandStrategy for every
     * {@link org.apache.fineract.batch.domain.BatchRequest}. It will be used when
     * the Query Parameter "enclosingTransaction "is set to 'true'. If one or
     * more of the requests are not completed properly then whole of the
     * transaction will be rolled back properly.
     * 
     * @param requestList
     * @param uriInfo
     * @return List&lt;BatchResponse&gt;
     */
    List<BatchResponse> handleBatchRequestsWithEnclosingTransaction(List<BatchRequest> requestList, UriInfo uriInfo);
}
