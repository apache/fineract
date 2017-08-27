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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetRowData;

import java.util.List;

/**
 * Created by sanyam on 31/7/17.
 */
final class DatatablesApiResourceSwagger {
    private DatatablesApiResourceSwagger() {

    }

    @ApiModel(value = "GetDataTablesResponse")
    public static final class GetDataTablesResponse {
        private GetDataTablesResponse() {

        }
        @ApiModelProperty(example = "m_client")
        public String appTableName;
        @ApiModelProperty(example = "extra_client_details")
        public String datatableName;
        public List<ResultsetColumnHeaderData> column;
    }

    @ApiModel(value = "PostDataTablesRequest")
    public static final class PostDataTablesRequest{
        private PostDataTablesRequest() {

        }
        @ApiModelProperty(example = "m_client")
        public String applicationTableName;
        @ApiModelProperty(example = "extra_client_details")
        public String registeredTableName;
        @ApiModelProperty(example = "true")
        public boolean multiRow;
        public List<ResultsetColumnHeaderData> columnHeaderData;
    }

    @ApiModel(value = "PostDataTablesResponse")
    public static final class PostDataTablesResponse{
        private PostDataTablesResponse() {

        }
        @ApiModelProperty(example = "extra_client_details")
        public String resourceIdentifier;
    }

    @ApiModel(value = "PutDataTablesRequest")
    public static final class PutDataTablesRequest{
        private PutDataTablesRequest() {

        }
        final class PutDataTablesRequestDropColumns {
            private PutDataTablesRequestDropColumns() {}
            @ApiModelProperty(example = "Gender_cd_Question")
            public String name;
        }
        final class PutDataTablesRequestAddColumns {
            private PutDataTablesRequestAddColumns() {
            }
            @ApiModelProperty(example = "Question")
            public String name;
            @ApiModelProperty(example = "Dropdown")
            public String type;
            @ApiModelProperty(example = "Gender")
            public String code;
            @ApiModelProperty(example = "true")
            public boolean mandatory;
        }
        final class PutDataTablesRequestChangeColumns {
            private PutDataTablesRequestChangeColumns() {
            }
            @ApiModelProperty(example = "Question")
            public String name;
            @ApiModelProperty(example = "Question 2")
            public String newName;
            @ApiModelProperty(example = "Gender")
            public String code;
            @ApiModelProperty(example = "Gender2")
            public String newCode;
            @ApiModelProperty(example = "true")
            public boolean mandatory;
        }
        @ApiModelProperty(example = "m_client")
        public String appTableName;
        public List<PutDataTablesRequestDropColumns> dropColumns;
        public List<PutDataTablesRequestAddColumns> addColumns;
        public List<PutDataTablesRequestChangeColumns> ChangeColumns;
    }

    @ApiModel(value = "PutDataTablesResponse")
    public static final class PutDataTablesResponse{
        private PutDataTablesResponse() {

        }
        @ApiModelProperty(example = "extra_client_details")
        public String resourceIdentifier;
    }

    @ApiModel(value = "DeleteDataTablesResponse")
    public static final class DeleteDataTablesResponse{
        private DeleteDataTablesResponse() {

        }
        @ApiModelProperty(example = "extra_client_details")
        public String resourceIdentifier;
    }

    @ApiModel(value = "PostDataTablesRegisterDatatableAppTable")
    public static final class PostDataTablesRegisterDatatableAppTable {
        private PostDataTablesRegisterDatatableAppTable () {}
    }

    @ApiModel(value = "PostDataTablesAppTableIdRequest")
    public static final class PostDataTablesAppTableIdRequest{
        private PostDataTablesAppTableIdRequest() {

        }
        @ApiModelProperty(example = "Livestock sales")
        public String BusinessDescription;
        @ApiModelProperty(example = "First comment made")
        public String Comment;
        @ApiModelProperty(example = "Primary")
        public String Education_cv;
        @ApiModelProperty(example = "6")
        public Long Gender_cd;
        @ApiModelProperty(example = "8.5")
        public Double HighestRatePaid;
        @ApiModelProperty(example = "01 October 2012")
        public String NextVisit;
        @ApiModelProperty(example = "5")
        public Long YearsinBusiness;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
    }

    @ApiModel(value = "PostDataTablesAppTableIdResponse ")
    public static final class PostDataTablesAppTableIdResponse {
        private PostDataTablesAppTableIdResponse () {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

    @ApiModel(value = "GetDataTablesAppTableIdResponse")
    public static final class GetDataTablesAppTableIdResponse {
        private GetDataTablesAppTableIdResponse() {

        }
        public List<ResultsetColumnHeaderData> columnHeaders;
        public List<ResultsetRowData> data;
    }

    @ApiModel(value = "PutDataTablesAppTableIdRequest")
    public static final class PutDataTablesAppTableIdRequest{
        private PutDataTablesAppTableIdRequest() {

        }
        @ApiModelProperty(example = "Livestock sales updated")
        public String BusinessDescription;
    }

    @ApiModel(value = "PutDataTablesAppTableIdResponse")
    public static final class PutDataTablesAppTableIdResponse {
        private PutDataTablesAppTableIdResponse() {

        }
        final class PutDataTablesAppTableIdResponseChanges{
            private PutDataTablesAppTableIdResponseChanges () {}
            @ApiModelProperty(example = "Livestock sales updated")
            public String BusinessDescription;
        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
        public PutDataTablesAppTableIdResponseChanges changes;
    }

    @ApiModel(value = "PutDataTablesAppTableIdDatatableIdRequest")
    public static final class PutDataTablesAppTableIdDatatableIdRequest{
        private PutDataTablesAppTableIdDatatableIdRequest() {

        }
        @ApiModelProperty(example = "01 June 1982")
        public String DateOfBirth;
        @ApiModelProperty(example = "5")
        public Long Education_cdHighest;
        @ApiModelProperty(example = "June")
        public String Name;
        @ApiModelProperty(example = "More notes")
        public String OtherNotes;
        @ApiModelProperty(example = "20")
        public Long PointsScore;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
    }

    @ApiModel(value = "PutDataTablesAppTableIdDatatableIdResponse ")
    public static final class PutDataTablesAppTableIdDatatableIdResponse {
        private PutDataTablesAppTableIdDatatableIdResponse () {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

    @ApiModel(value = "DeleteDataTablesDatatableAppTableIdResponse ")
    public static final class DeleteDataTablesDatatableAppTableIdResponse {
        private DeleteDataTablesDatatableAppTableIdResponse () {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

    @ApiModel(value = "DeleteDataTablesDatatableAppTableIdDatatableIdResponse ")
    public static final class DeleteDataTablesDatatableAppTableIdDatatableIdResponse {
        private DeleteDataTablesDatatableAppTableIdDatatableIdResponse () {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

}
