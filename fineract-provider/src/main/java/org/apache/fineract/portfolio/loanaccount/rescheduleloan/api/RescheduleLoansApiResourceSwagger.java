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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Reschedule Loans Api Resource Swagger.
 */
final class RescheduleLoansApiResourceSwagger {

    private RescheduleLoansApiResourceSwagger() {}

    @Schema(description = "GetRescheduleReasonsTemplateResponse")
    public static final class GetRescheduleReasonsTemplateResponse {

        private GetRescheduleReasonsTemplateResponse() {}

        static final class GetRescheduleReasonsAllowedTypes {

            private GetRescheduleReasonsAllowedTypes() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "reason")
            public String name;
            @Schema(example = "0")
            public Integer position;
            @Schema(example = "description")
            public String description;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "false")
            public Boolean mandatory;
        }

        public Set<GetRescheduleReasonsAllowedTypes> rescheduleReasons;
    }

    @Schema(description = "GetLoanRescheduleRequestResponse")
    public static final class GetLoanRescheduleRequestResponse {

        private GetLoanRescheduleRequestResponse() {}

        static final class GetLoanRescheduleRequestStatus {

            private GetLoanRescheduleRequestStatus() {}

            @Schema(example = "100")
            public Long id;
            @Schema(example = "loanStatusType.submitted.and.pending.approval")
            public String code;
            @Schema(example = "Submitted and pending approval")
            public String value;
            @Schema(example = "true")
            public Boolean pendingApproval;
            @Schema(example = "false")
            public Boolean approved;
            @Schema(example = "false")
            public Boolean rejected;
        }

        static final class RescheduleReasonsCodeValue {

            private RescheduleReasonsCodeValue() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "reason")
            public String name;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "false")
            public Boolean mandatory;
        }

        static final class RescheduleReasonsTimeline {

            private RescheduleReasonsTimeline() {}

            @Schema(example = "[2014, 5, 1]")
            public LocalDate submittedOnDate;
            @Schema(example = "mifos")
            public String submittedByUsername;
            @Schema(example = "App")
            public String submittedByFirstname;
            @Schema(example = "Administrator")
            public String submittedByLastname;
            @Schema(example = "[2014, 5, 1]")
            public LocalDate approvedOnDate;
            @Schema(example = "mifos")
            public String approvedByUsername;
            @Schema(example = "App")
            public String approvedByFirstname;
            @Schema(example = "Administrator")
            public String approvedByLastname;
        }

        static final class LoanTermTypeOptions {

            private LoanTermTypeOptions() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "loanTermType.dueDate")
            public String code;
            @Schema(example = "dueDate")
            public String value;
        }

        static final class LoanTermVariationsData {

            private LoanTermVariationsData() {}

            @Schema(example = "1")
            public Long id;
            public LoanTermTypeOptions termType;
            @Schema(example = "[2022, 5, 1]")
            public LocalDate termVariationApplicableFrom;
            @Schema(example = "100.00")
            public BigDecimal decimalValue;
            @Schema(example = "[2022, 5, 2]")
            public LocalDate dateValue;
            @Schema(example = "true")
            public Boolean isSpecificToInstallment;
            @Schema(example = "true")
            public Boolean isProcessed;

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "18")
        public Long loanId;
        @Schema(example = "15")
        public Long clientId;
        @Schema(example = "clientName")
        public String clientName;
        @Schema(example = "000000018")
        public String loanAccountNumber;

        public GetLoanRescheduleRequestStatus statusEnum;

        @Schema(example = "1")
        public Integer rescheduleFromInstallment;
        @Schema(example = "[2022, 5, 1]")
        public LocalDate rescheduleFromDate;
        @Schema(example = "false")
        public Boolean recalculateInterest;
        public RescheduleReasonsCodeValue rescheduleReasonCodeValue;
        public RescheduleReasonsTimeline timeline;
        @Schema(example = "rescheduleReasonComment")
        public String rescheduleReasonComment;
        public Set<LoanTermVariationsData> loanTermVariationsData;
    }

    @Schema(description = "PostCreateRescheduleLoansRequest")
    public static final class PostCreateRescheduleLoansRequest {

        @Schema(example = "20 September 2011")
        public String adjustedDueDate;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "1")
        public Integer extraTerms;
        @Schema(example = "1")
        public Integer graceOnInterest;
        @Schema(example = "1")
        public Integer graceOnPrincipal;
        @Schema(example = "1")
        public Long loanId;
        @Schema(example = "1.1")
        public BigDecimal newInterestRate;
        @Schema(example = "20 September 2011")
        public String rescheduleFromDate;
        @Schema(example = "comment")
        public String rescheduleReasonComment;
        @Schema(example = "1")
        public Long rescheduleReasonId;
        @Schema(example = "20 September 2011")
        public String submittedOnDate;
    }

    @Schema(description = "PostUpdateRescheduleLoansRequest")
    public static final class PostUpdateRescheduleLoansRequest {

        @Schema(example = "20 September 2011")
        public String approvedOnDate;
        @Schema(example = "20 September 2011")
        public String rejectedOnDate;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
    }

    @Schema(description = "PostCreateRescheduleLoansResponse ")
    public static final class PostCreateRescheduleLoansResponse {

        private PostCreateRescheduleLoansResponse() {}

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "12")
        public Long clientId;
        @Schema(example = "18")
        public Long loanId;
        @Schema(example = "12")
        public Long resourceId;
    }

    @Schema(description = "PostUpdateRescheduleLoansResponse ")
    public static final class PostUpdateRescheduleLoansResponse {

        private PostUpdateRescheduleLoansResponse() {}

        static final class PostUpdateRescheduleLoanChanges {

            private PostUpdateRescheduleLoanChanges() {}

            @Schema(example = "en")
            public String locale;
            @Schema(example = "en")
            public String dateFormat;
            @Schema(example = "05/02/2022")
            public String approvedOnDate;
            @Schema(example = "1")
            public Long approvedByUserId;
        }

        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "12")
        public Long clientId;
        @Schema(example = "18")
        public Long loanId;
        @Schema(example = "12")
        public Long resourceId;
        public PostUpdateRescheduleLoanChanges changes;
    }

}
