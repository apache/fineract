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
package org.apache.fineract.portfolio.workingcapitalloanproduct.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductConfigurableAttributesData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalPaymentAllocationData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductConfigurableAttributes;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductPaymentAllocationRule;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAllocationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class, uses = { DelinquencyBucketMapper.class })
public interface WorkingCapitalLoanProductMapper {

    @Mapping(target = "fundId", source = "fund.id")
    @Mapping(target = "fundName", source = "fund.name")
    @Mapping(target = "externalId", source = "externalId", qualifiedByName = "externalIdToString")
    @Mapping(target = "status", source = "closeDate", qualifiedByName = "productStatus")
    @Mapping(target = "currency", source = "currency", qualifiedByName = "monetaryCurrencyToCurrencyData")
    @Mapping(target = "amortizationType", source = "relatedDetail.amortizationType", qualifiedByName = "amortizationToStringEnumOptionData")
    @Mapping(target = "flatPercentageAmount", source = "relatedDetail.flatPercentageAmount")
    @Mapping(target = "npvDayCount", source = "relatedDetail.npvDayCount")
    @Mapping(target = "paymentAllocation", source = "paymentAllocationRules", qualifiedByName = "paymentAllocationRulesToData")
    @Mapping(target = "minPrincipal", source = "minMaxConstraints.minPrincipal")
    @Mapping(target = "principal", source = "relatedDetail.principal")
    @Mapping(target = "maxPrincipal", source = "minMaxConstraints.maxPrincipal")
    @Mapping(target = "minPeriodPaymentRate", source = "minMaxConstraints.minPeriodPaymentRate")
    @Mapping(target = "periodPaymentRate", source = "relatedDetail.periodPaymentRate")
    @Mapping(target = "maxPeriodPaymentRate", source = "minMaxConstraints.maxPeriodPaymentRate")
    @Mapping(target = "discount", source = "relatedDetail.discount")
    @Mapping(target = "repaymentEvery", source = "relatedDetail.repaymentEvery")
    @Mapping(target = "repaymentFrequencyType", source = "relatedDetail.repaymentFrequencyType", qualifiedByName = "periodFrequencyTypeToStringEnumOptionData")
    @Mapping(target = "allowAttributeOverrides", source = "configurableAttributes", qualifiedByName = "configurableAttributesToData")
    @Mapping(target = "fundOptions", ignore = true)
    @Mapping(target = "currencyOptions", ignore = true)
    @Mapping(target = "amortizationTypeOptions", ignore = true)
    @Mapping(target = "periodFrequencyTypeOptions", ignore = true)
    @Mapping(target = "advancedPaymentAllocationTypes", ignore = true)
    @Mapping(target = "advancedPaymentAllocationTransactionTypes", ignore = true)
    @Mapping(target = "applyTemplate", ignore = true)
    @Mapping(target = "delinquencyBucketOptions", ignore = true)
    WorkingCapitalLoanProductData toData(WorkingCapitalLoanProduct entity);

    List<WorkingCapitalLoanProductData> toDataList(List<WorkingCapitalLoanProduct> entities);

    @Named("externalIdToString")
    default String externalIdToString(final ExternalId externalId) {
        return externalId != null ? externalId.getValue() : null;
    }

    @Named("productStatus")
    default String productStatus(final LocalDate closeDate) {
        return (closeDate != null && DateUtils.isBeforeBusinessDate(closeDate)) ? "loanProduct.inActive" : "loanProduct.active";
    }

    @Named("monetaryCurrencyToCurrencyData")
    default CurrencyData monetaryCurrencyToCurrencyData(final MonetaryCurrency currency) {
        if (currency == null) {
            return null;
        }
        return new CurrencyData(currency.getCode(), null, currency.getDigitsAfterDecimal(), currency.getInMultiplesOf(), null, null);
    }

    @Named("amortizationToStringEnumOptionData")
    default StringEnumOptionData amortizationToStringEnumOptionData(final WorkingCapitalAmortizationType amortizationType) {
        return amortizationType != null ? amortizationType.getValueAsStringEnumOptionData() : null;
    }

    @Named("periodFrequencyTypeToStringEnumOptionData")
    default StringEnumOptionData periodFrequencyTypeToStringEnumOptionData(
            final WorkingCapitalLoanPeriodFrequencyType periodFrequencyType) {
        return periodFrequencyType != null ? periodFrequencyType.getValueAsStringEnumOptionData() : null;
    }

    @Named("paymentAllocationRulesToData")
    default List<WorkingCapitalPaymentAllocationData> paymentAllocationRulesToData(
            final List<WorkingCapitalLoanProductPaymentAllocationRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        return rules.stream().map(rule -> {
            final List<WorkingCapitalPaymentAllocationData.PaymentAllocationOrder> paymentAllocationOrder = new ArrayList<>();
            final AtomicInteger counter = new AtomicInteger(1);
            for (final WorkingCapitalPaymentAllocationType allocationType : rule.getAllocationTypes()) {
                paymentAllocationOrder.add(
                        new WorkingCapitalPaymentAllocationData.PaymentAllocationOrder(allocationType.name(), counter.getAndIncrement()));
            }
            return new WorkingCapitalPaymentAllocationData(rule.getTransactionType() != null ? rule.getTransactionType() : null,
                    paymentAllocationOrder);
        }).toList();
    }

    @Named("configurableAttributesToData")
    default WorkingCapitalLoanProductConfigurableAttributesData configurableAttributesToData(
            final WorkingCapitalLoanProductConfigurableAttributes configurableAttributes) {
        if (configurableAttributes == null) {
            return null;
        }
        return WorkingCapitalLoanProductConfigurableAttributesData.builder() //
                .flatPercentageAmount(configurableAttributes.getFlatPercentageAmount()) //
                .delinquencyBucketClassification(configurableAttributes.getDelinquencyBucketClassification()) //
                .discountDefault(configurableAttributes.getDiscountDefault()) //
                .periodPaymentFrequency(configurableAttributes.getPeriodPaymentFrequency()) //
                .periodPaymentFrequencyType(configurableAttributes.getPeriodPaymentFrequencyType()) //
                .build();
    }
}
