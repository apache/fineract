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
package org.apache.fineract.infrastructure.bulkimport.constants;

public class OfficeConstants {

    //Column indices
    public static final int OFFICE_NAME_COL = 0;
    public static final int PARENT_OFFICE_NAME_COL = 1;
    public static final int PARENT_OFFICE_ID_COL=2;
    public static final int OPENED_ON_COL = 3;
    public static final int EXTERNAL_ID_COL = 4;
    public static final int LOOKUP_OFFICE_COL=7;
    public static final int LOOKUP_OFFICE_ID_COL=8;
    public static final int STATUS_COL=10;

    //sheet names
    public static final String OFFICE_WORKBOOK_SHEET_NAME="Offices";

    //Column header names
    public static final String OFFICE_NAME_COL_HEADER_NAME="Office Name*";
    public static final String PARENT_OFFICE_NAME_COL_HEADER_NAME="Parent Office*";
    public static final String PARENT_OFFICE_ID_COL_HEADER_NAME="Parent OfficeId*";
    public static final String OPENED_ON_COL_HEADER_NAME="Opened On Date*";
    public static final String EXTERNAL_ID_COL_HEADER_NAME="External Id*";
    public static final String LOOKUP_OFFICE_COL_HEADER_NAME="Lookup Offices";
    public static final String LOOKUP_OFFICE_ID_COL_HEADER_NAME="Lookup OfficeId*";



}
