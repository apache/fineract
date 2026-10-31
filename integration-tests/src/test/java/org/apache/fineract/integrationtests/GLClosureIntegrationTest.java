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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.time.LocalDate;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.DeleteGlClosuresResponse;
import org.apache.fineract.client.models.GetGlClosureResponse;
import org.apache.fineract.client.models.PostGlClosuresRequest;
import org.apache.fineract.client.models.PostGlClosuresResponse;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.client.models.PutGlClosuresResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GLClosureIntegrationTest extends FeignIntegrationTest {

    private FeignOfficeHelper officeHelper;
    private GLClosureCustomApi glClosureCustomApi;

    @Headers({ "Accept: application/json", "Content-Type: application/json" })
    public interface GLClosureCustomApi {

        @RequestLine("POST /v1/glclosures")
        PostGlClosuresResponse createRaw(Map<String, Object> request);

        @RequestLine("PUT /v1/glclosures/{id}")
        PutGlClosuresResponse updateRaw(@Param("id") Long id, Map<String, Object> request);
    }

    @BeforeAll
    public void setupGLClosureHelpers() {
        officeHelper = new FeignOfficeHelper(fineractClient());
        glClosureCustomApi = fineractClient().create(GLClosureCustomApi.class);
    }

    @Test
    public void testGLClosureLifecycleWithDocumentedDateFormat() {
        // 1. Setup isolated branch office
        final PostOfficesResponse officeResponse = officeHelper.createOffice(LocalDate.of(2010, 1, 1));
        final Long officeId = officeResponse.getResourceId();
        assertNotNull(officeId);

        // 2. Create accounting closure using documented payload format ("06 December 2012", "dd MMMM yyyy", "en")
        final Map<String, Object> createPayload = Map.of("officeId", officeId, "closingDate", "06 December 2012", "dateFormat",
                "dd MMMM yyyy", "locale", "en", "comments", "Documented format closure test");
        final PostGlClosuresResponse createResponse = FeignCalls.ok(() -> glClosureCustomApi.createRaw(createPayload));
        final Long closureId = createResponse.getResourceId();
        assertNotNull(closureId);

        // 3. Retrieve and assert parsed date and properties
        final GetGlClosureResponse retrieved = FeignCalls.ok(() -> fineractClient().accountingClosure().retrieveOneGLClosure(closureId));
        assertEquals(officeId, retrieved.getOfficeId());
        assertEquals(LocalDate.of(2012, 12, 6), retrieved.getClosingDate());
        assertEquals("Documented format closure test", retrieved.getComments());

        // 4. Verify PUT without comments ({}) is rejected with 400 Bad Request
        final CallFailedRuntimeException emptyPutError = FeignCalls.fail(() -> glClosureCustomApi.updateRaw(closureId, Map.of()));
        assertEquals(400, emptyPutError.getStatus());

        // 5. Update comments
        final Map<String, Object> updatePayload = Map.of("comments", "Updated closure comments");
        final PutGlClosuresResponse updateResponse = FeignCalls.ok(() -> glClosureCustomApi.updateRaw(closureId, updatePayload));
        assertNotNull(updateResponse);

        final GetGlClosureResponse updated = FeignCalls.ok(() -> fineractClient().accountingClosure().retrieveOneGLClosure(closureId));
        assertEquals("Updated closure comments", updated.getComments());

        // 6. Delete closure
        final DeleteGlClosuresResponse deleteResponse = FeignCalls
                .ok(() -> fineractClient().accountingClosure().deleteGLClosure(closureId));
        assertEquals(closureId, deleteResponse.getResourceId());

        final CallFailedRuntimeException getAfterDeleteError = FeignCalls
                .fail(() -> fineractClient().accountingClosure().retrieveOneGLClosure(closureId));
        assertEquals(404, getAfterDeleteError.getStatus());
    }

    @Test
    public void testGLClosureCreateWithStandardClientModel() {
        // 1. Setup isolated branch office
        final PostOfficesResponse officeResponse = officeHelper.createOffice(LocalDate.of(2010, 1, 1));
        final Long officeId = officeResponse.getResourceId();
        assertNotNull(officeId);

        // 2. Create accounting closure with standard generated client model
        final PostGlClosuresRequest request = new PostGlClosuresRequest().officeId(officeId).closingDate(LocalDate.of(2013, 5, 10))
                .dateFormat("yyyy-MM-dd").locale("en").comments("Standard model closure");

        final PostGlClosuresResponse createResponse = FeignCalls.ok(() -> fineractClient().accountingClosure().createGLClosure(request));
        final Long closureId = createResponse.getResourceId();
        assertNotNull(closureId);

        // 3. Retrieve and assert
        final GetGlClosureResponse retrieved = FeignCalls.ok(() -> fineractClient().accountingClosure().retrieveOneGLClosure(closureId));
        assertEquals(LocalDate.of(2013, 5, 10), retrieved.getClosingDate());

        // 4. Cleanup
        FeignCalls.ok(() -> fineractClient().accountingClosure().deleteGLClosure(closureId));
    }
}
