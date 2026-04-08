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
package org.apache.fineract.batch.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import org.apache.fineract.batch.domain.Header;

/**
 * Created by sanyam on 26/7/17.
 */
final class BatchApiResourceSwagger {

    private BatchApiResourceSwagger() {

    }

    @Schema(description = "PostBatchesRequest")
    public static final class PostBatchesRequest {

        private PostBatchesRequest() {

        }

        public static final class PostBodyRequestSwagger {

            private PostBodyRequestSwagger() {

            }

            @Schema(example = "1")
            public Long officeId;
            @Schema(example = "\"Petra\"")
            public String firstname;
            @Schema(example = "\"Yton\"")
            public String lastname;
            @Schema(example = "\"ex_externalId1\"")
            public String externalId;
            @Schema(example = "\"dd MMMM yyyy\"")
            public String dateFormat;
            @Schema(example = "\"en\"")
            public String locale;
            @Schema(example = "true")
            public boolean active;
            @Schema(example = "\"04 March 2009\"")
            public String activationDate;
            @Schema(example = "\"04 March 2009\"")
            public String submittedOnDate;

        }

        @Schema(example = "1")
        public Long requestId;
        @Schema(example = "clients")
        public String relativeUrl;
        @Schema(example = "POST")
        public String method;
        public Set<Header> headers;
        @Schema(example = "1")
        public Long reference;
        public PostBodyRequestSwagger body;
    }
}
