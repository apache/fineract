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

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Collection;

/**
 * Created by sanyam on 14/8/17.
 */
final class OfficesApiResourceSwagger {

    private OfficesApiResourceSwagger() {

    }

    @Schema(description = "GetOfficesResponse")
    public static final class GetOfficesResponse {

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Head Office")
        public String name;
        @Schema(example = "Head Office")
        public String nameDecorated;
        @Schema(example = "1")
        public String externalId;
        @Schema(example = "[2009, 1, 1]")
        public LocalDate openingDate;
        @Schema(example = ".")
        public String hierarchy;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;

        public Collection<GetOfficesResponse> allowedParents;
    }

    @Schema(description = "GetOfficesTemplateResponse")
    public static final class GetOfficesTemplateResponse {

        private GetOfficesTemplateResponse() {

        }

        @Schema(example = "[2009, 1, 1]")
        public LocalDate openingDate;
        public Collection<GetOfficesResponse> allowedParents;
    }

    @Schema(description = "PostOfficesRequest")
    public static final class PostOfficesRequest {

        private PostOfficesRequest() {

        }

        @Schema(example = "Good Friday")
        public String name;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "01 July 2007")
        public LocalDate openingDate;
        @Schema(example = "2")
        public Long parentId;
        @Schema(example = "SYS54-88")
        public String externalId;

    }

    @Schema(description = "PostOfficesResponse")
    public static final class PostOfficesResponse {

        private PostOfficesResponse() {

        }

        @Schema(example = "3")
        public Long officeId;
        @Schema(example = "3")
        public Long resourceId;
    }

    @Schema(description = "PutOfficesOfficeIdRequest")
    public static final class PutOfficesOfficeIdRequest {

        private PutOfficesOfficeIdRequest() {

        }

        @Schema(example = "Name is updated")
        public String name;

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "01 July 2007", type = "date")
        public String openingDate;
        @Schema(example = "SYS54-88")
        public String externalId;
    }

    @Schema(description = "PutOfficesOfficeIdResponse")
    public static final class PutOfficesOfficeIdResponse {

        public static final class PutOfficesOfficeIdResponseChanges {

            @Schema(example = "Name is updated")
            public String name;
        }

        @Schema(example = "3")
        public Long officeId;
        @Schema(example = "3")
        public Long resourceId;
        public PutOfficesOfficeIdResponseChanges changes;
    }

}
