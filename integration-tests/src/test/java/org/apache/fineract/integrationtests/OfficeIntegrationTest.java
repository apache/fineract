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

import feign.Headers;
import feign.RequestLine;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.GetOfficesTemplateResponse;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.client.models.PutOfficesOfficeIdResponse;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OfficeIntegrationTest extends FeignIntegrationTest {

    private FeignOfficeHelper officeHelper;
    private RawOfficeApi rawOfficeApi;

    @BeforeAll
    public void setup() {
        officeHelper = new FeignOfficeHelper(fineractClient());
        rawOfficeApi = fineractClient().create(RawOfficeApi.class);
    }

    @Test
    public void testOfficeModification() {
        PostOfficesResponse createResponse = officeHelper.createOffice(LocalDate.of(2007, 7, 1));
        Long officeId = createResponse.getResourceId();

        String name = Utils.uniqueRandomStringGenerator("New_Office_", 4);
        String date = "02 July 2007";

        officeHelper.updateOffice(officeId, name, date);
        GetOfficesResponse updatedOffice = officeHelper.retrieveOffice(officeId);

        Assertions.assertEquals(name, updatedOffice.getName());
        Assertions.assertTrue(DateUtils.isEqual(updatedOffice.getOpeningDate(), LocalDate.of(2007, 7, 2)));
    }

    @Test
    public void testOfficeModificationWithExternalId() {
        String externalId = UUID.randomUUID().toString();
        PostOfficesResponse createResponse = officeHelper.createOffice(externalId, LocalDate.of(2007, 7, 1));
        Long officeId = createResponse.getResourceId();

        String name = Utils.uniqueRandomStringGenerator("New_Office_", 4);
        String date = "02 July 2007";

        PutOfficesOfficeIdResponse updateResult = officeHelper.updateOfficeByExternalId(externalId, name, date);
        Assertions.assertEquals(officeId, updateResult.getOfficeId());

        GetOfficesResponse updatedOffice = officeHelper.retrieveOffice(officeId);

        Assertions.assertEquals(name, updatedOffice.getName());
        Assertions.assertTrue(DateUtils.isEqual(updatedOffice.getOpeningDate(), LocalDate.of(2007, 7, 2)));
    }

    @Test
    public void testOfficeModificationAndFetchWithExternalId() {
        String externalId = UUID.randomUUID().toString();
        officeHelper.createOffice(externalId, LocalDate.of(2007, 7, 1));

        String name = Utils.uniqueRandomStringGenerator("New_Office_", 4);
        String date = "02 July 2007";

        officeHelper.updateOfficeByExternalId(externalId, name, date);
        GetOfficesResponse updatedOffice = officeHelper.retrieveOfficeByExternalId(externalId);

        Assertions.assertEquals(name, updatedOffice.getName());
        Assertions.assertTrue(DateUtils.isEqual(updatedOffice.getOpeningDate(), LocalDate.of(2007, 7, 2)));
    }

    @Test
    public void testRetrieveOfficeTemplate() {
        GetOfficesTemplateResponse template = ok(() -> fineractClient().offices().retrieveTemplateOffice());

        Assertions.assertNotNull(template);
        Assertions.assertNotNull(template.getOpeningDate());
        Assertions.assertNotNull(template.getAllowedParents());
        Assertions.assertFalse(template.getAllowedParents().isEmpty());
    }

    @Test
    public void testOfficeTemplateReturnsOpeningDateAsArray() {
        Map<String, Object> template = ok(rawOfficeApi::retrieveOfficeTemplate);

        Assertions.assertInstanceOf(List.class, template.get("openingDate"), "openingDate must be serialized as [year, month, day]");
    }

    @Test
    public void testAccountTransferTemplateKeepsOpeningDateAsIsoString() {
        Map<String, Object> template = ok(rawOfficeApi::accountTransferTemplate);
        Object fromOfficeValue = template.get("fromOffice");
        Assertions.assertInstanceOf(Map.class, fromOfficeValue);
        Map<?, ?> fromOffice = (Map<?, ?>) fromOfficeValue;
        Object openingDate = fromOffice.get("openingDate");

        Assertions.assertInstanceOf(String.class, openingDate, "fromOffice.openingDate must remain an ISO date string");

        Assertions.assertDoesNotThrow(() -> LocalDate.parse((String) openingDate));
    }

    @Headers("Accept: application/json")
    interface RawOfficeApi {

        @RequestLine("GET /v1/offices/template")
        Map<String, Object> retrieveOfficeTemplate();

        @RequestLine("GET /v1/accounttransfers/template?fromOfficeId=1&fromAccountType=2")
        Map<String, Object> accountTransferTemplate();
    }
}
