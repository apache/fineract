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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.apache.fineract.client.models.GetClientsClientIdAccountsResponse;
import org.apache.fineract.client.models.GetClientsLoanAccounts;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanAccountsContainsCurrencyFieldTest extends FeignLoanTestBase {

    private static final String PRINCIPAL_AMOUNT = "1200.00";
    private static final String NONE = "1";

    @Test
    public void testGetClientLoanAccountsUsingExternalIdContainsCurrency() {
        final String activationDate = "01 September 2022";

        // given
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                new PutGlobalConfigurationsRequest().enabled(true));

        // when
        final PostClientsResponse clientResponse = clientHelper
                .createClient(ClientRequestBuilders.createActivePersonClient(activationDate));
        final String clientExternalId = clientResponse.getResourceExternalId();
        final Long clientId = clientResponse.getClientId();

        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                new PutGlobalConfigurationsRequest().enabled(false));

        final Long loanProductId = createLoanProduct(buildLoanProductRequest());
        // Create Loan Account
        final Long loanId = createAndApproveLoan(clientId, loanProductId, activationDate);
        assertNotNull(loanId);

        final GetClientsClientIdAccountsResponse clientAccountsResponse = clientHelper.getClientAccounts(clientExternalId);
        final Set<GetClientsLoanAccounts> loanAccounts = clientAccountsResponse.getLoanAccounts();

        // Handle the case where getClientAccounts returned null
        assertNotNull(loanAccounts, "getClientAccounts returned no loan accounts");
        // Assert if loanAccounts contains a loan account with "currency" field
        assertTrue(loanAccounts.stream().anyMatch(account -> account.getCurrency() != null),
                "Loan accounts should expose the currency field");
    }

    private Long createAndApproveLoan(Long clientId, Long loanProductId, String operationDate) {
        final Long loanId = applyForLoan(new PostLoansRequest()//
                .clientId(clientId)//
                .productId(loanProductId)//
                .principal(new BigDecimal(PRINCIPAL_AMOUNT))//
                .loanTermFrequency(1)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(1)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.MIFOS_STANDARD_STRATEGY)//
                .expectedDisbursementDate("03 September 2022")//
                .submittedOnDate("01 September 2022")//
                .loanType("individual")//
                .maxOutstandingLoanBalance(new BigDecimal("36000"))//
                .collateral(List.of())//
                .locale("en_GB")//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
        approveLoan(loanId, approveLoanRequest(Double.valueOf(PRINCIPAL_AMOUNT), operationDate));
        return loanId;
    }

    private PostLoanProductsRequest buildLoanProductRequest() {
        return new LoanProductTestBuilder().withPrincipal("12,000.00").withNumberOfRepayments("4").withRepaymentAfterEvery("1")
                .withRepaymentTypeAsMonth().withinterestRatePerPeriod("1").withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance().withTranches(false)
                .withAccounting(NONE, new Account[] {}).buildRequest(null);
    }
}
