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
package org.apache.fineract.organisation.holiday.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by sanyam on 14/8/17.
 */
final class HolidaysApiResourceSwagger {
    private HolidaysApiResourceSwagger() {

    }

    @ApiModel(value = "GetHolidaysResponse")
    public static final class GetHolidaysResponse {
        private GetHolidaysResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Good Friday")
        public String name;
        @ApiModelProperty(example = "[2013, 10, 26]")
        public LocalDate fromDate;
        @ApiModelProperty(example = "[2013, 10, 26]")
        public LocalDate toDate;
        @ApiModelProperty(example = "[2013, 10, 27]")
        public LocalDate repaymentsRescheduledTo;
        @ApiModelProperty(example = "1")
        public Long officeId;
        public EnumOptionData status;
    }

    @ApiModel(value = "PostHolidaysRequest")
    public static final class PostHolidaysRequest {
        private PostHolidaysRequest(){

        }
        final class PostHolidaysRequestOffices {
            private PostHolidaysRequestOffices(){

            }
            @ApiModelProperty(example = "1")
            public Long officeId;
        }
        @ApiModelProperty(example = "Good Friday")
        public String name;
        @ApiModelProperty(example = "Good Friday")
        public String description;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "25 October 2013")
        public LocalDate fromDate;
        @ApiModelProperty(example = "25 October 2013")
        public LocalDate toDate;
        @ApiModelProperty(example = "26 October 2013")
        public LocalDate repaymentsRescheduledTo;
        public List<PostHolidaysRequestOffices> offices;
    }

    @ApiModel(value = "PostHolidaysResponse")
    public static final class PostHolidaysResponse {
        private PostHolidaysResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

    @ApiModel(value = "PostHolidaysHolidayIdRequest")
    public static final class PostHolidaysHolidayIdRequest {
        private PostHolidaysHolidayIdRequest() {

        }
    }

    @ApiModel(value = "PostHolidaysHolidayIdResponse")
    public static final class PostHolidaysHolidayIdResponse {
        private PostHolidaysHolidayIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

    @ApiModel(value = "PutHolidaysHolidayIdRequest")
    public static final class PutHolidaysHolidayIdRequest {
        private PutHolidaysHolidayIdRequest() {

        }
        @ApiModelProperty(example = "Independence day")
        public String name;
        @ApiModelProperty(example = "Holiday for Independence day celebration")
        public String description;
    }

    @ApiModel(value = "PutHolidaysHolidayIdResponse")
    public static final class PutHolidaysHolidayIdResponse {
        private PutHolidaysHolidayIdResponse() {

        }
        final class PutHolidaysHolidayIdResponseChanges{
            private PutHolidaysHolidayIdResponseChanges(){}
            @ApiModelProperty(example = "Independence day")
            public String name;
            @ApiModelProperty(example = "Holiday for Independence day celebration")
            public String description;
        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
        public PutHolidaysHolidayIdResponseChanges changes;
    }

    @ApiModel(value = "DeleteHolidaysHolidayIdResponse")
    public static final class DeleteHolidaysHolidayIdResponse {
        private DeleteHolidaysHolidayIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }
}
