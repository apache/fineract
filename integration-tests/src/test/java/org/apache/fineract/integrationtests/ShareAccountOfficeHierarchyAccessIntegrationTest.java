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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.AccountRequest;
import org.apache.fineract.client.models.PostAccountsTypeAccountIdRequest;
import org.apache.fineract.client.models.PostAccountsTypeAccountIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostProductsTypeRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignShareAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-2982: share account operations post purchase, redeem and charge transactions, so they are only allowed for
 * users whose office is the account's office or one of its parents.
 */
public class ShareAccountOfficeHierarchyAccessIntegrationTest extends FeignSavingsTestBase {

    private static final String SHARE = "share";
    private static final String DATE = "01 January 2026";
    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2010, 1, 1);
    private static final String NOT_AUTHORIZED_ERROR_CODE = "error.msg.not.authorized";
    private static final String HIERARCHY_ERROR_MESSAGE = "The user doesn't have enough permissions to access the resource.";
    private static final Map<String, Boolean> APPROVE_PERMISSIONS = Map.of("APPROVE_SHAREACCOUNT", true);

    private final FeignOfficeHelper officeHelper = new FeignOfficeHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignShareAccountHelper shareAccountHelper = new FeignShareAccountHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testShareAccountApprovalIsRestrictedToUserOfficeHierarchy() {
        runAt(DATE, () -> {
            Long accountOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
            Long siblingOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();

            Long clientId = createClientInOffice(accountOfficeId);
            Long savingsProductId = savingsProductHelper.createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct())
                    .getResourceId();
            Long savingsId = createApproveActivateSavings(clientId, savingsProductId, DATE);
            Long shareAccountId = shareAccountHelper.applyShareAccount(new AccountRequest().clientId(clientId)
                    .productId(createShareProduct()).savingsAccountId(savingsId).requestedShares(100L).submittedDate(DATE)
                    .applicationDate(DATE).dateFormat(SavingsTestData.DATETIME_PATTERN).locale("en"));

            FineractFeignClient siblingOfficeUser = createUserInOffice(siblingOfficeId, APPROVE_PERMISSIONS);
            FineractFeignClient accountOfficeUser = createUserInOffice(accountOfficeId, APPROVE_PERMISSIONS);

            CallFailedRuntimeException failure = FeignCalls.failVoid(() -> approve(siblingOfficeUser, shareAccountId));
            assertEquals(403, failure.getStatus());
            assertEquals(NOT_AUTHORIZED_ERROR_CODE, failure.getUserMessageGlobalisationCode());
            assertTrue(failure.getResponseBody().contains(HIERARCHY_ERROR_MESSAGE),
                    "Expected office hierarchy error but was: " + failure.getResponseBody());

            assertNotNull(ok(() -> approve(accountOfficeUser, shareAccountId)).getResourceId(),
                    "User from the account's office must be able to approve it");
        });
    }

    private Long createShareProduct() {
        return shareAccountHelper
                .createShareProduct(new PostProductsTypeRequest().name(Utils.uniqueRandomStringGenerator("SHARE_PRODUCT_", 6))
                        .shortName(Utils.uniqueRandomStringGenerator("", 4)).description("Description").currencyCode("USD")
                        .digitsAfterDecimal(2).inMultiplesOf(1).locale("en").totalShares(1000).unitPrice(1).nominalShares(20)
                        .allowDividendCalculationForInactiveClients(true).accountingRule(SavingsTestData.AccountingRule.NONE));
    }

    private static PostAccountsTypeAccountIdResponse approve(FineractFeignClient client, Long shareAccountId) {
        return client.shareAccount().handleCommandsShareAccount(SHARE, shareAccountId,
                new PostAccountsTypeAccountIdRequest().approvedDate(DATE).dateFormat(SavingsTestData.DATETIME_PATTERN).locale("en"),
                "approve");
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
}
