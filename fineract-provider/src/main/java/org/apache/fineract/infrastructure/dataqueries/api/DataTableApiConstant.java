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
package org.apache.fineract.infrastructure.dataqueries.api;

/**
 * Created by Cieyou on 2/26/14.
 */
public final class DataTableApiConstant {

    private DataTableApiConstant() {

    }

    public static final Integer CATEGORY_PPI = 200;
    public static final Integer CATEGORY_DEFAULT = 100;

    public static final String categoryParamName = "category";
    public static final String localParamName = "locale";
    public static final String DATATABLE_RESOURCE_NAME = "dataTables";

    public static final String CREATEDAT_FIELD_NAME = "created_at";
    public static final String UPDATEDAT_FIELD_NAME = "updated_at";

    // associationParameters
    public static final String allAssociateParamName = "all";
    public static final String repaymentScheduleAssociateParamName = "repaymentSchedule";
    public static final String originalScheduleAssociateParamName = "originalSchedule";
    public static final String transactionsAssociateParamName = "transactions";
    public static final String chargesAssociateParamName = "charges";
    public static final String guarantorsAssociateParamName = "guarantors";
    public static final String collateralAssociateParamName = "collateral";
    public static final String notesAssociateParamName = "notes";
    public static final String linkedAccountAssociateParamName = "linkedAccount";
    public static final String multiDisburseDetailsAssociateParamName = "multiDisburseDetails";
    public static final String futureScheduleAssociateParamName = "futureSchedule";
    public static final String meetingAssociateParamName = "meeting";
    public static final String emiAmountVariationsAssociateParamName = "emiAmountVariations";
    public static final String collectionAssociateParamName = "collection";

    public static final String TABLE_COLUMN_CODE_MAPPINGS = "x_table_column_code_mappings";
    public static final String TABLE_REGISTERED_TABLE = "x_registered_table";
    public static final String TABLE_FIELD_ID = "id";

    public static final String API_PARAM_COLUMNS = "columns";
    public static final String API_PARAM_CHANGECOLUMNS = "changeColumns";
    public static final String API_PARAM_ADDCOLUMNS = "addColumns";
    public static final String API_PARAM_DROPCOLUMNS = "dropColumns";
    public static final String API_PARAM_DATATABLE_NAME = "datatableName";
    public static final String API_PARAM_APPTABLE_NAME = "apptableName";
    public static final String API_PARAM_SUBTYPE = "entitySubType";
    public static final String API_PARAM_MULTIROW = "multiRow";

    public static final String API_FIELD_NAME = "name";
    public static final String API_FIELD_NEWNAME = "newName";
    public static final String API_FIELD_TYPE = "type";
    public static final String API_FIELD_LENGTH = "length";
    public static final String API_FIELD_MANDATORY = "mandatory";
    public static final String API_FIELD_UNIQUE = "unique";
    public static final String API_FIELD_INDEXED = "indexed";
    public static final String API_FIELD_AFTER = "after";
    public static final String API_FIELD_CODE = "code";
    public static final String API_FIELD_NEWCODE = "newCode";

    public static final String API_FIELD_TYPE_STRING = "string";
    public static final String API_FIELD_TYPE_NUMBER = "number";
    public static final String API_FIELD_TYPE_BOOLEAN = "boolean";
    public static final String API_FIELD_TYPE_DECIMAL = "decimal";
    public static final String API_FIELD_TYPE_DATE = "date";
    public static final String API_FIELD_TYPE_DATETIME = "datetime";
    public static final String API_FIELD_TYPE_TIMESTAMP = "timestamp";
    public static final String API_FIELD_TYPE_TEXT = "text";
    public static final String API_FIELD_TYPE_JSON = "json";
    public static final String API_FIELD_TYPE_DROPDOWN = "dropdown";
}
