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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.integrationtests.client.feign.FeignDepositTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.DepositRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-2639: monetary recurring deposit transactions are only allowed for users whose office is the account's
 * office or one of its parents.
 */
public class RecurringDepositOfficeHierarchyAccessIntegrationTest extends FeignDepositTestBase {

    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2010, 1, 1);
    private static final String HIERARCHY_ERROR_MESSAGE = "The user doesn't have enough permissions to access the resource.";

    private final FeignOfficeHelper officeHelper = new FeignOfficeHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testRecurringDepositIsRestrictedToUserOfficeHierarchy() {
        LocalDate today = Utils.getLocalDateOfTenant();
        String validFrom = Utils.dateFormatter.format(today.minusMonths(3));
        String validTo = Utils.dateFormatter.format(today.plusYears(10));
        String startDate = Utils.dateFormatter.format(today.minusMonths(1));
        Long accountOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
        Long siblingOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();

        Long clientId = createClientInOffice(accountOfficeId);
        Long productId = recurringDepositProductHelper.createProduct(DepositRequestBuilders.withChart(
                DepositRequestBuilders.recurringDepositProduct().accountingRule(SavingsTestData.AccountingRule.NONE), validFrom, validTo,
                DepositTestData.recurringChartSlabsFor("period"))).getResourceId();
        Long accountId = recurringDepositHelper.submitApplication(DepositRequestBuilders.recurringDepositAccount(clientId, productId,
                startDate, startDate, DepositTestData.PreClosurePenalInterestOnType.WHOLE_TERM)).getSavingsId();
        recurringDepositHelper.approve(accountId, startDate);
        recurringDepositHelper.activate(accountId, startDate);
        BigDecimal depositAmount = recurringDepositHelper.getAccount(accountId).getMandatoryRecommendedDepositAmount();

        FineractFeignClient siblingOfficeUser = createUserInOffice(siblingOfficeId);
        FineractFeignClient accountOfficeUser = createUserInOffice(accountOfficeId);

        CallFailedRuntimeException failure = FeignCalls.failVoid(() -> deposit(siblingOfficeUser, accountId, startDate, depositAmount));
        assertEquals(403, failure.getStatus());
        assertTrue(failure.getResponseBody().contains(HIERARCHY_ERROR_MESSAGE),
                "Expected office hierarchy error but was: " + failure.getResponseBody());

        assertNotNull(ok(() -> deposit(accountOfficeUser, accountId, startDate, depositAmount)).getResourceId(),
                "User from the account's office must be able to make a deposit");
    }

    private Long createClientInOffice(Long officeId) {
        return clientHelper.createClient(new PostClientsRequest().officeId(officeId).legalFormId(1L)
                .firstname(Utils.randomFirstNameGenerator()).lastname(Utils.randomLastNameGenerator()).active(true)
                .activationDate(ClientHelper.DEFAULT_DATE).dateFormat(SavingsTestData.DATETIME_PATTERN).locale(SavingsTestData.LOCALE))
                .getClientId();
    }

    private FineractFeignClient createUserInOffice(Long officeId) {
        String username = Utils.uniqueRandomStringGenerator("usr", 8);
        Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, Map.of("DEPOSIT_RECURRINGDEPOSITACCOUNT", true, "READ_RECURRINGDEPOSITACCOUNT", true));
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(officeId).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }

    private static PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse deposit(FineractFeignClient client,
            Long accountId, String date, BigDecimal amount) {
        return client.recurringDepositAccountTransactions().transactionRecurringDepositAccountTransaction(accountId,
                DepositRequestBuilders.depositTransaction(date, amount), "deposit");
    }
}
