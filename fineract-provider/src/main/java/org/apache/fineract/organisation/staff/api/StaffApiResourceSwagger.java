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
package org.apache.fineract.organisation.staff.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.LocalDate;

/**
 * Created by sanyam on 19/8/17.
 */

final class StaffApiResourceSwagger {
    private StaffApiResourceSwagger() {

    }

    @ApiModel(value = "PostStaffRequest")
    public static final class PostStaffRequest {
        private PostStaffRequest() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "John")
        public String firstname;
        @ApiModelProperty(example = "Doe")
        public String lastname;
        @ApiModelProperty(example = "true")
        public Boolean isLoanOfficer;
        @ApiModelProperty(example = "17H")
        public String externalId;
        @ApiModelProperty(example = "+353851239876")
        public String mobileNo;
        @ApiModelProperty(example = "true")
        public Boolean isActive;
        @ApiModelProperty(example = "01 January 2009")
        public LocalDate joiningDate;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;

    }

    @ApiModel(value = "PostStaffResponse")
    public static final class PostStaffResponse {
        private PostStaffResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

    @ApiModel(value = "GetStaffResponse")
    public static final class GetStaffResponse {
        private GetStaffResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "John")
        public String firstname;
        @ApiModelProperty(example = "Doe")
        public String lastname;
        @ApiModelProperty(example = "Doe, John")
        public String displayName;
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        @ApiModelProperty(example = "true")
        public Boolean isLoanOfficer;
        @ApiModelProperty(example = "17H")
        public String externalId;
        @ApiModelProperty(example = "+353851239876")
        public Boolean isActive;
        @ApiModelProperty(example = "[2009,8,1]")
        public LocalDate joiningDate;

    }

    @ApiModel(value = "PutStaffRequest")
    public static final class PutStaffRequest {
        private PutStaffRequest() {

        }
        @ApiModelProperty(example = "false")
        public Boolean isLoanOfficer;
        @ApiModelProperty(example = "17Hbb")
        public String externalId;

    }

    @ApiModel(value = "PutStaffResponse")
    public static final class PutStaffResponse {
        private PutStaffResponse() {

        }
        final class PutStaffResponseChanges {
            private PutStaffResponseChanges(){}
            @ApiModelProperty(example = "false")
            public Boolean isLoanOfficer;
            @ApiModelProperty(example = "17Hbb")
            public String externalId;
        }
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "1")
        public Long resourceId;

    }
}
