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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiFacingEnum;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanPeriodFrequencyType;
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

    @Override
    public List<WorkingCapitalLoanProductData> retrieveAllWorkingCapitalLoanProducts() {
        final List<WorkingCapitalLoanProduct> products = this.repository.findAllWithFund();
        return this.mapper.toDataList(products);
    }

    @Override
    public WorkingCapitalLoanProductData retrieveWorkingCapitalLoanProduct(final Long productId) {
        final WorkingCapitalLoanProduct product = this.repository.findByIdWithDetails(productId)
                .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(productId));
        return this.mapper.toData(product);
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
        final List<StringEnumOptionData> advancedPaymentAllocationTypes = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalPaymentAllocationType.class);
        final List<EnumOptionData> advancedPaymentAllocationTransactionTypes = PaymentAllocationTransactionType
                .getValuesAsEnumOptionDataList();
        final Collection<DelinquencyBucketData> delinquencyBucketOptions = this.delinquencyReadPlatformService
                .retrieveAllDelinquencyBuckets();

        return WorkingCapitalLoanProductData.builder() //
                .fundOptions(fundOptions) //
                .currencyOptions(currencyOptions) //
                .amortizationTypeOptions(amortizationTypeOptions) //
                .periodFrequencyTypeOptions(periodFrequencyTypeOptions) //
                .advancedPaymentAllocationTypes(advancedPaymentAllocationTypes) //
                .advancedPaymentAllocationTransactionTypes(advancedPaymentAllocationTransactionTypes) //
                .delinquencyBucketOptions(
                        delinquencyBucketOptions != null && !delinquencyBucketOptions.isEmpty() ? delinquencyBucketOptions : null) //
                .build();
    }
}
