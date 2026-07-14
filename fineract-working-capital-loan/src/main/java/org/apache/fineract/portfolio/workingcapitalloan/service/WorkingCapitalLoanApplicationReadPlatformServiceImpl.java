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
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.ApiFacingEnum;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.accountdetails.data.WorkingCapitalLoanAccountSummaryData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentType;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.loanorigination.data.LoanOriginatorData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCollectionData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanMapper;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanSummaryMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachData;
import org.apache.fineract.portfolio.workingcapitalloanbreach.service.WorkingCapitalBreachReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.service.WorkingCapitalNearBreachReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanBreachStartType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanDelinquencyStartType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.service.WorkingCapitalLoanProductReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUserRepository;
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
    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;
    private final WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleRepository delinquencyRangeScheduleRepository;
    private final Optional<WorkingCapitalLoanOriginatorReadPlatformService> originatorReadService;
    private final WorkingCapitalLoanChargeReadPlatformService chargeReadPlatformService;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper;
    private final AppUserRepository appUserRepository;

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
        final List<StringEnumOptionData> breachStartTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalLoanBreachStartType.class);
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
                        .paymentRate(product.getPeriodPaymentRate()) //
                        .repaymentEvery(product.getRepaymentEvery()) //
                        .repaymentFrequencyType(product.getRepaymentFrequencyType()) //
                        .discountFee(product.getDiscount()) //
                        .paymentAllocation(product.getPaymentAllocation()) //
                        .breach(product.getBreach()) //
                        .nearBreach(product.getNearBreach()) //
                        .breachGraceDays(product.getBreachGraceDays()); //
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
                .breachStartTypeOptions(breachStartTypeOptions)//
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
        data.setDelinquent(collectionData);
        data.setCharges(chargeReadPlatformService.retrieveLoanCharges(loanId));
        enrichWithFullCurrency(data);
        enrichWithSubmittedBy(loan, data);
        enrichWithRateAndTerm(loan, data);
        enrichWithStartDates(loan, data);
        enrichWithOriginators(loanId, data);
        return data;
    }

    @Override
    public WorkingCapitalLoanData retrieveOne(final ExternalId externalId) {
        return retrieveOne(repository.findIdByExternalId(externalId));
    }

    private void enrichWithFullCurrency(final WorkingCapitalLoanData data) {
        final CurrencyData currency = data.getCurrency();
        if (currency == null || currency.getCode() == null) {
            return;
        }
        final CurrencyData appCurrency = applicationCurrencyRepositoryWrapper.findOneWithNotFoundDetection(currency.getCode()).toData();
        final CurrencyData fullCurrency = new CurrencyData(currency.getCode(), appCurrency.getName(), currency.getDecimalPlaces(),
                currency.getInMultiplesOf(), appCurrency.getDisplaySymbol(), appCurrency.getNameCode());
        data.setCurrency(fullCurrency);
        Optional.ofNullable(data.getSummary()).ifPresent(summary -> summary.setCurrency(fullCurrency));
    }

    private void enrichWithSubmittedBy(final WorkingCapitalLoan loan, final WorkingCapitalLoanData data) {
        if (data.getTimeline() == null) {
            return;
        }
        loan.getCreatedBy().flatMap(appUserRepository::findById).ifPresent(user -> {
            data.getTimeline().setSubmittedByUsername(user.getUsername());
            data.getTimeline().setSubmittedByFirstname(user.getFirstname());
            data.getTimeline().setSubmittedByLastname(user.getLastname());
        });
    }

    private void enrichWithRateAndTerm(final WorkingCapitalLoan loan, final WorkingCapitalLoanData data) {
        final MathContext mc = MoneyHelper.getMathContext();
        final CurrencyData currency = WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan);
        scheduleRepositoryWrapper.readModel(loan.getId(), mc, currency).ifPresent(model -> {
            final BigDecimal dailyEir = model.effectiveInterestRate();
            data.setNumberOfRepayments(model.effectiveTotalTerm());
            data.setPeriodPaymentAmount(model.expectedPaymentAmount() != null ? model.expectedPaymentAmount().getAmount() : null);
            data.setNetDisbursalAmount(model.netDisbursementAmount() != null ? model.netDisbursementAmount().getAmount() : null);
            data.setDailyEir(dailyEir);
            if (dailyEir != null) {
                data.setCalculatedAnnualEir(BigDecimal.ONE.add(dailyEir, mc).pow(365, mc).subtract(BigDecimal.ONE, mc));
            }
        });
    }

    private void enrichWithStartDates(final WorkingCapitalLoan loan, final WorkingCapitalLoanData data) {
        // breachStartDate: fromDate of the earliest breached period. The breach schedule already offsets its first
        // period
        // by breachGraceDays, so the grace period is implicitly reflected in the fromDate.
        breachScheduleRepository.findTopByLoanIdAndBreachTrueOrderByFromDateAsc(loan.getId())
                .ifPresent(period -> data.setBreachStartDate(period.getFromDate()));

        // delinquencyStartDate: fromDate of the earliest delinquent period plus delinquencyGraceDays. The delinquency
        // range
        // schedule does not apply the grace days when generating periods, so they are added here.
        delinquencyRangeScheduleRepository.findTopByLoanIdAndMinPaymentCriteriaMetFalseOrderByFromDateAsc(loan.getId())
                .ifPresent(period -> {
                    final int graceDays = data.getDelinquencyGraceDays() != null ? data.getDelinquencyGraceDays() : 0;
                    data.setDelinquencyStartDate(period.getFromDate().plusDays(graceDays));
                });
    }

    private void enrichWithOriginators(final Long loanId, final WorkingCapitalLoanData data) {
        if (this.originatorReadService.isPresent()) {
            List<LoanOriginatorData> loanOriginatorData = this.originatorReadService.get().retrieveByLoanId(loanId);
            data.setOriginators(loanOriginatorData.isEmpty() ? Collections.emptyList() : loanOriginatorData);
        }
    }

    @Override
    public Long getResolvedLoanId(final ExternalId externalId) {
        return this.repository.findByExternalId(externalId).map(WorkingCapitalLoan::getId).orElse(null);
    }

    @Override
    public List<WorkingCapitalLoanAccountSummaryData> retrieveLoanSummaryData(final Long clientId) {
        return workingCapitalLoanSummaryMapper.toDataList(repository.findByClient_Id(clientId));
    }

    @Override
    public boolean existsByLoanId(Long loanId) {
        return this.repository.existsById(loanId);
    }
}
