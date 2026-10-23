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

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class AnnualSummaryStepDef extends AbstractStepDef {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
    private static final String OFFICE_ID_CONFIG = "office-id";

    private final JdbcTemplate testJdbcTemplate;
    private final FineractFeignClient fineractClient;

    @Given("any existing year-end retained earnings close for fiscal year ending {string} is removed")
    public void removeRetainedEarningsCloseForFiscalYear(final String yearEndDateStr) {
        final LocalDate yearEndDate = LocalDate.parse(yearEndDateStr, FORMATTER);
        testJdbcTemplate.update("DELETE FROM acc_gl_journal_entry_annual_summary WHERE year_end_date = ?", yearEndDate);
    }

    @When("Admin points the Retained Earning Job at the last created office")
    public void pointRetainedEarningJobAtLastCreatedOffice() {
        // Each scenario runs the close against its own freshly created office so the trial balance only contains
        // that scenario's loans - this is what isolates repeated runs from each other. office-id is a numeric
        // config, so it is set through the internal configuration API.
        final PostOfficesResponse office = testContext().get(TestContextKey.OFFICE_CREATE_RESPONSE);
        assertThat(office).as("No office was created. Use 'Admin creates a new office' step first.").isNotNull();
        fineractClient.defaultApi().updateInternalGlobalConfiguration(OFFICE_ID_CONFIG, office.getOfficeId());
    }

    @Then("The journal entry annual summary table contains {int} row(s) for GL code {string} with year end date {string}")
    public void annualSummaryTableContainsRows(final int expectedCount, final String glCode, final String yearEndDateStr) {
        final LocalDate yearEndDate = LocalDate.parse(yearEndDateStr, FORMATTER);
        final PostOfficesResponse office = testContext().get(TestContextKey.OFFICE_CREATE_RESPONSE);
        assertThat(office).as("No office was created. Use 'Admin creates a new office' step first.").isNotNull();
        final Integer actualCount = testJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM acc_gl_journal_entry_annual_summary WHERE gl_code = ? AND year_end_date = ? AND office_id = ?",
                Integer.class, glCode, yearEndDate, office.getOfficeId());
        assertThat(actualCount)
                .as("acc_gl_journal_entry_annual_summary: expected %d row(s) for gl_code '%s', year_end_date %s, office %s but found %d",
                        expectedCount, glCode, yearEndDate, office.getOfficeId(), actualCount)
                .isEqualTo(expectedCount);
    }

    @Then("The journal entry annual summary table contains {int} row(s) for GL code {string} with year end date {string} and product type {string}")
    public void annualSummaryTableContainsRowsForProductType(final int expectedCount, final String glCode, final String yearEndDateStr,
            final String productType) {
        final LocalDate yearEndDate = LocalDate.parse(yearEndDateStr, FORMATTER);
        final int productTypeId = switch (productType) {
            case "LOAN" -> 1;
            case "WORKING_CAPITAL_LOAN" -> 6;
            default -> throw new IllegalArgumentException("Unsupported product type: " + productType);
        };
        final PostOfficesResponse office = testContext().get(TestContextKey.OFFICE_CREATE_RESPONSE);
        assertThat(office).as("No office was created. Use 'Admin creates a new office' step first.").isNotNull();
        final Integer actualCount = testJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM acc_gl_journal_entry_annual_summary WHERE gl_code = ? AND year_end_date = ? AND office_id = ? AND product_type = ?",
                Integer.class, glCode, yearEndDate, office.getOfficeId(), productTypeId);
        assertThat(actualCount).as(
                "acc_gl_journal_entry_annual_summary: expected %d row(s) for gl_code '%s', year_end_date %s, office %s, product_type %s but found %d",
                expectedCount, glCode, yearEndDate, office.getOfficeId(), productType, actualCount).isEqualTo(expectedCount);
    }
}
