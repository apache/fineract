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
package org.apache.fineract.test.stepdef.loan;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.CapitalizedIncomeDetails;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanCapitalizedIncomeAmortizationTransactionCreatedEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class LoanCapitalizedIncomeStepDef extends AbstractStepDef {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    @Autowired
    FineractFeignClient fineractClient;

    @Autowired
    EventAssertion eventAssertion;

    @Then("Loan Capitalized Income Amortization Transaction Created Business Event is created on {string}")
    public void checkLoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEventCreated(String date) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        GetLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "transactions")));

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions capitalizedIncomeAmortizationTransaction = transactions.stream()
                .filter(t -> date.equals(FORMATTER.format(t.getDate())) && "Capitalized Income Amortization".equals(t.getType().getValue()))
                .reduce((first, second) -> second).orElseThrow(
                        () -> new IllegalStateException(String.format("No Capitalized Income Amortization transaction found on %s", date)));
        Long capitalizedIncomeAmortizationTransactionId = capitalizedIncomeAmortizationTransaction.getId();

        eventAssertion.assertEventRaised(LoanCapitalizedIncomeAmortizationTransactionCreatedEvent.class,
                capitalizedIncomeAmortizationTransactionId);
    }

    @Then("Deferred Capitalized Income contains the following data:")
    public void verifyDeferredCapitalizedIncome(DataTable dataTable) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();

        List<CapitalizedIncomeDetails> capitalizedIncomeDetails = ok(
                () -> fineractClient.loanCapitalizedIncome().fetchCapitalizedIncomeDetails(loanId));

        List<Map<String, String>> data = dataTable.asMaps();
        Map<String, String> expectedData = data.get(0);

        assertThat(capitalizedIncomeDetails).isNotNull().isNotEmpty();
        CapitalizedIncomeDetails actualData = capitalizedIncomeDetails.get(0);

        BigDecimal expectedAmount = new BigDecimal(expectedData.get("Amount"));
        BigDecimal expectedAmortizedAmount = new BigDecimal(expectedData.get("Amortized Amount"));
        BigDecimal expectedUnrecognizedAmount = new BigDecimal(expectedData.get("Unrecognized Amount"));
        BigDecimal expectedAdjustedAmount = new BigDecimal(expectedData.get("Adjusted Amount"));
        BigDecimal expectedChargedOffAmount = new BigDecimal(expectedData.get("Charged Off Amount"));

        BigDecimal actualAmount = actualData.getAmount() != null ? actualData.getAmount() : BigDecimal.ZERO;
        BigDecimal actualAmortizedAmount = actualData.getAmortizedAmount() != null ? actualData.getAmortizedAmount() : BigDecimal.ZERO;
        BigDecimal actualUnrecognizedAmount = actualData.getUnrecognizedAmount() != null ? actualData.getUnrecognizedAmount()
                : BigDecimal.ZERO;
        BigDecimal actualAdjustedAmount = actualData.getAmountAdjustment() != null ? actualData.getAmountAdjustment() : BigDecimal.ZERO;
        BigDecimal actualChargedOffAmount = actualData.getChargedOffAmount() != null ? actualData.getChargedOffAmount() : BigDecimal.ZERO;

        assertThat(actualAmount).as(ErrorMessageHelper.wrongAmountInDeferredCapitalizedIncome(actualAmount, expectedAmount))
                .isEqualByComparingTo(expectedAmount);
        assertThat(actualAmortizedAmount)
                .as(ErrorMessageHelper.wrongAmountInDeferredCapitalizedIncome(actualAmortizedAmount, expectedAmortizedAmount))
                .isEqualByComparingTo(expectedAmortizedAmount);
        assertThat(actualUnrecognizedAmount)
                .as(ErrorMessageHelper.wrongAmountInDeferredCapitalizedIncome(actualUnrecognizedAmount, expectedUnrecognizedAmount))
                .isEqualByComparingTo(expectedUnrecognizedAmount);
        assertThat(actualAdjustedAmount)
                .as(ErrorMessageHelper.wrongAmountInDeferredCapitalizedIncome(actualAdjustedAmount, expectedAdjustedAmount))
                .isEqualByComparingTo(expectedAdjustedAmount);
        assertThat(actualChargedOffAmount)
                .as(ErrorMessageHelper.wrongAmountInDeferredCapitalizedIncome(actualChargedOffAmount, expectedChargedOffAmount))
                .isEqualByComparingTo(expectedChargedOffAmount);
    }

    @Then("Deferred Capitalized Income by external-id contains the following data:")
    public void verifyDeferredCapitalizedIncomeByExternalId(DataTable dataTable) {
        PostLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        String loanExternalId = loanCreateResponse.getResourceExternalId();

        List<CapitalizedIncomeDetails> capitalizedIncomeDetails = ok(
                () -> fineractClient.loanCapitalizedIncome().fetchCapitalizedIncomeDetailsByExternalId(loanExternalId));

        List<Map<String, String>> data = dataTable.asMaps();
        Map<String, String> expectedData = data.get(0);

        assertThat(capitalizedIncomeDetails).isNotNull().isNotEmpty();
        CapitalizedIncomeDetails actualData = capitalizedIncomeDetails.get(0);

        BigDecimal expectedAmount = new BigDecimal(expectedData.get("Amount"));
        BigDecimal expectedAmortizedAmount = new BigDecimal(expectedData.get("Amortized Amount"));
        BigDecimal expectedUnrecognizedAmount = new BigDecimal(expectedData.get("Unrecognized Amount"));
        BigDecimal expectedAdjustedAmount = new BigDecimal(expectedData.get("Adjusted Amount"));
        BigDecimal expectedChargedOffAmount = new BigDecimal(expectedData.get("Charged Off Amount"));

        BigDecimal actualAmount = actualData.getAmount() != null ? actualData.getAmount() : BigDecimal.ZERO;
        BigDecimal actualAmortizedAmount = actualData.getAmortizedAmount() != null ? actualData.getAmortizedAmount() : BigDecimal.ZERO;
        BigDecimal actualUnrecognizedAmount = actualData.getUnrecognizedAmount() != null ? actualData.getUnrecognizedAmount()
                : BigDecimal.ZERO;
        BigDecimal actualAdjustedAmount = actualData.getAmountAdjustment() != null ? actualData.getAmountAdjustment() : BigDecimal.ZERO;
        BigDecimal actualChargedOffAmount = actualData.getChargedOffAmount() != null ? actualData.getChargedOffAmount() : BigDecimal.ZERO;

        assertThat(actualAmount).as(ErrorMessageHelper.wrongAmountInDeferredCapitalizedIncome(actualAmount, expectedAmount))
                .isEqualByComparingTo(expectedAmount);
        assertThat(actualAmortizedAmount)
                .as(ErrorMessageHelper.wrongAmountInDeferredCapitalizedIncome(actualAmortizedAmount, expectedAmortizedAmount))
                .isEqualByComparingTo(expectedAmortizedAmount);
        assertThat(actualUnrecognizedAmount)
                .as(ErrorMessageHelper.wrongAmountInDeferredCapitalizedIncome(actualUnrecognizedAmount, expectedUnrecognizedAmount))
                .isEqualByComparingTo(expectedUnrecognizedAmount);
        assertThat(actualAdjustedAmount)
                .as(ErrorMessageHelper.wrongAmountInDeferredCapitalizedIncome(actualAdjustedAmount, expectedAdjustedAmount))
                .isEqualByComparingTo(expectedAdjustedAmount);
        assertThat(actualChargedOffAmount)
                .as(ErrorMessageHelper.wrongAmountInDeferredCapitalizedIncome(actualChargedOffAmount, expectedChargedOffAmount))
                .isEqualByComparingTo(expectedChargedOffAmount);
    }

}
