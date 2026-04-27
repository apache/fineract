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
package org.apache.fineract.portfolio.workingcapitalloanproduct.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.api.ApiFacingEnum;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentType;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachData;
import org.apache.fineract.portfolio.workingcapitalloanbreach.service.WorkingCapitalBreachReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.service.WorkingCapitalNearBreachReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAccountingRuleType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanDelinquencyStartType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAllocationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.mapper.WorkingCapitalLoanProductMapper;
import org.apache.fineract.portfolio.workingcapitalloanproduct.repository.WorkingCapitalLoanProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingCapitalLoanProductReadPlatformServiceImpl implements WorkingCapitalLoanProductReadPlatformService {

    private final WorkingCapitalLoanProductRepository repository;
    private final WorkingCapitalLoanProductMapper mapper;
    private final FundReadPlatformService fundReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final WorkingCapitalBreachReadPlatformService breachReadPlatformService;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final WorkingCapitalProductAccountingMappingService wcAccountingMappingService;
    private final WorkingCapitalNearBreachReadPlatformService nearBreachReadPlatformService;

    @Override
    public List<WorkingCapitalLoanProductData> retrieveAllWorkingCapitalLoanProducts() {
        final List<WorkingCapitalLoanProduct> products = this.repository.findAllWithDetails();
        return this.mapper.toDataList(products);
    }

    @Override
    public WorkingCapitalLoanProductData retrieveWorkingCapitalLoanProduct(final Long productId) {
        final WorkingCapitalLoanProduct product = this.repository.findByIdWithDetails(productId)
                .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(productId));
        final WorkingCapitalLoanProductData productData = this.mapper.toData(product);

        if (product.getAccountingRule().isCashBased()) {
            final Map<String, GLAccountData> accountingMappings = this.wcAccountingMappingService.fetchAccountMappingDetails(productId,
                    product.getAccountingRule());
            productData.setAccountingMappings(accountingMappings);
        }

        return productData;
    }

    @Override
    public WorkingCapitalLoanProduct retrieveWorkingCapitalLoanProductByExternalId(final ExternalId externalId) {
        return this.repository.findByExternalIdWithDetails(externalId)
                .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(externalId));
    }

    @Override
    public WorkingCapitalLoanProductData retrieveNewWorkingCapitalLoanProductDetails() {
        final Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        final List<StringEnumOptionData> amortizationTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalAmortizationType.class);
        final List<StringEnumOptionData> periodFrequencyTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalLoanPeriodFrequencyType.class);
        final List<WorkingCapitalBreachData> breachOptions = breachReadPlatformService.retrieveAll();
        final List<StringEnumOptionData> advancedPaymentAllocationTypes = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalPaymentAllocationType.class);
        final List<StringEnumOptionData> delinquencyStartTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalLoanDelinquencyStartType.class);
        final List<StringEnumOptionData> delinquencyMinimumPaymentTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(DelinquencyMinimumPaymentType.class);
        final List<EnumOptionData> advancedPaymentAllocationTransactionTypes = PaymentAllocationTransactionType
                .getValuesAsEnumOptionDataList();
        final Collection<DelinquencyBucketData> delinquencyBucketOptions = this.delinquencyReadPlatformService
                .retrieveAllDelinquencyBuckets();
        final List<WorkingCapitalNearBreachData> nearBreachOptions = nearBreachReadPlatformService.retrieveAll();

        final List<StringEnumOptionData> accountingRuleOptions = WorkingCapitalAccountingRuleType.toStringEnumOptions();
        final Map<String, List<GLAccountData>> accountingMappingOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountMappingOptionsForLoanProducts();

        return WorkingCapitalLoanProductData.builder() //
                .fundOptions(fundOptions) //
                .currencyOptions(currencyOptions) //
                .amortizationTypeOptions(amortizationTypeOptions) //
                .periodFrequencyTypeOptions(periodFrequencyTypeOptions) //
                .breachOptions(breachOptions) //
                .advancedPaymentAllocationTypes(advancedPaymentAllocationTypes) //
                .advancedPaymentAllocationTransactionTypes(advancedPaymentAllocationTransactionTypes) //
                .delinquencyStartTypeOptions(delinquencyStartTypeOptions) //
                .delinquencyMinimumPaymentTypeOptions(delinquencyMinimumPaymentTypeOptions) //
                .delinquencyBucketOptions(
                        delinquencyBucketOptions != null && !delinquencyBucketOptions.isEmpty() ? delinquencyBucketOptions : null) //
                .accountingRuleOptions(accountingRuleOptions) //
                .accountingMappingOptions(accountingMappingOptions) //
                .nearBreachOptions(nearBreachOptions) //
                .build();
    }
}
