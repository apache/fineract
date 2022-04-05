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

    // Field Types
    public static final String DATETIME_FIELD_TYPE = "DateTime";

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
}
