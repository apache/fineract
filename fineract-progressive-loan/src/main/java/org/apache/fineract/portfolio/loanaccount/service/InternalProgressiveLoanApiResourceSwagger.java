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
package org.apache.fineract.portfolio.loanaccount.service;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

final class InternalProgressiveLoanApiResourceSwagger {

    @Schema(description = "ProgressiveLoanInterestScheduleModel")
    static final class ProgressiveLoanInterestScheduleModel {

        private ProgressiveLoanInterestScheduleModel() {}

        @Schema(example = "[]")
        public List<RepaymentPeriod> repaymentPeriods;
        @Schema(example = "[]")
        public TreeSet<InterestRate> interestRates;
        @Schema(example = "{}")
        public Map<Integer, List<LoanTermVariationsData>> loanTermVariations;
        @Schema(example = "1")
        public Integer installmentAmountInMultiplesOf;
        @Schema(example = "{}")
        public Map<Integer, Boolean> modifiers;
    }

    @Schema(description = "Interest Period")
    static final class InterestPeriod {

        private InterestPeriod() {}

        @Schema(example = "01/01/2024")
        public String fromDate;
        @Schema(example = "01/09/2024")
        public String dueDate;
        @Schema(example = "0.9636548454")
        public BigDecimal rateFactor;
        @Schema(example = "0.9456878987")
        public BigDecimal rateFactorTillPeriodDueDate;
        @Schema(example = "0.0")
        public BigDecimal chargebackPrincipal;
        @Schema(example = "0.0")
        public BigDecimal chargebackInterest;
        @Schema(example = "1000.0")
        public BigDecimal disbursementAmount;
        @Schema(example = "3.38")
        public BigDecimal balanceCorrectionAmount;
        @Schema(example = "865.71")
        public BigDecimal outstandingLoanBalance;
        @Schema(example = "false")
        public boolean isPaused;
    }

    @Schema(description = "Repayment Period")
    static final class RepaymentPeriod {

        private RepaymentPeriod() {}

        @Schema(example = "01/01/2024")
        public String fromDate;
        @Schema(example = "01/02/2024")
        public String dueDate;
        @Schema(example = "[]")
        public List<InterestPeriod> interestPeriods;
        @Schema(example = "127.04")
        public BigDecimal emi;
        @Schema(example = "127.04")
        public BigDecimal originalEmi;
        @Schema(example = "104.04")
        public BigDecimal paidPrincipal;
        @Schema(example = "23.00")
        public BigDecimal paidInterest;
    }

    @Schema(description = "Interest Rate")
    static final class InterestRate {

        private InterestRate() {}

        @Schema(example = "21/12/2024")
        public String effectiveFrom;
        @Schema(example = "7.963")
        public BigDecimal interestRate;
    }

    @Schema(description = "Loan Term Variations Data")
    static final class LoanTermVariationsData {

        private LoanTermVariationsData() {}

        @Schema(example = "12345")
        public Long id;
        @Schema(example = "null")
        public EnumOptionData termType;
        @Schema(example = "21/12/2024")
        public String termVariationApplicableFrom;
        @Schema(example = "1.20")
        public BigDecimal decimalValue;
        @Schema(example = "21/12/2024")
        public String dateValue;
        @Schema(example = "true")
        public boolean isSpecificToInstallment;
        @Schema(example = "true")
        public Boolean isProcessed;
    }
}
