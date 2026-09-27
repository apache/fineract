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
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.financialactivityaccount.exception.DuplicateFinancialActivityAccountFoundException;
import org.apache.fineract.accounting.financialactivityaccount.exception.FinancialActivityAccountInvalidException;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.PutFinancialActivityAccountsChanges;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FinancialActivityAccountsTest extends FeignIntegrationTest {

    private static final int BAD_REQUEST = 400;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final Integer INVALID_FINANCIAL_ACTIVITY_ID = 232;

    private FeignAccountHelper accountHelper;
    private FeignFinancialActivityAccountHelper financialActivityAccountHelper;
    private final Integer assetTransferFinancialActivityId = FinancialActivity.ASSET_TRANSFER.getValue();
    public static final Integer LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID = FinancialActivity.LIABILITY_TRANSFER.getValue();

    @BeforeAll
    public void setup() {
        this.accountHelper = new FeignAccountHelper(fineractClient());
        this.financialActivityAccountHelper = new FeignFinancialActivityAccountHelper(fineractClient());
    }

    @Test
    public void testFinancialActivityAccounts() {

        /** Create a Liability and an Asset Transfer Account **/
        Account liabilityTransferAccount = accountHelper.createLiabilityAccount();
        Account assetTransferAccount = accountHelper.createAssetAccount();
        Assertions.assertNotNull(assetTransferAccount);
        Assertions.assertNotNull(liabilityTransferAccount);

        /*** Create A Financial Activity to Account Mapping **/
        Long financialActivityAccountId = financialActivityAccountHelper
                .createMapping(LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID, liabilityTransferAccount).getResourceId();
        Assertions.assertNotNull(financialActivityAccountId);

        /***
         * Fetch Created Financial Activity to Account Mapping and validate created values
         **/
        assertFinancialActivityAccountCreation(financialActivityAccountId, LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID,
                liabilityTransferAccount);

        /**
         * Update Existing Financial Activity to Account Mapping and assert changes
         **/
        Account newLiabilityTransferAccount = accountHelper.createLiabilityAccount();
        Assertions.assertNotNull(newLiabilityTransferAccount);

        PutFinancialActivityAccountsChanges changes = financialActivityAccountHelper
                .updateMapping(financialActivityAccountId, LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID, newLiabilityTransferAccount)
                .getChanges();
        Assertions.assertEquals(newLiabilityTransferAccount.getAccountID().longValue(), changes.getGlAccountId());

        /** Validate update works correctly **/
        assertFinancialActivityAccountCreation(financialActivityAccountId, LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID,
                newLiabilityTransferAccount);

        /** Update with Invalid Financial Activity should fail **/
        CallFailedRuntimeException invalidFinancialActivityUpdateError = financialActivityAccountHelper
                .updateMappingExpectingError(financialActivityAccountId, INVALID_FINANCIAL_ACTIVITY_ID, newLiabilityTransferAccount);
        assertEquals(BAD_REQUEST, invalidFinancialActivityUpdateError.getStatus());
        assertEquals("validation.msg.financialactivityaccount.financialActivityId.is.not.one.of.expected.enumerations",
                FeignErrors.errorGlobalisationCode(invalidFinancialActivityUpdateError));

        /** Creating Duplicate Financial Activity should fail **/
        CallFailedRuntimeException duplicateFinancialActivityAccountError = financialActivityAccountHelper
                .createMappingExpectingError(LIABILITY_TRANSFER_FINANCIAL_ACTIVITY_ID, liabilityTransferAccount);
        assertEquals(FORBIDDEN, duplicateFinancialActivityAccountError.getStatus());
        assertEquals(DuplicateFinancialActivityAccountFoundException.getErrorcode(),
                FeignErrors.errorGlobalisationCode(duplicateFinancialActivityAccountError));

        /**
         * Associating incorrect GL account types with a financial activity should fail
         **/
        CallFailedRuntimeException invalidFinancialActivityAccountError = financialActivityAccountHelper
                .updateMappingExpectingError(financialActivityAccountId, assetTransferFinancialActivityId, newLiabilityTransferAccount);
        assertEquals(FORBIDDEN, invalidFinancialActivityAccountError.getStatus());
        assertEquals(FinancialActivityAccountInvalidException.getErrorcode(),
                FeignErrors.errorGlobalisationCode(invalidFinancialActivityAccountError));

        /** Should be able to delete a Financial Activity to Account Mapping **/
        Long deletedFinancialActivityAccountId = financialActivityAccountHelper.deleteMapping(financialActivityAccountId).getResourceId();
        Assertions.assertNotNull(deletedFinancialActivityAccountId);
        Assertions.assertEquals(financialActivityAccountId, deletedFinancialActivityAccountId);

        /*** Trying to fetch a Deleted Account Mapping should give me a 404 **/
        assertEquals(NOT_FOUND, financialActivityAccountHelper.getMappingExpectingError(deletedFinancialActivityAccountId).getStatus());
    }

    private void assertFinancialActivityAccountCreation(Long financialActivityAccountId, Integer financialActivityId, Account glAccount) {
        GetFinancialActivityAccountsResponse mappingDetails = financialActivityAccountHelper.getMapping(financialActivityAccountId);
        Assertions.assertEquals(financialActivityId, mappingDetails.getFinancialActivityData().getId());
        Assertions.assertEquals(glAccount.getAccountID().longValue(), mappingDetails.getGlAccountData().getId());
    }

    /**
     * Delete the Financial activities
     */
    @AfterEach
    public void tearDown() {
        List<GetFinancialActivityAccountsResponse> financialActivities = this.financialActivityAccountHelper.getAllMappings();
        for (GetFinancialActivityAccountsResponse financialActivity : financialActivities) {
            Long financialActivityAccountId = financialActivity.getId();
            Long deletedFinancialActivityAccountId = this.financialActivityAccountHelper.deleteMapping(financialActivityAccountId)
                    .getResourceId();
            Assertions.assertNotNull(deletedFinancialActivityAccountId);
            Assertions.assertEquals(financialActivityAccountId, deletedFinancialActivityAccountId);
        }
    }
}
