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
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.AdvancedMappingToExpenseAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachData;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;

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
    private DelinquencyBucketData delinquencyBucket;
    private WorkingCapitalBreachData breach;
    private Integer npvDayCount;
    private List<WorkingCapitalPaymentAllocationData> paymentAllocation;
    private WorkingCapitalNearBreachData nearBreach;

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
    private Integer delinquencyGraceDays;
    private StringEnumOptionData delinquencyStartType;
    private Integer breachGraceDays;
    private StringEnumOptionData breachStartType;

    // Configurable attributes (allowAttributeOverrides)
    private WorkingCapitalLoanProductConfigurableAttributesData allowAttributeOverrides;

    // Accounting
    private StringEnumOptionData accountingRule;
    private Map<String, GLAccountData> accountingMappings;
    private Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings;
    private Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings;
    private Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings;
    private List<AdvancedMappingToExpenseAccountData> chargeOffReasonToExpenseAccountMappings;
    private List<AdvancedMappingToExpenseAccountData> writeOffReasonsToExpenseMappings;

    // Template related
    private Collection<FundData> fundOptions;
    private Collection<PaymentTypeData> paymentTypeOptions;
    private Collection<ChargeData> chargeOptions;
    private Collection<ChargeData> penaltyOptions;
    private Collection<CurrencyData> currencyOptions;
    private List<StringEnumOptionData> amortizationTypeOptions;
    private List<StringEnumOptionData> periodFrequencyTypeOptions;
    private List<StringEnumOptionData> advancedPaymentAllocationTypes;
    private List<StringEnumOptionData> delinquencyStartTypeOptions;
    private List<StringEnumOptionData> breachStartTypeOptions;
    private List<StringEnumOptionData> delinquencyMinimumPaymentTypeOptions;
    private List<EnumOptionData> advancedPaymentAllocationTransactionTypes;
    private Collection<DelinquencyBucketData> delinquencyBucketOptions;
    private List<WorkingCapitalBreachData> breachOptions;
    private List<StringEnumOptionData> accountingRuleOptions;
    private Map<String, List<GLAccountData>> accountingMappingOptions;
    private List<WorkingCapitalNearBreachData> nearBreachOptions;
    private List<CodeValueData> chargeOffReasonOptions;
    private List<CodeValueData> writeOffReasonOptions;

    public WorkingCapitalLoanProductData applyTemplate(final WorkingCapitalLoanProductData productTemplate) {
        setFundOptions(productTemplate.getFundOptions());
        setCurrencyOptions(productTemplate.getCurrencyOptions());
        setAmortizationTypeOptions(productTemplate.getAmortizationTypeOptions());
        setPeriodFrequencyTypeOptions(productTemplate.getPeriodFrequencyTypeOptions());
        setAdvancedPaymentAllocationTransactionTypes(productTemplate.getAdvancedPaymentAllocationTransactionTypes());
        setAdvancedPaymentAllocationTypes(productTemplate.getAdvancedPaymentAllocationTypes());
        setDelinquencyBucketOptions(productTemplate.getDelinquencyBucketOptions());
        setBreachOptions(productTemplate.getBreachOptions());
        setDelinquencyStartTypeOptions(productTemplate.getDelinquencyStartTypeOptions());
        setBreachStartTypeOptions(productTemplate.getBreachStartTypeOptions());
        setAccountingRuleOptions(productTemplate.getAccountingRuleOptions());
        setAccountingMappingOptions(productTemplate.getAccountingMappingOptions());
        setPaymentTypeOptions(productTemplate.getPaymentTypeOptions());
        setChargeOptions(productTemplate.getChargeOptions());
        setPenaltyOptions(productTemplate.getPenaltyOptions());
        setChargeOffReasonOptions(productTemplate.getChargeOffReasonOptions());
        setWriteOffReasonOptions(productTemplate.getWriteOffReasonOptions());
        setDelinquencyMinimumPaymentTypeOptions(productTemplate.getDelinquencyMinimumPaymentTypeOptions());
        setNearBreachOptions(productTemplate.getNearBreachOptions());
        return this;
    }
}
