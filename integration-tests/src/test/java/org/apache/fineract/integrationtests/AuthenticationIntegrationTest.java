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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class AuthenticationIntegrationTest extends FeignLoanTestBase {

    private static final String LOAN_DATE = "11 July 2022";
    private static final String APPROVE_COMMAND = "approve";
    private static final String CASH_BASED_ACCOUNTING = "2";
    // The Feign client adds Basic credentials to every call unless an Authorization header is already set.
    // The server's basic-auth filter ignores a header without the Basic scheme, so the call arrives unauthenticated.
    private static final Map<String, String> NO_CREDENTIALS = Map.of("Authorization", "None");
    private Long loanID;

    @BeforeEach
    public void setup() {
        Long staffId = new FeignStaffHelper(fineractClient()).createStaff().getResourceId();
        String username = Utils.uniqueRandomStringGenerator("user", 8);
        FeignUserHelper.createUser(1L, staffId, username, "A1b2c3d4e5f$");
        Long clientID = clientHelper.createClient();

        Long loanProductID = setupLoanProduct();
        this.loanID = applyForLoan(new PostLoansRequest().clientId(clientID).productId(loanProductID).loanType("individual")
                .principal(new BigDecimal("10000")).loanTermFrequency(6).loanTermFrequencyType(2).numberOfRepayments(6).repaymentEvery(1)
                .repaymentFrequencyType(2).interestRatePerPeriod(new BigDecimal("2")).amortizationType(1).interestType(1)
                .interestCalculationPeriodType(1).transactionProcessingStrategyCode(LoanApplicationTestBuilder.DEFAULT_STRATEGY)
                .expectedDisbursementDate(LOAN_DATE).submittedOnDate("10 July 2022").maxOutstandingLoanBalance(new BigDecimal("36000"))
                .collateral(new ArrayList<>()).charges(new ArrayList<>()).dateFormat("dd MMMM yyyy").locale("en_GB"));
    }

    @Test
    public void shouldAllowAccessForAuthenticatedUser() {
        PostLoansLoanIdResponse response = ok(
                () -> fineractClient().loans().stateTransitions(loanID, createLoanApprovalRequest(), APPROVE_COMMAND));

        assertEquals(200L, response.getChanges().getStatus().getId());
    }

    @Test
    public void shouldReturnUnauthorizedForUnauthenticatedAccess() throws JsonProcessingException {
        FeignException.Unauthorized exception = assertThrows(FeignException.Unauthorized.class,
                () -> fineractClient().loans().stateTransitions(loanID, createLoanApprovalRequest(), APPROVE_COMMAND, NO_CREDENTIALS));

        Map<String, Object> response = new ObjectMapper().readValue(exception.contentUTF8(), new TypeReference<>() {});

        assertEquals(401, (Integer) response.get("status"));
        assertEquals("Unauthorized", response.get("error"));
    }

    private Long setupLoanProduct() {
        Account[] productAccounts = { accountHelper.createAssetAccount(), accountHelper.createIncomeAccount(),
                accountHelper.createExpenseAccount(), accountHelper.createLiabilityAccount() };
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal("10000000.00").withNumberOfRepayments("24")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withRepaymentStrategy(LoanProductTestBuilder.DEFAULT_STRATEGY)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance().currencyDetails("0", "0")
                .withAccounting(CASH_BASED_ACCOUNTING, productAccounts).buildRequest(null));
    }

    private PostLoansLoanIdRequest createLoanApprovalRequest() {
        return new PostLoansLoanIdRequest().approvedOnDate(LOAN_DATE).note("Approval NOTE").dateFormat("dd MMMM yyyy").locale("en");
    }
}
