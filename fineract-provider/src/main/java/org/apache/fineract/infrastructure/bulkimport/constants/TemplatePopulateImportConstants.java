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

public class TemplatePopulateImportConstants {

    //columns sizes
    public final static int SMALL_COL_SIZE =4000;
    public final static int MEDIUM_COL_SIZE =6000;
    public final static int LARGE_COL_SIZE=8000;
    public final static int EXTRALARGE_COL_SIZE=10000;


    public final static int ROWHEADER_INDEX=0;
    public final static int FIRST_COLUMN_INDEX=0;

    //Status column
    public final static String STATUS_CELL_IMPORTED="Imported";
    public final static String STATUS_COLUMN_HEADER_NAME="Status";
    public static final String STATUS_COL_REPORT_HEADER="Status";


}
