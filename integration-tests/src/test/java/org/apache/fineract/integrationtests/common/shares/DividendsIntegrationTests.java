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
package org.apache.fineract.integrationtests.common.shares;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.AccountRequest;
import org.apache.fineract.client.models.GetShareProductDividendDetailsPageItems;
import org.apache.fineract.client.models.GetShareProductDividendDetailsResponse;
import org.apache.fineract.client.models.GetShareProductDividendsPageItems;
import org.apache.fineract.client.models.GetShareProductDividendsResponse;
import org.apache.fineract.client.models.PostShareProductDividendsRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignShareAccountHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DividendsIntegrationTests {

    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private final String[] dates = { "01 Jan 2015", "01 Apr 2015", "01 Oct 2015", "01 Dec 2015", "01 Mar 2016" };
    private final long[] shares = { 100, 200, 300, 100, 500 };

    private final FeignClientHelper clientHelper = new FeignClientHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsHelper savingsHelper = new FeignSavingsHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignSavingsProductHelper savingsProductHelper = new FeignSavingsProductHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final FeignShareAccountHelper shareAccountHelper = new FeignShareAccountHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testCreateDividends() {
        final Long productId = shareAccountHelper.createShareProduct(FeignShareAccountHelper.defaultShareProduct());
        ArrayList<Long> shareAccounts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final Long clientId = clientHelper.createClient();
            Assertions.assertNotNull(clientId);
            final Long savingsAccountId = openSavingsAccount(clientId);
            Assertions.assertNotNull(savingsAccountId);
            final Long shareAccountId = createShareAccount(clientId, productId, savingsAccountId, dates[i], shares[i]);
            shareAccounts.add(shareAccountId);
            Assertions.assertNotNull(shareAccountId);
            Assertions.assertNotNull(shareAccountHelper.getShareAccount(shareAccountId));
            shareAccountHelper.approve(shareAccountId, "01 Jan 2016", "Share Account Approval Note", DATE_FORMAT, "en");
            shareAccountHelper.activate(shareAccountId, "01 Jan 2016", DATE_FORMAT, "en");
        }

        final Long dividendId = shareAccountHelper.createDividend(productId,
                new PostShareProductDividendsRequest().dividendPeriodStartDate("01 Jan 2015").dividendPeriodEndDate("01 Apr 2016")
                        .dividendAmount(new BigDecimal("50000")).dateFormat(DATE_FORMAT).locale("en"));

        GetShareProductDividendsResponse productdividends = shareAccountHelper.getDividends(productId);
        Assertions.assertEquals(1, productdividends.getTotalFilteredRecords());
        GetShareProductDividendsPageItems dividend = productdividends.getPageItems().get(0);
        Assertions.assertEquals(50000.0f, dividend.getAmount().floatValue());
        Assertions.assertEquals("shareAccountDividendStatusType.initiated", dividend.getStatus().getCode());
        Assertions.assertEquals(LocalDate.of(2015, 1, 1), dividend.getDividendPeriodStartDate());
        Assertions.assertEquals(LocalDate.of(2016, 4, 1), dividend.getDividendPeriodEndDate());

        GetShareProductDividendDetailsResponse dividenddetails = shareAccountHelper.getDividendDetails(productId, dividendId);
        Assertions.assertEquals(5, dividenddetails.getTotalFilteredRecords());
        verifyAccountDividends(dividenddetails.getPageItems(), shareAccounts);

        shareAccountHelper.approveDividend(productId, dividendId);

        productdividends = shareAccountHelper.getDividends(productId);
        Assertions.assertEquals(1, productdividends.getTotalFilteredRecords());
        dividend = productdividends.getPageItems().get(0);
        Assertions.assertEquals(50000.0f, dividend.getAmount().floatValue());
        Assertions.assertEquals("shareAccountDividendStatusType.approved", dividend.getStatus().getCode());
        Assertions.assertEquals(LocalDate.of(2015, 1, 1), dividend.getDividendPeriodStartDate());
        Assertions.assertEquals(LocalDate.of(2016, 4, 1), dividend.getDividendPeriodEndDate());

        dividenddetails = shareAccountHelper.getDividendDetails(productId, dividendId);
        Assertions.assertEquals(5, dividenddetails.getTotalFilteredRecords());
        verifyAccountDividends(dividenddetails.getPageItems(), shareAccounts);
    }

    private static void verifyAccountDividends(List<GetShareProductDividendDetailsPageItems> pageItems, List<Long> shareAccounts) {
        for (GetShareProductDividendDetailsPageItems dividendData : pageItems) {
            Long accountId = dividendData.getAccountData().getId();
            if (shareAccounts.get(0).equals(accountId)) {
                Assertions.assertEquals(11320.755f, dividendData.getAmount().floatValue());
            } else if (shareAccounts.get(1).equals(accountId)) {
                Assertions.assertEquals(18172.791f, dividendData.getAmount().floatValue());
            } else if (shareAccounts.get(2).equals(accountId)) {
                Assertions.assertEquals(13629.593f, dividendData.getAmount().floatValue());
            } else if (shareAccounts.get(3).equals(accountId)) {
                Assertions.assertEquals(3028.7983f, dividendData.getAmount().floatValue());
            } else if (shareAccounts.get(4).equals(accountId)) {
                Assertions.assertEquals(3848.0637f, dividendData.getAmount().floatValue());
            }
            Assertions.assertEquals("shareAccountDividendStatusType.initiated", dividendData.getStatus().getCode());
        }
    }

    private Long openSavingsAccount(final Long clientId) {
        final Long savingsProductId = savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().minRequiredOpeningBalance(BigDecimal.valueOf(1000)))
                .getResourceId();
        final Long savingsId = savingsHelper.submitApplication(clientId, savingsProductId, "08 January 2013").getSavingsId();
        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsStatus(savingsId));
        savingsHelper.approveSavings(savingsId, "09 January 2013");
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));
        savingsHelper.activateSavings(savingsId, "01 March 2013");
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private Long createShareAccount(final Long clientId, final Long productId, final Long savingsAccountId, String applicationDate,
            long requestedShares) {
        return shareAccountHelper.applyShareAccount(new AccountRequest().clientId(clientId).productId(productId).externalId("External1")
                .savingsAccountId(savingsAccountId).submittedDate("01 Jan 2016").applicationDate(applicationDate)
                .requestedShares(requestedShares).dateFormat(DATE_FORMAT).locale("en_GB"));
    }
}
