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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fineract.test.stepdef.common;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.AuditsApi.RetrieveAuditEntriesQueryParams;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AuditData;
import org.apache.fineract.client.models.ExecuteWorkingCapitalLoanTransactionCommandResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.PutPermissionsRequest;
import org.apache.fineract.test.api.FineractClientConfiguration;
import org.apache.fineract.test.data.paymenttype.DefaultPaymentType;
import org.apache.fineract.test.data.paymenttype.PaymentTypeResolver;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.AuditHelper;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

/**
 * Steps around the {@code /audits} API covering submitted vs returned command identifiers (FINERACT-2186).
 */
@Slf4j
@RequiredArgsConstructor
public class AuditStepDef extends AbstractStepDef {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String DEFAULT_LOCALE = "en";
    private static final Pattern TRANSACTION_REF_PATTERN = Pattern.compile("(\\d+)th (\\S+) transaction made on (.+)");
    private static final DateTimeFormatter TRANSACTION_DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.ENGLISH);

    private final FineractFeignClient fineractClient;
    private final EventCheckHelper eventCheckHelper;
    private final PaymentTypeResolver paymentTypeResolver;
    private final LoanRequestFactory loanRequestFactory;
    private final AuditHelper auditHelper;
    private final FineractClientConfiguration fineractClientConfiguration;

    private static final String LAST_AUDIT_ENTRY = "lastAuditEntry";
    private static final String LAST_AUDIT_ENTRIES = "lastAuditEntries";

    @Then("The audit search for action {string} on entity {string} filtered by resourceId of the {string}th {string} transaction made on {string} returns {int} entries")
    public void auditSearchByResourceIdOfTransaction(String actionName, String entityName, String nthItemStr, String transactionType,
            String transactionDate, int expectedCount) {
        Long resourceId = findTransactionId(nthItemStr, transactionType, transactionDate);
        searchAudits(new RetrieveAuditEntriesQueryParams().actionName(actionName).entityName(entityName).resourceId(resourceId),
                expectedCount);
    }

    @Then("The audit search for action {string} on entity {string} filtered by resourceId of the last adjusted transaction returns {int} entries")
    public void auditSearchByResourceIdOfAdjustedTransaction(String actionName, String entityName, int expectedCount) {
        PostLoansLoanIdTransactionsResponse adjustResponse = testContext().get(TestContextKey.LOAN_ADJUST_TRANSACTION_RESPONSE);
        searchAudits(new RetrieveAuditEntriesQueryParams().actionName(actionName).entityName(entityName)
                .resourceId(adjustResponse.getResourceId()), expectedCount);
    }

    @Then("The audit search for action {string} on entity {string} filtered by submittedResourceId of the {string}th {string} transaction made on {string} returns {int} entries")
    public void auditSearchBySubmittedResourceIdOfTransaction(String actionName, String entityName, String nthItemStr,
            String transactionType, String transactionDate, int expectedCount) {
        Long submittedResourceId = findTransactionId(nthItemStr, transactionType, transactionDate);
        searchAudits(new RetrieveAuditEntriesQueryParams().actionName(actionName).entityName(entityName)
                .submittedResourceId(submittedResourceId), expectedCount);
    }

    @Then("The audit search for action {string} on entity {string} filtered by submittedLoanExternalId of the created loan returns {int} entries")
    public void auditSearchBySubmittedLoanExternalId(String actionName, String entityName, int expectedCount) {
        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveOneLoan(createdLoanId(), Map.of()));
        searchAudits(new RetrieveAuditEntriesQueryParams().actionName(actionName).entityName(entityName)
                .submittedLoanExternalId(loanDetails.getExternalId()), expectedCount);
    }

    @When("Save ID of {string}th {string} transaction made on {string} as {string}")
    public void saveTransactionId(String nthItemStr, String transactionType, String transactionDate, String idKey) {
        Long transactionId = findTransactionId(nthItemStr, transactionType, transactionDate);
        testContext().set(idKey, transactionId);
        log.info("Transaction ID {} saved as '{}'", transactionId, idKey);
    }

    @When("Save ID of the last adjusted transaction as {string}")
    public void saveAdjustedTransactionId(String idKey) {
        Long transactionId = testContext().<PostLoansLoanIdTransactionsResponse>get(TestContextKey.LOAN_ADJUST_TRANSACTION_RESPONSE)
                .getResourceId();
        testContext().set(idKey, transactionId);
        log.info("Adjusted transaction ID {} saved as '{}'", transactionId, idKey);
    }

    @When("Save ID of {string}th {string} transaction made on {string} as {string} on Working Capital loan")
    public void saveWorkingCapitalTransactionId(String nthItemStr, String transactionType, String transactionDate, String idKey) {
        Long transactionId = findWorkingCapitalTransactionId(nthItemStr, transactionType, transactionDate);
        testContext().set(idKey, transactionId);
        log.info("Working capital transaction ID {} saved as '{}'", transactionId, idKey);
    }

    @When("Save ID of the last adjusted transaction as {string} on Working Capital loan")
    public void saveAdjustedWorkingCapitalTransactionId(String idKey) {
        Long transactionId = testContext()
                .<ExecuteWorkingCapitalLoanTransactionCommandResponse>get(TestContextKey.WORKING_CAPITAL_LOAN_ADJUST_TRANSACTION_RESPONSE)
                .getResourceId();
        testContext().set(idKey, transactionId);
        log.info("Adjusted working capital transaction ID {} saved as '{}'", transactionId, idKey);
    }

    @Then("The audit search for action {string} on entity {string} filtered by resourceId of the created client returns {int} entries")
    public void auditSearchByResourceIdOfCreatedClient(String actionName, String entityName, int expectedCount) {
        Long resourceId = testContext().get(TestContextKey.CLIENT_IDEMPOTENT_FIRST_SUCCESS_CLIENT_ID);
        assertThat(resourceId).as("No client was created successfully yet").isNotNull();
        searchAudits(new RetrieveAuditEntriesQueryParams().actionName(actionName).entityName(entityName).resourceId(resourceId),
                expectedCount);
    }

    @Then("The audit search for action {string} on entity {string} filtered by {word} of {string} returns {int} entries")
    public void auditSearchByField(String actionName, String entityName, String field, String value, int expectedCount) {
        Long identifier = (Long) resolveExpectedValue(value);
        RetrieveAuditEntriesQueryParams queryParams = new RetrieveAuditEntriesQueryParams().actionName(actionName).entityName(entityName);
        switch (field) {
            case "resourceId" -> queryParams.resourceId(identifier);
            case "submittedResourceId" -> queryParams.submittedResourceId(identifier);
            case "submittedLoanId" -> queryParams.submittedLoanId(identifier);
            default -> throw new IllegalArgumentException("Unknown audit search field: " + field);
        }
        searchAudits(queryParams, expectedCount);
    }

    /**
     * Checks audit fields against a table of {@code field | expected value} rows. Expected values can be:
     * <ul>
     * <li>{@code none} - the field is null</li>
     * <li>{@code <key>} - a value stored in the test context, e.g. by {@link #saveTransactionId}</li>
     * <li>{@code last adjusted transaction} - the resourceId returned by the last adjust command</li>
     * <li>{@code created loan} / {@code created loan externalId} - the loan under test</li>
     * <li>{@code <n>th <type> transaction made on <date>} - a loan transaction lookup</li>
     * <li>any literal - compared as a string (numeric fields are compared as numbers)</li>
     * </ul>
     */
    @Then("The returned audit entry has the following data:")
    public void returnedAuditEntryHasData(DataTable table) {
        AuditData audit = testContext().get(LAST_AUDIT_ENTRY);
        assertThat(audit).as("No audit search has returned exactly one entry yet").isNotNull();

        table.asMaps().forEach(row -> {
            String field = row.get("field");
            Object expected = resolveExpectedValue(row.get("value"));
            Object actual = auditField(audit, field);
            assertThat(actual).as("Audit field '%s'", field).isEqualTo(expected);
        });
    }

    private Object auditField(AuditData audit, String field) {
        return switch (field) {
            case "resourceId" -> audit.getResourceId();
            case "submittedResourceId" -> audit.getSubmittedResourceId();
            case "submittedLoanId" -> audit.getSubmittedLoanId();
            case "submittedLoanExternalId" -> audit.getSubmittedLoanExternalId();
            case "processingResult" -> audit.getProcessingResult();
            default -> throw new IllegalArgumentException("Unknown audit field: " + field);
        };
    }

    private Object resolveExpectedValue(String value) {
        if (value == null || "none".equalsIgnoreCase(value)) {
            return null;
        }
        Object savedValue = testContext().get(value);
        if (savedValue != null) {
            return savedValue;
        }
        if ("last adjusted transaction".equals(value)) {
            return testContext().<PostLoansLoanIdTransactionsResponse>get(TestContextKey.LOAN_ADJUST_TRANSACTION_RESPONSE).getResourceId();
        }
        if ("created client".equals(value)) {
            return testContext().<Long>get(TestContextKey.CLIENT_IDEMPOTENT_FIRST_SUCCESS_CLIENT_ID);
        }
        if ("created loan".equals(value)) {
            return createdLoanId();
        }
        if ("created working capital loan".equals(value)) {
            return createdWorkingCapitalLoanId();
        }
        if ("created loan externalId".equals(value)) {
            return ok(() -> fineractClient.loans().retrieveOneLoan(createdLoanId(), Map.of())).getExternalId();
        }
        Matcher transactionRef = TRANSACTION_REF_PATTERN.matcher(value);
        if (transactionRef.matches()) {
            return findTransactionId(transactionRef.group(1), transactionRef.group(2), transactionRef.group(3));
        }
        return value;
    }

    @Then("The audit search for action {string} on entity {string} filtered by maker of the created user returns {int} entries")
    public void auditSearchByMakerOfCreatedUser(String actionName, String entityName, int expectedCount) {
        PostUsersResponse createdUser = testContext().get(TestContextKey.CREATED_SIMPLE_USER_RESPONSE);
        assertThat(createdUser).as("No user was created. Use 'Admin creates new user with ...' step first.").isNotNull();

        List<AuditData> result = auditHelper.searchAudits(
                new RetrieveAuditEntriesQueryParams().actionName(actionName).entityName(entityName).makerId(createdUser.getResourceId()));
        log.info("Audit entries for action {} on entity {} by maker {}: {}", actionName, entityName, createdUser.getResourceId(),
                result.stream().map(a -> a.getId() + "(resourceId=" + a.getResourceId() + ", " + a.getProcessingResult() + ")").toList());
        assertThat(result).as("Audit entries returned").hasSize(expectedCount);
        testContext().set(LAST_AUDIT_ENTRIES, result);
        if (result.size() == 1) {
            testContext().set(LAST_AUDIT_ENTRY, result.get(0));
        }
    }

    /**
     * Checks the {@code processingResult} of every audit entry returned by the previous multi-entry search. Rows are
     * compared in call order (ascending audit id), so list expected values oldest first. The search itself is ordered
     * descending, so entries are re-sorted here before comparison.
     */
    @Then("The returned audit entries have the following processing results in call order:")
    public void auditEntriesProcessingResultsInCallOrder(DataTable table) {
        List<AuditData> audits = testContext().get(LAST_AUDIT_ENTRIES);
        assertThat(audits).as("No multi-entry audit search has run yet").isNotNull();

        List<AuditData> ordered = audits.stream().sorted(java.util.Comparator.comparing(AuditData::getId)).toList();
        List<String> expected = table.asList();
        assertThat(ordered).as("Audit entries").hasSameSizeAs(expected);
        for (int i = 0; i < expected.size(); i++) {
            assertThat(ordered.get(i).getProcessingResult()).as("processingResult of audit entry #%d", i + 1).isEqualTo(expected.get(i));
        }
    }

    @Given("Maker-checker is {word} for permission {string}")
    public void setMakerCheckerPermission(String enabled, String permissionCode) {
        ok(() -> fineractClient.permissions()
                .updatePermissions(new PutPermissionsRequest().putPermissionsItem(permissionCode, "enabled".equals(enabled))));
    }

    @When("Created user adjusts {string}th {string} transaction made on {string} with {string} payment type on {string} with {double} EUR transaction amount")
    public void createdUserAdjustTransaction(String nthItemStr, String transactionType, String transactionDate, String paymentType,
            String adjustDate, double transactionAmount) {
        long loanId = createdLoanId();
        String apiTransactionType = StringUtils.capitalize(transactionType.toLowerCase(Locale.ENGLISH));
        GetLoansLoanIdTransactions targetTransaction = eventCheckHelper.findNthTransaction(nthItemStr, apiTransactionType, transactionDate,
                loanId);

        Long paymentTypeId = paymentTypeResolver.resolve(DefaultPaymentType.valueOf(paymentType));
        PostLoansLoanIdTransactionsTransactionIdRequest adjustRequest = loanRequestFactory.defaultRepaymentAdjustRequest(transactionAmount)
                .paymentTypeId(paymentTypeId).transactionDate(adjustDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        String username = testContext().get(TestContextKey.CREATED_SIMPLE_USER_USERNAME);
        String password = testContext().get(TestContextKey.CREATED_SIMPLE_USER_PASSWORD);
        FineractFeignClient userClient = fineractClientConfiguration.fineractFeignClientForUser(username, password);
        ok(() -> userClient.loanTransactions().adjustLoanTransaction(loanId, targetTransaction.getId(), adjustRequest,
                Map.of("command", "adjust")));
    }

    @Then("Customer fails to adjust {string}th {string} transaction made on {string} with {string} payment type on {string} with {double} EUR transaction amount")
    public void failToAdjustTransaction(String nthItemStr, String transactionType, String transactionDate, String paymentType,
            String adjustDate, double transactionAmount) {
        long loanId = createdLoanId();
        String apiTransactionType = StringUtils.capitalize(transactionType.toLowerCase(Locale.ENGLISH));
        GetLoansLoanIdTransactions targetTransaction = eventCheckHelper.findNthTransaction(nthItemStr, apiTransactionType, transactionDate,
                loanId);

        Long paymentTypeId = paymentTypeResolver.resolve(DefaultPaymentType.valueOf(paymentType));
        PostLoansLoanIdTransactionsTransactionIdRequest adjustRequest = loanRequestFactory.defaultRepaymentAdjustRequest(transactionAmount)
                .paymentTypeId(paymentTypeId).transactionDate(adjustDate).dateFormat(DATE_FORMAT).locale(DEFAULT_LOCALE);

        CallFailedRuntimeException exception = fail(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                targetTransaction.getId(), adjustRequest, Map.of("command", "adjust")));
        assertThat(exception.getStatus()).isEqualTo(403);
    }

    private void searchAudits(RetrieveAuditEntriesQueryParams queryParams, int expectedCount) {
        List<AuditData> result = auditHelper.searchAudits(queryParams);
        assertThat(result).as("Audit entries returned").hasSize(expectedCount);
        if (result.size() == 1) {
            testContext().set(LAST_AUDIT_ENTRY, result.get(0));
        }
    }

    private Long findTransactionId(String nthItemStr, String transactionTypeInput, String transactionDate) {
        long loanId = createdLoanId();
        // The API reports types capitalized (e.g. "Repayment"), the feature uses uppercase ("REPAYMENT").
        String apiTransactionType = StringUtils.capitalize(transactionTypeInput.toLowerCase(Locale.ENGLISH));
        GetLoansLoanIdTransactions transaction = eventCheckHelper.findNthTransaction(nthItemStr, apiTransactionType, transactionDate,
                loanId);
        return transaction.getId();
    }

    private long createdLoanId() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return loanResponse.getLoanId();
    }

    private long createdWorkingCapitalLoanId() {
        PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.WORKING_CAPITAL_LOAN_CREATE_RESPONSE);
        return loanResponse.getLoanId();
    }

    private Long findWorkingCapitalTransactionId(String nthItemStr, String transactionType, String transactionDate) {
        long loanId = createdWorkingCapitalLoanId();
        String expectedCode = "loanTransactionType." + transactionType.toLowerCase(Locale.ENGLISH);
        GetWorkingCapitalLoanTransactionsResponse response = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(loanId));
        int nthItem = Integer.parseInt(nthItemStr) - 1;
        return response.getContent().stream().filter(t -> t.getType() != null && expectedCode.equals(t.getType().getCode())
                && transactionDate.equals(TRANSACTION_DATE_FORMATTER.format(t.getTransactionDate()))).toList().get(nthItem).getId();
    }
}
