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
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.AccountTransferRequest;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.PostAccountTransfersResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.AccountTransferRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-2639: an account transfer is only allowed when both the source and the destination account belong to the
 * user's office or one of its child offices.
 */
public class AccountTransferOfficeHierarchyAccessIntegrationTest extends FeignSavingsTestBase {

    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final String TRANSFER_AMOUNT = "100";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2010, 1, 1);
    private static final Integer LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID = FinancialActivity.LIABILITY_TRANSFER.getValue();
    private static final String HIERARCHY_ERROR_MESSAGE = "The user doesn't have enough permissions to access the resource.";
    private static final Map<String, Boolean> CREATE_PERMISSIONS = Map.of("CREATE_ACCOUNTTRANSFER", true, "READ_ACCOUNTTRANSFER", true);
    private static final Map<String, Boolean> UNDO_PERMISSIONS = Map.of("UNDO_ACCOUNTTRANSFER", true, "READ_ACCOUNTTRANSFER", true);

    private final FeignOfficeHelper officeHelper = new FeignOfficeHelper(FineractFeignClientHelper.getFineractFeignClient());

    private static FeignFinancialActivityAccountHelper financialActivityAccountHelper;
    private static Long createdLiabilityTransferMappingId;

    /** Account transfers post to the liability transfer account, so the mapping has to exist. */
    @BeforeAll
    public static void setupLiabilityTransferMapping() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        financialActivityAccountHelper = new FeignFinancialActivityAccountHelper(client);
        boolean exists = financialActivityAccountHelper.getAllMappings().stream()
                .anyMatch(mapping -> mapping.getFinancialActivityData() != null
                        && LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID.equals(mapping.getFinancialActivityData().getId()));
        if (!exists) {
            Account liabilityTransferAccount = new FeignAccountHelper(client).createLiabilityAccount();
            createdLiabilityTransferMappingId = financialActivityAccountHelper
                    .createMapping(LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID, liabilityTransferAccount).getResourceId();
        }
    }

    @AfterAll
    public static void removeLiabilityTransferMapping() {
        if (createdLiabilityTransferMappingId != null) {
            financialActivityAccountHelper.deleteMapping(createdLiabilityTransferMappingId);
        }
    }

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

        FineractFeignClient officeAUser = createUserInOffice(officeAId, CREATE_PERMISSIONS);
        FineractFeignClient officeBUser = createUserInOffice(officeBId, CREATE_PERMISSIONS);

        assertRejected(() -> transfer(officeBUser, today, officeAId, clientA, fromSavingsId, officeAId, clientA, toSavingsInOfficeA));
        assertRejected(() -> transfer(officeAUser, today, officeAId, clientA, fromSavingsId, officeBId, clientB, toSavingsInOfficeB));

        assertNotNull(ok(() -> transfer(officeAUser, today, officeAId, clientA, fromSavingsId, officeAId, clientA, toSavingsInOfficeA))
                .getResourceId(), "User from the accounts' office must be able to transfer between them");
    }

    @Test
    public void testUndoAccountTransferRequiresBothAccountsInUserOfficeHierarchy() {
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

        Long transferInOfficeA = ok(
                () -> transfer(fineractClient(), today, officeAId, clientA, fromSavingsId, officeAId, clientA, toSavingsInOfficeA))
                .getResourceId();
        Long transferAcrossOffices = ok(
                () -> transfer(fineractClient(), today, officeAId, clientA, fromSavingsId, officeBId, clientB, toSavingsInOfficeB))
                .getResourceId();

        FineractFeignClient officeAUser = createUserInOffice(officeAId, UNDO_PERMISSIONS);
        FineractFeignClient officeBUser = createUserInOffice(officeBId, UNDO_PERMISSIONS);

        assertRejected(() -> undo(officeBUser, transferInOfficeA));
        assertRejected(() -> undo(officeAUser, transferAcrossOffices));

        assertNotNull(ok(() -> undo(officeAUser, transferInOfficeA)).getResourceId(),
                "User from the accounts' office must be able to undo a transfer between them");
    }

    private static CommandProcessingResult undo(FineractFeignClient client, Long accountTransferId) {
        return client.accountTransfers().accountTransferOperation(accountTransferId, "undo");
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

    private FineractFeignClient createUserInOffice(Long officeId, Map<String, Boolean> permissions) {
        String username = Utils.uniqueRandomStringGenerator("usr", 8);
        Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, permissions);
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(officeId).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }
}
