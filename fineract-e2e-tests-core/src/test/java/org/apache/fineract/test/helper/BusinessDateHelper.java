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

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.services.BusinessDateManagementApi;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@RequiredArgsConstructor
@Component
public class BusinessDateHelper {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DEFAULT_LOCALE = "en";
    public static final String BUSINESS_DATE = "BUSINESS_DATE";
    public static final String BUSINESS_DATE_REQUEST_TYPE = "BUSINESS_DATE";

    private final BusinessDateManagementApi businessDateManagementApi;

    public void setBusinessDate(String businessDate) throws IOException {
        BusinessDateRequest businessDateRequest = defaultBusinessDateRequest().date(businessDate);

        Response<BusinessDateResponse> businessDateRequestResponse = businessDateManagementApi.updateBusinessDate(businessDateRequest)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(businessDateRequestResponse);
        TestContext.INSTANCE.set(TestContextKey.BUSINESS_DATE_RESPONSE, businessDateRequestResponse);
    }

    public void setBusinessDateToday() throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String today = formatter.format(LocalDate.now(Clock.systemUTC()));
        setBusinessDate(today);
    }

    public BusinessDateRequest defaultBusinessDateRequest() {
        return new BusinessDateRequest().type(BUSINESS_DATE_REQUEST_TYPE).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);
    }
}
