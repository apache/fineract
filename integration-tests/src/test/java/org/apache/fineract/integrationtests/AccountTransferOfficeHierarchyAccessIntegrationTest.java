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
import org.apache.fineract.client.models.AccountTransferRequest;
import org.apache.fineract.client.models.PostAccountTransfersResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.AccountTransferRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-2639: an account transfer is only allowed when both the source and the destination account belong to the
 * user's office or one of its child offices.
 */
public class AccountTransferOfficeHierarchyAccessIntegrationTest extends FeignSavingsTestBase {

    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final String TRANSFER_AMOUNT = "100";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2010, 1, 1);
    private static final String HIERARCHY_ERROR_MESSAGE = "The user doesn't have enough permissions to access the resource.";

    private final FeignOfficeHelper officeHelper = new FeignOfficeHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testAccountTransferRequiresBothAccountsInUserOfficeHierarchy() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long officeAId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
        Long officeBId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
        Long productId = createDefaultSavingsProduct().getResourceId();

        Long clientA = createClientInOffice(officeAId);
        Long fromSavingsId = createApproveActivateSavings(clientA, productId, today);
        Long toSavingsInOfficeA = createApproveActivateSavings(clientA, productId, today);
        savingsTransactionHelper.deposit(fromSavingsId, "1000", today);

        Long clientB = createClientInOffice(officeBId);
        Long toSavingsInOfficeB = createApproveActivateSavings(clientB, productId, today);

        FineractFeignClient officeAUser = createUserInOffice(officeAId);
        FineractFeignClient officeBUser = createUserInOffice(officeBId);

        assertRejected(() -> transfer(officeBUser, today, officeAId, clientA, fromSavingsId, officeAId, clientA, toSavingsInOfficeA));
        assertRejected(() -> transfer(officeAUser, today, officeAId, clientA, fromSavingsId, officeBId, clientB, toSavingsInOfficeB));

        assertNotNull(ok(() -> transfer(officeAUser, today, officeAId, clientA, fromSavingsId, officeAId, clientA, toSavingsInOfficeA))
                .getResourceId(), "User from the accounts' office must be able to transfer between them");
    }

    private static void assertRejected(Runnable call) {
        CallFailedRuntimeException failure = FeignCalls.failVoid(call);
        assertEquals(403, failure.getStatus());
        assertTrue(failure.getResponseBody().contains(HIERARCHY_ERROR_MESSAGE),
                "Expected office hierarchy error but was: " + failure.getResponseBody());
    }

    private static PostAccountTransfersResponse transfer(FineractFeignClient client, String date, Long fromOfficeId, Long fromClientId,
            Long fromSavingsId, Long toOfficeId, Long toClientId, Long toSavingsId) {
        AccountTransferRequest request = AccountTransferRequestBuilders
                .transfer(date, fromClientId, fromSavingsId, PortfolioAccountType.SAVINGS, toClientId, toSavingsId,
                        PortfolioAccountType.SAVINGS, TRANSFER_AMOUNT)
                .fromOfficeId(String.valueOf(fromOfficeId)).toOfficeId(String.valueOf(toOfficeId));
        return client.accountTransfers().createAccountTransfer(request);
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
        FeignRoleHelper.addPermissionsToRole(roleId, Map.of("CREATE_ACCOUNTTRANSFER", true, "READ_ACCOUNTTRANSFER", true));
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(officeId).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }
}
