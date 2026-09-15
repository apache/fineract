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

import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.AccountRequest;
import org.apache.fineract.client.models.GetAccountsTypeAccountIdResponse;
import org.apache.fineract.client.models.PostAccountsTypeAccountIdRedeemRequest;
import org.apache.fineract.client.models.PostAccountsTypeAccountIdRequest;
import org.apache.fineract.client.models.PostAccountsTypeAccountIdResponse;
import org.apache.fineract.client.models.PostAccountsTypeResponse;

public class FeignShareAccountHelper {

    private final FineractFeignClient fineractClient;

    public FeignShareAccountHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostAccountsTypeResponse createShareAccount(AccountRequest request) {
        return ok(() -> fineractClient.shareAccount().createShareAccount("share", request));
    }

    public PostAccountsTypeAccountIdResponse approveShareAccount(Long shareAccountId) {
        return ok(() -> fineractClient.shareAccount().handleCommandsShareAccount("share", shareAccountId,
                new PostAccountsTypeAccountIdRequest(), "approve"));
    }

    public PostAccountsTypeAccountIdResponse activateShareAccount(Long shareAccountId, PostAccountsTypeAccountIdRequest request) {
        return ok(() -> fineractClient.shareAccount().handleCommandsShareAccount("share", shareAccountId, request, "activate"));
    }

    public PostAccountsTypeAccountIdResponse redeemShares(Long shareAccountId, PostAccountsTypeAccountIdRedeemRequest request) {
        return ok(() -> fineractClient.shareAccountV2().redeemShares("share", shareAccountId, request));
    }

    public GetAccountsTypeAccountIdResponse getShareAccount(Long shareAccountId) {
        return ok(() -> fineractClient.shareAccount().retrieveOneShareAccount(shareAccountId, "share"));
    }

}
