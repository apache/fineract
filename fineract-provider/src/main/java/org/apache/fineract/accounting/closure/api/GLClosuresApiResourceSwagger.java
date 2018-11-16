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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.LocalDate;

import java.lang.reflect.Array;
import java.util.ArrayList;

final class GLClosuresApiResourceSwagger {
    private GLClosuresApiResourceSwagger() {
        // don't allow to instantiate; use only for live API documentation
    }

    /**
     * TODO: describe where this belongs: {@link GLClosuresApiResource }
     * {@Link GLClosuresApiResource}
     */
    // Check !!

    @ApiModel(value = "GetGLClosureResponse")
    public static final class GetGlClosureResponse {
        private GetGlClosureResponse() {
            // dont allow to initiatiate
        }

        @ApiModelProperty(example = "7")
        public Long id;
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        @ApiModelProperty(example = "2013,1,2")
        public LocalDate closingDate;
        @ApiModelProperty(example = "false")
        public boolean deleted;
        @ApiModelProperty(example = "2013,1,3")
        public LocalDate createdDate;
        @ApiModelProperty(example = "2013,1,3")
        public LocalDate lastUpdatedDate;
        @ApiModelProperty(example = "1")
        public Long createdByUserId;
        @ApiModelProperty(example = "mifos")
        public String createdByUsername;
        @ApiModelProperty(example = "1")
        public Long lastUpdatedByUserId;
        @ApiModelProperty(example = "mifos")
        public String lastUpdatedByUsername;
        @ApiModelProperty(example = "closed")
        public String comments;

    }

    @ApiModel(value = "PostGLCLosuresRequest")
    public static final class PostGlClosuresRequest {
        private PostGlClosuresRequest() {
            // don't allow to instantiate; use only for live API documentation
        }
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "06 December 2012")
        public LocalDate closingDate;
        @ApiModelProperty(example = "The accountants are heading for a carribean vacation")
        public String comments;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
    }

    @ApiModel(value = "PostGlClosuresResponse")
    public static final class PostGlClosuresResponse {
        private PostGlClosuresResponse() {
            // don't allow to instantiate; use only for live API documentation
        }
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "9")
        public Long resourceId;
    }

    @ApiModel(value = "PutGlClosuresRequest")
    public static final class PutGlClosuresRequest {
        private PutGlClosuresRequest() {
            // don't allow to instantiate; use only for live API documentation
        }
        @ApiModelProperty(example = "All transactions verified by Johnny Cash")
        public String comments;
    }

    @ApiModel(value = "PutGlClosuresResponse")
    public static final class PutGlClosuresResponse{
        private PutGlClosuresResponse() {
            // don't allow to instantiate; use only for live API documentation
        }
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "9")
        public Long resourceId;
        @ApiModelProperty(example = "All transactions verified by Johnny Cash")
        public String comments;
    }

    @ApiModel(value = "DeleteGlClosuresResponse")
    public static final class DeleteGlClosuresResponse{
        private DeleteGlClosuresResponse(){

        }

        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "9")
        public Long resourceId;
    }
}