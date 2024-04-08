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
package org.apache.fineract.test.initializer.global;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PostColumnHeaderData;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.services.DataTablesApi;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatatablesGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final String DATA_TABLE_1_NAME = "dt_autopay_details";
    public static final String DATA_TABLE_1_APP_NAME = "m_loan";
    public static final String DATA_TABLE_1_COLUMN_1_NAME = "financial_instruments";
    public static final String DATA_TABLE_1_COLUMN_1_TYPE = "Dropdown";
    public static final String DATA_TABLE_1_COLUMN_1_CODE = "financial_instrument";
    public static final String DATA_TABLE_1_COLUMN_2_NAME = "date_of_payment";
    public static final String DATA_TABLE_1_COLUMN_2_TYPE = "Date";
    public static final String DATA_TABLE_2_NAME = "dt_schedule_payments";
    public static final String DATA_TABLE_2_APP_NAME = "m_loan";
    public static final String DATA_TABLE_2_COLUMN_1_NAME = "external_reference_id";
    public static final String DATA_TABLE_2_COLUMN_1_TYPE = "String";
    public static final Long DATA_TABLE_2_COLUMN_1_LENGTH = 50L;
    public static final String DATA_TABLE_2_COLUMN_2_NAME = "scheduled_date";
    public static final String DATA_TABLE_2_COLUMN_2_TYPE = "Date";
    public static final String DATA_TABLE_2_COLUMN_3_NAME = "amount";
    public static final String DATA_TABLE_2_COLUMN_3_TYPE = "Decimal";
    public static final String DATA_TABLE_2_COLUMN_4_NAME = "transaction_type";
    public static final String DATA_TABLE_2_COLUMN_4_TYPE = "Dropdown";
    public static final String DATA_TABLE_2_COLUMN_4_CODE = "transaction_type";
    public static final String DATA_TABLE_3_NAME = "dt_user_tags";
    public static final String DATA_TABLE_3_APP_NAME = "m_client";
    public static final String DATA_TABLE_3_ENTITY_SUBTYPE = "PERSON";
    public static final String DATA_TABLE_3_COLUMN_1_NAME = "bankruptcy_tag";
    public static final String DATA_TABLE_3_COLUMN_1_TYPE = "Dropdown";
    public static final String DATA_TABLE_3_COLUMN_1_CODE = "bankruptcy_tag";
    public static final String DATA_TABLE_3_COLUMN_2_NAME = "pending_fraud_tag";
    public static final String DATA_TABLE_3_COLUMN_2_TYPE = "Dropdown";
    public static final String DATA_TABLE_3_COLUMN_2_CODE = "pending_fraud_tag";
    public static final String DATA_TABLE_3_COLUMN_3_NAME = "pending_deceased_tag";
    public static final String DATA_TABLE_3_COLUMN_3_TYPE = "Dropdown";
    public static final String DATA_TABLE_3_COLUMN_3_CODE = "pending_deceased_tag";
    public static final String DATA_TABLE_3_COLUMN_4_NAME = "hardship_tag";
    public static final String DATA_TABLE_3_COLUMN_4_TYPE = "Dropdown";
    public static final String DATA_TABLE_3_COLUMN_4_CODE = "hardship_tag";
    public static final String DATA_TABLE_3_COLUMN_5_NAME = "active_duty_tag";
    public static final String DATA_TABLE_3_COLUMN_5_TYPE = "Dropdown";
    public static final String DATA_TABLE_3_COLUMN_5_CODE = "active_duty_tag";

    private final DataTablesApi dataTablesApi;

    @Override
    public void initialize() throws Exception {
        // autopay
        PostColumnHeaderData column1 = new PostColumnHeaderData();
        column1.name(DATA_TABLE_1_COLUMN_1_NAME);
        column1.type(DATA_TABLE_1_COLUMN_1_TYPE);
        column1.code(DATA_TABLE_1_COLUMN_1_CODE);
        column1.mandatory(false);

        PostColumnHeaderData column2 = new PostColumnHeaderData();
        column2.name(DATA_TABLE_1_COLUMN_2_NAME);
        column2.type(DATA_TABLE_1_COLUMN_2_TYPE);
        column2.mandatory(false);

        List<PostColumnHeaderData> columns = new ArrayList<>();
        columns.add(column1);
        columns.add(column2);

        PostDataTablesRequest postDataTablesRequest = new PostDataTablesRequest();
        postDataTablesRequest.datatableName(DATA_TABLE_1_NAME);
        postDataTablesRequest.apptableName(DATA_TABLE_1_APP_NAME);
        postDataTablesRequest.multiRow(true);
        postDataTablesRequest.columns(columns);

        dataTablesApi.createDatatable(postDataTablesRequest).execute();

        // scheduled payments
        PostColumnHeaderData columnScheduled1 = new PostColumnHeaderData();
        columnScheduled1.name(DATA_TABLE_2_COLUMN_1_NAME);
        columnScheduled1.type(DATA_TABLE_2_COLUMN_1_TYPE);
        columnScheduled1.length(DATA_TABLE_2_COLUMN_1_LENGTH);
        columnScheduled1.mandatory(false);

        PostColumnHeaderData columnScheduled2 = new PostColumnHeaderData();
        columnScheduled2.name(DATA_TABLE_2_COLUMN_2_NAME);
        columnScheduled2.type(DATA_TABLE_2_COLUMN_2_TYPE);
        columnScheduled2.mandatory(false);

        PostColumnHeaderData columnScheduled3 = new PostColumnHeaderData();
        columnScheduled3.name(DATA_TABLE_2_COLUMN_3_NAME);
        columnScheduled3.type(DATA_TABLE_2_COLUMN_3_TYPE);
        columnScheduled3.mandatory(false);

        PostColumnHeaderData columnScheduled4 = new PostColumnHeaderData();
        columnScheduled4.name(DATA_TABLE_2_COLUMN_4_NAME);
        columnScheduled4.type(DATA_TABLE_2_COLUMN_4_TYPE);
        columnScheduled4.code(DATA_TABLE_2_COLUMN_4_CODE);
        columnScheduled4.mandatory(false);

        List<PostColumnHeaderData> columnsScheduled = new ArrayList<>();
        columnsScheduled.add(columnScheduled1);
        columnsScheduled.add(columnScheduled2);
        columnsScheduled.add(columnScheduled3);
        columnsScheduled.add(columnScheduled4);

        PostDataTablesRequest postDataTablesRequestScheduled = new PostDataTablesRequest();
        postDataTablesRequestScheduled.datatableName(DATA_TABLE_2_NAME);
        postDataTablesRequestScheduled.apptableName(DATA_TABLE_2_APP_NAME);
        postDataTablesRequestScheduled.multiRow(true);
        postDataTablesRequestScheduled.columns(columnsScheduled);

        dataTablesApi.createDatatable(postDataTablesRequestScheduled).execute();

        // 3 tags
        PostColumnHeaderData column3Tags1 = new PostColumnHeaderData();
        column3Tags1.name(DATA_TABLE_3_COLUMN_1_NAME);
        column3Tags1.type(DATA_TABLE_3_COLUMN_1_TYPE);
        column3Tags1.code(DATA_TABLE_3_COLUMN_1_CODE);
        column3Tags1.mandatory(false);

        PostColumnHeaderData column3Tags2 = new PostColumnHeaderData();
        column3Tags2.name(DATA_TABLE_3_COLUMN_2_NAME);
        column3Tags2.type(DATA_TABLE_3_COLUMN_2_TYPE);
        column3Tags2.code(DATA_TABLE_3_COLUMN_2_CODE);
        column3Tags2.mandatory(false);

        PostColumnHeaderData column3Tags3 = new PostColumnHeaderData();
        column3Tags3.name(DATA_TABLE_3_COLUMN_3_NAME);
        column3Tags3.type(DATA_TABLE_3_COLUMN_3_TYPE);
        column3Tags3.code(DATA_TABLE_3_COLUMN_3_CODE);
        column3Tags3.mandatory(false);

        PostColumnHeaderData column3Tags4 = new PostColumnHeaderData();
        column3Tags4.name(DATA_TABLE_3_COLUMN_4_NAME);
        column3Tags4.type(DATA_TABLE_3_COLUMN_4_TYPE);
        column3Tags4.code(DATA_TABLE_3_COLUMN_4_CODE);
        column3Tags4.mandatory(false);

        PostColumnHeaderData column3Tags5 = new PostColumnHeaderData();
        column3Tags5.name(DATA_TABLE_3_COLUMN_5_NAME);
        column3Tags5.type(DATA_TABLE_3_COLUMN_5_TYPE);
        column3Tags5.code(DATA_TABLE_3_COLUMN_5_CODE);
        column3Tags5.mandatory(false);

        List<PostColumnHeaderData> columns3Tags = new ArrayList<>();
        columns3Tags.add(column3Tags1);
        columns3Tags.add(column3Tags2);
        columns3Tags.add(column3Tags3);
        columns3Tags.add(column3Tags4);
        columns3Tags.add(column3Tags5);

        PostDataTablesRequest postDataTablesRequest3Tags = new PostDataTablesRequest();
        postDataTablesRequest3Tags.datatableName(DATA_TABLE_3_NAME);
        postDataTablesRequest3Tags.apptableName(DATA_TABLE_3_APP_NAME);
        postDataTablesRequest3Tags.entitySubType(DATA_TABLE_3_ENTITY_SUBTYPE);
        postDataTablesRequest3Tags.multiRow(false);
        postDataTablesRequest3Tags.columns(columns3Tags);

        dataTablesApi.createDatatable(postDataTablesRequest3Tags).execute();
    }
}
