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
package org.apache.fineract.integrationtests.client.feign.modules;

import com.google.gson.Gson;
import jakarta.ws.rs.HttpMethod;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;

/**
 * Builds the individual {@link BatchRequest}s posted to the Batch API. The Batch API takes each entry as a raw relative
 * URL plus a raw JSON body, so the bodies here are assembled as JSON strings rather than typed models; the enclosing
 * request itself is the generated {@link BatchRequest} model.
 *
 * <p>
 * A request may resolve an id from an earlier response in the same batch via a JSON path placeholder (for example
 * {@code $.loanId}) combined with {@code reference} pointing at that earlier request's id.
 */
public final class BatchRequestBuilders {

    private static final String DATE_FORMAT = FeignTestConstants.DATETIME_PATTERN;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final String LOCALE = FeignTestConstants.LOCALE;
    private static final String LOAN_APPLICATION_LOCALE = "en_GB";
    private static final Gson GSON = new Gson();

    /** Default principal applied by the loan-application batch requests. */
    private static final String DEFAULT_LOAN_AMOUNT = "10,000.00";
    /** Default number of days before today used as the loan submission/approval date. */
    private static final int DEFAULT_SUBMITTED_DAYS_AGO = 10;
    /** Default number of days before today used as the disbursement date. */
    private static final int DEFAULT_DISBURSEMENT_DAYS_AGO = 8;

    private BatchRequestBuilders() {}

    private static String today() {
        return format(LocalDate.now(Utils.getZoneIdOfTenant()));
    }

