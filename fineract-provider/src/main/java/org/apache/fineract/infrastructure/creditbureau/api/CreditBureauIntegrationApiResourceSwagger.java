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
package org.apache.fineract.infrastructure.creditbureau.api;

import io.swagger.v3.oas.annotations.media.Schema;

final class CreditBureauIntegrationApiResourceSwagger {

    private CreditBureauIntegrationApiResourceSwagger() {}

    @Schema(description = "PostCreditReportRequest")
    public static final class PostCreditReportRequest {

        private PostCreditReportRequest() {}

        @Schema(example = "1", description = "The organisation credit bureau to query")
        public String creditBureauID;
        @SuppressWarnings("MemberName")
        @Schema(example = "NRC213", description = "The national registration card number of the borrower")
        public String NRC;
    }
}
