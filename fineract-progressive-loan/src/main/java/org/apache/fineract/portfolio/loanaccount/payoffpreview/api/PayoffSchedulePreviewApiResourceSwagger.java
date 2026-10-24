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
package org.apache.fineract.portfolio.loanaccount.payoffpreview.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

final class PayoffSchedulePreviewApiResourceSwagger {

    private PayoffSchedulePreviewApiResourceSwagger() {}

    @Schema(description = "PostPayoffSchedulePreviewRequest")
    public static final class PostPayoffSchedulePreviewRequest {

        private PostPayoffSchedulePreviewRequest() {}

        @Schema(example = "1000", description = "Amount disbursed on the start date. Must be greater than 0 and have no more decimal places than the schedule (decimalPlaces).")
        public BigDecimal amount;
        @Schema(example = "P6M", description = "Loan term as a single-unit ISO-8601 period (months, weeks or days). Must use the unit of frequency and be an exact multiple of it. Exactly one of term and fixedPaymentAmount is required.")
        public String term;
        @Schema(example = "200", description = "Amount paid each period until the amount is paid off. Must exceed every period's interest and have no more decimal places than the schedule (decimalPlaces). Exactly one of term and fixedPaymentAmount is required.")
        public BigDecimal fixedPaymentAmount;
        @Schema(example = "P1M", description = "Repayment frequency as a single-unit ISO-8601 period (months, weeks or days), e.g. P1M, P2W, P14D.")
        public String frequency;
        @Schema(example = "7.0", description = "Annual nominal interest rate as a percentage. Defaults to 0.")
        public BigDecimal annualNominalInterestRate;
        @Schema(example = "DECLINING_BALANCE", description = "Only DECLINING_BALANCE is supported. Defaults to DECLINING_BALANCE.")
        public String interestMethod;
        @Schema(example = "USD", description = "Currency code. Defaults to the organisation currency when the organisation has exactly one; required otherwise.")
        public String currencyCode;
        @Schema(example = "2", description = "Rounding scale, 0 to 6. Defaults to the currency's decimal places.")
        public Integer decimalPlaces;
        @Schema(example = "2024-01-01", description = "Disbursement and schedule start date, parsed with dateFormat and locale.")
        public String startDate;
        @Schema(example = "yyyy-MM-dd", description = "Format of startDate. Required with startDate.")
        public String dateFormat;
        @Schema(example = "en", description = "Locale used to parse startDate and any numbers sent as strings. Required with startDate.")
        public String locale;
        @Schema(example = "1", description = "1 (actual), 360, 364 or 365. Defaults to actual.")
        public Integer daysInYearType;
        @Schema(example = "1", description = "1 (actual) or 30. Defaults to actual.")
        public Integer daysInMonthType;
    }

    @Schema(description = "PostPayoffSchedulePreviewResponse")
    public static final class PostPayoffSchedulePreviewResponse {

        private PostPayoffSchedulePreviewResponse() {}

        static final class PostPayoffSchedulePreviewInstallment {

            private PostPayoffSchedulePreviewInstallment() {}

            @Schema(example = "1")
            public Integer installmentNumber;
            @Schema(example = "2024-01-01")
            public String fromDate;
            @Schema(example = "2024-02-01")
            public String dueDate;
            @Schema(example = "16.67")
            public BigDecimal principalDue;
            @Schema(example = "0.00")
            public BigDecimal interestDue;
            @Schema(example = "16.67")
            public BigDecimal totalDue;
            @Schema(example = "83.33")
            public BigDecimal outstandingPrincipalBalance;
        }

        @Schema(example = "USD")
        public String currencyCode;
        @Schema(example = "100")
        public BigDecimal amount;
        @Schema(example = "0")
        public BigDecimal annualNominalInterestRate;
        @Schema(example = "DECLINING_BALANCE")
        public String interestMethod;
        @Schema(example = "P6M", description = "Omitted in fixed-payment-amount mode.")
        public String term;
        @Schema(example = "P1M")
        public String frequency;
        @Schema(example = "2024-01-01")
        public String startDate;
        @Schema(example = "6")
        public Integer numberOfInstallments;
        @Schema(example = "100.00")
        public BigDecimal totalPrincipal;
        @Schema(example = "0.00")
        public BigDecimal totalInterest;
        @Schema(example = "100.00")
        public BigDecimal totalRepayment;
        public List<PostPayoffSchedulePreviewInstallment> installments;
    }
}
