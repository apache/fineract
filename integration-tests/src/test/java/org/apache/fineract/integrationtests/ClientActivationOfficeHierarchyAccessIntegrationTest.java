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
import java.util.Set;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.GetClientsSavingsAccounts;
import org.apache.fineract.client.models.PostClientsClientIdResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-2982: activating a client with a default savings product opens an active savings account and posts its
 * minimum opening balance, so client activation is only allowed for users whose office is the client's office or one of
 * its parents.
 */
public class ClientActivationOfficeHierarchyAccessIntegrationTest extends FeignSavingsTestBase {

    private static final String DATE = "01 January 2026";
    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2010, 1, 1);
    private static final String NOT_AUTHORIZED_ERROR_CODE = "error.msg.not.authorized";
    private static final String HIERARCHY_ERROR_MESSAGE = "The user doesn't have enough permissions to access the resource.";
    /** Activating a client with a default savings product also activates that savings account. */
    private static final Map<String, Boolean> ACTIVATE_PERMISSIONS = Map.of("ACTIVATE_CLIENT", true, "READ_CLIENT", true,
            "ACTIVATE_SAVINGSACCOUNT", true);

    private final FeignOfficeHelper officeHelper = new FeignOfficeHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testClientActivationIsRestrictedToUserOfficeHierarchy() {
        runAt(DATE, () -> {
            Long clientOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
            Long siblingOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();

            Long savingsProductId = savingsProductHelper
                    .createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().minRequiredOpeningBalance(new BigDecimal("100")))
                    .getResourceId();
            Long clientId = clientHelper
                    .createClientPending(
                            ClientRequestBuilders.createPendingClient(DATE).officeId(clientOfficeId).savingsProductId(savingsProductId))
                    .getClientId();

            FineractFeignClient siblingOfficeUser = createUserInOffice(siblingOfficeId);
            FineractFeignClient clientOfficeUser = createUserInOffice(clientOfficeId);

            CallFailedRuntimeException failure = FeignCalls.failVoid(() -> activate(siblingOfficeUser, clientId));
            assertEquals(403, failure.getStatus());
            assertEquals(NOT_AUTHORIZED_ERROR_CODE, failure.getUserMessageGlobalisationCode());
            assertTrue(failure.getResponseBody().contains(HIERARCHY_ERROR_MESSAGE),
                    "Expected office hierarchy error but was: " + failure.getResponseBody());
            assertTrue(savingsAccountsOf(clientId).isEmpty(), "A rejected activation must not open the default savings account");

            assertNotNull(ok(() -> activate(clientOfficeUser, clientId)).getClientId(),
                    "User from the client's office must be able to activate the client");
            Set<GetClientsSavingsAccounts> savingsAccounts = savingsAccountsOf(clientId);
            assertEquals(1, savingsAccounts.size());
            GetClientsSavingsAccounts savingsAccount = savingsAccounts.iterator().next();
            assertEquals(savingsProductId, savingsAccount.getProductId());
            assertTrue(savingsAccount.getStatus().getActive(), "Activation must open an active default savings account");
        });
    }

    private static PostClientsClientIdResponse activate(FineractFeignClient client, Long clientId) {
        return client.clients().handleCommandClient(clientId, ClientRequestBuilders.activateClient(DATE), "activate");
    }

    private Set<GetClientsSavingsAccounts> savingsAccountsOf(Long clientId) {
        Set<GetClientsSavingsAccounts> savingsAccounts = clientHelper.getClientAccounts(clientId).getSavingsAccounts();
        return savingsAccounts == null ? Set.of() : savingsAccounts;
    }

    private FineractFeignClient createUserInOffice(Long officeId) {
        String username = Utils.uniqueRandomStringGenerator("usr", 8);
        Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, ACTIVATE_PERMISSIONS);
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(officeId).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }
}
