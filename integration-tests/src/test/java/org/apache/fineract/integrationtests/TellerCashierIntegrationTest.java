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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.models.CashierTransactionData;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse;
import org.apache.fineract.client.models.GetTellersTellerIdCashiersResponse;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.fineract.integrationtests.teller.TellerHelper;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TellerCashierIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private Account cashAtMainvaultAccount;
    private Account cashAtTellerAccount;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        AccountHelper accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);

        cashAtMainvaultAccount = accountHelper.createAssetAccount();
        cashAtTellerAccount = accountHelper.createAssetAccount();
        setProperFinancialActivity();
    }

    private void setProperFinancialActivity() {
        FinancialActivityAccountHelper financialActivityAccountHelper = new FinancialActivityAccountHelper(requestSpec);

        // Clear any existing financial mappings before creating new ones
        List<GetFinancialActivityAccountsResponse> financialMappings = financialActivityAccountHelper.getAllFinancialActivityAccounts();
        financialMappings.forEach(mapping -> financialActivityAccountHelper.deleteFinancialActivityAccount(mapping.getId()));

        // Map CASH_AT_MAINVAULT to a new asset account
        Integer financialActivityCashAtMainVaultAccountId = (Integer) financialActivityAccountHelper.createFinancialActivityAccount(
                AccountingConstants.FinancialActivity.CASH_AT_MAINVAULT.getValue(), cashAtMainvaultAccount.getAccountID(), responseSpec,
                CommonConstants.RESPONSE_RESOURCE_ID);
        assertNotNull(financialActivityCashAtMainVaultAccountId);

        // Map CASH_AT_TELLER to a new asset account
        Integer financialActivityCashAtTellerAccountId = (Integer) financialActivityAccountHelper.createFinancialActivityAccount(
                AccountingConstants.FinancialActivity.CASH_AT_TELLER.getValue(), cashAtTellerAccount.getAccountID(), responseSpec,
                CommonConstants.RESPONSE_RESOURCE_ID);
        assertNotNull(financialActivityCashAtTellerAccountId);
    }

    @Test
    public void shouldReturnCashierTransactionsAfterAllocatingCash() {
        final Integer roleId = RolesHelper.createRole(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(roleId);

        final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(staffId);

        final Integer userId = UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId);
        Assertions.assertNotNull(userId);

        final Integer tellerId = TellerHelper.createTeller(requestSpec, responseSpec);
        Assertions.assertNotNull(tellerId);

        TellerHelper.createCashier(requestSpec, responseSpec, tellerId, staffId);

        final GetTellersTellerIdCashiersResponse cashiersResponse = TellerHelper.getCashiers(this.requestSpec, this.responseSpec, tellerId);
        Assertions.assertNotNull(cashiersResponse);

        final Long cashierId = cashiersResponse.getCashiers().get(0).getId();

        final BigDecimal allocatedAmount = new BigDecimal(1000);
        TellerHelper.allocateCashToCashier(requestSpec, responseSpec, tellerId, cashierId, allocatedAmount);

        final GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse summaryResponse = TellerHelper
                .getCashierSummaryAndTransactions(requestSpec, responseSpec, tellerId, cashierId);
        Assertions.assertNotNull(summaryResponse);

        // Verify allocated cash
        assertThat(allocatedAmount).isEqualByComparingTo(summaryResponse.getNetCash());

        // Verify cashier transactions
        List<CashierTransactionData> cashierTransactions = summaryResponse.getCashierTransactions().getPageItems();
        Assertions.assertEquals(1, cashierTransactions.size());
        assertThat(allocatedAmount).isEqualByComparingTo(cashierTransactions.get(0).getTxnAmount());
    }
}
