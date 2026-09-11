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
import org.apache.fineract.client.models.PostAccountsTypeAccountIdRequest;
import org.apache.fineract.client.models.PostProductsTypeRequest;

public class FeignShareAccountHelper {

    /** Share products and accounts are reached through the generic products/accounts resources, keyed by this type. */
    private static final String SHARE = "share";

    private final FineractFeignClient fineractClient;

    public FeignShareAccountHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Long createShareProduct(PostProductsTypeRequest request) {
        return ok(() -> fineractClient.products().createShareProduct(SHARE, request)).getResourceId();
    }

    public Long applyShareAccount(AccountRequest request) {
        return ok(() -> fineractClient.shareAccount().createShareAccount(SHARE, request)).getResourceId();
    }

    public void approve(Long shareAccountId) {
        command(shareAccountId, new PostAccountsTypeAccountIdRequest(), "approve");
    }

    public void activate(Long shareAccountId, String activatedDate, String dateFormat, String locale) {
        command(shareAccountId, new PostAccountsTypeAccountIdRequest().activatedDate(activatedDate).dateFormat(dateFormat).locale(locale),
                "activate");
    }

    private void command(Long shareAccountId, PostAccountsTypeAccountIdRequest request, String command) {
        ok(() -> fineractClient.shareAccount().handleCommandsShareAccount(SHARE, shareAccountId, request, command));
    }

    /** See {@link ShareAccountCommandsApi#redeemShares} for why this command needs its own request model. */
    public void redeemShares(Long shareAccountId, long shares, String requestedDate, String dateFormat, String locale) {
        ShareAccountCommandsApi.RedeemSharesRequest request = new ShareAccountCommandsApi.RedeemSharesRequest()//
                .requestedDate(requestedDate)//
                .dateFormat(dateFormat)//
                .locale(locale)//
                .requestedShares(shares);
        ok(() -> fineractClient.create(ShareAccountCommandsApi.class).redeemShares(SHARE, shareAccountId, request));
    }

    public GetAccountsTypeAccountIdResponse getShareAccount(Long shareAccountId) {
        return ok(() -> fineractClient.shareAccount().retrieveOneShareAccount(shareAccountId, SHARE));
    }
}
