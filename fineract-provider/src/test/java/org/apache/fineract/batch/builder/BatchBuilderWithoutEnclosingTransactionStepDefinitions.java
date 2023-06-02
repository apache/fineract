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
package org.apache.fineract.batch.builder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.cucumber.java8.En;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.batch.service.BatchApiService;

public class BatchBuilderWithoutEnclosingTransactionStepDefinitions implements En {

    private BatchApiService batchApiService;

    private List<BatchRequest> requests;

    private UriInfo uriInfo;

    private List<BatchResponse> responses;

    public BatchBuilderWithoutEnclosingTransactionStepDefinitions() {
        Given("A batch request", () -> {
            this.batchApiService = mock(BatchApiService.class);

            this.uriInfo = mock(UriInfo.class);

            this.requests = Collections.singletonList(new BatchRequest());
        });

        When("The user calls the batch service handle request method without enclosing transaction", () -> {
            responses = this.batchApiService.handleBatchRequestsWithoutEnclosingTransaction(this.requests, uriInfo);
        });

        Then("The batch service handle request method without enclosing transaction should have been called", () -> {
            assertNotNull(responses);

            verify(this.batchApiService).handleBatchRequestsWithoutEnclosingTransaction(requests, uriInfo);
        });
    }
}
