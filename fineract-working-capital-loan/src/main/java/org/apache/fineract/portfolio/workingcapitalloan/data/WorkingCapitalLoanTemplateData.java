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
package org.apache.fineract.portfolio.workingcapitalloan.data;

import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachData;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductData;

/**
 * DTO for Working Capital Loan template response: loan details plus dropdown options (productOptions, fundOptions,
 * delinquencyBucketOptions, periodFrequencyTypeOptions, chargeOptions).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingCapitalLoanTemplateData {

    private WorkingCapitalLoanData loanData;
    private List<WorkingCapitalLoanProductData> productOptions;
    private Collection<FundData> fundOptions;
    private Collection<DelinquencyBucketData> delinquencyBucketOptions;
    private List<StringEnumOptionData> periodFrequencyTypeOptions;
    private List<StringEnumOptionData> delinquencyStartTypeOptions;
    private List<StringEnumOptionData> breachStartTypeOptions;
    private List<WorkingCapitalBreachData> breachOptions;
    private List<StringEnumOptionData> delinquencyMinimumPaymentTypeOptions;
    private List<WorkingCapitalNearBreachData> nearBreachOptions;
    /**
     * Charge definitions that can be attached to the application: every active Working Capital fee in the selected
     * product's currency, not just the ones the product catalogues. Null when no productId was supplied, since without
     * a product there is no currency to filter by.
     */
    private List<ChargeData> chargeOptions;
}
