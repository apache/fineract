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

import java.time.LocalDate;
import java.util.UUID;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.client.models.PutOfficesOfficeIdResponse;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OfficeIntegrationTest {

    private final OfficeHelper officeHelper = new OfficeHelper();

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
}
