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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.integrationtests.common.Utils.uniqueRandomStringGenerator;

import java.util.function.Supplier;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostProductsTypeRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutProductsTypeProductIdRequest;
import org.apache.fineract.client.models.PutSavingsProductsProductIdRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.junit.jupiter.api.Test;

/**
 * Product short names may be at most 8 characters long (FINERACT-2854), on create and on update.
 */
public class ProductShortNameLengthValidationTest extends FeignIntegrationTest {

    private static final int MAX_SHORT_NAME_LENGTH = 8;
    private static final String SHARE = "share";

    @Test
    public void loanProductShortNameAcceptsEightAndRejectsNineCharacters() {
        Long productId = ok(() -> fineractClient().loanProducts().createLoanProduct(loanProductRequest(shortNameOfMaxLength())))
                .getResourceId();
        assertThat(ok(() -> fineractClient().loanProducts().retrieveOneLoanProduct(productId)).getShortName()).hasSize(8);

        assertShortNameTooLong("loanproduct",
                () -> fineractClient().loanProducts().createLoanProduct(loanProductRequest(shortNameOverMaxLength())));
        assertShortNameTooLong("loanproduct", () -> fineractClient().loanProducts().updateLoanProduct(productId,
                new PutLoanProductsProductIdRequest().shortName(shortNameOverMaxLength())));
    }

    @Test
    public void savingsProductShortNameAcceptsEightAndRejectsNineCharacters() {
        Long productId = ok(() -> fineractClient().savingsProduct()
                .createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().shortName(shortNameOfMaxLength()))).getResourceId();
        assertThat(ok(() -> fineractClient().savingsProduct().retrieveOneSavingsProduct(productId)).getShortName()).hasSize(8);

        assertShortNameTooLong("savingsproduct", () -> fineractClient().savingsProduct()
                .createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().shortName(shortNameOverMaxLength())));
        assertShortNameTooLong("savingsproduct", () -> fineractClient().savingsProduct().updateSavingsProduct(productId,
                new PutSavingsProductsProductIdRequest().shortName(shortNameOverMaxLength()).locale("en")));
    }

    @Test
    public void shareProductShortNameAcceptsEightAndRejectsNineCharacters() {
        Long productId = ok(() -> fineractClient().products().createShareProduct(SHARE, shareProductRequest(shortNameOfMaxLength())))
                .getResourceId();
        assertThat(ok(() -> fineractClient().products().retrieveOneShareProduct(productId, SHARE)).getShortName()).hasSize(8);

        assertShortNameTooLong("sharesproduct",
                () -> fineractClient().products().createShareProduct(SHARE, shareProductRequest(shortNameOverMaxLength())));
        assertShortNameTooLong("sharesproduct", () -> fineractClient().products().updateShareProduct(SHARE, productId,
                new PutProductsTypeProductIdRequest().shortName(shortNameOverMaxLength()).locale("en_GB")));
    }

    private static void assertShortNameTooLong(String resource, Supplier<?> call) {
        CallFailedRuntimeException exception = fail(call);
        assertThat(exception.getStatus()).isEqualTo(400);
        assertThat(exception.getResponseBody()).contains("validation.msg." + resource + ".shortName.exceeds.max.length");
        assertThat(exception.getDeveloperMessage()).contains("`shortName` exceeds max length of " + MAX_SHORT_NAME_LENGTH);
    }

    private static String shortNameOfMaxLength() {
        return uniqueRandomStringGenerator("", MAX_SHORT_NAME_LENGTH);
    }

    private static String shortNameOverMaxLength() {
        return uniqueRandomStringGenerator("", MAX_SHORT_NAME_LENGTH + 1);
    }

    private static PostLoanProductsRequest loanProductRequest(String shortName) {
        return new PostLoanProductsRequest().name(uniqueRandomStringGenerator("LoanProduct_", 6)).shortName(shortName).principal(10000.0)
                .numberOfRepayments(5).repaymentEvery(1).repaymentFrequencyType(2L).interestRatePerPeriod(2.0).interestRateFrequencyType(2)
                .amortizationType(1).interestType(0).transactionProcessingStrategyCode("mifos-standard-strategy").currencyCode("USD")
                .digitsAfterDecimal(2).inMultiplesOf(0).locale("en").dateFormat("dd MMMM yyyy").interestCalculationPeriodType(1)
                .daysInYearType(1).daysInMonthType(1).isInterestRecalculationEnabled(false).accountingRule(1);
    }

    private static PostProductsTypeRequest shareProductRequest(String shortName) {
        return new PostProductsTypeRequest().name(uniqueRandomStringGenerator("SHARE_PROD_", 6)).shortName(shortName)
                .description("Share product short name test").currencyCode("USD").digitsAfterDecimal(4).inMultiplesOf(0).locale("en_GB")
                .totalShares(10000).sharesIssued(10000).unitPrice(2).nominalShares(20).minimumShares(10).maximumShares(3000)
                .allowDividendCalculationForInactiveClients(true).accountingRule(1).minimumActivePeriodForDividends(1)
                .minimumactiveperiodFrequencyType(0).lockinPeriodFrequency(1).lockinPeriodFrequencyType(0);
    }
}
