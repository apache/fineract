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
package org.apache.fineract.integrationtests.datatable;

import static org.apache.fineract.client.models.FilterData.OperatorEnum.BTW;
import static org.apache.fineract.client.models.FilterData.OperatorEnum.EQ;
import static org.apache.fineract.client.models.FilterData.OperatorEnum.GT;
import static org.apache.fineract.client.models.FilterData.OperatorEnum.GTE;
import static org.apache.fineract.client.models.FilterData.OperatorEnum.IN;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_BOOLEAN;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_DATE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_DECIMAL;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_NUMBER;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_STRING;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_TEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.AdvancedQueryData;
import org.apache.fineract.client.models.AdvancedQueryRequest;
import org.apache.fineract.client.models.ColumnFilterData;
import org.apache.fineract.client.models.FilterData;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PagedLocalRequestAdvancedQueryData;
import org.apache.fineract.client.models.PagedLocalRequestAdvancedQueryRequest;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostDataTablesResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.client.models.SortOrder;
import org.apache.fineract.client.models.TableQueryData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatatableAdvancedQueryTest extends FeignIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(DatatableAdvancedQueryTest.class);

    private static final String SAVINGS_TRANSACTION_APP_TABLE_NAME = EntityTables.SAVINGS_TRANSACTION.getName();
    public static final String SAVINGS_DATE_FORMAT = Utils.DATE_FORMAT;

    private static final String COLUMN_STRING = "aString";
    private static final String COLUMN_TEXT = "aText";
    private static final String COLUMN_DATE = "aDate";
    private static final String COLUMN_BOOLEAN = "aBoolean";
    private static final String COLUMN_INTEGER = "aNumber";
    private static final String COLUMN_DECIMAL = "aDecimal";
    private static final String COLUMN_TRANSACTION_ID = "savings_transaction_id";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SUBMITTED_DATE = "submitted_on_date";
    private static final String COLUMN_AMOUNT = "amount";

    private FeignDatatableHelper datatableHelper;
    private FeignClientHelper clientHelper;
    private FeignSavingsProductHelper savingsProductHelper;
    private FeignSavingsHelper savingsHelper;
    private FeignSavingsTransactionHelper savingsTransactionHelper;
    private FeignGlobalConfigurationHelper globalConfigurationHelper;

    @BeforeAll
    public void setup() {
        datatableHelper = new FeignDatatableHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        savingsProductHelper = new FeignSavingsProductHelper(fineractClient());
        savingsHelper = new FeignSavingsHelper(fineractClient());
        savingsTransactionHelper = new FeignSavingsTransactionHelper(fineractClient());
        globalConfigurationHelper = new FeignGlobalConfigurationHelper(fineractClient());
    }

    @Test
    public void testDatatableAdvancedQuery() {
        String datatable = createAndVerifyDatatable(SAVINGS_TRANSACTION_APP_TABLE_NAME, null, false);
        LocalDate today = Utils.getLocalDateOfTenant();
        String todayS = DateUtils.format(today, SAVINGS_DATE_FORMAT);
        LocalDate yesterday = today.minus(1, ChronoUnit.DAYS);
        String yesterdayS = DateUtils.format(yesterday, SAVINGS_DATE_FORMAT);
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, today);

            final Long clientId = clientHelper.createClient(yesterdayS);
            assertNotNull(clientId);
            final Long savingsId = createSavingsAccountDailyPosting(clientId, yesterdayS);
            assertNotNull(savingsId);

            final Long transactionIdD1 = savingsTransactionHelper.deposit(savingsId, "100", yesterdayS).getResourceId();
            assertNotNull(transactionIdD1);
            BigDecimal decValue1 = new BigDecimal("1.111");
            createDatatableEntry(datatable, transactionIdD1, yesterday, true, 1, decValue1);
            final Long transactionIdD2 = savingsTransactionHelper.deposit(savingsId, "300", yesterdayS).getResourceId();
            assertNotNull(transactionIdD2);
            createDatatableEntry(datatable, transactionIdD2, yesterday, false, 2, new BigDecimal("2.2"));
            final Long transactionIdW1 = savingsTransactionHelper.withdraw(savingsId, "100", todayS).getResourceId();
            assertNotNull(transactionIdW1);
            createDatatableEntry(datatable, transactionIdW1, today, true, 3, new BigDecimal("3"));

            String yesterdayIsoS = DateUtils.format(yesterday, DateUtils.DEFAULT_DATE_FORMAT);
            String todayIsoS = DateUtils.format(today, DateUtils.DEFAULT_DATE_FORMAT);
            AdvancedQueryData query = new AdvancedQueryData()
                    .resultColumns(List.of(COLUMN_TRANSACTION_ID, COLUMN_STRING, COLUMN_TEXT, COLUMN_DATE, COLUMN_BOOLEAN, COLUMN_INTEGER,
                            COLUMN_DECIMAL))
                    .addColumnFiltersItem(new ColumnFilterData().column(COLUMN_TEXT)
                            .addFiltersItem(new FilterData().operator(EQ).values(List.of(transactionIdD1.toString()))))
                    .addColumnFiltersItem(new ColumnFilterData().column(COLUMN_DATE)
                            .addFiltersItem(new FilterData().operator(BTW).values(List.of(yesterdayIsoS, yesterdayIsoS))));
            PagedLocalRequestAdvancedQueryData pagedQuery = new PagedLocalRequestAdvancedQueryData().page(0).size(3)
                    .addSortsItem(new SortOrder().property("created_at").direction(SortOrder.DirectionEnum.DESC))
                    .addSortsItem(new SortOrder().property(COLUMN_TRANSACTION_ID).direction(SortOrder.DirectionEnum.DESC)).request(query);
            JsonNode response = datatableHelper.queryDatatable(datatable, pagedQuery);

            assertEquals(1, response.path("total").asInt());
            JsonNode content = response.path("content");
            assertNotNull(content);
            assertEquals(1, content.size());
            JsonNode first = content.get(0);
            assertEquals(transactionIdD1, first.path(COLUMN_TRANSACTION_ID).asLong());
            assertEquals(transactionIdD1.toString(), first.path(COLUMN_TEXT).asText());
            assertEquals(yesterdayIsoS, first.path(COLUMN_DATE).asText());
            assertTrue(first.path(COLUMN_BOOLEAN).asBoolean());
            assertEquals(1, first.path(COLUMN_INTEGER).asInt());
            assertEquals(0, decValue1.compareTo(first.path(COLUMN_DECIMAL).decimalValue()));

            query.resultColumns(List.of(COLUMN_TRANSACTION_ID));
            query.columnFilters(List.of(
                    new ColumnFilterData().column(COLUMN_INTEGER).addFiltersItem(new FilterData().operator(GTE).values(List.of("1"))),
                    new ColumnFilterData().column(COLUMN_DATE)
                            .addFiltersItem(new FilterData().operator(BTW).values(List.of(yesterdayIsoS, todayIsoS)))));
            response = datatableHelper.queryDatatable(datatable, pagedQuery);

            assertEquals(3, response.path("total").asInt());
            content = response.path("content");
            assertNotNull(content);
            assertEquals(3, content.size());
            first = content.get(0);
            assertEquals(transactionIdW1, first.path(COLUMN_TRANSACTION_ID).asLong());
            assertFalse(first.hasNonNull(COLUMN_TEXT));
            assertFalse(first.hasNonNull(COLUMN_DATE));
            assertFalse(first.hasNonNull(COLUMN_BOOLEAN));
            assertFalse(first.hasNonNull(COLUMN_INTEGER));
            assertFalse(first.hasNonNull(COLUMN_DECIMAL));
            assertEquals(transactionIdD2, content.get(1).path(COLUMN_TRANSACTION_ID).asLong());

            deleteDatatable(datatable, transactionIdD1, transactionIdD2, transactionIdW1);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testApptableWithDatatableAdvancedQuery() {
        String datatable = createAndVerifyDatatable(SAVINGS_TRANSACTION_APP_TABLE_NAME, null, false);

        LocalDate today = Utils.getLocalDateOfTenant();
        String todayS = DateUtils.format(today, SAVINGS_DATE_FORMAT);
        LocalDate yesterday = today.minus(1, ChronoUnit.DAYS);
        String yesterdayS = DateUtils.format(yesterday, SAVINGS_DATE_FORMAT);
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, today);

            final Long clientId = clientHelper.createClient(yesterdayS);
            assertNotNull(clientId);
            final Long savingsId = createSavingsAccountDailyPosting(clientId, yesterdayS);
            assertNotNull(savingsId);

            final Long transactionIdD1 = savingsTransactionHelper.deposit(savingsId, "100", yesterdayS).getResourceId();
            assertNotNull(transactionIdD1);
            BigDecimal decValue1 = new BigDecimal("1.111");
            createDatatableEntry(datatable, transactionIdD1, yesterday, true, 1, decValue1);
            final Long transactionIdD2 = savingsTransactionHelper.deposit(savingsId, "300", yesterdayS).getResourceId();
            assertNotNull(transactionIdD2);
            BigDecimal decValue2 = new BigDecimal("2.2");
            createDatatableEntry(datatable, transactionIdD2, yesterday, false, 2, decValue2);
            final Long transactionIdW1 = savingsTransactionHelper.withdraw(savingsId, "100", todayS).getResourceId();
            assertNotNull(transactionIdW1);
            createDatatableEntry(datatable, transactionIdW1, today, true, 3, new BigDecimal("3"));

            String yesterdayIsoS = DateUtils.format(yesterday, DateUtils.DEFAULT_DATE_FORMAT);
            String todayIsoS = DateUtils.format(today, DateUtils.DEFAULT_DATE_FORMAT);

            AdvancedQueryData baseQuery = new AdvancedQueryData().resultColumns(List.of(COLUMN_ID, COLUMN_SUBMITTED_DATE))
                    .addColumnFiltersItem(new ColumnFilterData().column(COLUMN_AMOUNT)
                            .addFiltersItem(new FilterData().operator(GT).values(List.of("100"))))
                    .addColumnFiltersItem(new ColumnFilterData().column(COLUMN_SUBMITTED_DATE)
                            .addFiltersItem(new FilterData().operator(BTW).values(List.of(todayIsoS, todayIsoS))));
            AdvancedQueryData dataQuery = new AdvancedQueryData()
                    .resultColumns(List.of(COLUMN_TRANSACTION_ID, COLUMN_STRING, COLUMN_TEXT, COLUMN_DATE, COLUMN_BOOLEAN, COLUMN_INTEGER,
                            COLUMN_DECIMAL))
                    .addColumnFiltersItem(new ColumnFilterData().column(COLUMN_TEXT).addFiltersItem(
                            new FilterData().operator(IN).values(List.of(transactionIdD1.toString(), transactionIdD2.toString()))))
                    .addColumnFiltersItem(new ColumnFilterData().column(COLUMN_DATE)
                            .addFiltersItem(new FilterData().operator(BTW).values(List.of(yesterdayIsoS, yesterdayIsoS))));
            AdvancedQueryRequest queryRequest = new AdvancedQueryRequest().baseQuery(baseQuery)
                    .datatableQueries(List.of(new TableQueryData().table(datatable).query(dataQuery)));
            PagedLocalRequestAdvancedQueryRequest pagedRequest = new PagedLocalRequestAdvancedQueryRequest().page(0).size(2)
                    .addSortsItem(new SortOrder().property(COLUMN_SUBMITTED_DATE).direction(SortOrder.DirectionEnum.DESC))
                    .addSortsItem(new SortOrder().property(COLUMN_ID).direction(SortOrder.DirectionEnum.DESC)).request(queryRequest);
            JsonNode response = savingsTransactionHelper.querySavingsTransactions(savingsId, pagedRequest);

            assertEquals(1, response.path("total").asInt());
            JsonNode content = response.path("content");
            assertNotNull(content);
            assertEquals(1, content.size());
            JsonNode first = content.get(0);
            assertEquals(transactionIdD2, first.path(COLUMN_ID).asLong());
            assertEquals(todayIsoS, first.path(COLUMN_SUBMITTED_DATE).asText());
            assertEquals(transactionIdD2, first.path(COLUMN_TRANSACTION_ID).asLong());
            assertEquals(transactionIdD2.toString(), first.path(COLUMN_TEXT).asText());
            assertEquals(yesterdayIsoS, first.path(COLUMN_DATE).asText());
            assertFalse(first.path(COLUMN_BOOLEAN).asBoolean());
            assertEquals(2, first.path(COLUMN_INTEGER).asInt());
            assertEquals(0, decValue2.compareTo(first.path(COLUMN_DECIMAL).decimalValue()));

            baseQuery.columnFilters(List.of(
                    new ColumnFilterData().column(COLUMN_AMOUNT).addFiltersItem(new FilterData().operator(GTE).values(List.of("100"))),
                    new ColumnFilterData().column(COLUMN_SUBMITTED_DATE)
                            .addFiltersItem(new FilterData().operator(BTW).values(List.of(todayIsoS, todayIsoS)))));
            dataQuery.resultColumns(List.of(COLUMN_TRANSACTION_ID));
            dataQuery.columnFilters(List.of(
                    new ColumnFilterData().column(COLUMN_INTEGER).addFiltersItem(new FilterData().operator(GTE).values(List.of("1"))),
                    new ColumnFilterData().column(COLUMN_DATE)
                            .addFiltersItem(new FilterData().operator(BTW).values(List.of(yesterdayIsoS, todayIsoS)))));
            response = savingsTransactionHelper.querySavingsTransactions(savingsId, pagedRequest);

            assertEquals(3, response.path("total").asInt());
            content = response.path("content");
            assertNotNull(content);
            assertEquals(2, content.size()); // page size 2
            first = content.get(0);
            assertEquals(transactionIdW1, first.path(COLUMN_ID).asLong());
            assertEquals(todayIsoS, first.path(COLUMN_SUBMITTED_DATE).asText());
            assertEquals(transactionIdW1, first.path(COLUMN_TRANSACTION_ID).asLong());
            assertFalse(first.hasNonNull(COLUMN_TEXT));
            assertFalse(first.hasNonNull(COLUMN_DATE));
            assertFalse(first.hasNonNull(COLUMN_BOOLEAN));
            assertFalse(first.hasNonNull(COLUMN_INTEGER));
            assertFalse(first.hasNonNull(COLUMN_DECIMAL));
            assertEquals(transactionIdD2, content.get(1).path(COLUMN_TRANSACTION_ID).asLong());

            deleteDatatable(datatable, transactionIdD1, transactionIdD2, transactionIdW1);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private String createAndVerifyDatatable(String apptable, String subType, boolean multiRow) {
        // creating datatable for apptable entity
        final List<PostColumnHeaderData> datatableColumns = new ArrayList<>();
        datatableColumns.add(column(COLUMN_STRING, API_FIELD_TYPE_STRING, true, 50L).unique(!multiRow).indexed(true));
        datatableColumns.add(column(COLUMN_TEXT, API_FIELD_TYPE_TEXT, false, null));
        datatableColumns.add(column(COLUMN_DATE, API_FIELD_TYPE_DATE, true, null));
        datatableColumns.add(column(COLUMN_BOOLEAN, API_FIELD_TYPE_BOOLEAN, false, null));
        datatableColumns.add(column(COLUMN_INTEGER, API_FIELD_TYPE_NUMBER, false, null));
        datatableColumns.add(column(COLUMN_DECIMAL, API_FIELD_TYPE_DECIMAL, false, null));

        final PostDataTablesRequest request = new PostDataTablesRequest()
                .datatableName(Utils.uniqueRandomStringGenerator("dt_" + apptable + "_", 5)).apptableName(apptable).entitySubType(subType)
                .multiRow(multiRow).columns(datatableColumns);
        LOG.info("request : {}", request);

        PostDataTablesResponse response = datatableHelper.createDatatable(request);
        String datatable = response.getResourceIdentifier();
        assertNotNull(datatable);
        GetDataTablesResponse dataTable = datatableHelper.getDatatable(datatable);
        List<ResultsetColumnHeaderData> columnHeaderData = dataTable.getColumnHeaderData();
        assertNotNull(columnHeaderData);
        // pk column and 2 audit columns were added automatically
        assertEquals(9, columnHeaderData.size());
        return datatable;
    }

    private static PostColumnHeaderData column(String name, String type, boolean mandatory, Long length) {
        return new PostColumnHeaderData().name(name).type(type).mandatory(mandatory).length(length);
    }

    private void createDatatableEntry(String datatable, Long apptableId, LocalDate dateValue, Boolean boolValue, Integer intValue,
            BigDecimal decValue) {
        final Map<String, Object> request = new HashMap<>();
        request.put(COLUMN_STRING, Utils.uniqueRandomStringGenerator(apptableId.toString() + "_", 5));
        request.put(COLUMN_TEXT, apptableId);
        request.put(COLUMN_DATE, DateUtils.format(dateValue, SAVINGS_DATE_FORMAT));
        request.put(COLUMN_BOOLEAN, boolValue);
        request.put(COLUMN_INTEGER, intValue == null ? null : intValue.toString());
        request.put(COLUMN_DECIMAL, decValue == null ? null : decValue.toString());
        request.put("locale", "en");
        request.put("dateFormat", SAVINGS_DATE_FORMAT);

        assertNotNull(datatableHelper.createDatatableEntry(datatable, apptableId, request).getResourceId());
    }

    private void deleteDatatable(String datatable, Long... apptableIds) {
        for (Long apptableId : apptableIds) {
            String deletedId = this.datatableHelper.deleteDatatableEntries(datatable, apptableId).getTransactionId();
            assertEquals(apptableId, Long.valueOf(deletedId), "ERROR IN DELETING THE DATATABLE ENTRY");
        }
        String deletedDatatable = this.datatableHelper.deleteDatatable(datatable).getResourceIdentifier();
        assertEquals(datatable, deletedDatatable, "ERROR IN DELETING THE DATATABLE");
    }

    private Long createSavingsProductDailyPosting() {
        return savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                        SavingsTestData.InterestPostingPeriodType.DAILY, SavingsTestData.InterestCalculationType.DAILY_BALANCE))
                .getResourceId();
    }

    private Long createSavingsAccountDailyPosting(final Long clientID, final String startDate) {
        final Long savingsProductID = createSavingsProductDailyPosting();
        assertNotNull(savingsProductID);
        final Long savingsId = savingsHelper.submitApplication(clientID, savingsProductID, startDate).getSavingsId();
        assertNotNull(savingsId);
        savingsHelper.approveSavings(savingsId, startDate);
        assertTrue(savingsHelper.getSavingsStatus(savingsId).getApproved());
        savingsHelper.activateSavings(savingsId, startDate);
        assertTrue(savingsHelper.getSavingsStatus(savingsId).getActive());
        return savingsId;
    }
}
