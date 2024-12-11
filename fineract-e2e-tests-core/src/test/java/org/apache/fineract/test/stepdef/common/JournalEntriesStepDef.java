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
package org.apache.fineract.test.stepdef.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.JournalEntriesApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.test.data.TransactionType;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class JournalEntriesStepDef extends AbstractStepDef {

    public static final String DATE_FORMAT = "dd MMMM yyyy";

    @Autowired
    private LoansApi loansApi;

    @Autowired
    private JournalEntriesApi journalEntriesApi;

    @Then("Loan Transactions tab has a {string} transaction with date {string} which has the following Journal entries:")
    public void journalEntryDataCheck(String transactionType, String transactionDate, DataTable table) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        TransactionType transactionType1 = TransactionType.valueOf(transactionType);
        String transactionTypeExpected = transactionType1.getValue();

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
        List<GetLoansLoanIdTransactions> transactionsMatch = transactions.stream()
                .filter(t -> transactionDate.equals(formatter.format(t.getDate()))
                        && transactionTypeExpected.equals(t.getType().getCode().substring(20)))
                .collect(Collectors.toList());

        List<List<JournalEntryTransactionItem>> journalLinesActualList = transactionsMatch.stream().map(t -> {
            String transactionId = "L" + t.getId();
            Response<GetJournalEntriesTransactionIdResponse> journalEntryDataResponse = null;
            try {
                journalEntryDataResponse = journalEntriesApi.retrieveAll1(//
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        transactionId, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        true//
                ).execute();
                ErrorHelper.checkSuccessfulApiCall(journalEntryDataResponse);
            } catch (IOException e) {
                log.error("Exception", e);
            }

            return journalEntryDataResponse.body().getPageItems();
        }).collect(Collectors.toList());

        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<List<List<String>>> possibleActualValuesList = new ArrayList<>();
            List<String> expectedValues = data.get(i);
            boolean containsAnyExpected = false;

            for (int j = 0; j < journalLinesActualList.size(); j++) {
                List<JournalEntryTransactionItem> journalLinesActual = journalLinesActualList.get(j);

                List<List<String>> actualValuesList = journalLinesActual.stream().map(t -> {
                    List<String> actualValues = new ArrayList<>();
                    actualValues.add(t.getGlAccountType().getValue() == null ? null : t.getGlAccountType().getValue());
                    actualValues.add(t.getGlAccountCode() == null ? null : t.getGlAccountCode());
                    actualValues.add(t.getGlAccountName() == null ? null : t.getGlAccountName());
                    actualValues.add("DEBIT".equals(t.getEntryType().getValue()) ? String.valueOf(t.getAmount()) : null);
                    actualValues.add("CREDIT".equals(t.getEntryType().getValue()) ? String.valueOf(t.getAmount()) : null);

                    return actualValues;
                }).collect(Collectors.toList());
                possibleActualValuesList.add(actualValuesList);

                boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));
                if (containsExpectedValues) {
                    containsAnyExpected = true;
                }
            }
            assertThat(containsAnyExpected)
                    .as(ErrorMessageHelper.wrongValueInLineInJournalEntries(resourceId, i, possibleActualValuesList, expectedValues))
                    .isTrue();
        }
    }

    @Then("In Loan transactions the replayed {string} transaction with date {string} has a reverted transaction pair with the following Journal entries:")
    public void revertedJournalEntryDataCheck(String transactionType, String transactionDate, DataTable table) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();
        String resourceId = String.valueOf(loanId);

        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(loanDetailsResponse);

        TransactionType transactionType1 = TransactionType.valueOf(transactionType);
        String transactionTypeExpected = transactionType1.getValue();

        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();

        List<GetLoansLoanIdTransactions> transactionsMatch = transactions.stream()
                .filter(t -> transactionDate.equals(formatter.format(t.getDate()))
                        && transactionTypeExpected.equals(t.getType().getCode().substring(20)))
                .collect(Collectors.toList());

        List<String> transactionIdList = transactionsMatch.stream().flatMap(t -> t.getTransactionRelations().stream()
                .filter(e -> "REPLAYED".equals(e.getRelationType())).map(c -> "L" + c.getToLoanTransaction().toString()))
                .collect(Collectors.toList());

        List<List<JournalEntryTransactionItem>> journalLinesActualList = transactionIdList.stream().map(t -> {
            Response<GetJournalEntriesTransactionIdResponse> journalEntryDataResponse = null;
            try {
                journalEntryDataResponse = journalEntriesApi.retrieveAll1(//
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        t, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        true//
                ).execute();
                ErrorHelper.checkSuccessfulApiCall(journalEntryDataResponse);
            } catch (IOException e) {
                log.error("Exception", e);
            }

            return journalEntryDataResponse.body().getPageItems();
        }).collect(Collectors.toList());

        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<List<List<String>>> possibleActualValuesList = new ArrayList<>();
            List<String> expectedValues = data.get(i);
            boolean containsAnyExpected = false;

            for (int j = 0; j < journalLinesActualList.size(); j++) {
                List<JournalEntryTransactionItem> journalLinesActual = journalLinesActualList.get(j);

                List<List<String>> actualValuesList = journalLinesActual.stream().map(t -> {
                    List<String> actualValues = new ArrayList<>();
                    actualValues.add(t.getGlAccountType().getValue() == null ? null : t.getGlAccountType().getValue());
                    actualValues.add(t.getGlAccountCode() == null ? null : t.getGlAccountCode());
                    actualValues.add(t.getGlAccountName() == null ? null : t.getGlAccountName());
                    actualValues.add("DEBIT".equals(t.getEntryType().getValue()) ? String.valueOf(t.getAmount()) : null);
                    actualValues.add("CREDIT".equals(t.getEntryType().getValue()) ? String.valueOf(t.getAmount()) : null);

                    return actualValues;
                }).collect(Collectors.toList());
                possibleActualValuesList.add(actualValuesList);

                boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));
                if (containsExpectedValues) {
                    containsAnyExpected = true;
                }
            }
            assertThat(containsAnyExpected)
                    .as(ErrorMessageHelper.wrongValueInLineInJournalEntries(resourceId, i, possibleActualValuesList, expectedValues))
                    .isTrue();
        }
    }
}
