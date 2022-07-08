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
package org.apache.fineract.infrastructure.businessdate.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

final class BusinessDateApiResourceSwagger {

    private BusinessDateApiResourceSwagger() {

    }

    @Schema(description = "BusinessDateResponse")
    public static final class BusinessDateResponse {

        @Schema(example = "COB date")
        public String description;
        @Schema(example = "COB_DATE")
        public String type;
        @Schema(example = "[2015,02,15]")
        public LocalDate date;

        private BusinessDateResponse() {

        }
    }

    @Schema(description = "BusinessDateRequest")
    public static final class BusinessDateRequest {

        @Schema(example = "yyyy-MM-dd")
        public String dateFormat;
        @Schema(example = "COB_DATE")
        public String type;
        @Schema(example = "2015-02-15")
        public String date;
        @Schema(example = "en")
        public String locale;

        private BusinessDateRequest() {}

    }

}
