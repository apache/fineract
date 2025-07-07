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

import static org.apache.fineract.test.stepdef.loan.LoanRescheduleStepDef.FORMATTER_EN;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.JournalEntryCommand;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostJournalEntriesResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.JournalEntriesApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.test.data.TransactionType;
import org.apache.fineract.test.factory.LoanRequestFactory;
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

    @Autowired
    private LoanRequestFactory loanRequestFactory;

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
        final int expectedCount = data.size() - 1;
        final int actualCount = journalLinesActualList.stream().mapToInt(List::size).sum();
        assertThat(actualCount).as("The number of journal entries for the transaction does not match the expected count! Expected: "
                + expectedCount + ", Actual: " + actualCount).isEqualTo(expectedCount);
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

    @Then("Reversed loan capitalized income amortization transaction has the following Journal entries:")
    public void capitalizedIncomeAmortizationJournalEntryDataCheck(final DataTable table) {
        final long capitalizedIncomeAmortizationId = testContext().get(TestContextKey.LOAN_CAPITALIZED_INCOME_AMORTIZATION_ID);
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse.body() != null;
        final long loanId = loanResponse.body().getLoanId();
        final String resourceId = String.valueOf(loanId);

        List<JournalEntryTransactionItem> journalLinesActualList;
        final String transactionId = "L" + capitalizedIncomeAmortizationId;
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
                    loanId, //
                    null, //
                    null, //
                    true//
            ).execute();
            ErrorHelper.checkSuccessfulApiCall(journalEntryDataResponse);
        } catch (IOException e) {
            log.error("Exception", e);
        }
        assert journalEntryDataResponse != null;
        assert journalEntryDataResponse.body() != null;
        journalLinesActualList = journalEntryDataResponse.body().getPageItems();

        final List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            final List<List<String>> possibleActualValuesList = new ArrayList<>();
            final List<String> expectedValues = data.get(i);
            boolean containsAnyExpected = false;

            for (int j = 0; j < Objects.requireNonNull(journalLinesActualList).size(); j++) {
                final JournalEntryTransactionItem journalLinesActual = journalLinesActualList.get(j);
                final List<String> actualValues = new ArrayList<>();
                assert journalLinesActual.getGlAccountType() != null;
                actualValues.add(
                        journalLinesActual.getGlAccountType().getValue() == null ? null : journalLinesActual.getGlAccountType().getValue());
                actualValues.add(journalLinesActual.getGlAccountCode() == null ? null : journalLinesActual.getGlAccountCode());
                actualValues.add(journalLinesActual.getGlAccountName() == null ? null : journalLinesActual.getGlAccountName());
                assert journalLinesActual.getEntryType() != null;
                actualValues
                        .add("DEBIT".equals(journalLinesActual.getEntryType().getValue()) ? String.valueOf(journalLinesActual.getAmount())
                                : null);
                actualValues
                        .add("CREDIT".equals(journalLinesActual.getEntryType().getValue()) ? String.valueOf(journalLinesActual.getAmount())
                                : null);
                possibleActualValuesList.add(actualValues);

                final boolean containsExpectedValues = possibleActualValuesList.stream()
                        .anyMatch(actualValue -> actualValue.equals(expectedValues));
                if (containsExpectedValues) {
                    containsAnyExpected = true;
                }
            }
            assertThat(containsAnyExpected)
                    .as(ErrorMessageHelper.wrongValueInLineInJournalEntry(resourceId, i, possibleActualValuesList, expectedValues))
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

    @Then("Loan Transactions tab has a {string} transaction with date {string} has no the Journal entries")
    public void journalEntryNoDataCheck(String transactionType, String transactionDate) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.body().getLoanId();

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

        assertThat(journalLinesActualList.stream().findFirst().get().size()).isZero();
    }

    public Response<PostJournalEntriesResponse> addManualJournalEntryWithoutExternalAssetOwner(String amount, String date)
            throws IOException {
        LocalDate transactionDate = LocalDate.parse(date, FORMATTER_EN);
        JournalEntryCommand journalEntriesRequest = loanRequestFactory.defaultManualJournalEntryRequest(new BigDecimal(amount))
                .transactionDate(transactionDate);
        Response<PostJournalEntriesResponse> journalEntriesResponse = journalEntriesApi.createGLJournalEntry("", journalEntriesRequest)
                .execute();
        testContext().set(TestContextKey.MANUAL_JOURNAL_ENTRIES_REQUEST, journalEntriesRequest);
        return journalEntriesResponse;
    }

    public Response<PostJournalEntriesResponse> addManualJournalEntryWithExternalAssetOwner(String amount, String date,
            String externalAssetOwner) throws IOException {
        LocalDate transactionDate = LocalDate.parse(date, FORMATTER_EN);
        JournalEntryCommand journalEntriesRequest = loanRequestFactory
                .defaultManualJournalEntryRequest(new BigDecimal(amount), externalAssetOwner).transactionDate(transactionDate);
        Response<PostJournalEntriesResponse> journalEntriesResponse = journalEntriesApi.createGLJournalEntry("", journalEntriesRequest)
                .execute();
        testContext().set(TestContextKey.MANUAL_JOURNAL_ENTRIES_REQUEST, journalEntriesRequest);
        return journalEntriesResponse;
    }

    @Then("Admin creates manual Journal entry with {string} amount and {string} date and unique External Asset Owner")
    public void createManualJournalEntryWithExternalAssetOwner(String amount, String date) throws IOException {
        String ownerExternalIdStored = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        Response<PostJournalEntriesResponse> journalEntriesResponse = addManualJournalEntryWithExternalAssetOwner(amount, date,
                ownerExternalIdStored);

        testContext().set(TestContextKey.MANUAL_JOURNAL_ENTRIES_RESPONSE, journalEntriesResponse);
        ErrorHelper.checkSuccessfulApiCall(journalEntriesResponse);
    }

    @Then("Admin creates manual Journal entry with {string} amount and {string} date and empty External Asset Owner")
    public void createManualJournalEntryWithEmptyExternalAssetOwner(String amount, String date) throws IOException {
        Response<PostJournalEntriesResponse> journalEntriesResponse = addManualJournalEntryWithExternalAssetOwner(amount, date, "");

        testContext().set(TestContextKey.MANUAL_JOURNAL_ENTRIES_RESPONSE, journalEntriesResponse);
        ErrorHelper.checkSuccessfulApiCall(journalEntriesResponse);
    }

    @Then("Admin creates manual Journal entry with {string} amount and {string} date and without External Asset Owner")
    public void createManualJournalEntryWithoutExternalAssetOwner(String amount, String date) throws IOException {
        Response<PostJournalEntriesResponse> journalEntriesResponse = addManualJournalEntryWithoutExternalAssetOwner(amount, date);

        testContext().set(TestContextKey.MANUAL_JOURNAL_ENTRIES_RESPONSE, journalEntriesResponse);
        ErrorHelper.checkSuccessfulApiCall(journalEntriesResponse);
    }

    @Then("Verify manual Journal entry with External Asset Owner {string} and with the following Journal entries:")
    public void checkManualJournalEntry(String externalAssetOwnerEnabled, DataTable table) {
        Response<PostJournalEntriesResponse> journalEnriesResponse = testContext().get(TestContextKey.MANUAL_JOURNAL_ENTRIES_RESPONSE);
        PostJournalEntriesResponse journalEntriesResponseBody = journalEnriesResponse.body();
        String transactionId = journalEntriesResponseBody.getTransactionId();

        JournalEntryCommand journalEntriesRequest = testContext().get(TestContextKey.MANUAL_JOURNAL_ENTRIES_REQUEST);

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

        List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            List<List<List<String>>> possibleActualValuesList = new ArrayList<>();
            List<String> expectedValues = data.get(i);
            if (Boolean.parseBoolean(externalAssetOwnerEnabled)) {
                expectedValues
                        .add(journalEntriesRequest.getExternalAssetOwner() == null ? null : journalEntriesRequest.getExternalAssetOwner());
            }
            boolean containsAnyExpected = false;

            GetJournalEntriesTransactionIdResponse journalEntryData = journalEntryDataResponse.body();

            List<JournalEntryTransactionItem> journalLinesActual = journalEntryData.getPageItems();

            List<List<String>> actualValuesList = journalLinesActual.stream().map(t -> {
                List<String> actualValues = new ArrayList<>();
                actualValues.add(t.getGlAccountType().getValue() == null ? null : t.getGlAccountType().getValue());
                actualValues.add(t.getGlAccountCode() == null ? null : t.getGlAccountCode());
                actualValues.add(t.getGlAccountName() == null ? null : t.getGlAccountName());
                actualValues.add("DEBIT".equals(t.getEntryType().getValue()) ? String.valueOf(t.getAmount()) : null);
                actualValues.add("CREDIT".equals(t.getEntryType().getValue()) ? String.valueOf(t.getAmount()) : null);
                actualValues.add(String.valueOf(t.getManualEntry()).toLowerCase());
                if (Boolean.parseBoolean(externalAssetOwnerEnabled)) {
                    actualValues.add(t.getExternalAssetOwner() == null ? null : t.getExternalAssetOwner());
                }

                return actualValues;
            }).collect(Collectors.toList());

            possibleActualValuesList.add(actualValuesList);

            boolean containsExpectedValues = actualValuesList.stream().anyMatch(actualValues -> actualValues.equals(expectedValues));
            if (containsExpectedValues) {
                containsAnyExpected = true;
            }

            assertThat(containsAnyExpected)
                    .as(ErrorMessageHelper.wrongValueInLineInJournalEntries(transactionId, i, possibleActualValuesList, expectedValues))
                    .isTrue();
        }
    }

}
