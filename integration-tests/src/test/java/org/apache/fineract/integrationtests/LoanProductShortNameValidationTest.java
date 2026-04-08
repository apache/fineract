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

import static org.apache.fineract.integrationtests.common.Utils.uniqueRandomStringGenerator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.junit.jupiter.api.Test;

public class LoanProductShortNameValidationTest {

    @Test
    public void createLoanProductsWithSameShortName() {
        String shortName = uniqueRandomStringGenerator("", 4);
        PostLoanProductsRequest request = buildMinimalLoanProductRequest(shortName);

        // First creation should succeed
        Calls.ok(FineractClientHelper.getFineractClient().loanProducts.createLoanProduct(request));

        // Second creation with same short name should fail with 403
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> Calls.ok(FineractClientHelper.getFineractClient().loanProducts.createLoanProduct(request)));
        assertEquals(403, exception.getResponse().code());
    }

    private PostLoanProductsRequest buildMinimalLoanProductRequest(String shortName) {
        return new PostLoanProductsRequest().name(uniqueRandomStringGenerator("LoanProduct_", 4)).shortName(shortName).principal(10000.0)
                .numberOfRepayments(5).repaymentEvery(1).repaymentFrequencyType(2L).interestRatePerPeriod(2.0).interestRateFrequencyType(2)
                .amortizationType(1).interestType(0).transactionProcessingStrategyCode("mifos-standard-strategy").currencyCode("USD")
                .digitsAfterDecimal(2).inMultiplesOf(0).locale("en").dateFormat("dd MMMM yyyy").interestCalculationPeriodType(1)
                .daysInYearType(1).daysInMonthType(1).isInterestRecalculationEnabled(false).accountingRule(1);
    }
}
