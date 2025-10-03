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

import java.util.Collections;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignClientHelper {

    private final FineractFeignClient fineractClient;

    public FeignClientHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Long createClient(String firstName, String lastName) {
        String externalId = Utils.randomStringGenerator("EXT_", 7);
        String activationDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        PostClientsRequest request = new PostClientsRequest()//
                .officeId(1L)//
                .legalFormId(1L)//
                .firstname(firstName)//
                .lastname(lastName)//
                .externalId(externalId)//
                .active(true)//
                .activationDate(activationDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);

        return createClient(request);
    }

    public Long createClient(PostClientsRequest request) {
        PostClientsResponse response = ok(() -> fineractClient.clients().create6(request));
        return response.getClientId();
    }

    public GetClientsClientIdResponse getClient(Long clientId) {
        return ok(() -> fineractClient.clients().retrieveOne11(clientId, Collections.emptyMap()));
    }
}
