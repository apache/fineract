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
package org.apache.fineract.test.initializer.global;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class FinancialActivityMappingGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final Long FINANCIAL_ACTIVITY_ID_ASSET_TRANSFER = 100L;
    public static final Long GL_ACCOUNT_ID_ASSET_TRANSFER = 21L;

    private final FineractFeignClient fineractClient;

    @Override
    public void initialize() {
        PostFinancialActivityAccountsRequest request = new PostFinancialActivityAccountsRequest()
                .financialActivityId(FINANCIAL_ACTIVITY_ID_ASSET_TRANSFER).glAccountId(GL_ACCOUNT_ID_ASSET_TRANSFER);

        try {
            executeVoid(() -> fineractClient.mappingFinancialActivitiesToAccounts().createGLAccount(request, Map.of()));
            log.debug("Financial activity mapping created successfully");
        } catch (CallFailedRuntimeException e) {
            if (e.getStatus() == 403 && e.getDeveloperMessage() != null && e.getDeveloperMessage().contains("already exists")) {
                log.debug("Financial activity mapping already exists, skipping creation");
                return;
            }
            throw e;
        }
    }
}
