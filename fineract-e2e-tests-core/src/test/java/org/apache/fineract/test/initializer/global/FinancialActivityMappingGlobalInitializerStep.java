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

import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.services.MappingFinancialActivitiesToAccountsApi;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FinancialActivityMappingGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final Long FINANCIAL_ACTIVITY_ID_ASSET_TRANSFER = 100L;
    public static final Long GL_ACCOUNT_ID_ASSET_TRANSFER = 21L;

    private final MappingFinancialActivitiesToAccountsApi mappingFinancialActivitiesToAccountsApi;

    @Override
    public void initialize() throws Exception {

        PostFinancialActivityAccountsRequest request = new PostFinancialActivityAccountsRequest()
                .financialActivityId(FINANCIAL_ACTIVITY_ID_ASSET_TRANSFER).glAccountId(GL_ACCOUNT_ID_ASSET_TRANSFER);
        mappingFinancialActivitiesToAccountsApi.createGLAccount(request).execute();
    }
}
