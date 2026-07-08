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
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ExternalAssetOwnerRequest;
import org.apache.fineract.client.models.ExternalTransferOwnerData;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PostExternalAssetOwnerRequest;
import org.apache.fineract.client.models.PostExternalAssetOwnerResponse;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.integrationtests.common.accounting.Account;

public class FeignExternalAssetOwnerHelper {

    private final FineractFeignClient fineractClient;

    public FeignExternalAssetOwnerHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostExternalAssetOwnerResponse createExternalAssetOwner(PostExternalAssetOwnerRequest request) {
        return ok(() -> fineractClient.externalAssetOwners().createExternalAssetOwner(request));
    }

    public List<ExternalTransferOwnerData> retrieveExternalAssetOwners() {
        return ok(() -> fineractClient.externalAssetOwners().retrieveExternalAssetOwners());
    }

    public PostInitiateTransferResponse initiateTransferByLoanId(Long loanId, String command, ExternalAssetOwnerRequest request) {
        return ok(() -> fineractClient.externalAssetOwners().transferRequestWithLoanId(loanId, request, command));
    }

    public void setProperFinancialActivity(Account transferAccount) {
        List<GetFinancialActivityAccountsResponse> financialMappings = ok(
                () -> fineractClient.mappingFinancialActivitiesToAccounts().retrieveAll());
        financialMappings.forEach(mapping -> ok(() -> fineractClient.mappingFinancialActivitiesToAccounts()
                .deleteGLAccountMappingFinancialActivityAccount(mapping.getId())));
        ok(() -> fineractClient.mappingFinancialActivitiesToAccounts()
                .createGLAccountMappingFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                        .financialActivityId((long) AccountingConstants.FinancialActivity.ASSET_TRANSFER.getValue())
                        .glAccountId((long) transferAccount.getAccountID())));
    }
}
