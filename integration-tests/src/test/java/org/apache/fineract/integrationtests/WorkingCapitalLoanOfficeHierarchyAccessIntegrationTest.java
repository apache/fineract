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
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.integrationtests.client.feign.FeignWorkingCapitalTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-2982: monetary working capital loan transactions are only allowed for users whose office is the loan's
 * office or one of its parents.
 */
public class WorkingCapitalLoanOfficeHierarchyAccessIntegrationTest extends FeignWorkingCapitalTestBase {

    private static final String BUSINESS_DATE = "2026-01-01";
    private static final String DISBURSE_DATE = "01 January 2026";
    private static final BigDecimal PRINCIPAL = new BigDecimal("9000");
    private static final BigDecimal REPAYMENT_AMOUNT = new BigDecimal("100");
    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2010, 1, 1);
    private static final String NOT_AUTHORIZED_ERROR_CODE = "error.msg.not.authorized";
    private static final String HIERARCHY_ERROR_MESSAGE = "The user doesn't have enough permissions to access the resource.";
    private static final Map<String, Boolean> REPAYMENT_PERMISSIONS = Map.of("REPAYMENT_WORKINGCAPITALLOAN", true,
            "READ_WORKINGCAPITALLOAN", true);

    private final FeignOfficeHelper officeHelper = new FeignOfficeHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testRepaymentIsRestrictedToUserOfficeHierarchy() {
        runAt(BUSINESS_DATE, () -> {
            Long loanOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();
            Long siblingOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getOfficeId();

            Long clientId = createClientInOffice(loanOfficeId);
            Long productId = productHelper.createWorkingCapitalLoanProduct(
                    new WorkingCapitalLoanProductTestBuilder().withName("WCL Office " + Utils.uniqueRandomStringGenerator("", 8))
                            .withShortName(Utils.uniqueRandomStringGenerator("", 4)).build())
                    .getResourceId();
            Long loanId = createApproveAndDisburseWcLoan(clientId, productId, PRINCIPAL, DISBURSE_DATE);

            FineractFeignClient siblingOfficeUser = createUserInOffice(siblingOfficeId, REPAYMENT_PERMISSIONS);
            FineractFeignClient loanOfficeUser = createUserInOffice(loanOfficeId, REPAYMENT_PERMISSIONS);

            CallFailedRuntimeException failure = FeignCalls.failVoid(() -> repay(siblingOfficeUser, loanId));
            assertEquals(403, failure.getStatus());
            assertEquals(NOT_AUTHORIZED_ERROR_CODE, failure.getUserMessageGlobalisationCode());
            assertTrue(failure.getResponseBody().contains(HIERARCHY_ERROR_MESSAGE),
                    "Expected office hierarchy error but was: " + failure.getResponseBody());

            assertNotNull(ok(() -> repay(loanOfficeUser, loanId)).getResourceId(),
                    "User from the loan's office must be able to make a repayment");
        });
    }

    private static PostWorkingCapitalLoanTransactionsResponse repay(FineractFeignClient client, Long loanId) {
        return client.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionById(loanId, "repayment",
                WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT_AMOUNT, DISBURSE_DATE));
    }

    private Long createClientInOffice(Long officeId) {
        return clientHelper.createClient(new PostClientsRequest().officeId(officeId).legalFormId(1L)
                .firstname(Utils.randomFirstNameGenerator()).lastname(Utils.randomLastNameGenerator()).active(true)
                .activationDate(ClientHelper.DEFAULT_DATE).dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE))
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
