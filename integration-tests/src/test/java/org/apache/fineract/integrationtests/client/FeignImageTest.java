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
package org.apache.fineract.integrationtests.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import feign.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.CreateStaffResponse;
import org.apache.fineract.client.models.StaffRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeignImageTest extends FeignIntegrationTest {

    final File testImage = new File(getClass().getResource("/michael.vorburger-crepes.jpg").getFile());

    Long staffId;

    @Override
    protected FineractFeignClient fineractClient() {
        return org.apache.fineract.integrationtests.common.FineractFeignClientHelper.createNewFineractFeignClient("mifos", "password",
                true);
    }

    @Test
    @Order(1)
    void setupStaff() {
        StaffRequest request = new StaffRequest();
        request.setOfficeId(1L);
        request.setFirstname("Feign");
        request.setLastname("ImageTest" + System.currentTimeMillis());
        request.setJoiningDate(LocalDate.now(ZoneId.of("UTC")).toString());
        request.setDateFormat("yyyy-MM-dd");
        request.setLocale("en_US");

        CreateStaffResponse response = ok(() -> fineractClient().staff().create3(request));
        assertThat(response).isNotNull();
        assertThat(response.getResourceId()).isNotNull();
        staffId = response.getResourceId();
    }

    @Test
    @Order(2)
    void testCreateStaffImage() throws Exception {
        String dataUrl = org.apache.fineract.client.feign.services.ImagesApi.prepareFileUpload(testImage);
        Response response = fineractClient().images().create("staff", staffId, dataUrl);

        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @Test
    @Order(3)
    void testRetrieveStaffImage() throws IOException {
        Response response = fineractClient().images().get("staff", staffId, new HashMap<>());

        assertNotNull(response);
        assertEquals(200, response.status());

        try (InputStream inputStream = response.body().asInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            assertThat(bytes.length).isGreaterThan(0);
        }
    }

    @Test
    @Order(4)
    void testUpdateStaffImage() {
        Response response = fineractClient().images().update("staff", staffId,
                org.apache.fineract.client.feign.services.ImagesApi.prepareFileUpload(testImage));

        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @Test
    @Order(99)
    void testDeleteStaffImage() {
        Response response = fineractClient().images().delete("staff", staffId);

        assertNotNull(response);
        assertEquals(200, response.status());
    }
}
