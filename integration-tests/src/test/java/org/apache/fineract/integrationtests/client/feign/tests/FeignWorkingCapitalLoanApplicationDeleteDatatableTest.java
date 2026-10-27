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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.apache.fineract.client.models.FilterData.OperatorEnum.EQ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.AdvancedQueryData;
import org.apache.fineract.client.models.ColumnFilterData;
import org.apache.fineract.client.models.FilterData;
import org.apache.fineract.client.models.PagedLocalRequestAdvancedQueryData;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.EntityDatatableChecksHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Deleting a Working Capital loan application deletes the entries that datatables registered on {@code m_wc_loan} hold
 * for it, so a datatable's foreign key no longer blocks the delete.
 */
public class FeignWorkingCapitalLoanApplicationDeleteDatatableTest extends FeignIntegrationTest {

    private static final String WC_LOAN_APP_TABLE_NAME = "m_wc_loan";
    private static final String WC_LOAN_FK_COLUMN = "wc_loan_id";
    private static final long STATUS_CREATE = 100L;

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignDatatableHelper datatableHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private Long productId;
    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<String> createdDatatables = new ArrayList<>();

    @BeforeAll
    void setup() {
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        datatableHelper = new FeignDatatableHelper(fineractClient());
        productHelper = new WorkingCapitalLoanProductHelper();
        productId = productHelper.createWorkingCapitalLoanProduct(
                new WorkingCapitalLoanProductTestBuilder().withName("WCL DelDt " + Utils.uniqueRandomStringGenerator("", 8))
                        .withShortName(Utils.uniqueRandomStringGenerator("", 4)).build())
                .getResourceId();
    }

    @AfterAll
    void cleanup() {
        for (final Long loanId : createdLoanIds) {
            try {
                wcLoanHelper.undoApproval(loanId, WorkingCapitalLoanApplicationTestBuilder.buildUndoApproveRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
            try {
                wcLoanHelper.delete(loanId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        for (final String datatable : createdDatatables) {
            try {
                datatableHelper.deleteDatatable(datatable);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
        if (productId != null) {
            try {
                productHelper.deleteWorkingCapitalLoanProductById(productId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup
            }
        }
    }

    @Test
    void deleteApplicationDeletesItsSingleRowDatatableEntry() {
        final String datatable = createWcLoanDatatable(false);
        final Long loanId = submitApplication();
        datatableHelper.createDatatableEntry(datatable, loanId, Map.of("note", "one"));

        wcLoanHelper.delete(loanId);

        assertEquals(404, wcLoanHelper.deleteApplicationExpectingFailure(loanId).getStatus());
        assertNoEntries(datatable, loanId);
    }

    @Test
    void deleteApplicationDeletesEntriesOfEveryDatatableOnTheLoan() {
        final String multiRow = createWcLoanDatatable(true);
        final String singleRow = createWcLoanDatatable(false);
        final Long loanId = submitApplication();
        final Long otherLoanId = submitApplication();
        datatableHelper.createDatatableEntry(multiRow, loanId, Map.of("note", "first"));
        datatableHelper.createDatatableEntry(multiRow, loanId, Map.of("note", "second"));
        datatableHelper.createDatatableEntry(singleRow, loanId, Map.of("note", "one"));
        datatableHelper.createDatatableEntry(multiRow, otherLoanId, Map.of("note", "other loan"));

        wcLoanHelper.delete(loanId);

        assertNoEntries(multiRow, loanId);
        assertNoEntries(singleRow, loanId);
        assertEquals(1, countEntries(multiRow, otherLoanId));
    }

    @Test
    void deleteApplicationDeletesTheEntryOfADatatableMadeMandatoryByAnEntityCheck() {
        final String datatable = createWcLoanDatatable(false);
        final Long loanId = submitApplication();
        datatableHelper.createDatatableEntry(datatable, loanId, Map.of("note", "one"));
        final Long checkId = EntityDatatableChecksHelper.createEntityDatatableCheck(WC_LOAN_APP_TABLE_NAME, datatable, STATUS_CREATE, null)
                .getResourceId();
        try {
            // with the check in place the entry cannot be deleted on its own
            final CallFailedRuntimeException refused = FeignCalls
                    .fail(() -> fineractClient().dataTables().deleteDatatableEntries(datatable, loanId));
            assertEquals(403, refused.getStatus());

            wcLoanHelper.delete(loanId);

            assertNoEntries(datatable, loanId);
        } finally {
            EntityDatatableChecksHelper.deleteEntityDatatableCheck(checkId);
        }
    }

    @Test
    void approvedApplicationIsStillRefusedAndKeepsItsEntries() {
        final String datatable = createWcLoanDatatable(false);
        final Long loanId = submitApplication();
        datatableHelper.createDatatableEntry(datatable, loanId, Map.of("note", "kept"));
        final String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(today, BigDecimal.valueOf(10000), today));

        final CallFailedRuntimeException refused = wcLoanHelper.deleteApplicationExpectingFailure(loanId);

        assertEquals(403, refused.getStatus());
        assertTrue(refused.getResponseBody().contains("error.msg.wc.loan.cannot.delete.in.present.state"), refused.getResponseBody());
        assertEquals(1, countEntries(datatable, loanId));
        datatableHelper.deleteDatatableEntries(datatable, loanId);
    }

    private Long submitApplication() {
        final Long clientId = clientHelper.createClient(Utils.dateFormatter.format(Utils.getLocalDateOfTenant()));
        final String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(18), today, today));
        createdLoanIds.add(loanId);
        return loanId;
    }

    private String createWcLoanDatatable(final boolean multiRow) {
        final String name = Utils.uniqueRandomStringGenerator("dt_wc_loan_", 5).toLowerCase();
        datatableHelper.createDatatable(new PostDataTablesRequest().datatableName(name).apptableName(WC_LOAN_APP_TABLE_NAME)
                .multiRow(multiRow).columns(List.of(new PostColumnHeaderData().name("note").type("String").length(50L).mandatory(false))));
        createdDatatables.add(name);
        return name;
    }

    private void assertNoEntries(final String datatable, final Long loanId) {
        assertEquals(0, countEntries(datatable, loanId));
    }

    // GET /datatables/<datatable>/<id> answers 404 once the loan is gone, so the entries are counted by a query
    private int countEntries(final String datatable, final Long loanId) {
        final AdvancedQueryData query = new AdvancedQueryData().resultColumns(List.of("note")).addColumnFiltersItem(new ColumnFilterData()
                .column(WC_LOAN_FK_COLUMN).addFiltersItem(new FilterData().operator(EQ).values(List.of(loanId.toString()))));
        final JsonNode page = datatableHelper.queryDatatable(datatable,
                new PagedLocalRequestAdvancedQueryData().page(0).size(10).request(query));
        return page.path("total").asInt();
    }
}
