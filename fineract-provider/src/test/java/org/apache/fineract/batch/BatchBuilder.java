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
package org.apache.fineract.batch;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.batch.service.BatchApiService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Runs a unit test for BatchApiResource by mocking the BatchRequest and
 * BatchApiService objects.
 * 
 * @author Rishabh Shukla
 */
public class BatchBuilder {

    // Verify a non-empty response by BatchApiResource.
    @Test
    public void batchApiTest() {

        // Mock a BatchRequest objects
        final BatchRequest batchTest = Mockito.mock(BatchRequest.class);

        // Mock a BatchApiService object
        final BatchApiService serviceTest = Mockito.mock(BatchApiService.class);

        // Mock a UriInfo object
        final UriInfo uriInfo = Mockito.mock(UriInfo.class);

        final List<BatchRequest> requestList = new ArrayList<>();
        requestList.add(batchTest);

        // Call the BatchApiService using mocked objects
        final List<BatchResponse> result = serviceTest.handleBatchRequestsWithoutEnclosingTransaction(requestList, uriInfo);

        // Verifies whether handleBatchRequests() function of BatchApiService
        // was called
        Mockito.verify(serviceTest).handleBatchRequestsWithoutEnclosingTransaction(requestList, uriInfo);

        // Verifies a non-empty response by the BatchApiResource
        Assert.assertNotNull(result);
    }
}
