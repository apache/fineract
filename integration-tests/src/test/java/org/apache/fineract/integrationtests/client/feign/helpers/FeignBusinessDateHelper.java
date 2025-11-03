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
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.BusinessDateUpdateRequest;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;

public class FeignBusinessDateHelper {

    private final FineractFeignClient fineractClient;

    public FeignBusinessDateHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public BusinessDateResponse getBusinessDate(String type) {
        return ok(() -> fineractClient.businessDateManagement().getBusinessDate(type));
    }

    public void updateBusinessDate(String type, String date) {
        BusinessDateUpdateRequest request = new BusinessDateUpdateRequest()//
                .type(BusinessDateUpdateRequest.TypeEnum.fromValue(type))//
                .date(date)//
                .dateFormat("yyyy-MM-dd")//
                .locale(LoanTestData.LOCALE);

        ok(() -> fineractClient.businessDateManagement().updateBusinessDate(request, Collections.emptyMap()));
    }

    public void runAt(String date, Runnable action) {
        BusinessDateResponse originalDate = getBusinessDate("BUSINESS_DATE");
        try {
            updateBusinessDate("BUSINESS_DATE", date);
            action.run();
        } finally {
            if (originalDate != null && originalDate.getDate() != null) {
                updateBusinessDate("BUSINESS_DATE", originalDate.getDate().toString());
            }
        }
    }
}
