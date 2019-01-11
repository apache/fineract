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
package org.apache.fineract.organisation.office.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.joda.time.LocalDate;

import java.util.Collection;

/**
 * Created by sanyam on 14/8/17.
 */
final class OfficesApiResourceSwagger {
    private OfficesApiResourceSwagger() {

    }

    @ApiModel(value = "GetOfficesResponse")
    public static final class GetOfficesResponse {
        private GetOfficesResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Head Office")
        public String name;
        @ApiModelProperty(example = "Head Office")
        public String nameDecorated;
        @ApiModelProperty(example = "1")
        public String externalId;
        @ApiModelProperty(example = "[2009, 1, 1]")
        public LocalDate openingDate;
        @ApiModelProperty(example = ".")
        public String hierarchy;
//        @ApiModelProperty(example = "")
//        public Long parentId;
//        @ApiModelProperty(example = "")
//        public String parentName;
    }

    @ApiModel(value = "GetOfficesTemplateResponse")
    public static final class GetOfficesTemplateResponse {
        private GetOfficesTemplateResponse() {

        }
        @ApiModelProperty(example = "[2009, 1, 1]")
        public LocalDate openingDate;
        public Collection<GetOfficesResponse> allowedParents;
    }

    @ApiModel(value = "PostOfficesRequest")
    public static final class PostOfficesRequest {
        private PostOfficesRequest() {

        }
        @ApiModelProperty(example = "Good Friday")
        public String name;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "01 July 2007")
        public LocalDate openingDate;
        @ApiModelProperty(example = "2")
        public Long parentId;
        @ApiModelProperty(example = "SYS54-88")
        public String externalId;

    }

    @ApiModel(value = "PostOfficesResponse")
    public static final class PostOfficesResponse {
        private PostOfficesResponse() {

        }
        @ApiModelProperty(example = "3")
        public Long officeId;
        @ApiModelProperty(example = "3")
        public Long resourceId;
    }

    @ApiModel(value = "PutOfficesOfficeIdRequest")
    public static final class PutOfficesOfficeIdRequest {
        private PutOfficesOfficeIdRequest() {

        }
        @ApiModelProperty(example = "Name is updated")
        public String name;
    }

    @ApiModel(value = "PutOfficesOfficeIdResponse")
    public static final class PutOfficesOfficeIdResponse {
        private PutOfficesOfficeIdResponse() {

        }
        final class PutOfficesOfficeIdResponseChanges {
            private PutOfficesOfficeIdResponseChanges(){

            }
            @ApiModelProperty(example = "Name is updated")
            public String name;
        }
        @ApiModelProperty(example = "3")
        public Long officeId;
        @ApiModelProperty(example = "3")
        public Long resourceId;
        public PutOfficesOfficeIdResponseChanges changes;
    }

}
