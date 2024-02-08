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

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Created by sanyam on 14/8/17.
 */
final class HolidaysApiResourceSwagger {

    private HolidaysApiResourceSwagger() {

    }

    @Schema(description = "GetHolidaysResponse")
    public static final class GetHolidaysResponse {

        private GetHolidaysResponse() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Good Friday")
        public String name;
        @Schema(example = "[2013, 10, 26]")
        public LocalDate fromDate;
        @Schema(example = "[2013, 10, 26]")
        public LocalDate toDate;
        @Schema(example = "[2013, 10, 27]")
        public LocalDate repaymentsRescheduledTo;
        @Schema(example = "1")
        public Long officeId;
        public EnumOptionData status;
    }

    @Schema(description = "PostHolidaysRequest")
    public static final class PostHolidaysRequest {

        private PostHolidaysRequest() {

        }

        static final class PostHolidaysRequestOffices {

            private PostHolidaysRequestOffices() {

            }

            @Schema(example = "1")
            public Long officeId;
        }

        @Schema(example = "Good Friday")
        public String name;
        @Schema(example = "Good Friday")
        public String description;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "25 October 2013")
        public LocalDate fromDate;
        @Schema(example = "25 October 2013")
        public LocalDate toDate;
        @Schema(example = "26 October 2013")
        public LocalDate repaymentsRescheduledTo;
        public List<PostHolidaysRequestOffices> offices;
    }

    @Schema(description = "PostHolidaysResponse")
    public static final class PostHolidaysResponse {

        private PostHolidaysResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "PostHolidaysHolidayIdRequest")
    public static final class PostHolidaysHolidayIdRequest {

        private PostHolidaysHolidayIdRequest() {

        }
    }

    @Schema(description = "PostHolidaysHolidayIdResponse")
    public static final class PostHolidaysHolidayIdResponse {

        private PostHolidaysHolidayIdResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "PutHolidaysHolidayIdRequest")
    public static final class PutHolidaysHolidayIdRequest {

        private PutHolidaysHolidayIdRequest() {

        }

        @Schema(example = "Independence day")
        public String name;
        @Schema(example = "Holiday for Independence day celebration")
        public String description;
    }

    @Schema(description = "PutHolidaysHolidayIdResponse")
    public static final class PutHolidaysHolidayIdResponse {

        private PutHolidaysHolidayIdResponse() {

        }

        static final class PutHolidaysHolidayIdResponseChanges {

            private PutHolidaysHolidayIdResponseChanges() {}

            @Schema(example = "Independence day")
            public String name;
            @Schema(example = "Holiday for Independence day celebration")
            public String description;
        }

        @Schema(example = "1")
        public Long resourceId;
        public PutHolidaysHolidayIdResponseChanges changes;
    }

    @Schema(description = "DeleteHolidaysHolidayIdResponse")
    public static final class DeleteHolidaysHolidayIdResponse {

        private DeleteHolidaysHolidayIdResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;
    }
}
