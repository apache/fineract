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
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.InterestPauseRequestDto;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RepaymentFrequencyType;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-3027: managing interest pauses needs READ_LOAN plus the matching CREATE/UPDATE/DELETE_INTEREST_PAUSE
 * permission, not ALL_FUNCTIONS_READ.
 */
public class LoanInterestPausePermissionIntegrationTest extends FeignLoanTestBase {

    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final Long HEAD_OFFICE_ID = 1L;
    private static final String LOAN_START_DATE = "01 January 2023";
    private static final double PRINCIPAL = 10000.0;
    private static final double INTEREST_RATE = 18.0;
    private static final int NUMBER_OF_REPAYMENTS = 12;

    @Test
    public void testUserWithInterestPausePermissionsCanManageInterestPauses() {
        Long loanId = createDisbursedLoan();
        FineractFeignClient user = createUserWithPermissions(
                Map.of("READ_LOAN", true, "CREATE_INTEREST_PAUSE", true, "UPDATE_INTEREST_PAUSE", true, "DELETE_INTEREST_PAUSE", true));

        Long variationId = ok(() -> user.loanInterestPause().createLoanInterestPause(loanId, pause("2023-01-01", "2023-01-12")))
                .getResourceId();
        assertNotNull(variationId, "User with CREATE_INTEREST_PAUSE must be able to create an interest pause");

        assertNotNull(ok(() -> user.loanInterestPause().updateLoanInterestPause(loanId, variationId, pause("2023-01-02", "2023-01-10")))
                .getResourceId(), "User with UPDATE_INTEREST_PAUSE must be able to update an interest pause");

        executeVoid(() -> user.loanInterestPause().deleteLoanInterestPause(loanId, variationId));
    }

    @Test
    public void testUserWithoutCreateInterestPausePermissionIsRejected() {
        Long loanId = createDisbursedLoan();
        FineractFeignClient user = createUserWithPermissions(Map.of("READ_LOAN", true));

        CallFailedRuntimeException failure = FeignCalls
                .failVoid(() -> user.loanInterestPause().createLoanInterestPause(loanId, pause("2023-01-01", "2023-01-12")));
        assertEquals(403, failure.getStatus());
        assertEquals("error.msg.not.authorized", failure.getUserMessageGlobalisationCode());
        assertTrue(failure.getResponseBody().contains("CREATE_INTEREST_PAUSE"),
                "Expected the missing CREATE_INTEREST_PAUSE permission to be reported but was: " + failure.getResponseBody());
    }

    private Long createDisbursedLoan() {
        Long clientId = createClient();
        Long productId = createLoanProduct(
                create4IProgressive().numberOfRepayments(NUMBER_OF_REPAYMENTS).interestRatePerPeriod(INTEREST_RATE).principal(PRINCIPAL));
        Long loanId = applyForLoan(applyLP2ProgressiveLoanRequest(clientId, productId, LOAN_START_DATE, PRINCIPAL, INTEREST_RATE,
                NUMBER_OF_REPAYMENTS, r -> r.repaymentEvery(1).repaymentFrequencyType(RepaymentFrequencyType.DAYS)
                        .loanTermFrequency(NUMBER_OF_REPAYMENTS).loanTermFrequencyType(RepaymentFrequencyType.DAYS)));
        approveLoan(loanId, approveLoanRequest(PRINCIPAL, LOAN_START_DATE));
        disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), LOAN_START_DATE);
        return loanId;
    }

    private static InterestPauseRequestDto pause(String startDate, String endDate) {
        return new InterestPauseRequestDto().startDate(startDate).endDate(endDate).dateFormat("yyyy-MM-dd").locale("en");
    }

    private FineractFeignClient createUserWithPermissions(Map<String, Boolean> permissions) {
        String username = Utils.uniqueRandomStringGenerator("usr", 8);
        Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, permissions);
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User").email("test@localhost")
                .officeId(HEAD_OFFICE_ID).roles(List.of(roleId)).password(PASSWORD).repeatPassword(PASSWORD).sendPasswordToEmail(false));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }
}
