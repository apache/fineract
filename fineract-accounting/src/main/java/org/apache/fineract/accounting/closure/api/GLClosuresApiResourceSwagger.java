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
package org.apache.fineract.accounting.closure.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

final class GLClosuresApiResourceSwagger {

    private GLClosuresApiResourceSwagger() {
        // don't allow to instantiate; use only for live API documentation
    }

    /**
     * TODO: describe where this belongs: {@link GLClosuresApiResource } {@link GLClosuresApiResource}
     */
    // Check !!

    @Schema(description = "GetGLClosureResponse")
    public static final class GetGlClosureResponse {

        private GetGlClosureResponse() {
            // dont allow to initiatiate
        }

        @Schema(example = "7")
        public Long id;
        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "Head Office")
        public String officeName;
        @Schema(example = "2013,1,2")
        public LocalDate closingDate;
        @Schema(example = "false")
        public boolean deleted;
        @Schema(example = "2013,1,3")
        public LocalDate createdDate;
        @Schema(example = "2013,1,3")
        public LocalDate lastUpdatedDate;
        @Schema(example = "1")
        public Long createdByUserId;
        @Schema(example = "mifos")
        public String createdByUsername;
        @Schema(example = "1")
        public Long lastUpdatedByUserId;
        @Schema(example = "mifos")
        public String lastUpdatedByUsername;
        @Schema(example = "closed")
        public String comments;

    }

    @Schema(description = "PostGLCLosuresRequest")
    public static final class PostGlClosuresRequest {

        private PostGlClosuresRequest() {
            // don't allow to instantiate; use only for live API documentation
        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "06 December 2012")
        public LocalDate closingDate;
        @Schema(example = "The accountants are heading for a carribean vacation")
        public String comments;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
    }

    @Schema(description = "PostGlClosuresResponse")
    public static final class PostGlClosuresResponse {

        private PostGlClosuresResponse() {
            // don't allow to instantiate; use only for live API documentation
        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "9")
        public Long resourceId;
    }

    @Schema(description = "PutGlClosuresRequest")
    public static final class PutGlClosuresRequest {

        private PutGlClosuresRequest() {
            // don't allow to instantiate; use only for live API documentation
        }

        @Schema(example = "All transactions verified by Johnny Cash")
        public String comments;
    }

    @Schema(description = "PutGlClosuresResponse")
    public static final class PutGlClosuresResponse {

        private PutGlClosuresResponse() {
            // don't allow to instantiate; use only for live API documentation
        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "9")
        public Long resourceId;
        @Schema(example = "All transactions verified by Johnny Cash")
        public String comments;
    }

    @Schema(description = "DeleteGlClosuresResponse")
    public static final class DeleteGlClosuresResponse {

        private DeleteGlClosuresResponse() {

        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "9")
        public Long resourceId;
    }
}
