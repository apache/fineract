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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jayway.jsonpath.ReadContext;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.batch.exception.BatchReferenceInvalidException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.Test;

public class ResolutionHelperTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final ResolutionHelper resolutionHelper = new ResolutionHelper(fromJsonHelper);

    @Test
    void testBuildNodesTreeWithValidRequests() {
        List<BatchRequest> requests = new ArrayList<>();
        BatchRequest firstRequest = new BatchRequest();
        firstRequest.setRequestId(1L);
        firstRequest.setBody("{\"key1\": \"value1\", \"key2\": 12, \"key3\": null, \"key4\": false}");
        firstRequest.setRelativeUrl("/resource/id");
        requests.add(firstRequest);
        BatchRequest secondRequest = new BatchRequest();
        secondRequest.setRequestId(2L);
        secondRequest.setReference(1L);
        secondRequest.setRelativeUrl("/resource/id");
        secondRequest.setBody("{\"key1\": { \"subKey\": false }, \"key2\": [1,2,3]}");
        requests.add(secondRequest);
        BatchRequest thirdRequest = new BatchRequest();
        thirdRequest.setRequestId(3L);
        thirdRequest.setReference(2L);
        requests.add(thirdRequest);
        List<ResolutionHelper.BatchRequestNode> nodes = resolutionHelper.buildNodesTree(requests);
        assertEquals(1, nodes.size());
        assertEquals(1, nodes.get(0).getRequest().getRequestId());
        assertEquals("{\"key1\": \"value1\", \"key2\": 12, \"key3\": null, \"key4\": false}", nodes.get(0).getRequest().getBody());
        assertEquals(1, nodes.get(0).getChildNodes().size());
        assertEquals(2, nodes.get(0).getChildNodes().get(0).getRequest().getRequestId());
        assertEquals("{\"key1\": { \"subKey\": false }, \"key2\": [1,2,3]}", nodes.get(0).getChildNodes().get(0).getRequest().getBody());
    }

    @Test
    void testBuildNodesTreeWithInvalidReference() {
        List<BatchRequest> requests = new ArrayList<>();
        BatchRequest invalidRequest = new BatchRequest();
        invalidRequest.setRequestId(2L);
        // Not existing reference
        invalidRequest.setReference(1L);
        requests.add(invalidRequest);
        assertThrows(BatchReferenceInvalidException.class, () -> resolutionHelper.buildNodesTree(requests));
    }

    @Test
    void testResolveRequestWithValidDependenciesResolveParameterFromResponse() {
        BatchRequest batchRequest = new BatchRequest();
        batchRequest.setBody("{\"key1\": \"$.value1\"}");
        batchRequest.setRelativeUrl("/resource/$.id?key=value");
        BatchResponse parentResponse = new BatchResponse();
        parentResponse.setBody("{\"value1\": \"resolvedValue\", \"id\": \"123\"}");
        ReadContext readContext = mock(ReadContext.class);
        when(readContext.read("$.value1")).thenReturn("resolvedValue");
        when(readContext.read("$.id")).thenReturn("123");
        BatchRequest resolvedRequest = resolutionHelper.resolveRequest(batchRequest, parentResponse);
        assertNotNull(resolvedRequest);
        assertEquals("{\"key1\":\"resolvedValue\"}", resolvedRequest.getBody());
        assertEquals("/resource/123?key=value", resolvedRequest.getRelativeUrl());
    }

    @Test
    void testResolveRequestWithNoDependencies() {
        BatchRequest batchRequest = new BatchRequest();
        batchRequest.setBody("{\"key1\": \"value1\",\"key2\": { \"subKey\": false }, \"key3\": [1,2,3], \"key4\": null}");
        batchRequest.setRelativeUrl("/resource/id");
        BatchResponse parentResponse = new BatchResponse();
        parentResponse.setBody("{\"value2\": \"not used\"}");
        BatchRequest resolvedRequest = resolutionHelper.resolveRequest(batchRequest, parentResponse);
        assertNotNull(resolvedRequest);
        assertEquals("{\"key1\":\"value1\",\"key2\":{\"subKey\":false},\"key3\":[1,2,3],\"key4\":null}", resolvedRequest.getBody());
        assertEquals("/resource/id", resolvedRequest.getRelativeUrl());
    }
}
