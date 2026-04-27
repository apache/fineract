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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.ApiFacingEnum;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.accountdetails.data.WorkingCapitalLoanAccountSummaryData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentType;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCollectionData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanMapper;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanSummaryMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachData;
import org.apache.fineract.portfolio.workingcapitalloanbreach.service.WorkingCapitalBreachReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.service.WorkingCapitalNearBreachReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanDelinquencyStartType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.service.WorkingCapitalLoanProductReadPlatformService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingCapitalLoanApplicationReadPlatformServiceImpl implements WorkingCapitalLoanApplicationReadPlatformService {

    private final WorkingCapitalLoanRepository repository;
    private final WorkingCapitalLoanMapper mapper;
    private final WorkingCapitalLoanProductReadPlatformService productReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final WorkingCapitalLoanSummaryMapper workingCapitalLoanSummaryMapper;
    private final WorkingCapitalBreachReadPlatformService breachReadPlatformService;
    private final WorkingCapitalLoanDelinquencyReadPlatformService workingCapitalLoanDelinquencyReadPlatformService;
    private final WorkingCapitalNearBreachReadPlatformService nearBreachReadPlatformService;

    @Override
    public WorkingCapitalLoanTemplateData retrieveTemplate(final Long productId, final Long clientId) {
        final List<WorkingCapitalLoanProductData> productOptions = this.productReadPlatformService.retrieveAllWorkingCapitalLoanProducts();
        final WorkingCapitalLoanProductData productTemplate = this.productReadPlatformService.retrieveNewWorkingCapitalLoanProductDetails();
        final Collection<DelinquencyBucketData> delinquencyBucketOptions = this.delinquencyReadPlatformService
                .retrieveAllDelinquencyBuckets();
        final List<StringEnumOptionData> periodFrequencyTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalLoanPeriodFrequencyType.class);
        final List<WorkingCapitalBreachData> breachOptions = breachReadPlatformService.retrieveAll();
        final List<WorkingCapitalNearBreachData> nearBreachOptions = nearBreachReadPlatformService.retrieveAll();
        final List<StringEnumOptionData> delinquencyStartTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalLoanDelinquencyStartType.class);
        final List<StringEnumOptionData> delinquencyMinimumPaymentTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(DelinquencyMinimumPaymentType.class);
        final WorkingCapitalLoanData.WorkingCapitalLoanDataBuilder builder = WorkingCapitalLoanData.builder();
        if (productId != null) {
            final WorkingCapitalLoanProductData product = this.productReadPlatformService.retrieveWorkingCapitalLoanProduct(productId);
            if (product != null) {
                builder.product(product) //
                        .fundId(product.getFundId()) //
                        .fundName(product.getFundName()) //
                        .currency(product.getCurrency()) //
                        .periodPaymentRate(product.getPeriodPaymentRate()) //
                        .repaymentEvery(product.getRepaymentEvery()) //
                        .repaymentFrequencyType(product.getRepaymentFrequencyType()) //
                        .discount(product.getDiscount()) //
                        .paymentAllocation(product.getPaymentAllocation()) //
                        .breach(product.getBreach()) //
                        .nearBreach(product.getNearBreach()); //
            }
        }
        if (clientId != null) {
            builder.client(clientReadPlatformService.retrieveOne(clientId));
        }
        final WorkingCapitalLoanData loanData = builder.build();

        return WorkingCapitalLoanTemplateData.builder()//
                .loanData(loanData)//
                .productOptions(productOptions)//
                .fundOptions(productTemplate.getFundOptions())//
                .delinquencyBucketOptions(delinquencyBucketOptions)//
                .periodFrequencyTypeOptions(periodFrequencyTypeOptions)//
                .breachOptions(breachOptions)//
                .nearBreachOptions(nearBreachOptions)//
                .delinquencyStartTypeOptions(delinquencyStartTypeOptions)//
                .delinquencyMinimumPaymentTypeOptions(delinquencyMinimumPaymentTypeOptions).build();
    }

    @Override
    public Page<WorkingCapitalLoanData> retrieveAllPaged(final Pageable pageable, final Long clientId, final String externalId,
            final String status, final String accountNo) {
        final Specification<WorkingCapitalLoan> spec = (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (clientId != null) {
                predicates.add(cb.equal(root.get("client").get("id"), clientId));
            }
            if (StringUtils.isNotBlank(externalId)) {
                predicates.add(cb.equal(root.get("externalId").get("value"), externalId));
            }
            if (StringUtils.isNotBlank(status)) {
                predicates.add(cb.equal(root.get("loanStatus").as(String.class), status.toUpperCase()));
            }
            if (StringUtils.isNotBlank(accountNo)) {
                predicates.add(cb.equal(root.get("accountNumber"), accountNo));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        final Page<WorkingCapitalLoan> loanPage = this.repository.findAll(spec, pageable);
        final List<Long> loanIds = loanPage.getContent().stream().map(WorkingCapitalLoan::getId).toList();
        if (loanIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, loanPage.getTotalElements());
        }
        final List<WorkingCapitalLoan> loansWithDetails = this.repository.findByIdInWithFullDetails(loanIds);
        final Map<Long, WorkingCapitalLoan> loansById = loansWithDetails.stream()
                .collect(Collectors.toMap(WorkingCapitalLoan::getId, loan -> loan));
        final List<WorkingCapitalLoan> loansInPageOrder = loanIds.stream().map(loansById::get).filter(Objects::nonNull).toList();
        final List<WorkingCapitalLoanData> content = this.mapper.toDataList(loansInPageOrder);
        return new PageImpl<>(content, pageable, loanPage.getTotalElements());
    }

    @Override
    public WorkingCapitalLoanData retrieveOne(final Long loanId) {
        final WorkingCapitalLoan loan = this.repository.findByIdWithFullDetails(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        WorkingCapitalLoanData data = this.mapper.toData(loan);
        WorkingCapitalLoanCollectionData collectionData = workingCapitalLoanDelinquencyReadPlatformService.getCollectionData(loanId,
                ThreadLocalContextUtil.getBusinessDate());
        data.setCollectionData(collectionData);
        return data;
    }

    @Override
    public WorkingCapitalLoanData retrieveOne(final ExternalId externalId) {
        final WorkingCapitalLoan loan = this.repository.findByExternalIdWithDetails(externalId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(externalId));
        final WorkingCapitalLoan loanWithDetails = this.repository.findByIdWithFullDetails(loan.getId())
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loan.getId()));
        WorkingCapitalLoanData data = this.mapper.toData(loanWithDetails);
        WorkingCapitalLoanCollectionData collectionData = workingCapitalLoanDelinquencyReadPlatformService.getCollectionData(loan.getId(),
                ThreadLocalContextUtil.getBusinessDate());
        data.setCollectionData(collectionData);
        return data;
    }

    @Override
    public Long getResolvedLoanId(final ExternalId externalId) {
        return this.repository.findByExternalId(externalId).map(WorkingCapitalLoan::getId).orElse(null);
    }

    @Override
    public List<WorkingCapitalLoanAccountSummaryData> retrieveLoanSummaryData(final Long clientId) {
        return workingCapitalLoanSummaryMapper.toDataList(repository.findByClient_Id(clientId));
    }
}
