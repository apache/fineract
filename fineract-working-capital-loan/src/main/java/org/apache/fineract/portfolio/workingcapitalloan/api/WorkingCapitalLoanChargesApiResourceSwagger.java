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

package org.apache.fineract.portfolio.workingcapitalloan.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

final class WorkingCapitalLoanChargesApiResourceSwagger {

    private WorkingCapitalLoanChargesApiResourceSwagger() {}

    @Schema(description = " PostLoansLoanIdChargesRequest")
    public static final class PostLoansLoanIdChargesRequest {

        private PostLoansLoanIdChargesRequest() {}

        @Schema(example = "2")
        public Long chargeId;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "100.00")
        public Double amount;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "29 April 2013")
        public String dueDate;
        @Schema(example = "786444UUUYYH7")
        public String externalId;
    }

    @Schema(description = " PostLoansLoanIdChargesResponse")
    public static final class PostLoansLoanIdChargesResponse {

        private PostLoansLoanIdChargesResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long loanId;
        @Schema(example = "31")
        public Long resourceId;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String resourceExternalId;
    }

    @Schema(description = "PostWorkingCapitalLoansLoanIdChargesChargeIdRequest")
    public static final class PostWorkingCapitalLoansLoanIdChargesChargeIdRequest {

        private PostWorkingCapitalLoansLoanIdChargesChargeIdRequest() {}

        @Schema(example = "100.00")
        public BigDecimal amount;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "786444UUUYYH7")
        public String externalId;
        @Schema(example = "some note")
        public String note;
    }

    @Schema(description = "PostWorkingCapitalLoansLoanIdChargesChargeIdResponse")
    public static final class PostWorkingCapitalLoansLoanIdChargesChargeIdResponse {

        private PostWorkingCapitalLoansLoanIdChargesChargeIdResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "1")
        public Long clientId;
        @Schema(example = "1")
        public Long loanId;
        @Schema(example = "31")
        public Long resourceId;
        @Schema(example = "95174ff9-1a75-4d72-a413-6f9b1cb988b7")
        public String resourceExternalId;
        @Schema(example = "12")
        public Long subResourceId;
        @Schema(example = "a1b2c3d4-0000-0000-0000-000000000000")
        public String subResourceExternalId;
    }

}
