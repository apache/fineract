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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.GetDataTablesResponse;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PostDataTablesResponse;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutDataTablesAppTableIdDatatableIdResponse;
import org.apache.fineract.client.models.PutDataTablesRequest;
import org.apache.fineract.client.models.PutDataTablesRequestAddColumns;
import org.apache.fineract.client.models.PutDataTablesResponse;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class SavingsAccountTransactionDatatableIntegrationTest extends FeignSavingsTestBase {

    private static final String SAVINGS_TRANSACTION_APP_TABLE_NAME = EntityTables.SAVINGS_TRANSACTION.getName();
    private static final String DATATABLE_NAME_PREFIX = "dt_savings_transaction_";
    private static final String NUMBER_COLUMN = "aNumber";
    private static final String STRING_COLUMN = "aString";
    private static final String BOOLEAN_COLUMN = "aBoolean";
    private static final long COLUMN_LENGTH = 10L;

    /** three declared columns plus the primary key and the two audit columns the server adds */
    private static final int EXPECTED_COLUMN_COUNT = 6;

    private static final String START_DATE = "01 Jun 2023";
    private static final String FIRST_DEPOSIT_DATE = "05 Jun 2023";
    private static final String DEPOSIT_AMOUNT = "100";
    private static final int UPDATED_NUMBER_VALUE = 100;

    @Test
    public void testDatatableCreateReadUpdateDeleteForSavingsAccountTransaction() {
        final String datatableName = uniqueDatatableName();

        final PostDataTablesRequest request = new PostDataTablesRequest().datatableName(datatableName)
                .apptableName(SAVINGS_TRANSACTION_APP_TABLE_NAME).multiRow(false).addColumnsItem(numberColumn())
                .addColumnsItem(stringColumn());

        final PostDataTablesResponse response = datatableHelper.createDatatable(request);
        assertNotNull(response.getResourceIdentifier());

        final PutDataTablesRequest putRequest = new PutDataTablesRequest().apptableName(SAVINGS_TRANSACTION_APP_TABLE_NAME)
                .addAddColumnsItem(new PutDataTablesRequestAddColumns().name(BOOLEAN_COLUMN).type("Boolean").mandatory(false));

        final PutDataTablesResponse updateResponse = datatableHelper.updateDatatable(datatableName, putRequest);
        assertNotNull(updateResponse.getResourceIdentifier());

        final GetDataTablesResponse dataTable = datatableHelper.getDatatable(datatableName);
        final List<ResultsetColumnHeaderData> columnHeaderData = dataTable.getColumnHeaderData();
        assertNotNull(columnHeaderData);
        assertEquals(EXPECTED_COLUMN_COUNT, columnHeaderData.size());

        assertEquals(datatableName, datatableHelper.deleteDatatable(datatableName).getResourceIdentifier(),
                "ERROR IN DELETING THE DATATABLE");
    }

    @Test
    public void testDatatableCreateReadUpdateDeleteEntryForSavingsAccountTransaction() {
        final Long clientId = createClient(START_DATE);
        assertNotNull(clientId);

        final Long savingsId = createSavingsAccountDailyPosting(clientId);
        final Long transactionId = deposit(savingsId, DEPOSIT_AMOUNT, FIRST_DEPOSIT_DATE).getResourceId();
        assertNotNull(transactionId);

        final String datatableName = uniqueDatatableName();
        final PostDataTablesRequest request = new PostDataTablesRequest().datatableName(datatableName)
                .apptableName(SAVINGS_TRANSACTION_APP_TABLE_NAME).multiRow(true).addColumnsItem(numberColumn());

        final PostDataTablesResponse response = datatableHelper.createDatatable(request);
        assertNotNull(response);
        assertEquals(datatableName, response.getResourceIdentifier());

        final Long datatableId = datatableHelper.createDatatableEntry(datatableName, transactionId, entry(Utils.randomNumberGenerator(5)))
                .getResourceId();
        assertNotNull(datatableId);

        assertEquals(1, datatableHelper.getDatatableEntries(datatableName, transactionId).path("data").size(),
                "Expected exactly the one row that was just added");

        final PutDataTablesAppTableIdDatatableIdResponse updateResponse = datatableHelper.updateDatatableEntry(datatableName, transactionId,
                datatableId, entry(UPDATED_NUMBER_VALUE));
        assertEquals(transactionId, Long.valueOf(updateResponse.getTransactionId()));
        assertEquals(datatableId, updateResponse.getResourceId());

        final String deletedTransactionId = datatableHelper.deleteDatatableEntries(datatableName, transactionId).getTransactionId();
        assertEquals(transactionId, Long.valueOf(deletedTransactionId), "ERROR IN DELETING THE DATATABLE ENTRIES");

        assertEquals(datatableName, datatableHelper.deleteDatatable(datatableName).getResourceIdentifier(),
                "ERROR IN DELETING THE DATATABLE");
    }

    private String uniqueDatatableName() {
        return Utils.uniqueRandomStringGenerator(DATATABLE_NAME_PREFIX, 5).toLowerCase();
    }

    private PostColumnHeaderData numberColumn() {
        return column(NUMBER_COLUMN, "Number");
    }

    private PostColumnHeaderData stringColumn() {
        return column(STRING_COLUMN, "String");
    }

    private PostColumnHeaderData column(final String name, final String type) {
        return new PostColumnHeaderData().name(name).type(type).mandatory(false).length(COLUMN_LENGTH).code("").unique(false)
                .indexed(false);
    }

    private Map<String, Object> entry(final Integer numberValue) {
        return Map.of(NUMBER_COLUMN, numberValue, "locale", SavingsTestData.LOCALE, "dateFormat", "yyyy-MM-dd");
    }

    private Long createSavingsAccountDailyPosting(final Long clientId) {
        final PostSavingsProductsRequest product = SavingsRequestBuilders.savingsProduct(
                SavingsTestData.InterestCompoundingPeriodType.DAILY, SavingsTestData.InterestPostingPeriodType.DAILY,
                SavingsTestData.InterestCalculationType.DAILY_BALANCE);
        final Long productId = createSavingsProduct(product).getResourceId();
        assertNotNull(productId);

        final Long savingsId = submitSavingsApplication(clientId, productId, START_DATE).getSavingsId();
        assertNotNull(savingsId);

        approveSavings(savingsId, START_DATE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        activateSavings(savingsId, START_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }
}
