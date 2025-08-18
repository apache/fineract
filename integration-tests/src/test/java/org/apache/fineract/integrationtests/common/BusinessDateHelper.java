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

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.BusinessDateUpdateRequest;
import org.apache.fineract.client.models.BusinessDateUpdateResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;

@Slf4j
public final class BusinessDateHelper {

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static final Gson GSON = new JSON().getGson();

    public BusinessDateHelper() {}

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static HashMap updateBusinessDate(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final BusinessDateType type, final LocalDate date) {
        final String BUSINESS_DATE_API = "/fineract-provider/api/v1/businessdate?" + Utils.TENANT_IDENTIFIER;
        log.info("------------------UPDATE BUSINESS DATE----------------------");
        log.info("------------------Type: {}, date: {}----------------------", type, date);
        return Utils.performServerPost(requestSpec, responseSpec, BUSINESS_DATE_API, buildBusinessDateRequest(type, date), "changes");
    }

    public static BusinessDateUpdateResponse updateBusinessDate(final BusinessDateUpdateRequest request) {
        log.info("------------------UPDATE BUSINESS DATE----------------------");
        log.info("------------------Type: {}, date: {}----------------------", request.getType(), request.getDate());
        return Calls.ok(FineractClientHelper.getFineractClient().businessDateManagement.updateBusinessDate(null, request));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public BusinessDateResponse getBusinessDateByType(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final BusinessDateType type) {
        final String BUSINESS_DATE_API = "/fineract-provider/api/v1/businessdate/" + type.name() + "?" + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerGet(requestSpec, responseSpec, BUSINESS_DATE_API);
        log.info("{}", response);
        return GSON.fromJson(response, BusinessDateResponse.class);
    }

    public BusinessDateResponse getBusinessDate(final String type) {
        return Calls.ok(FineractClientHelper.getFineractClient().businessDateManagement.getBusinessDate(type));
    }

    public List<BusinessDateResponse> getBusinessDates() {
        return Calls.ok(FineractClientHelper.getFineractClient().businessDateManagement.getBusinessDates());
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static String buildBusinessDateRequest(BusinessDateType type, LocalDate date) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("type", type.name());
        map.put("date", Utils.dateFormatter.format(date));
        map.put("dateFormat", Utils.DATE_FORMAT);
        map.put("locale", "en");
        log.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    public static void runAt(String date, Runnable runnable) {
        try {
            new GlobalConfigurationHelper().updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE).date(date)
                    .dateFormat(DATETIME_PATTERN).locale("en"));
            runnable.run();
        } finally {
            new GlobalConfigurationHelper().updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

}
