/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Test;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.mifosplatform.batch.service.BatchApiService;
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
