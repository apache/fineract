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

import com.fasterxml.jackson.databind.JsonNode;
import feign.Headers;
import feign.RequestLine;
import java.math.BigDecimal;
import java.util.stream.StreamSupport;
import org.apache.fineract.client.models.CollectionSheetRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class CollectionSheetIntegrationTest extends FeignLoanTestBase {

    private static final String NONE = "1";

    interface IndividualCollectionSheetApi {

        @RequestLine("POST /v1/collectionsheet?command=generateCollectionSheet")
        @Headers({ "Content-Type: application/json", "Accept: application/json" })
        JsonNode generate(CollectionSheetRequest request);
    }

    @Test
    public void generateIndividualCollectionSheetWithDueInstallmentAndCharge() {
        final Long clientId = createClient("20 September 2011");
        final PostLoanProductsRequest loanProductRequest = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withAccounting(NONE, new Account[0]) //
                .buildRequest(null);
        final Long loanProductId = createLoanProduct(loanProductRequest);

        final Long loanId = applyForLoan(LoanRequestBuilders.legacyIndividualApplication(clientId, loanProductId, "12,000.00", 4,
                new BigDecimal("2"), "20 September 2011"));
        approveLoan(loanId, approveLoanRequest(12000.0, "20 September 2011"));
        disburseLoanWithNetDisbursalAmount(loanId, "20 September 2011", "12000.00");
        addLoanCharge(loanId, createLoanSpecifiedDueDateCharge(10.0), "25 September 2011", 10.0);

        final JsonNode collectionSheet = ok(() -> fineractClient().create(IndividualCollectionSheetApi.class).generate(
                new CollectionSheetRequest().officeId(1L).transactionDate("20 October 2011").dateFormat("dd MMMM yyyy").locale("en")));

        final JsonNode client = StreamSupport.stream(collectionSheet.path("clients").spliterator(), false)
                .filter(c -> c.path("clientId").asLong() == clientId).findFirst().orElse(null);
        assertNotNull(client, "Client with a due installment is missing from the collection sheet");
        final JsonNode loan = StreamSupport.stream(client.path("loans").spliterator(), false)
                .filter(l -> l.path("loanId").asLong() == loanId).findFirst().orElse(null);
        assertNotNull(loan, "Loan with a due installment is missing from the collection sheet");
        assertTrue(loan.path("principalDue").decimalValue().signum() > 0);
        assertEquals(0, new BigDecimal("10").compareTo(loan.path("chargesDue").decimalValue()));
    }
}
