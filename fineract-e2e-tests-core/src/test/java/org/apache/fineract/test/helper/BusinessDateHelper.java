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
package org.apache.fineract.test.helper;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.BusinessDateUpdateRequest;
import org.apache.fineract.client.models.BusinessDateUpdateResponse;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BusinessDateHelper {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DEFAULT_LOCALE = "en";
    public static final String BUSINESS_DATE = "BUSINESS_DATE";
    public static final String COB = "COB_DATE";

    private final FineractFeignClient fineractClient;

    public void setBusinessDate(String businessDate) {
        BusinessDateUpdateRequest businessDateRequest = defaultBusinessDateRequest().date(businessDate);
        try {
            BusinessDateUpdateResponse response = ok(
                    () -> fineractClient.businessDateManagement().updateBusinessDate(null, businessDateRequest, Map.of()));
            TestContext.INSTANCE.set(TestContextKey.BUSINESS_DATE_RESPONSE, response);
            ok(() -> fineractClient.businessDateManagement().getBusinessDate(BUSINESS_DATE, Map.of()));

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw e;
        }
    }

    public void setBusinessDateToday() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String today = formatter.format(Utils.now());
        setBusinessDate(today);
    }

    public BusinessDateUpdateRequest defaultBusinessDateRequest() {
        return new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE).dateFormat(DATE_FORMAT)
                .locale(DEFAULT_LOCALE);
    }

    public String getBusinessDate() {
        log.debug("Getting business date (using Feign)");
        BusinessDateResponse response = ok(() -> fineractClient.businessDateManagement().getBusinessDate(BUSINESS_DATE, Map.of()));
        return response.toString();
    }
}
