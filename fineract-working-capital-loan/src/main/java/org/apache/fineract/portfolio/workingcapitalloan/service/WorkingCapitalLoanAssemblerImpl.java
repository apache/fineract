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

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatLookup;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.accountnumberformat.service.AccountNumberGeneratorService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.exception.FundNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPaymentAllocationRule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductPaymentAllocationRule;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.apache.fineract.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloanproduct.repository.WorkingCapitalLoanProductRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanAssemblerImpl implements WorkingCapitalLoanAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final WorkingCapitalLoanProductRepository loanProductRepository;
    private final ClientRepository clientRepository;
    private final FundRepository fundRepository;
    private final DelinquencyBucketRepository delinquencyBucketRepository;
    private final ExternalIdFactory externalIdFactory;
    private final WorkingCapitalAdvancedPaymentAllocationsJsonParser paymentAllocationParser;
    private final AccountNumberFormatLookup accountNumberFormatLookup;
    private final AccountNumberGeneratorService accountNumberGeneratorService;
    private final WorkingCapitalLoanRepository workingCapitalLoanRepository;

    @Override
    public WorkingCapitalLoan assembleFrom(final JsonCommand command) {
        final JsonElement element = command.parsedJson();

        final Long clientId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.clientIdParameterName, element);
        final Client client = clientRepository.findById(clientId).orElseThrow(() -> new ClientNotFoundException(clientId));

        final Long productId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.productIdParameterName, element);
        final WorkingCapitalLoanProduct product = loanProductRepository.findById(productId)
                .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(productId));

        final Long fundId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.fundIdParameterName, element);
        final Fund fund = fundId != null ? fundRepository.findById(fundId).orElseThrow(() -> new FundNotFoundException(fundId)) : null;

        final String accountNo = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.accountNoParameterName, element);
        final String externalIdStr = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.externalIdParameterName, element);
        final ExternalId externalId = externalIdFactory.create(externalIdStr);

        final BigDecimal principal = fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(WorkingCapitalLoanConstants.principalAmountParamName, element);
        final BigDecimal totalPayment = fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.totalPaymentParamName, element,
                new java.util.HashSet<>());

        final LocalDate submittedOnDate = fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.submittedOnDateParameterName,
                element) ? fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanConstants.submittedOnDateParameterName, element)
                        : DateUtils.getBusinessLocalDate();
        final LocalDate expectedDisbursementDate = fromApiJsonHelper
                .extractLocalDateNamed(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName, element);

        final WorkingCapitalLoanProductRelatedDetails loanProductRelatedDetails = buildLoanProductRelatedDetails(element, product);

        final WorkingCapitalLoan loan = new WorkingCapitalLoan();
        loan.setAccountNumber(accountNo != null ? accountNo : "");
        loan.setExternalId(externalId);
        loan.setClient(client);
        loan.setFund(fund);
        loan.setLoanProduct(product);
        loan.setLoanStatus(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        final Integer currentMaxLoanProductCounter = workingCapitalLoanRepository
                .findMaxLoanProductCounterByClientAndProduct(client.getId(), product.getId());
        final int newLoanProductCounter = currentMaxLoanProductCounter == null ? 1 : currentMaxLoanProductCounter + 1;
        loan.setLoanProductCounter(newLoanProductCounter);
        loan.setLoanCounter(newLoanProductCounter);
        loan.setSubmittedOnDate(submittedOnDate);
        if (expectedDisbursementDate != null) {
            final WorkingCapitalLoanDisbursementDetails detail = new WorkingCapitalLoanDisbursementDetails();
            detail.setWcLoan(loan);
            detail.setExpectedDisbursementDate(expectedDisbursementDate);
            detail.setExpectedAmount(principal);
            loan.getDisbursementDetails().add(detail);
        }
        loan.setProposedPrincipal(principal);
        loan.setApprovedPrincipal(BigDecimal.ZERO);
        final WorkingCapitalLoanBalance balance = WorkingCapitalLoanBalance.createFor(loan);
        balance.setPrincipalOutstanding(principal != null ? principal : BigDecimal.ZERO);
        balance.setTotalPayment(totalPayment != null ? totalPayment : BigDecimal.ZERO);
        loan.setBalance(balance);
        loan.setLoanProductRelatedDetails(loanProductRelatedDetails);

        copyPaymentAllocationRules(loan, command, product);

        return loan;
    }

    private WorkingCapitalLoanProductRelatedDetails buildLoanProductRelatedDetails(final JsonElement element,
            final WorkingCapitalLoanProduct product) {
        final WorkingCapitalLoanProductRelatedDetail productDetail = product.getRelatedDetail();
        final MonetaryCurrency currency = product.getCurrency();

        final WorkingCapitalLoanProductRelatedDetails detail = new WorkingCapitalLoanProductRelatedDetails();
        detail.setCurrency(currency);
        detail.setPrincipal(fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.principalAmountParamName, element)
                ? fromApiJsonHelper.extractBigDecimalWithLocaleNamed(WorkingCapitalLoanConstants.principalAmountParamName, element)
                : productDetail.getPrincipal());
        detail.setPeriodPaymentRate(
                fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element)
                        ? fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element,
                                new java.util.HashSet<>())
                        : productDetail.getPeriodPaymentRate());
        detail.setRepaymentEvery(fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element)
                ? fromApiJsonHelper.extractIntegerWithLocaleNamed(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element)
                : productDetail.getRepaymentEvery());
        detail.setRepaymentFrequencyType(
                fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element)
                        ? WorkingCapitalLoanPeriodFrequencyType.valueOf(fromApiJsonHelper
                                .extractStringNamed(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element))
                        : productDetail.getRepaymentFrequencyType());
        detail.setAmortizationType(productDetail.getAmortizationType());
        detail.setNpvDayCount(productDetail.getNpvDayCount());
        detail.setDiscount(fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.discountParamName, element)
                ? fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.discountParamName, element,
                        new java.util.HashSet<>())
                : productDetail.getDiscount());

        if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, element)) {
            final Long bucketId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName,
                    element);
            detail.setDelinquencyBucket(bucketId != null ? delinquencyBucketRepository.findById(bucketId).orElse(null) : null);
        } else {
            detail.setDelinquencyBucket(product.getDelinquencyBucket());
        }

        return detail;
    }

    private void copyPaymentAllocationRules(final WorkingCapitalLoan loan, final JsonCommand command,
            final WorkingCapitalLoanProduct product) {
        final List<WorkingCapitalLoanPaymentAllocationRule> rules;
        if (command.arrayOfParameterNamed(WorkingCapitalLoanProductConstants.paymentAllocationParamName) != null) {
            final List<WorkingCapitalLoanProductPaymentAllocationRule> productRules = paymentAllocationParser
                    .assembleWCPaymentAllocationRules(command);
            rules = productRules.stream()
                    .map(pr -> new WorkingCapitalLoanPaymentAllocationRule(loan, pr.getTransactionType(), pr.getAllocationTypes()))
                    .toList();
        } else {
            rules = product.getPaymentAllocationRules().stream()
                    .map(pr -> new WorkingCapitalLoanPaymentAllocationRule(loan, pr.getTransactionType(), pr.getAllocationTypes()))
                    .toList();
        }
        loan.getPaymentAllocationRules().clear();
        loan.getPaymentAllocationRules().addAll(rules);
    }

    @Override
    public Map<String, Object> updateFrom(final JsonCommand command, final WorkingCapitalLoan loan) {
        final Map<String, Object> changes = new HashMap<>();
        final JsonElement element = command.parsedJson();

        if (command.isChangeInLongParameterNamed(WorkingCapitalLoanConstants.clientIdParameterName,
                loan.getClient() != null ? loan.getClient().getId() : null)) {
            final Long clientId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.clientIdParameterName, element);
            final Client client = clientRepository.findById(clientId).orElseThrow(() -> new ClientNotFoundException(clientId));
            loan.setClient(client);
            changes.put(WorkingCapitalLoanConstants.clientIdParameterName, clientId);
        }
        if (command.isChangeInLongParameterNamed(WorkingCapitalLoanConstants.productIdParameterName,
                loan.getLoanProduct() != null ? loan.getLoanProduct().getId() : null)) {
            final Long productId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.productIdParameterName, element);
            final WorkingCapitalLoanProduct product = loanProductRepository.findById(productId)
                    .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(productId));
            loan.setLoanProduct(product);
            changes.put(WorkingCapitalLoanConstants.productIdParameterName, productId);
        }
        final Long existingFundId = loan.getFund() != null ? loan.getFund().getId() : null;
        if (command.isChangeInLongParameterNamed(WorkingCapitalLoanConstants.fundIdParameterName, existingFundId)) {
            final Long fundId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.fundIdParameterName, element);
            final Fund fund = fundId != null ? fundRepository.findById(fundId).orElseThrow(() -> new FundNotFoundException(fundId)) : null;
            loan.setFund(fund);
            changes.put(WorkingCapitalLoanConstants.fundIdParameterName, fundId);
        }
        if (command.isChangeInStringParameterNamed(WorkingCapitalLoanConstants.accountNoParameterName, loan.getAccountNumber())) {
            final String accountNo = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.accountNoParameterName, element);
            loan.setAccountNumber(accountNo != null ? accountNo : "");
            changes.put(WorkingCapitalLoanConstants.accountNoParameterName, loan.getAccountNumber());
        }
        if (command.parameterExists(WorkingCapitalLoanConstants.externalIdParameterName)) {
            final ExternalId existing = loan.getExternalId();
            final boolean changed = existing == null
                    ? command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.externalIdParameterName) != null
                    : command.isChangeInExternalIdParameterNamed(WorkingCapitalLoanConstants.externalIdParameterName, existing);
            if (changed) {
                final String externalIdStr = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.externalIdParameterName,
                        element);
                loan.setExternalId(externalIdFactory.create(externalIdStr));
                changes.put(WorkingCapitalLoanConstants.externalIdParameterName, externalIdStr);
            }
        }
        final BigDecimal currentPrincipal = loan.getBalance() != null ? loan.getBalance().getPrincipalOutstanding() : null;
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanConstants.principalAmountParamName, currentPrincipal)) {
            final BigDecimal principal = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(WorkingCapitalLoanConstants.principalAmountParamName, element);
            loan.setProposedPrincipal(principal);
            loan.setApprovedPrincipal(BigDecimal.ZERO);
            ensureBalance(loan).setPrincipalOutstanding(principal != null ? principal : BigDecimal.ZERO);
            changes.put(WorkingCapitalLoanConstants.principalAmountParamName, principal);
        }
        final BigDecimal currentTotalPayment = loan.getBalance() != null ? loan.getBalance().getTotalPayment() : null;
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanConstants.totalPaymentParamName, currentTotalPayment)) {
            final BigDecimal totalPayment = fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.totalPaymentParamName,
                    element, new java.util.HashSet<>());
            ensureBalance(loan).setTotalPayment(totalPayment != null ? totalPayment : BigDecimal.ZERO);
            changes.put(WorkingCapitalLoanConstants.totalPaymentParamName, totalPayment);
        }
        if (command.isChangeInLocalDateParameterNamed(WorkingCapitalLoanConstants.submittedOnDateParameterName,
                loan.getSubmittedOnDate())) {
            final LocalDate submittedOnDate = fromApiJsonHelper
                    .extractLocalDateNamed(WorkingCapitalLoanConstants.submittedOnDateParameterName, element);
            loan.setSubmittedOnDate(submittedOnDate);
            changes.put(WorkingCapitalLoanConstants.submittedOnDateParameterName, submittedOnDate);
        }
        final LocalDate currentExpectedDisbursementDate = loan.getDisbursementDetails().isEmpty() ? null
                : loan.getDisbursementDetails().get(0).getExpectedDisbursementDate();
        if (command.isChangeInLocalDateParameterNamed(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName,
                currentExpectedDisbursementDate)) {
            final LocalDate expectedDisbursementDate = fromApiJsonHelper
                    .extractLocalDateNamed(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName, element);
            if (!loan.getDisbursementDetails().isEmpty()) {
                loan.getDisbursementDetails().getFirst().setExpectedDisbursementDate(expectedDisbursementDate);
            } else if (expectedDisbursementDate != null) {
                final WorkingCapitalLoanDisbursementDetails detail = new WorkingCapitalLoanDisbursementDetails();
                detail.setWcLoan(loan);
                detail.setExpectedDisbursementDate(expectedDisbursementDate);
                loan.getDisbursementDetails().add(detail);
            }
            changes.put(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName, expectedDisbursementDate);
        }

        final WorkingCapitalLoanProductRelatedDetails detail = loan.getLoanProductRelatedDetails();
        if (detail != null) {
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element)
                    && command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName,
                            detail.getPeriodPaymentRate())) {
                final BigDecimal periodPaymentRate = fromApiJsonHelper.extractBigDecimalNamed(
                        WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element, new java.util.HashSet<>());
                detail.setPeriodPaymentRate(periodPaymentRate);
                changes.put(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, periodPaymentRate);
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element)
                    && command.isChangeInIntegerParameterNamed(WorkingCapitalLoanProductConstants.repaymentEveryParamName,
                            detail.getRepaymentEvery())) {
                final Integer repaymentEvery = fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element);
                detail.setRepaymentEvery(repaymentEvery);
                changes.put(WorkingCapitalLoanProductConstants.repaymentEveryParamName, repaymentEvery);
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element)
                    && command.isChangeInStringParameterNamed(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName,
                            detail.getRepaymentFrequencyType().name())) {
                final WorkingCapitalLoanPeriodFrequencyType type = WorkingCapitalLoanPeriodFrequencyType.valueOf(
                        fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element));
                detail.setRepaymentFrequencyType(type);
                changes.put(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, type.name());
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.discountParamName, element)) {
                final BigDecimal discount = fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.discountParamName,
                        element, new java.util.HashSet<>());
                if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.discountParamName,
                        detail.getDiscount())) {
                    detail.setDiscount(discount);
                    changes.put(WorkingCapitalLoanProductConstants.discountParamName, discount);
                }
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, element)) {
                final Long bucketId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName,
                        element);
                final DelinquencyBucket bucket = bucketId != null ? delinquencyBucketRepository.findById(bucketId).orElse(null) : null;
                final Long existingBucketId = detail.getDelinquencyBucket() != null ? detail.getDelinquencyBucket().getId() : null;
                if (!java.util.Objects.equals(bucketId, existingBucketId)) {
                    detail.setDelinquencyBucket(bucket);
                    changes.put(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, bucketId);
                }
            }
        }

        if (command.arrayOfParameterNamed(WorkingCapitalLoanProductConstants.paymentAllocationParamName) != null) {
            copyPaymentAllocationRules(loan, command, loan.getLoanProduct());
            changes.put(WorkingCapitalLoanProductConstants.paymentAllocationParamName,
                    command.arrayOfParameterNamed(WorkingCapitalLoanProductConstants.paymentAllocationParamName));
        }

        return changes;
    }

    /**
     * If accountNo was provided in the request, leave it. Otherwise generate via the same infrastructure as Loan
     * (AccountNumberFormat + AccountNumberGeneratorService for EntityAccountType.WORKING_CAPITAL_LOAN).
     */
    @Override
    public void accountNumberGeneration(final JsonCommand command, final WorkingCapitalLoan loan) {
        final JsonElement element = command.parsedJson();
        final String accountNo = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.accountNoParameterName, element);
        if (!StringUtils.isBlank(accountNo)) {
            return;
        }
        final AccountNumberFormat format = accountNumberFormatLookup.findByAccountType(EntityAccountType.WORKING_CAPITAL_LOAN);
        final String generated = accountNumberGeneratorService.generate(EntityAccountType.WORKING_CAPITAL_LOAN, loan, format);
        loan.setAccountNumber(generated);
    }

    private WorkingCapitalLoanBalance ensureBalance(final WorkingCapitalLoan loan) {
        if (loan.getBalance() == null) {
            final WorkingCapitalLoanBalance balance = WorkingCapitalLoanBalance.createFor(loan);
            balance.setPrincipalOutstanding(BigDecimal.ZERO);
            balance.setTotalPayment(BigDecimal.ZERO);
            loan.setBalance(balance);
        }
        return loan.getBalance();
    }
}
