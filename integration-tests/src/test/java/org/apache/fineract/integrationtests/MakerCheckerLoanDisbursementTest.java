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

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.AuditData;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutPermissionsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class MakerCheckerLoanDisbursementTest extends FeignLoanTestBase {

    private static final String DATE = "01 January 2023";
    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final String ACTIVE = "loanStatusType.active";
    private static final String APPROVED = "loanStatusType.approved";

    private final FineractFeignClient admin = FineractFeignClientHelper.getFineractFeignClient();
    private final FeignGlobalConfigurationHelper configurationHelper = new FeignGlobalConfigurationHelper(admin);

    @BeforeEach
    void enableMakerCheckerForDisbursement() {
        configurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(true));
        ok(() -> admin.permissions().updatePermissions(new PutPermissionsRequest().putPermissionsItem("DISBURSE_LOAN", true)));
    }

    @AfterEach
    void disableMakerCheckerForDisbursement() {
        configurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(false));
        ok(() -> admin.permissions().updatePermissions(new PutPermissionsRequest().putPermissionsItem("DISBURSE_LOAN", false)));
    }

    @Test
    void makersDisbursementIsQueuedAndApprovedByACheckerWithoutTheDisbursePermission() {
        runAt(DATE, () -> {
            final Long loanId = approvedLoan();
            final Long staffId = new FeignStaffHelper(admin).createStaff().getResourceId();
            final FineractFeignClient maker = userWith("DISBURSE_LOAN", staffId);
            final FineractFeignClient checker = userWith("DISBURSE_LOAN_CHECKER", staffId);

            ok(() -> maker.loans().handleCommandsLoan(loanId, LoanRequestBuilders.disburseLoan(1000.0, DATE),
                    Map.of("command", "disburse")));

            assertEquals(APPROVED, getLoanDetails(loanId).getStatus().getCode(), "The maker's disbursement must not be applied");
            final List<AuditData> pending = pendingFor(loanId);
            assertEquals(1, pending.size(), "The maker's disbursement must be in the checker inbox");

            ok(() -> checker.makerCheckerOr4EyeFunctionality().approveMakerCheckerEntry(pending.get(0).getId(), "approve"));

            assertEquals(ACTIVE, getLoanDetails(loanId).getStatus().getCode());
            assertEquals(0, pendingFor(loanId).size());
        });
    }

    @Test
    void checkerSuperUsersDisbursementIsAppliedImmediately() {
        runAt(DATE, () -> {
            final Long loanId = approvedLoan();

            ok(() -> admin.loans().handleCommandsLoan(loanId, LoanRequestBuilders.disburseLoan(1000.0, DATE),
                    Map.of("command", "disburse")));

            assertEquals(ACTIVE, getLoanDetails(loanId).getStatus().getCode());
            assertEquals(0, pendingFor(loanId).size());
        });
    }

    private Long approvedLoan() {
        final Long productId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());
        return applyAndApproveLoan(createClient(), productId, DATE, 1000.0);
    }

    private FineractFeignClient userWith(final String permission, final Long staffId) {
        final Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, Map.of(permission, true));
        final String username = Utils.uniqueRandomStringGenerator("user", 8);
        FeignUserHelper.createUser(roleId, staffId, username, PASSWORD);
        return FineractFeignClientHelper.createNewFineractFeignClient(username, PASSWORD);
    }

    private List<AuditData> pendingFor(final Long loanId) {
        return ok(() -> admin.makerCheckerOr4EyeFunctionality().retrieveCommandsUniversal(Map.of("loanid", loanId.toString())));
    }
}
