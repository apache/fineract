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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by Chirag Gupta on 12/30/17.
 */
final class LoanScheduleApiResourceSwagger {

    private LoanScheduleApiResourceSwagger() {}

    @Schema(description = "PostLoansLoanIdScheduleRequest")
    public static final class PostLoansLoanIdScheduleRequest {

        private PostLoansLoanIdScheduleRequest() {}

        @Schema(description = "The installment changes to apply to the repayment schedule")
        public static final class PostLoansLoanIdScheduleExceptions {

            private PostLoansLoanIdScheduleExceptions() {}

            @Schema(description = "Installments whose due date, principal or instalment amount changes")
            public List<PostLoansLoanIdScheduleModifiedInstallment> modifiedinstallments;
            @Schema(description = "Installments added to the schedule")
            public List<PostLoansLoanIdScheduleNewInstallment> newinstallments;
            @Schema(description = "Installments removed from the schedule")
            public List<PostLoansLoanIdScheduleDeletedInstallment> deletedinstallments;
        }

        static final class PostLoansLoanIdScheduleModifiedInstallment {

            private PostLoansLoanIdScheduleModifiedInstallment() {}

            @Schema(example = "20 November 2011")
            public String dueDate;
            @Schema(example = "25 November 2011")
            public String modifiedDueDate;
            @Schema(example = "5000")
            public BigDecimal principal;
            @Schema(example = "30000")
            public BigDecimal installmentAmount;
        }

        static final class PostLoansLoanIdScheduleNewInstallment {

            private PostLoansLoanIdScheduleNewInstallment() {}

            @Schema(example = "31 October 2011")
            public String dueDate;
            @Schema(example = "5000")
            public BigDecimal principal;
            @Schema(example = "5000")
            public BigDecimal installmentAmount;
        }

        static final class PostLoansLoanIdScheduleDeletedInstallment {

            private PostLoansLoanIdScheduleDeletedInstallment() {}

            @Schema(example = "20 December 2011")
            public String dueDate;
        }

        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        public PostLoansLoanIdScheduleExceptions exceptions;
    }

    @Schema(description = "PostLoansLoanIdScheduleResponse")
    public static final class PostLoansLoanIdScheduleResponse {

        private PostLoansLoanIdScheduleResponse() {}

        static final class PostLoanChanges {

            private PostLoanChanges() {}

            @Schema(example = "[21, 22]")
            public List<Long> removedEntityIds;
        }

        @Schema(description = "A period of the schedule the calculateLoanSchedule command returns")
        static final class PostLoansLoanIdSchedulePeriod {

            private PostLoansLoanIdSchedulePeriod() {}

            @Schema(example = "1")
            public Integer period;
            @Schema(example = "[2011, 10, 20]")
            public LocalDate dueDate;
            @Schema(example = "[2011, 9, 20]")
            public LocalDate fromDate;
            @Schema(example = "34675.47")
            public BigDecimal principalDue;
            @Schema(example = "34675.47")
            public BigDecimal principalOriginalDue;
            @Schema(example = "1972.60")
            public BigDecimal interestOriginalDue;
            @Schema(example = "36648.07")
            public BigDecimal totalOriginalDueForPeriod;
            @Schema(example = "36648.07")
            public BigDecimal totalDueForPeriod;
            @Schema(example = "36648.07")
            public BigDecimal totalOutstandingForPeriod;
        }

        @Schema(example = "1")
        public Long loanId;
        public PostLoanChanges changes;
        @Schema(description = "Returned by the calculateLoanSchedule command: the schedule the variations produce")
        public List<PostLoansLoanIdSchedulePeriod> periods;
    }
}
