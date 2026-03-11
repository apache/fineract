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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostOfficesRequest;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.client.models.PutOfficesOfficeIdRequest;
import org.apache.fineract.client.models.PutOfficesOfficeIdResponse;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignOfficeHelper {

    public static final long HEAD_OFFICE_ID = 1L;

    private final FineractFeignClient fineractClient;

    public FeignOfficeHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostOfficesResponse createOffice(LocalDate openingDate) {
        PostOfficesRequest request = new PostOfficesRequest()//
                .parentId(HEAD_OFFICE_ID)//
                .name(Utils.uniqueRandomStringGenerator("O_", 9))//
                .openingDate(openingDate)//
                .dateFormat("yyyy-MM-dd")//
                .locale("en");
        return ok(() -> fineractClient.offices().createOffice(request));
    }

    public PostOfficesResponse createOffice(String externalId, LocalDate openingDate) {
        PostOfficesRequest request = new PostOfficesRequest()//
                .parentId(HEAD_OFFICE_ID)//
                .name(Utils.uniqueRandomStringGenerator("O_", 9))//
                .externalId(externalId)//
                .openingDate(openingDate)//
                .dateFormat("yyyy-MM-dd")//
                .locale("en");
        return ok(() -> fineractClient.offices().createOffice(request));
    }

    public GetOfficesResponse retrieveOffice(Long officeId) {
        return ok(() -> fineractClient.offices().retrieveOffice(officeId));
    }

    public GetOfficesResponse retrieveOfficeByExternalId(String externalId) {
        return ok(() -> fineractClient.offices().retrieveOfficeByExternalId(externalId));
    }

    public GetOfficesResponse getHeadOffice() {
        return ok(() -> fineractClient.offices().retrieveOffice(HEAD_OFFICE_ID));
    }

    public PutOfficesOfficeIdResponse updateOffice(Long officeId, String name, String openingDate) {
        PutOfficesOfficeIdRequest request = new PutOfficesOfficeIdRequest()//
                .name(name)//
                .openingDate(openingDate)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");
        return ok(() -> fineractClient.offices().updateOffice(officeId, request));
    }

    public PutOfficesOfficeIdResponse updateOfficeByExternalId(String externalId, String name, String openingDate) {
        PutOfficesOfficeIdRequest request = new PutOfficesOfficeIdRequest()//
                .name(name)//
                .openingDate(openingDate)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");
        return ok(() -> fineractClient.offices().updateOfficeWithExternalId(externalId, request));
    }

    public List<GetOfficesResponse> retrieveAllOffices() {
        return ok(() -> fineractClient.offices().retrieveOffices(false, null, null));
    }
}
