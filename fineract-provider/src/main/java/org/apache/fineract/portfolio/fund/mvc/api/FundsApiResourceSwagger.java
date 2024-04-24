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
package org.apache.fineract.portfolio.fund.mvc.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Created by Chirag Gupta on 12/08/17.
 */
final class FundsApiResourceSwagger {

    private FundsApiResourceSwagger() {}

    @Schema(description = "PostFundsResponse")
    public static final class PostFundsResponse {

        private PostFundsResponse() {}

        @Schema(example = "1")
        public Long resourceId;
    }

    @Schema(description = "PutFundsFundIdRequest")
    public static final class PutFundsFundIdRequest {

        private PutFundsFundIdRequest() {}

        @Schema(example = "EU Agri Fund (2010-2020)")
        public String name;
        @Schema(example = "123")
        public String externalId;
    }

    @Schema(description = "PutFundsFundIdResponse")
    public static final class PutFundsFundIdResponse {

        private PutFundsFundIdResponse() {}

        @Schema(example = "1")
        public Long resourceId;
        public PutFundsFundIdRequest changes;
    }
}
