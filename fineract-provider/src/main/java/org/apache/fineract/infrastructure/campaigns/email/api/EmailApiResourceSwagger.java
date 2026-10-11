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
package org.apache.fineract.infrastructure.campaigns.email.api;

import io.swagger.v3.oas.annotations.media.Schema;

final class EmailApiResourceSwagger {

    private EmailApiResourceSwagger() {}

    @Schema(description = "PostEmailRequest")
    public static final class PostEmailRequest {

        private PostEmailRequest() {}

        @Schema(example = "1", description = "The client the message is for; either clientId or staffId must be given")
        public Long clientId;
        @Schema(example = "1", description = "The staff member the message is for; either clientId or staffId must be given")
        public Long staffId;
        @Schema(example = "Loan repayment reminder")
        public String emailSubject;
        @Schema(example = "Your next repayment is due on 1 March.")
        public String emailMessage;
        @Schema(example = "en")
        public String locale;
    }

    @Schema(description = "PutEmailRequest")
    public static final class PutEmailRequest {

        private PutEmailRequest() {}

        @Schema(example = "Your next repayment is due on 1 April.", description = "The only field an update accepts")
        public String emailMessage;
    }
}