    private static String format(final LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    private static LocalDate daysAgo(final int days) {
        return LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(days);
    }

    private static BatchRequest request(final Long requestId, final Long reference, final String relativeUrl, final String method,
            final String body) {
        return new BatchRequest().requestId(requestId).reference(reference).relativeUrl(relativeUrl).method(method).body(body);
    }

    // Clients

    /**
     * Creates a client in {@code pending} status (active=false). An empty {@code externalId} is replaced by a random
     * one, so that repeated runs against a long-lived server do not collide.
     */
    public static BatchRequest createClient(final Long requestId, final String externalId) {
        final String resolvedExternalId = externalId.isEmpty() ? UUID.randomUUID().toString() : externalId;
        final String body = """
                {"officeId": 1, "legalFormId": 1, "firstname": "Petra", "lastname": "Yton", "externalId": "%s", \
                "dateFormat": "%s", "locale": "%s", "active": false, "submittedOnDate": "04 March 2009"}""".formatted(resolvedExternalId,
                DATE_FORMAT, LOCALE);
        return request(requestId, null, "v1/clients", HttpMethod.POST, body);
    }

    /** Creates a client already in {@code active} status. */
    public static BatchRequest createActiveClient(final Long requestId, final String externalId) {
        final String body = """
                {"officeId": 1, "legalFormId": 1, "firstname": "Petra", "lastname": "Yton", "externalId": "%s", \
                "dateFormat": "%s", "locale": "%s", "active": true, "activationDate": "04 March 2010", \
                "submittedOnDate": "04 March 2010"}""".formatted(externalId, DATE_FORMAT, LOCALE);
        return request(requestId, null, "v1/clients", HttpMethod.POST, body);
    }

    public static BatchRequest updateClient(final Long requestId, final Long reference) {
        return request(requestId, reference, "v1/clients/$.clientId", HttpMethod.PUT,
                "{\"firstname\": \"TestFirstName\", \"lastname\": \"TestLastName\"}");
    }

    public static BatchRequest activateClient(final Long requestId, final Long reference) {
        final String body = "{\"locale\": \"%s\", \"dateFormat\": \"%s\", \"activationDate\": \"01 March 2011\"}".formatted(LOCALE,
                DATE_FORMAT);
        return request(requestId, reference, "v1/clients/$.clientId?command=activate", HttpMethod.POST, body);
    }

    // Loan application

    /** Applies for a loan on behalf of the client created by the referenced request. */
    public static BatchRequest applyLoan(final Long requestId, final Long reference, final Long productId, final Long clientCollateralId) {
        return applyLoan(requestId, reference, productId, clientCollateralId, daysAgo(DEFAULT_SUBMITTED_DAYS_AGO), DEFAULT_LOAN_AMOUNT);
    }

    public static BatchRequest applyLoan(final Long requestId, final Long reference, final Long productId, final Long clientCollateralId,
            final LocalDate submittedOnDate, final String loanAmount) {
        final String dateString = format(submittedOnDate);
        final String collateral = clientCollateralId == null ? ""
                : "\"collateral\": [{\"clientCollateralId\": \"%d\", \"quantity\": \"1\"}],".formatted(clientCollateralId);
        final String body = """
                {"dateFormat": "%s", "locale": "%s", "clientId": "$.clientId", "productId": %d, "principal": "%s", \
                "loanTermFrequency": 10, "loanTermFrequencyType": 2, "loanType": "individual", "numberOfRepayments": 10, \
                "repaymentEvery": 1, "repaymentFrequencyType": 2, "interestRatePerPeriod": 10, "amortizationType": 1, \
                "interestType": 0, "interestCalculationPeriodType": 1, "transactionProcessingStrategyCode": "%s", \
                "expectedDisbursementDate": "%s", %s"submittedOnDate": "%s"}""".formatted(DATE_FORMAT, LOAN_APPLICATION_LOCALE, productId,
                loanAmount, LoanProductTestBuilder.DEFAULT_STRATEGY, dateString, collateral, dateString);
        return request(requestId, reference, "v1/loans", HttpMethod.POST, body);
    }

    /** Applies for a loan for an already existing client, with a random loan external id. */
    public static BatchRequest applyLoanWithClientId(final Long requestId, final Long clientId, final Long productId) {
        return applyLoanWithClientIdAndExternalId(requestId, clientId, productId, UUID.randomUUID().toString());
    }

    public static BatchRequest applyLoanWithClientIdAndExternalId(final Long requestId, final Long clientId, final Long productId,
            final String externalId) {
        final String body = """
                {"dateFormat": "%s", "locale": "%s", "clientId": %d, "productId": %d, "principal": "%s", \
                "loanTermFrequency": 10, "loanTermFrequencyType": 2, "loanType": "individual", "numberOfRepayments": 10, \
                "repaymentEvery": 1, "repaymentFrequencyType": 2, "interestRatePerPeriod": 10, "amortizationType": 1, \
                "interestType": 0, "interestCalculationPeriodType": 1, "transactionProcessingStrategyCode": "%s", \
                "expectedDisbursementDate": "10 Jun 2013", "submittedOnDate": "10 Jun 2013", "externalId": "%s"}""".formatted(DATE_FORMAT,
                LOAN_APPLICATION_LOCALE, clientId, productId, DEFAULT_LOAN_AMOUNT, LoanProductTestBuilder.DEFAULT_STRATEGY, externalId);
        return request(requestId, null, "v1/loans", HttpMethod.POST, body);
    }

    public static BatchRequest applySavings(final Long requestId, final Long reference, final Long productId) {
        final String body = """
                {"clientId": "$.clientId", "productId": %d, "locale": "%s", "dateFormat": "%s", \
                "submittedOnDate": "01 March 2011"}""".formatted(productId, LOCALE, DATE_FORMAT);
        return request(requestId, reference, "v1/savingsaccounts", HttpMethod.POST, body);
    }

    // Loan state transitions

    public static BatchRequest approveLoan(final Long requestId, final Long reference) {
        return approveLoan(requestId, reference, daysAgo(DEFAULT_SUBMITTED_DAYS_AGO));
    }

    public static BatchRequest approveLoan(final Long requestId, final Long reference, final LocalDate approvedOnDate) {
        return request(requestId, reference, "v1/loans/$.loanId?command=approve", HttpMethod.POST, approveLoanBody(approvedOnDate));
    }

    /**
     * Approve request pointing at a non-existent {@code approveX} command, used to assert the batch API answers 501 for
     * an unknown command without aborting the rest of the batch.
     */
    public static BatchRequest approveLoanWithUnknownCommand(final Long requestId, final Long reference) {
        final String dateString = format(daysAgo(DEFAULT_SUBMITTED_DAYS_AGO));
        final String body = """
                {"locale": "%s", "dateFormat": "%s", "approvedOnDate": "%s", "note": "Loan approval note"}""".formatted(LOCALE, DATE_FORMAT,
                dateString);
        return request(requestId, reference, "v1/loans/$.loanId?command=approveX", HttpMethod.POST, body);
    }

    public static BatchRequest disburseLoan(final Long requestId, final Long reference) {
        return disburseLoan(requestId, reference, daysAgo(DEFAULT_DISBURSEMENT_DAYS_AGO));
    }

    public static BatchRequest disburseLoan(final Long requestId, final Long reference, final LocalDate disbursementDate) {
        return request(requestId, reference, "v1/loans/$.loanId?command=disburse", HttpMethod.POST, disburseLoanBody(disbursementDate));
    }

    /** Approves or disburses the loan addressed by the external id resolved from the referenced response. */
    public static BatchRequest transitionLoanStateByExternalId(final Long requestId, final Long reference, final LocalDate date,
            final String command) {
        final String body = switch (command) {
            case "approve" -> approveLoanBody(date);
            case "disburse" -> disburseLoanBody(date);
            default -> throw new IllegalArgumentException("Unsupported loan state transition command: " + command);
        };
        return request(requestId, reference, "v1/loans/external-id/$.resourceExternalId?command=" + command, HttpMethod.POST, body);
    }

    private static String approveLoanBody(final LocalDate approvedOnDate) {
        final String dateString = format(approvedOnDate);
        return """
                {"locale": "%s", "dateFormat": "%s", "approvedOnDate": "%s", "note": "Loan approval note", \
                "expectedDisbursementDate": "%s"}""".formatted(LOCALE, DATE_FORMAT, dateString, dateString);
    }

    private static String disburseLoanBody(final LocalDate disbursementDate) {
        return "{\"locale\": \"%s\", \"dateFormat\": \"%s\", \"actualDisbursementDate\": \"%s\"}".formatted(LOCALE, DATE_FORMAT,
                format(disbursementDate));
    }

    public static BatchRequest markLoanAsFraud(final Long requestId, final Long reference) {
        return request(requestId, reference, "v1/loans/$.loanId?command=markAsFraud", HttpMethod.PUT, "{\"fraud\": \"true\"}");
    }

    public static BatchRequest markLoanAsFraudByExternalId(final Long requestId, final Long reference) {
        return request(requestId, reference, "v1/loans/external-id/$.resourceExternalId?command=markAsFraud", HttpMethod.PUT,
                "{\"fraud\": \"true\"}");
    }

    // Loan transactions

    public static BatchRequest repayLoan(final Long requestId, final Long reference, final String amount) {
        return createTransaction(requestId, reference, "repayment", amount);
    }

    public static BatchRequest creditBalanceRefund(final Long requestId, final Long reference, final String amount) {
        return createTransaction(requestId, reference, "creditBalanceRefund", amount);
    }

    public static BatchRequest goodwillCredit(final Long requestId, final Long reference, final String amount) {
        return createTransaction(requestId, reference, "goodwillCredit", amount);
    }

    public static BatchRequest merchantIssuedRefund(final Long requestId, final Long reference, final String amount) {
        return createTransaction(requestId, reference, "merchantIssuedRefund", amount);
    }

    public static BatchRequest payoutRefund(final Long requestId, final Long reference, final String amount) {
        return createTransaction(requestId, reference, "payoutRefund", amount);
    }

    private static BatchRequest createTransaction(final Long requestId, final Long reference, final String transactionCommand,
            final String amount) {
        final String body = """
                {"locale": "%s", "dateFormat": "%s", "transactionDate": "%s", "transactionAmount": %s, "note": null}""".formatted(LOCALE,
                DATE_FORMAT, today(), amount);
        return request(requestId, reference, "v1/loans/$.loanId/transactions?command=" + transactionCommand, HttpMethod.POST, body);
    }

    public static BatchRequest createTransactionByLoanExternalId(final Long requestId, final Long reference,
            final String transactionCommand, final String amount, final LocalDate transactionDate) {
        final String body = transactionBody(amount, transactionDate);
        return request(requestId, reference, "v1/loans/external-id/$.externalId/transactions?command=" + transactionCommand,
                HttpMethod.POST, body);
    }

    public static BatchRequest chargeOff(final Long requestId, final Long reference) {
        final String body = "{\"locale\": \"%s\", \"dateFormat\": \"%s\", \"transactionDate\": \"%s\", \"note\": null}".formatted(LOCALE,
                DATE_FORMAT, today());
        return request(requestId, reference, "v1/loans/$.loanId/transactions?command=charge-off", HttpMethod.POST, body);
    }

    /** Adjusts (an amount of {@code 0} reverses) the transaction created by the referenced request. */
    public static BatchRequest adjustTransaction(final Long requestId, final Long reference, final String amount,
            final LocalDate transactionDate) {
        return request(requestId, reference, "v1/loans/$.loanId/transactions/$.resourceId", HttpMethod.POST,
                transactionBody(amount, transactionDate));
    }

    public static BatchRequest adjustTransactionByExternalId(final Long requestId, final Long reference, final String loanExternalId,
            final String transactionExternalId, final String amount, final LocalDate transactionDate) {
        final String relativeUrl = "v1/loans/external-id/%s/transactions/external-id/%s".formatted(loanExternalId, transactionExternalId);
        return request(requestId, reference, relativeUrl, HttpMethod.POST, transactionBody(amount, transactionDate));
    }

    public static BatchRequest chargebackTransaction(final Long requestId, final Long reference, final String amount) {
        final String body = "{\"locale\": \"%s\", \"transactionAmount\": %s, \"paymentTypeId\": 2}".formatted(LOCALE, amount);
        return request(requestId, reference, "v1/loans/$.loanId/transactions/$.resourceId?command=chargeback", HttpMethod.POST, body);
    }

    private static String transactionBody(final String amount, final LocalDate transactionDate) {
        return """
                {"locale": "%s", "dateFormat": "%s", "transactionDate": "%s", "transactionAmount": %s}""".formatted(LOCALE, DATE_FORMAT,
                format(transactionDate), amount);
    }

    /**
     * Repayment addressed by an explicit loan id. {@code apiVersionPrefixed} selects between the current
     * {@code v1/loans/...} relative URL and the legacy un-prefixed {@code loans/...} one, both of which the batch API
     * must accept.
     */
    public static BatchRequest repayLoanByLoanId(final Long requestId, final Long loanId, final String amount,
            final boolean apiVersionPrefixed) {
        final String relativeUrl = (apiVersionPrefixed ? "v1/loans/" : "loans/") + loanId + "/transactions?command=repayment";
        final String body = transactionBody(amount, LocalDate.now(Utils.getZoneIdOfTenant()));
        return request(requestId, null, relativeUrl, HttpMethod.POST, body);
    }

    // Loan charges

    public static BatchRequest createChargeByLoanId(final Long requestId, final Long reference, final Long chargeId) {
        return request(requestId, reference, "v1/loans/$.loanId/charges", HttpMethod.POST, createChargeBody(chargeId));
    }

    public static BatchRequest createChargeByLoanExternalId(final Long requestId, final Long reference, final Long chargeId) {
        return request(requestId, reference, "v1/loans/external-id/$.externalId/charges", HttpMethod.POST, createChargeBody(chargeId));
    }

    private static String createChargeBody(final Long chargeId) {
        return """
                {"chargeId": "%d", "locale": "%s", "amount": "100.0", "dateFormat": "%s", "dueDate": "%s"}""".formatted(chargeId, LOCALE,
                DATE_FORMAT, today());
    }

    public static BatchRequest collectChargesByLoanId(final Long requestId, final Long reference) {
        return request(requestId, reference, "v1/loans/$.loanId/charges", HttpMethod.GET, "{ }");
    }

    public static BatchRequest collectChargesByLoanExternalId(final Long requestId, final Long reference) {
        return request(requestId, reference, "v1/loans/external-id/$.externalId/charges", HttpMethod.GET, "{ }");
    }

    public static BatchRequest getChargeByLoanIdChargeId(final Long requestId, final Long reference) {
        return request(requestId, reference, "v1/loans/$.loanId/charges/$.resourceId", HttpMethod.GET, "{}");
    }

    public static BatchRequest getChargeByLoanExternalIdChargeExternalId(final Long requestId, final Long reference,
            final String loanExternalId, final String chargeExternalId) {
        final String relativeUrl = "v1/loans/external-id/%s/charges/external-id/%s".formatted(loanExternalId, chargeExternalId);
        return request(requestId, reference, relativeUrl, HttpMethod.GET, "{}");
    }

    public static BatchRequest adjustCharge(final Long requestId, final Long reference) {
        return request(requestId, reference, "v1/loans/$.loanId/charges/$.resourceId?command=adjustment", HttpMethod.POST,
                adjustChargeBody());
    }

    public static BatchRequest adjustChargeByExternalId(final Long requestId, final Long reference, final String loanExternalId,
            final String chargeExternalId) {
        final String relativeUrl = "v1/loans/external-id/%s/charges/external-id/%s?command=adjustment".formatted(loanExternalId,
                chargeExternalId);
        return request(requestId, reference, relativeUrl, HttpMethod.POST, adjustChargeBody());
    }

    private static String adjustChargeBody() {
        return "{\"amount\": 7.00, \"locale\": \"%s\"}".formatted(LOCALE);
    }

    // Loan reads

    /** Reads the loan whose id is resolved from the referenced response. */
    public static BatchRequest getLoanByReference(final Long requestId, final Long reference, final String queryParameter) {
        return request(requestId, reference, withQueryParameter("v1/loans/$.loanId", queryParameter), HttpMethod.GET, "{}");
    }

    /** Reads the loan whose external id is resolved from the referenced response. */
    public static BatchRequest getLoanByExternalIdReference(final Long requestId, final Long reference, final String queryParameter) {
        return request(requestId, reference, withQueryParameter("v1/loans/external-id/$.resourceExternalId", queryParameter),
                HttpMethod.GET, "{}");
    }

    /** Reads the loan addressed by an explicit loan id. */
    public static BatchRequest getLoanByLoanId(final Long requestId, final Long reference, final Long loanId, final String queryParameter) {
        return request(requestId, reference, withQueryParameter("v1/loans/" + loanId, queryParameter), HttpMethod.GET, "{}");
    }

    public static BatchRequest getTransactionById(final Long requestId, final Long reference, final boolean subResourceId) {
        final String relativeUrl = "v1/loans/$.loanId/transactions/" + (subResourceId ? "$.subResourceId" : "$.resourceId");
        return request(requestId, reference, relativeUrl, HttpMethod.GET, "{}");
    }

    public static BatchRequest getTransactionByExternalId(final Long requestId, final Long reference, final String loanExternalId,
            final boolean subResourceExternalId) {
        final String transactionExternalId = subResourceExternalId ? "$.subResourceExternalId" : "$.resourceExternalId";
        final String relativeUrl = "v1/loans/external-id/%s/transactions/external-id/%s".formatted(loanExternalId, transactionExternalId);
        return request(requestId, reference, relativeUrl, HttpMethod.GET, "{}");
    }

    // Loan rescheduling

    public static BatchRequest createRescheduleLoan(final Long requestId, final Long reference, final LocalDate rescheduleFromDate,
            final Long rescheduleReasonId, final LocalDate adjustedDueDate) {
        final String body = """
                {"locale": "%s", "dateFormat": "%s", "submittedOnDate": "%s", "rescheduleFromDate": "%s", \
                "rescheduleReasonId": %d, "adjustedDueDate": "%s", "loanId": "$.loanId"}""".formatted(LOCALE, DATE_FORMAT, today(),
                format(rescheduleFromDate), rescheduleReasonId, format(adjustedDueDate));
        return request(requestId, reference, "v1/rescheduleloans", HttpMethod.POST, body);
    }

    public static BatchRequest approveRescheduleLoan(final Long requestId, final Long reference) {
        final String body = "{\"locale\": \"%s\", \"dateFormat\": \"%s\", \"approvedOnDate\": \"%s\"}".formatted(LOCALE, DATE_FORMAT,
                today());
        return request(requestId, reference, "v1/rescheduleloans/$.resourceId?command=approve", HttpMethod.POST, body);
    }

    // Datatables

    public static BatchRequest createDatatableEntry(final Long requestId, final String datatableName, final Long entityId,
            final List<String> columnNames) {
        final String relativeUrl = "v1/datatables/%s/%d".formatted(datatableName, entityId);
        return request(requestId, null, relativeUrl, HttpMethod.POST, randomColumnValuesJson(columnNames));
    }

    public static BatchRequest updateDatatableEntryByEntryId(final Long requestId, final Long reference, final String datatableName,
            final Long entityId, final Long datatableEntryId, final List<String> columnNames) {
        final String relativeUrl = "v1/datatables/%s/%d/%d".formatted(datatableName, entityId, datatableEntryId);
        return request(requestId, reference, relativeUrl, HttpMethod.PUT, randomColumnValuesJson(columnNames));
    }

    public static BatchRequest getDatatableEntries(final Long requestId, final Long reference, final String datatableName,
            final Long entityId, final String queryParameter) {
        final String relativeUrl = withQueryParameter("v1/datatables/%s/%d".formatted(datatableName, entityId), queryParameter);
        return request(requestId, reference, relativeUrl, HttpMethod.GET, "{}");
    }

    public static BatchRequest getDatatableEntryByAppTableId(final Long requestId, final Long reference, final String datatableName,
            final Long entityId, final String appTableId, final String queryParameter) {
        final String relativeUrl = withQueryParameter("v1/datatables/%s/%d/%s".formatted(datatableName, entityId, appTableId),
                queryParameter);
        return request(requestId, reference, relativeUrl, HttpMethod.GET, "{}");
    }

    public static BatchRequest queryDatatableEntries(final Long requestId, final String datatableName, final String columnName,
            final String columnValue, final String resultColumns) {
        final String relativeUrl = "v1/datatables/%s/query?columnFilter=%s&valueFilter=%s&resultColumns=%s".formatted(datatableName,
                columnName, columnValue, resultColumns);
        return request(requestId, null, relativeUrl, HttpMethod.GET, "{}");
    }

    public static BatchRequest updateDatatableEntry(final Long requestId, final Long reference, final String datatableName,
            final String resourceId, final String columnName, final String columnValue) {
        final String relativeUrl = "v1/datatables/%s/%s".formatted(datatableName, resourceId);
        return request(requestId, reference, relativeUrl, HttpMethod.PUT, GSON.toJson(Map.of(columnName, columnValue)));
    }

    public static BatchRequest updateDatatableEntry(final Long requestId, final Long reference, final String datatableName,
            final String resourceId, final String subResourceId, final String columnName, final String columnValue) {
        final String relativeUrl = "v1/datatables/%s/%s/%s".formatted(datatableName, resourceId, subResourceId);
        return request(requestId, reference, relativeUrl, HttpMethod.PUT, GSON.toJson(Map.of(columnName, columnValue)));
    }

    private static String randomColumnValuesJson(final List<String> columnNames) {
        return GSON.toJson(columnNames.stream().collect(Collectors.toMap(name -> name, name -> Utils.randomStringGenerator("VAL_", 3))));
    }

    // Savings accounts

    public static BatchRequest getSavingsAccount(final Long requestId, final Long reference, final Long savingsId,
            final String queryParameter) {
        final String relativeUrl = withQueryParameter("v1/savingsaccounts/" + savingsId, queryParameter);
        return request(requestId, reference, relativeUrl, HttpMethod.GET, "{}");
    }

    public static BatchRequest depositSavingsAccount(final Long requestId, final Long reference, final float amount) {
        return savingsTransaction(requestId, reference, "deposit", savingsTransactionBody(amount));
    }

    public static BatchRequest withdrawSavingsAccount(final Long requestId, final Long reference, final float amount) {
        return savingsTransaction(requestId, reference, "withdrawal", savingsTransactionBody(amount));
    }

    public static BatchRequest holdAmountOnSavingsAccount(final Long requestId, final Long reference, final float amount) {
        final String body = """
                {"locale": "%s", "dateFormat": "%s", "transactionDate": "%s", "transactionAmount": "%s", \
                "reasonForBlock": "test"}""".formatted(LOCALE, DATE_FORMAT, today(), amount);
        return savingsTransaction(requestId, reference, "holdAmount", body);
    }

    public static BatchRequest releaseAmountOnSavingsAccount(final Long requestId, final Long reference, final Long transactionId) {
        final String relativeUrl = "v1/savingsaccounts/$.id/transactions/%d?command=releaseAmount".formatted(transactionId);
        return request(requestId, reference, relativeUrl, HttpMethod.POST, "{\"isBulk\": \"false\"}");
    }

    private static BatchRequest savingsTransaction(final Long requestId, final Long reference, final String command, final String body) {
        return request(requestId, reference, "v1/savingsaccounts/$.id/transactions?command=" + command, HttpMethod.POST, body);
    }

    private static String savingsTransactionBody(final float amount) {
        return """
                {"locale": "%s", "dateFormat": "%s", "transactionDate": "%s", "transactionAmount": "%s", \
                "paymentTypeId": "1"}""".formatted(LOCALE, DATE_FORMAT, today(), amount);
    }

    private static String withQueryParameter(final String relativeUrl, final String queryParameter) {
        return queryParameter == null ? relativeUrl : relativeUrl + "?" + queryParameter;
    }
}
