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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.Map;
import java.util.Set;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.SelfDividendApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AccountRequest;
import org.apache.fineract.client.models.GetAccountsTypeAccountIdResponse;
import org.apache.fineract.client.models.GetProductsTypeProductIdResponse;
import org.apache.fineract.client.models.GetShareProductDividendDetailsResponse;
import org.apache.fineract.client.models.GetShareProductDividendsResponse;
import org.apache.fineract.client.models.PostAccountsRequestedShares;
import org.apache.fineract.client.models.PostAccountsTypeAccountIdRequest;
import org.apache.fineract.client.models.PostProductsTypeRequest;
import org.apache.fineract.client.models.PostShareProductDividendsRequest;
import org.apache.fineract.client.models.PutAccountsTypeAccountIdRequest;
import org.apache.fineract.client.models.PutProductsTypeProductIdRequest;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignShareAccountHelper {

    /** Share products and accounts are reached through the generic products/accounts resources, keyed by this type. */
    private static final String SHARE = "share";

    private final FineractFeignClient fineractClient;

    public FeignShareAccountHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    /** The share product the RestAssured {@code ShareProductHelper} built by default. */
    public static PostProductsTypeRequest defaultShareProduct() {
        return new PostProductsTypeRequest()//
                .name(Utils.uniqueRandomStringGenerator("SHARE_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description(Utils.randomStringGenerator("", 20))//
                .currencyCode("USD")//
                .locale("en_GB")//
                .digitsAfterDecimal(4)//
                .inMultiplesOf(0)//
                .totalShares(10000)//
                .sharesIssued(10000)//
                .unitPrice(2)//
                .minimumShares(10)//
                .nominalShares(20)//
                .maximumShares(3000)//
                .allowDividendCalculationForInactiveClients(true)//
                .accountingRule(SavingsTestData.AccountingRule.NONE)//
                .minimumActivePeriodForDividends(1)//
                .minimumactiveperiodFrequencyType(0)//
                .lockinPeriodFrequency(1)//
                .lockinPeriodFrequencyType(0);
    }

    public Long createShareProduct(PostProductsTypeRequest request) {
        return ok(() -> fineractClient.products().createShareProduct(SHARE, request)).getResourceId();
    }

    public GetProductsTypeProductIdResponse getShareProduct(Long productId) {
        return ok(() -> fineractClient.products().retrieveOneShareProduct(productId, SHARE));
    }

    public Long updateShareProduct(Long productId, PutProductsTypeProductIdRequest request) {
        return ok(() -> fineractClient.products().updateShareProduct(SHARE, productId, request)).getResourceId();
    }

    public Long applyShareAccount(AccountRequest request) {
        return ok(() -> fineractClient.shareAccount().createShareAccount(SHARE, request)).getResourceId();
    }

    public void updateShareAccount(Long shareAccountId, PutAccountsTypeAccountIdRequest request) {
        ok(() -> fineractClient.shareAccount().updateShareAccount(SHARE, shareAccountId, request));
    }

    public void approve(Long shareAccountId) {
        command(shareAccountId, new PostAccountsTypeAccountIdRequest(), "approve");
    }

    public void approve(Long shareAccountId, String approvedDate, String note, String dateFormat, String locale) {
        command(shareAccountId,
                new PostAccountsTypeAccountIdRequest().approvedDate(approvedDate).note(note).dateFormat(dateFormat).locale(locale),
                "approve");
    }

    public void activate(Long shareAccountId, String activatedDate, String dateFormat, String locale) {
        command(shareAccountId, new PostAccountsTypeAccountIdRequest().activatedDate(activatedDate).dateFormat(dateFormat).locale(locale),
                "activate");
    }

    public void reject(Long shareAccountId, String note) {
        command(shareAccountId, new PostAccountsTypeAccountIdRequest().note(note), "reject");
    }

    public void undoApproval(Long shareAccountId) {
        command(shareAccountId, new PostAccountsTypeAccountIdRequest(), "undoapproval");
    }

    public void close(Long shareAccountId, String closedDate, String note, String dateFormat, String locale) {
        command(shareAccountId, closeRequest(closedDate, note, dateFormat, locale), "close");
    }

    public CallFailedRuntimeException closeExpectingError(Long shareAccountId, String closedDate, String note, String dateFormat,
            String locale) {
        return fail(() -> fineractClient.shareAccount().handleCommandsShareAccount(SHARE, shareAccountId,
                closeRequest(closedDate, note, dateFormat, locale), "close"));
    }

    private static PostAccountsTypeAccountIdRequest closeRequest(String closedDate, String note, String dateFormat, String locale) {
        return new PostAccountsTypeAccountIdRequest().closedDate(closedDate).note(note).dateFormat(dateFormat).locale(locale);
    }

    public void approveAdditionalShares(Long shareAccountId, Long purchasedSharesId) {
        command(shareAccountId, requestedShares(purchasedSharesId), "approveadditionalshares");
    }

    public void rejectAdditionalShares(Long shareAccountId, Long purchasedSharesId) {
        command(shareAccountId, requestedShares(purchasedSharesId), "rejectadditionalshares");
    }

    private static PostAccountsTypeAccountIdRequest requestedShares(Long purchasedSharesId) {
        return new PostAccountsTypeAccountIdRequest().requestedShares(Set.of(new PostAccountsRequestedShares().id(purchasedSharesId)));
    }

    private void command(Long shareAccountId, PostAccountsTypeAccountIdRequest request, String command) {
        ok(() -> fineractClient.shareAccount().handleCommandsShareAccount(SHARE, shareAccountId, request, command));
    }

    /** See {@link ShareAccountCommandsApi#redeemShares} for why this command needs its own request model. */
    public void redeemShares(Long shareAccountId, long shares, String requestedDate, String dateFormat, String locale) {
        ShareAccountCommandsApi.ShareCountRequest request = shareCount(shares, requestedDate, dateFormat, locale);
        ok(() -> fineractClient.create(ShareAccountCommandsApi.class).redeemShares(SHARE, shareAccountId, request));
    }

    public void applyAdditionalShares(Long shareAccountId, long shares, String requestedDate, String dateFormat, String locale) {
        ShareAccountCommandsApi.ShareCountRequest request = shareCount(shares, requestedDate, dateFormat, locale);
        ok(() -> fineractClient.create(ShareAccountCommandsApi.class).applyAdditionalShares(SHARE, shareAccountId, request));
    }

    public CallFailedRuntimeException applyAdditionalSharesExpectingError(Long shareAccountId, long shares, String requestedDate,
            String dateFormat, String locale) {
        ShareAccountCommandsApi.ShareCountRequest request = shareCount(shares, requestedDate, dateFormat, locale);
        return fail(() -> fineractClient.create(ShareAccountCommandsApi.class).applyAdditionalShares(SHARE, shareAccountId, request));
    }

    private static ShareAccountCommandsApi.ShareCountRequest shareCount(long shares, String requestedDate, String dateFormat,
            String locale) {
        return new ShareAccountCommandsApi.ShareCountRequest()//
                .requestedDate(requestedDate)//
                .dateFormat(dateFormat)//
                .locale(locale)//
                .requestedShares(shares);
    }

    public Long createDividend(Long productId, PostShareProductDividendsRequest request) {
        return ok(() -> dividends().createShareDividend(productId, request)).getSubResourceId();
    }

    public void approveDividend(Long productId, Long dividendId) {
        ok(() -> dividends().updateShareDividend(productId, dividendId, "approve", "{}"));
    }

    public GetShareProductDividendsResponse getDividends(Long productId) {
        return ok(() -> dividends().retrieveAllShareDividends(productId, Map.of()));
    }

    public GetShareProductDividendDetailsResponse getDividendDetails(Long productId, Long dividendId) {
        return ok(() -> dividends().retrieveOneShareDividend(dividendId, productId, Map.of()));
    }

    private SelfDividendApi dividends() {
        return fineractClient.create(SelfDividendApi.class);
    }

    public GetAccountsTypeAccountIdResponse getShareAccount(Long shareAccountId) {
        return ok(() -> fineractClient.shareAccount().retrieveOneShareAccount(shareAccountId, SHARE));
    }
}
