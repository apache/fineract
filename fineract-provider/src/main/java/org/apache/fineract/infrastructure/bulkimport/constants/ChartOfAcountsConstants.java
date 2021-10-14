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

public final class ChartOfAcountsConstants {

    private ChartOfAcountsConstants() {

    }

    public static final int ACCOUNT_TYPE_COL = 0;// A
    public static final int ACCOUNT_NAME_COL = 1;// B
    public static final int ACCOUNT_USAGE_COL = 2;// C
    public static final int MANUAL_ENTRIES_ALLOWED_COL = 3;// D
    public static final int PARENT_COL = 4;// E
    public static final int PARENT_ID_COL = 5;// F
    public static final int GL_CODE_COL = 6;// G
    public static final int TAG_COL = 7;// H
    public static final int TAG_ID_COL = 8;// I
    public static final int DESCRIPTION_COL = 9;// J
    // adding for opening balance bulk import
    public static final int OFFICE_COL = 10; // K
    public static final int OFFICE_COL_ID = 11; // L
    public static final int CURRENCY_CODE = 12; // M
    public static final int DEBIT_AMOUNT = 13; // N
    public static final int CREDIT_AMOUNT = 14; // O

    public static final int LOOKUP_ACCOUNT_TYPE_COL = 18;// S
    public static final int LOOKUP_ACCOUNT_NAME_COL = 19; // T
    public static final int LOOKUP_ACCOUNT_ID_COL = 20;// U
    public static final int LOOKUP_TAG_COL = 21; // V
    public static final int LOOKUP_TAG_ID_COL = 22; // W

    // adding for opening balance bulk import
    public static final int LOOKUP_OFFICE_COL = 23; // X
    public static final int LOOKUP_OFFICE_ID_COL = 24; // Y

    public static final int STATUS_COL = 25;
}
