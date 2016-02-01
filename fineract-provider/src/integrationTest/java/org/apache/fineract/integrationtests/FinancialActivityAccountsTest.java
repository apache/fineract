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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.apache.fineract.accounting.common.AccountingConstants.FINANCIAL_ACTIVITY;
import org.apache.fineract.accounting.financialactivityaccount.exception.DuplicateFinancialActivityAccountFoundException;
import org.apache.fineract.accounting.financialactivityaccount.exception.FinancialActivityAccountInvalidException;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class FinancialActivityAccountsTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecForValidationError;
    private ResponseSpecification responseSpecForDomainRuleViolation;
    private ResponseSpecification responseSpecForResourceNotFoundError;
    private RequestSpecification requestSpec;
    private AccountHelper accountHelper;
    private FinancialActivityAccountHelper financialActivityAccountHelper;
    private final Integer assetTransferFinancialActivityId = FINANCIAL_ACTIVITY.ASSET_TRANSFER.getValue();
    public static final Integer liabilityTransferFinancialActivityId = FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue();

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecForValidationError = new ResponseSpecBuilder().expectStatusCode(400).build();
        this.responseSpecForDomainRuleViolation = new ResponseSpecBuilder().expectStatusCode(403).build();
        this.responseSpecForResourceNotFoundError = new ResponseSpecBuilder().expectStatusCode(404).build();
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.financialActivityAccountHelper = new FinancialActivityAccountHelper(this.requestSpec);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFinancialActivityAccounts() {

        /** Create a Liability and an Asset Transfer Account **/
        Account liabilityTransferAccount = accountHelper.createLiabilityAccount();
        Account assetTransferAccount = accountHelper.createAssetAccount();
        Assert.assertNotNull(assetTransferAccount);
        Assert.assertNotNull(liabilityTransferAccount);

        /*** Create A Financial Activity to Account Mapping **/
        Integer financialActivityAccountId = (Integer) financialActivityAccountHelper.createFinancialActivityAccount(
                liabilityTransferFinancialActivityId, liabilityTransferAccount.getAccountID(), responseSpec,
                CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(financialActivityAccountId);

        /***
         * Fetch Created Financial Activity to Account Mapping and validate
         * created values
         **/
        assertFinancialActivityAccountCreation(financialActivityAccountId, liabilityTransferFinancialActivityId, liabilityTransferAccount);

        /**
         * Update Existing Financial Activity to Account Mapping and assert
         * changes
         **/
        Account newLiabilityTransferAccount = accountHelper.createLiabilityAccount();
        Assert.assertNotNull(newLiabilityTransferAccount);

        HashMap changes = (HashMap) financialActivityAccountHelper.updateFinancialActivityAccount(financialActivityAccountId,
                liabilityTransferFinancialActivityId, newLiabilityTransferAccount.getAccountID(), responseSpec,
                CommonConstants.RESPONSE_CHANGES);
        Assert.assertEquals(newLiabilityTransferAccount.getAccountID(), changes.get("glAccountId"));

        /** Validate update works correctly **/
        assertFinancialActivityAccountCreation(financialActivityAccountId, liabilityTransferFinancialActivityId,
                newLiabilityTransferAccount);

        /** Update with Invalid Financial Activity should fail **/
        List<HashMap> invalidFinancialActivityUpdateError = (List<HashMap>) financialActivityAccountHelper.updateFinancialActivityAccount(
                financialActivityAccountId, 232, newLiabilityTransferAccount.getAccountID(), responseSpecForValidationError,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("validation.msg.financialactivityaccount.financialActivityId.is.not.one.of.expected.enumerations",
                invalidFinancialActivityUpdateError.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        /** Creating Duplicate Financial Activity should fail **/
        List<HashMap> duplicateFinancialActivityAccountError = (List<HashMap>) financialActivityAccountHelper
                .createFinancialActivityAccount(liabilityTransferFinancialActivityId, liabilityTransferAccount.getAccountID(),
                        responseSpecForDomainRuleViolation, CommonConstants.RESPONSE_ERROR);
        assertEquals(DuplicateFinancialActivityAccountFoundException.getErrorcode(),
                duplicateFinancialActivityAccountError.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        /**
         * Associating incorrect GL account types with a financial activity
         * should fail
         **/
        List<HashMap> invalidFinancialActivityAccountError = (List<HashMap>) financialActivityAccountHelper.updateFinancialActivityAccount(
                financialActivityAccountId, assetTransferFinancialActivityId, newLiabilityTransferAccount.getAccountID(),
                responseSpecForDomainRuleViolation, CommonConstants.RESPONSE_ERROR);
        assertEquals(FinancialActivityAccountInvalidException.getErrorcode(),
                invalidFinancialActivityAccountError.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        /** Should be able to delete a Financial Activity to Account Mapping **/
        Integer deletedFinancialActivityAccountId = financialActivityAccountHelper.deleteFinancialActivityAccount(
                financialActivityAccountId, responseSpec, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(deletedFinancialActivityAccountId);
        Assert.assertEquals(financialActivityAccountId, deletedFinancialActivityAccountId);

        /*** Trying to fetch a Deleted Account Mapping should give me a 404 **/
        financialActivityAccountHelper.getFinancialActivityAccount(deletedFinancialActivityAccountId, responseSpecForResourceNotFoundError);
    }

    private void assertFinancialActivityAccountCreation(Integer financialActivityAccountId, Integer financialActivityId, Account glAccount) {
        HashMap mappingDetails = financialActivityAccountHelper.getFinancialActivityAccount(financialActivityAccountId, responseSpec);
        Assert.assertEquals(financialActivityId, ((HashMap) mappingDetails.get("financialActivityData")).get("id"));
        Assert.assertEquals(glAccount.getAccountID(), ((HashMap) mappingDetails.get("glAccountData")).get("id"));
    }

    /**
     * Delete the Financial activities
     */
    @After
    public void tearDown() {
        List<HashMap> financialActivities = this.financialActivityAccountHelper.getAllFinancialActivityAccounts(this.responseSpec);
        for (HashMap financialActivity : financialActivities) {
            Integer financialActivityAccountId = (Integer) financialActivity.get("id");
            Integer deletedFinancialActivityAccountId = this.financialActivityAccountHelper.deleteFinancialActivityAccount(
                    financialActivityAccountId, this.responseSpec, CommonConstants.RESPONSE_RESOURCE_ID);
            Assert.assertNotNull(deletedFinancialActivityAccountId);
            Assert.assertEquals(financialActivityAccountId, deletedFinancialActivityAccountId);
        }
    }
}
