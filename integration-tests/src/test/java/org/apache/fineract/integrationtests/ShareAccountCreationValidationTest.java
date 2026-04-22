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
package org.apache.fineract.integrationtests;

import java.time.format.DateTimeFormatter;
import org.apache.fineract.client.models.AccountRequest;
import org.apache.fineract.client.models.PostAccountsTypeResponse;
import org.apache.fineract.client.models.PostProductsTypeRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

public class ShareAccountCreationValidationTest extends IntegrationTest {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    @Test
    public void shouldReturn400WhenRequiredShareAccountFieldsMissing() {
        String today = FORMATTER.format(Utils.getLocalDateOfTenant());

        Long productId = ok(fineractClient().shareProducts.createShareProduct("share",
                new PostProductsTypeRequest().name(Utils.uniqueRandomStringGenerator("SHARE_PROD_", 6))
                        .shortName(Utils.uniqueRandomStringGenerator("", 4)).description(Utils.randomStringGenerator("", 20))
                        .currencyCode("USD").digitsAfterDecimal(4).inMultiplesOf(0).locale("en_GB").totalShares(10000).sharesIssued(10000)
                        .unitPrice(2).nominalShares(20).minimumShares(10).maximumShares(3000)
                        .allowDividendCalculationForInactiveClients(true).accountingRule(1).minimumActivePeriodForDividends(1)
                        .minimumactiveperiodFrequencyType(0).lockinPeriodFrequency(1).lockinPeriodFrequencyType(0)))
                .getResourceId();

        Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

        Long savingsProductId = ok(fineractClient().savingsProducts
                .createSavingsProduct(new PostSavingsProductsRequest().name(Utils.uniqueRandomStringGenerator("SAV_PROD_", 6))
                        .shortName(Utils.uniqueRandomStringGenerator("", 4)).currencyCode("USD").digitsAfterDecimal(4).inMultiplesOf(0)
                        .nominalAnnualInterestRate(10.0).locale("en_GB").interestCompoundingPeriodType(4).interestPostingPeriodType(4)
                        .interestCalculationType(1).interestCalculationDaysInYearType(365).accountingRule(1)))
                .getResourceId();

        Long savingsId = ok(fineractClient().savingsAccounts.submitSavingsApplication(new PostSavingsAccountsRequest().clientId(clientId)
                .productId(savingsProductId).locale("en").dateFormat(DATE_FORMAT).submittedOnDate(today))).getSavingsId();

        ok(fineractClient().savingsAccounts.handleCommandsSavingsAccount(savingsId,
                new PostSavingsAccountsAccountIdRequest().dateFormat(DATE_FORMAT).locale("en").approvedOnDate(today), "approve"));

        ok(fineractClient().savingsAccounts.handleCommandsSavingsAccount(savingsId,
                new PostSavingsAccountsAccountIdRequest().dateFormat(DATE_FORMAT).locale("en").activatedOnDate(today), "activate"));

        // missing requestedShares
        Response<PostAccountsTypeResponse> missingShares = Calls.executeU(
                fineractClient().shareAccounts.createShareAccount("share", new AccountRequest().clientId(clientId).productId(productId)
                        .savingsAccountId(savingsId).submittedDate(today).applicationDate(today).dateFormat(DATE_FORMAT).locale("en_GB")));
        assertThat(missingShares.code()).isEqualTo(400);

        // missing applicationDate
        Response<PostAccountsTypeResponse> missingAppDate = Calls.executeU(
                fineractClient().shareAccounts.createShareAccount("share", new AccountRequest().clientId(clientId).productId(productId)
                        .savingsAccountId(savingsId).requestedShares(25L).submittedDate(today).dateFormat(DATE_FORMAT).locale("en_GB")));
        assertThat(missingAppDate.code()).isEqualTo(400);

        // missing savingsAccountId
        Response<PostAccountsTypeResponse> missingSavings = Calls.executeU(
                fineractClient().shareAccounts.createShareAccount("share", new AccountRequest().clientId(clientId).productId(productId)
                        .requestedShares(25L).submittedDate(today).applicationDate(today).dateFormat(DATE_FORMAT).locale("en_GB")));
        assertThat(missingSavings.code()).isEqualTo(400);
    }
}
