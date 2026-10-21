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

import com.fasterxml.jackson.databind.JsonNode;
import feign.Headers;
import feign.RequestLine;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-2639: monetary savings transactions are only allowed for users whose office is the account's office or one
 * of its parents.
 */
public class SavingsOfficeHierarchyAccessIntegrationTest extends FeignSavingsTestBase {

    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final String DEPOSIT_AMOUNT = "100";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2010, 1, 1);
    private static final String HIERARCHY_ERROR_MESSAGE = "The user doesn't have enough permissions to access the resource.";
    private static final Map<String, Boolean> DEPOSIT_PERMISSIONS = Map.of("DEPOSIT_SAVINGSACCOUNT", true, "READ_SAVINGSACCOUNT", true);
    private static final Map<String, Boolean> COLLECTION_SHEET_PERMISSIONS = Map.of("SAVE_COLLECTIONSHEET", true, "READ_SAVINGSACCOUNT",
            true);

    private final FeignOfficeHelper officeHelper = new FeignOfficeHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testSavingsDepositIsRestrictedToUserOfficeHierarchy() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long accountOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
        Long siblingOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();

        Long clientId = createClientInOffice(accountOfficeId);
        Long productId = createDefaultSavingsProduct().getResourceId();
        Long savingsId = createApproveActivateSavings(clientId, productId, today);

        FineractFeignClient siblingOfficeUser = createUserInOffice(siblingOfficeId, DEPOSIT_PERMISSIONS);
        FineractFeignClient accountOfficeUser = createUserInOffice(accountOfficeId, DEPOSIT_PERMISSIONS);

        CallFailedRuntimeException failure = FeignCalls.failVoid(() -> deposit(siblingOfficeUser, savingsId, today));
        assertEquals(403, failure.getStatus());
        assertTrue(failure.getResponseBody().contains(HIERARCHY_ERROR_MESSAGE),
                "Expected office hierarchy error but was: " + failure.getResponseBody());

        assertNotNull(ok(() -> deposit(accountOfficeUser, savingsId, today)).getResourceId(),
                "User from the account's office must be able to make a deposit");
    }

    @Test
    public void testCollectionSheetSavingsDepositIsRestrictedToUserOfficeHierarchy() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long accountOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
        Long siblingOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();

        Long clientId = createClientInOffice(accountOfficeId);
        Long productId = createDefaultSavingsProduct().getResourceId();
        Long savingsId = createApproveActivateSavings(clientId, productId, today);

        FineractFeignClient siblingOfficeUser = createUserInOffice(siblingOfficeId, COLLECTION_SHEET_PERMISSIONS);
        FineractFeignClient accountOfficeUser = createUserInOffice(accountOfficeId, COLLECTION_SHEET_PERMISSIONS);

        CallFailedRuntimeException failure = FeignCalls.failVoid(() -> saveCollectionSheet(siblingOfficeUser, savingsId, today));
        assertEquals(403, failure.getStatus());
        assertTrue(failure.getResponseBody().contains(HIERARCHY_ERROR_MESSAGE),
                "Expected office hierarchy error but was: " + failure.getResponseBody());

        assertNotNull(ok(() -> saveCollectionSheet(accountOfficeUser, savingsId, today)),
                "User from the account's office must be able to save a collection sheet deposit");
    }

    interface IndividualCollectionSheetApi {

        @RequestLine("POST /v1/collectionsheet?command=saveCollectionSheet")
        @Headers({ "Content-Type: application/json", "Accept: application/json" })
        JsonNode save(Map<String, Object> request);
    }

    private static JsonNode saveCollectionSheet(FineractFeignClient client, Long savingsId, String date) {
        return client.create(IndividualCollectionSheetApi.class)
                .save(Map.of("dateFormat", SavingsTestData.DATETIME_PATTERN, "locale", SavingsTestData.LOCALE, "transactionDate", date,
                        "actualDisbursementDate", date, "bulkRepaymentTransactions", List.of(), "bulkSavingsDueTransactions",
                        List.of(Map.of("savingsId", savingsId, "transactionAmount", DEPOSIT_AMOUNT, "depositAccountType",
                                DepositAccountType.SAVINGS_DEPOSIT.getValue()))));
    }

    private Long createClientInOffice(Long officeId) {
        return clientHelper.createClient(new PostClientsRequest().officeId(officeId).legalFormId(1L)
                .firstname(Utils.randomFirstNameGenerator()).lastname(Utils.randomLastNameGenerator()).active(true)
                .activationDate(ClientHelper.DEFAULT_DATE).dateFormat(SavingsTestData.DATETIME_PATTERN).locale(SavingsTestData.LOCALE))
                .getClientId();
    }

    private FineractFeignClient createUserInOffice(Long officeId, Map<String, Boolean> permissions) {
        String username = Utils.uniqueRandomStringGenerator("usr", 8);
        Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, permissions);
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(officeId).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }

    private static PostSavingsAccountTransactionsResponse deposit(FineractFeignClient client, Long savingsId, String date) {
        return client.savingsAccountTransactions().createSavingsAccountTransaction(savingsId,
                SavingsRequestBuilders.deposit(DEPOSIT_AMOUNT, date), "deposit");
    }
}
