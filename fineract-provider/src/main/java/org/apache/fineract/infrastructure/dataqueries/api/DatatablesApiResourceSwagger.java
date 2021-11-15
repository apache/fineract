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

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetRowData;

/**
 * Created by sanyam on 31/7/17.
 */
@SuppressWarnings({ "MemberName" })
final class DatatablesApiResourceSwagger {

    private DatatablesApiResourceSwagger() {

    }

    @Schema(description = "GetDataTablesResponse")
    public static final class GetDataTablesResponse {

        private GetDataTablesResponse() {

        }

        @Schema(example = "m_client")
        public String applicationTableName;
        @Schema(example = "extra_client_details")
        public String registeredTableName;
        public List<ResultsetColumnHeaderData> columnHeaderData;
    }

    @Schema(description = "PostDataTablesRequest")
    public static final class PostDataTablesRequest {

        private PostDataTablesRequest() {}

        static final class PostColumnHeaderData {

            private PostColumnHeaderData() {}

            @Schema(required = true, example = "DOB")
            public String name;
            @Schema(required = true, example = "String", description = "Any of them: Boolean | Date | DateTime | Decimal | Dropdown | Number | String | Text")
            public String type;
            @Schema(example = "Gender", description = "Used in Code Value fields. Column name becomes: code_cd_name. Mandatory if using type Dropdown, otherwise an error is returned.")
            public String code;
            @Schema(example = "true", description = "Defaults to false")
            public Boolean mandatory;
            @Schema(example = "1653", description = "Length of the text field. Mandatory if type String is used, otherwise an error is returned.")
            public Long length;
        }

        @Schema(required = true, example = "m_client")
        public String apptableName;
        @Schema(required = true, example = "extra_client_details")
        public String datatableName;
        @Schema(required = false, description = "Allows to create multiple entries in the Data Table. Optional, defaults to false. If this property is not provided Data Table will allow only one entry.", example = "true")
        public boolean multiRow;
        @Schema(required = true)
        public List<PostColumnHeaderData> columns;
    }

    @Schema(description = "PostDataTablesResponse")
    public static final class PostDataTablesResponse {

        private PostDataTablesResponse() {

        }

        @Schema(example = "extra_client_details")
        public String resourceIdentifier;
    }

    @Schema(description = "PutDataTablesRequest")
    public static final class PutDataTablesRequest {

        private PutDataTablesRequest() {

        }

        static final class PutDataTablesRequestDropColumns {

            private PutDataTablesRequestDropColumns() {}

            @Schema(example = "Gender_cd_Question")
            public String name;
        }

        static final class PutDataTablesRequestAddColumns {

            private PutDataTablesRequestAddColumns() {}

            @Schema(example = "Question")
            public String name;
            @Schema(example = "Dropdown")
            public String type;
            @Schema(example = "Gender")
            public String code;
            @Schema(example = "true")
            public boolean mandatory;
        }

        static final class PutDataTablesRequestChangeColumns {

            private PutDataTablesRequestChangeColumns() {}

            @Schema(example = "Question")
            public String name;
            @Schema(example = "Question 2")
            public String newName;
            @Schema(example = "Gender")
            public String code;
            @Schema(example = "Gender2")
            public String newCode;
            @Schema(example = "true")
            public boolean mandatory;
        }

        @Schema(example = "m_client")
        public String appTableName;
        public List<PutDataTablesRequestDropColumns> dropColumns;
        public List<PutDataTablesRequestAddColumns> addColumns;
        public List<PutDataTablesRequestChangeColumns> ChangeColumns;
    }

    @Schema(description = "PutDataTablesResponse")
    public static final class PutDataTablesResponse {

        private PutDataTablesResponse() {

        }

        @Schema(example = "extra_client_details")
        public String resourceIdentifier;
    }

    @Schema(description = "DeleteDataTablesResponse")
    public static final class DeleteDataTablesResponse {

        private DeleteDataTablesResponse() {

        }

        @Schema(example = "extra_client_details")
        public String resourceIdentifier;
    }

    @Schema(description = "PostDataTablesRegisterDatatableAppTable")
    public static final class PostDataTablesRegisterDatatableAppTable {

        private PostDataTablesRegisterDatatableAppTable() {}
    }

    @Schema(description = "PostDataTablesAppTableIdRequest")
    public static final class PostDataTablesAppTableIdRequest {

        private PostDataTablesAppTableIdRequest() {

        }

        @Schema(example = "Livestock sales")
        public String BusinessDescription;
        @Schema(example = "First comment made")
        public String Comment;
        @Schema(example = "Primary")
        public String Education_cv;
        @Schema(example = "6")
        public Long Gender_cd;
        @Schema(example = "8.5")
        public Double HighestRatePaid;
        @Schema(example = "01 October 2012")
        public String NextVisit;
        @Schema(example = "5")
        public Long YearsinBusiness;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
    }

    @Schema(description = "PostDataTablesAppTableIdResponse ")
    public static final class PostDataTablesAppTableIdResponse {

        private PostDataTablesAppTableIdResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "GetDataTablesAppTableIdResponse")
    public static final class GetDataTablesAppTableIdResponse {

        private GetDataTablesAppTableIdResponse() {

        }

        public List<ResultsetColumnHeaderData> columnHeaders;
        public List<ResultsetRowData> data;
    }

    @Schema(description = "PutDataTablesAppTableIdRequest")
    public static final class PutDataTablesAppTableIdRequest {

        private PutDataTablesAppTableIdRequest() {

        }

        @Schema(example = "Livestock sales updated")
        public String BusinessDescription;
    }

    @Schema(description = "PutDataTablesAppTableIdResponse")
    public static final class PutDataTablesAppTableIdResponse {

        private PutDataTablesAppTableIdResponse() {

        }

        static final class PutDataTablesAppTableIdResponseChanges {

            private PutDataTablesAppTableIdResponseChanges() {}

            @Schema(example = "Livestock sales updated")
            public String BusinessDescription;
        }

        @Schema(example = "1")
        public Long resourceId;
        public PutDataTablesAppTableIdResponseChanges changes;
    }

    @Schema(description = "PutDataTablesAppTableIdDatatableIdRequest")
    public static final class PutDataTablesAppTableIdDatatableIdRequest {

        private PutDataTablesAppTableIdDatatableIdRequest() {

        }

        @Schema(example = "01 June 1982")
        public String DateOfBirth;
        @Schema(example = "5")
        public Long Education_cdHighest;
        @Schema(example = "June")
        public String Name;
        @Schema(example = "More notes")
        public String OtherNotes;
        @Schema(example = "20")
        public Long PointsScore;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
    }

    @Schema(description = "PutDataTablesAppTableIdDatatableIdResponse ")
    public static final class PutDataTablesAppTableIdDatatableIdResponse {

        private PutDataTablesAppTableIdDatatableIdResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "DeleteDataTablesDatatableAppTableIdResponse ")
    public static final class DeleteDataTablesDatatableAppTableIdResponse {

        private DeleteDataTablesDatatableAppTableIdResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "DeleteDataTablesDatatableAppTableIdDatatableIdResponse ")
    public static final class DeleteDataTablesDatatableAppTableIdDatatableIdResponse {

        private DeleteDataTablesDatatableAppTableIdDatatableIdResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }

}
