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
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.BusinessDateUpdateRequest;
import org.apache.fineract.client.models.BusinessDateUpdateResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;

@Slf4j
public final class BusinessDateHelper {

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";

    public BusinessDateHelper() {}

    public static BusinessDateUpdateResponse updateBusinessDate(final BusinessDateUpdateRequest request) {
        log.info("------------------UPDATE BUSINESS DATE----------------------");
        log.info("------------------Type: {}, date: {}----------------------", request.getType(), request.getDate());
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().businessDateManagement().updateBusinessDate(request));
    }

    public static BusinessDateUpdateResponse updateBusinessDate(final BusinessDateType type, final LocalDate date) {
        return updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.valueOf(type.name()))
                .date(Utils.dateFormatter.format(date)).dateFormat(Utils.DATE_FORMAT).locale("en"));
    }

    public BusinessDateResponse getBusinessDate(final String type) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().businessDateManagement().getBusinessDate(type));
    }

    public List<BusinessDateResponse> getBusinessDates() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().businessDateManagement().getBusinessDates());
    }

    public static void runAt(String date, Runnable runnable) {
        try {
            new FeignGlobalConfigurationHelper(FineractFeignClientHelper.getFineractFeignClient()).updateGlobalConfiguration(
                    GlobalConfigurationConstants.ENABLE_BUSINESS_DATE, new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE).date(date)
                    .dateFormat(DATETIME_PATTERN).locale("en"));
            runnable.run();
        } finally {
            new FeignGlobalConfigurationHelper(FineractFeignClientHelper.getFineractFeignClient()).updateGlobalConfiguration(
                    GlobalConfigurationConstants.ENABLE_BUSINESS_DATE, new PutGlobalConfigurationsRequest().enabled(false));
        }
    }
}
