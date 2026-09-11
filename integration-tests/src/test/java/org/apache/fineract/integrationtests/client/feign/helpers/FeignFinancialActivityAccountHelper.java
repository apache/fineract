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

import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DeleteFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostFinancialActivityAccountsResponse;
import org.apache.fineract.integrationtests.common.accounting.Account;

public class FeignFinancialActivityAccountHelper {

    private final FineractFeignClient fineractClient;

    public FeignFinancialActivityAccountHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostFinancialActivityAccountsResponse createMapping(Integer financialActivityId, Account glAccount) {
        PostFinancialActivityAccountsRequest request = new PostFinancialActivityAccountsRequest()//
                .financialActivityId(financialActivityId.longValue())//
                .glAccountId(glAccount.getAccountID().longValue());
        return ok(() -> fineractClient.mappingFinancialActivitiesToAccounts().createGLAccountMappingFinancialActivityAccount(request));
    }

    public GetFinancialActivityAccountsResponse getMapping(Long mappingId) {
        return ok(() -> fineractClient.mappingFinancialActivitiesToAccounts().retreive(mappingId));
    }

    public List<GetFinancialActivityAccountsResponse> getAllMappings() {
        return ok(() -> fineractClient.mappingFinancialActivitiesToAccounts().retrieveAll());
    }

    public DeleteFinancialActivityAccountsResponse deleteMapping(Long mappingId) {
        return ok(() -> fineractClient.mappingFinancialActivitiesToAccounts().deleteGLAccountMappingFinancialActivityAccount(mappingId));
    }
}
