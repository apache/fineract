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
import java.util.Set;

public final class WorkingCapitalLoanChargesApiResourceSwagger {

    private WorkingCapitalLoanChargesApiResourceSwagger() {}

    @Schema(description = "GetWorkingCapitalLoansLoanIdChargesTemplateResponse")
    public static final class GetWorkingCapitalLoansLoanIdChargesTemplateResponse {

        private GetWorkingCapitalLoansLoanIdChargesTemplateResponse() {}

        static final class GetWCLoanChargeCurrency {

            private GetWCLoanChargeCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetWCLoanChargeTemplateChargeTimeType {

            private GetWCLoanChargeTemplateChargeTimeType() {}

            @Schema(example = "2")
            public Long id;
            @Schema(example = "chargeTimeType.specifiedDueDate")
            public String code;
            @Schema(example = "Specified due date")
            public String description;
        }

        static final class GetWCLoanChargeTemplateChargeAppliesTo {

            private GetWCLoanChargeTemplateChargeAppliesTo() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "chargeAppliesTo.loan")
            public String code;
            @Schema(example = "Loan")
            public String description;
        }

        static final class GetWCLoanChargeCalculationType {

            private GetWCLoanChargeCalculationType() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "chargeCalculationType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        static final class GetWCLoanChargeTemplateChargeOptions {

            private GetWCLoanChargeTemplateChargeOptions() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "Collection fee")
            public String name;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "false")
            public Boolean penalty;
            public GetWCLoanChargeCurrency currency;
            @Schema(example = "100.00")
            public Double amount;
            public GetWCLoanChargeTemplateChargeTimeType chargeTimeType;
            public GetWCLoanChargeTemplateChargeAppliesTo chargeAppliesTo;
            public GetWCLoanChargeCalculationType chargeCalculationType;
        }

        @Schema(example = "0.00")
        public Double amountPaid;
        @Schema(example = "0.00")
        public Double amountWaived;
        @Schema(example = "0.00")
        public Double amountWrittenOff;
        @Schema(example = "false")
        public Boolean penalty;
        public Set<GetWCLoanChargeTemplateChargeOptions> chargeOptions;
    }
}
