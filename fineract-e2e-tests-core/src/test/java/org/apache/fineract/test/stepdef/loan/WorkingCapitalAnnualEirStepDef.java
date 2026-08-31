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

import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanAccountDataV1;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.Header;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBalanceChangedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanStatusChangedEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalAnnualEirStepDef extends AbstractStepDef {

    private static final String ROUNDING_MODE_CONFIG = "rounding-mode";
    private static final long ROUNDING_MODE_HALF_EVEN = 6L;
    private static final String WCL_BASE_URL = "v1/working-capital-loans";
    private static final String AMORTIZATION_MODEL_TABLE = "m_wc_loan_amortization_model";
    private static final String ANNUAL_EIR_JSON_FIELD = "annualEffectiveInterestRate";

    private final FineractFeignClient fineractClient;
    private final EventAssertion eventAssertion;
    private final EventCheckHelper eventCheckHelper;
    private final JdbcTemplate testJdbcTemplate;

    /**
     * The rounding-mode scenario switches the tenant to CEILING; every scenario of the feature restores HALF_EVEN so a
     * failure mid-way cannot leak a foreign rounding mode into the rest of the suite.
     */
    @After("@WorkingCapitalAnnualEirFeature")
    public void restoreTenantRoundingMode() {
        fineractClient.defaultApi().updateInternalGlobalConfiguration(ROUNDING_MODE_CONFIG, ROUNDING_MODE_HALF_EVEN);
    }

    // --- raw details API -------------------------------------------------------------------------------------------

    @Then("Working capital loan details raw JSON serialises {string} as {string}")
    public void rawJsonSerialisesFieldAs(final String field, final String literal) {
        final String body = fetchRawLoanJson();
        assertThat(body).as("raw GET %s/%s", WCL_BASE_URL, getCreatedLoanId()).contains("\"" + field + "\":" + literal);
    }

    @Then("Working capital loan details raw JSON does not contain field {string}")
    public void rawJsonDoesNotContainField(final String field) {
        final String body = fetchRawLoanJson();
        assertThat(body).as("raw GET %s/%s must not carry %s", WCL_BASE_URL, getCreatedLoanId(), field).doesNotContain("\"" + field + "\"");
    }

    // --- event contract ----------------------------------------------------------------------------------------------

    @Then("Working Capital loan account event schema contains field {string}")
    public void eventSchemaContainsField(final String field) {
        assertThat(avroField(field)).as("Avro field %s on WorkingCapitalLoanAccountDataV1", field).isNotNull();
    }

    @Then("Working Capital loan account event schema does not contain field {string}")
    public void eventSchemaDoesNotContainField(final String field) {
        assertThat(avroField(field)).as("Avro field %s must be gone from WorkingCapitalLoanAccountDataV1", field).isNull();
    }

    @Then("a Working Capital Loan Balance Changed business event carries the same annual effective interest rate as loan details")
    public void balanceChangedEventAnnualEirMatchesLoanDetails() {
        eventCheckHelper.waitForTransactionCommit();
        final BigDecimal apiValue = retrieveLoanDetails().getCalculatedAnnualEir();
        assertThat(apiValue).as("calculatedAnnualEir on loan details").isNotNull();
        eventAssertion.assertEvent(WorkingCapitalLoanBalanceChangedEvent.class, getCreatedLoanId())
                .extractingBigDecimal(WorkingCapitalLoanAccountDataV1::getCalculatedAnnualEir).isEqualTo(apiValue);
    }

    @Then("a Working Capital Loan Status Changed business event carries the same annual effective interest rate as loan details")
    public void statusChangedEventAnnualEirMatchesLoanDetails() {
        eventCheckHelper.waitForTransactionCommit();
        final BigDecimal apiValue = retrieveLoanDetails().getCalculatedAnnualEir();
        assertThat(apiValue).as("calculatedAnnualEir on loan details").isNotNull();
        eventAssertion.assertEvent(WorkingCapitalLoanStatusChangedEvent.class, getCreatedLoanId())
                .extractingBigDecimal(WorkingCapitalLoanAccountDataV1::getCalculatedAnnualEir).isEqualTo(apiValue);
    }

    // --- persisted amortization model --------------------------------------------------------------------------------

    @Then("The persisted amortization model of the Working Capital loan stores annual effective interest rate literal {string}")
    public void persistedModelStoresAnnualEirLiteral(final String expectedLiteral) {
        assertThat(persistedAnnualEirLiteral()).as("%s literal inside %s.json_model", ANNUAL_EIR_JSON_FIELD, AMORTIZATION_MODEL_TABLE)
                .isEqualTo(expectedLiteral);
    }

    @Then("The persisted amortization model of the Working Capital loan does not store the annual effective interest rate")
    public void persistedModelDoesNotStoreAnnualEir() {
        assertThat(persistedAnnualEirLiteral()).as("%s must be absent from %s.json_model", ANNUAL_EIR_JSON_FIELD, AMORTIZATION_MODEL_TABLE)
                .isNull();
    }

    /** Turns the persisted model into one written before PS-3218: no annual rate, daily rate left as it is. */
    @When("Admin removes the stored annual effective interest rate from the persisted amortization model of the Working Capital loan")
    public void removeAnnualEirFromPersistedModel() {
        final int updated = testJdbcTemplate.update("UPDATE " + AMORTIZATION_MODEL_TABLE
                + " SET json_model = regexp_replace(json_model, '\"" + ANNUAL_EIR_JSON_FIELD + "\":[0-9.E-]+,', '') WHERE loan_id = ?",
                getCreatedLoanId());
        assertThat(updated).as("amortization model rows updated for loan %s", getCreatedLoanId()).isEqualTo(1);
        assertThat(persistedAnnualEirLiteral()).as("fixture must not carry the field any more").isNull();
    }

    // --- helpers -----------------------------------------------------------------------------------------------------

    private String persistedAnnualEirLiteral() {
        return testJdbcTemplate.queryForObject("SELECT substring(json_model from '\"" + ANNUAL_EIR_JSON_FIELD + "\":([0-9.E-]+)') FROM "
                + AMORTIZATION_MODEL_TABLE + " WHERE loan_id = ?", String.class, getCreatedLoanId());
    }

    private static Schema.Field avroField(final String field) {
        return WorkingCapitalLoanAccountDataV1.getClassSchema().getField(field);
    }

    private String fetchRawLoanJson() {
        final BatchRequest request = new BatchRequest().requestId(1L).relativeUrl(WCL_BASE_URL + "/" + getCreatedLoanId()).method("GET")
                .headers(Set.of(new Header().name("Content-type").value("application/json"))).body("{}");
        final Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("enclosingTransaction", false);
        final List<BatchResponse> responses = fineractClient.batch().handleBatchRequests(List.of(request), queryParams);
        assertThat(responses).as("batch responses").hasSize(1);
        final BatchResponse response = responses.getFirst();
        assertThat(response.getStatusCode()).as("batch GET status, body=%s", response.getBody()).isEqualTo(200);
        return response.getBody();
    }

    private GetWorkingCapitalLoansLoanIdResponse retrieveLoanDetails() {
        return ok(() -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(getCreatedLoanId()));
    }

    private Long getCreatedLoanId() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return loanResponse.getLoanId();
    }
}
