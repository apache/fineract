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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanValidationIntegrationTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(LoanValidationIntegrationTest.class);
    private static final String NEW_USER_PASSWORD = "A1b2c3d4e5f$";

    @Test
    public void checkPrincipalErrors() {
        final Long staffId = new FeignStaffHelper(FineractFeignClientHelper.getFineractFeignClient()).createStaff().getResourceId();
        String username = Utils.uniqueRandomStringGenerator("user", 8);
        FeignUserHelper.createUser(1L, staffId, username, NEW_USER_PASSWORD);

        LOG.info("-------------------------Creating Client---------------------------");
        final Long clientId = createClient();
        Assertions.assertNotNull(clientHelper.getClient(clientId));

        LOG.info("-------------------------Creating Loan---------------------------");
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder() //
                .withPrincipal("10000000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withRepaymentStrategy(LoanProductTestBuilder.DEFAULT_STRATEGY) //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "0")
                .withAccounting("2", new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount }).buildRequest(null));

        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        // a negative principal is rejected, and the rejection names exactly one offending field
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> applyForLoan(LoanRequestBuilders
                        .legacyIndividualApplication(clientId, loanProductId, "-1", 6, BigDecimal.valueOf(2), "12 July 2022")//
                        .interestType(LoanTestData.InterestType.FLAT)//
                        .submittedOnDate("10 July 2022")));

        assertEquals(400, exception.getStatus());
        assertEquals(1, extractErrorCount(exception));
    }
}
