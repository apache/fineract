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

import java.math.BigDecimal;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanApplicationBorrowerCycleTest extends FeignLoanTestBase {

    @Test
    public void testFirstLoanApplicationOnBorrowerCycleProductSucceeds() {
        final Long clientId = createClient();

        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder() //
                .withPrincipal("10000.00") //
                .withNumberOfRepayments("6") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withRepaymentStrategy(LoanProductTestBuilder.DEFAULT_STRATEGY) //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "0") //
                .buildRequest(null) //
                .useBorrowerCycle(true));

        // the client has no loans for this product yet, so its loan counter lookup finds no rows
        final Long loanId = applyForLoan(
                LoanRequestBuilders.legacyIndividualApplication(clientId, loanProductId, "10000", 6, BigDecimal.valueOf(2), "12 July 2022") //
                        .interestType(LoanTestData.InterestType.FLAT) //
                        .submittedOnDate("10 July 2022"));

        assertNotNull(loanId);
    }
}
