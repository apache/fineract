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
package org.apache.fineract.portfolio.workingcapitalloanproduct.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.fund.data.FundData;

/**
 * Data Transfer Object for Working Capital Loan Product.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingCapitalLoanProductData implements Serializable {

    private Long id;
    private String name;
    private String shortName;
    private String description;
    private Long fundId;
    private String fundName;
    private LocalDate startDate;
    private LocalDate closeDate;
    private String externalId;
    private String status;

    // Currency details
    private CurrencyData currency;

    // Settings details
    private StringEnumOptionData amortizationType;
    private BigDecimal flatPercentageAmount;
    private DelinquencyBucketData delinquencyBucket;
    private Integer npvDayCount;
    private List<WorkingCapitalPaymentAllocationData> paymentAllocation;

    // Term details
    private BigDecimal minPrincipal;
    private BigDecimal principal;
    private BigDecimal maxPrincipal;
    private BigDecimal minPeriodPaymentRate;
    private BigDecimal periodPaymentRate;
    private BigDecimal maxPeriodPaymentRate;
    private BigDecimal discount;
    private Integer repaymentEvery;
    private StringEnumOptionData repaymentFrequencyType;

    // Configurable attributes (allowAttributeOverrides)
    private WorkingCapitalLoanProductConfigurableAttributesData allowAttributeOverrides;

    // Template related
    private Collection<FundData> fundOptions;
    private Collection<CurrencyData> currencyOptions;
    private List<StringEnumOptionData> amortizationTypeOptions;
    private List<StringEnumOptionData> periodFrequencyTypeOptions;
    private List<StringEnumOptionData> advancedPaymentAllocationTypes;
    private List<EnumOptionData> advancedPaymentAllocationTransactionTypes;
    private Collection<DelinquencyBucketData> delinquencyBucketOptions;

    public WorkingCapitalLoanProductData applyTemplate(final WorkingCapitalLoanProductData productTemplate) {
        setFundOptions(productTemplate.getFundOptions());
        setCurrencyOptions(productTemplate.getCurrencyOptions());
        setAmortizationTypeOptions(productTemplate.getAmortizationTypeOptions());
        setPeriodFrequencyTypeOptions(productTemplate.getPeriodFrequencyTypeOptions());
        setAdvancedPaymentAllocationTransactionTypes(productTemplate.getAdvancedPaymentAllocationTransactionTypes());
        setAdvancedPaymentAllocationTypes(productTemplate.getAdvancedPaymentAllocationTypes());
        setDelinquencyBucketOptions(productTemplate.getDelinquencyBucketOptions());
        return this;
    }
}
