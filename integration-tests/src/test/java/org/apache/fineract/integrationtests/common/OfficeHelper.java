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
package org.apache.fineract.integrationtests.common;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.time.LocalDate;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostOfficesRequest;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.client.models.PutOfficesOfficeIdRequest;
import org.apache.fineract.client.models.PutOfficesOfficeIdResponse;

public class OfficeHelper {

    public static final long HEAD_OFFICE_ID = 1L; // The ID is hardcoded in the initial Liquibase migration script

    public GetOfficesResponse retrieveOffice(Long officeId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().offices().retrieveOffice(officeId));
    }

    public static GetOfficesResponse getHeadOffice() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().offices().retrieveOffice(HEAD_OFFICE_ID));
    }

    public PostOfficesResponse createOffice(final LocalDate openingDate) {
        PostOfficesRequest request = new PostOfficesRequest()//
                .parentId(HEAD_OFFICE_ID)//
                .name(Utils.uniqueRandomStringGenerator("O_", 9))//
                .openingDate(openingDate)//
                .dateFormat("yyyy-MM-dd")//
                .locale("en");
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().offices().createOffice(request));
    }

    public PostOfficesResponse createOffice(final String externalId, final LocalDate openingDate) {
        PostOfficesRequest request = new PostOfficesRequest()//
                .parentId(HEAD_OFFICE_ID)//
                .name(Utils.uniqueRandomStringGenerator("O_", 9))//
                .externalId(externalId)//
                .openingDate(openingDate)//
                .dateFormat("yyyy-MM-dd")//
                .locale("en");
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().offices().createOffice(request));
    }

    public PutOfficesOfficeIdResponse updateOffice(Long officeId, String name, String openingDate) {
        PutOfficesOfficeIdRequest request = new PutOfficesOfficeIdRequest()//
                .name(name)//
                .openingDate(openingDate)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().offices().updateOffice(officeId, request));
    }

    public GetOfficesResponse retrieveOfficeByExternalId(String externalId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().offices().retrieveOfficeByExternalId(externalId));
    }

    public PutOfficesOfficeIdResponse updateOfficeByExternalId(String externalId, String name, String openingDate) {
        PutOfficesOfficeIdRequest request = new PutOfficesOfficeIdRequest()//
                .name(name)//
                .openingDate(openingDate)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().offices().updateOfficeWithExternalId(externalId, request));
    }
}
