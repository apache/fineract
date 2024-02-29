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
package org.apache.fineract.portfolio.delinquency.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountDelinquencyPauseChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDelinquencyRangeChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.delinquency.api.DelinquencyApiConstants;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketMappings;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketMappingsRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistory;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import org.apache.fineract.portfolio.delinquency.exception.DelinquencyBucketAgesOverlapedException;
import org.apache.fineract.portfolio.delinquency.exception.DelinquencyRangeInvalidAgesException;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParseAndValidator;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyBucketParseAndValidator;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyRangeParseAndValidator;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class DelinquencyWritePlatformServiceImpl implements DelinquencyWritePlatformService {

    private final DelinquencyBucketParseAndValidator dataValidatorBucket;
    private final DelinquencyRangeParseAndValidator dataValidatorRange;
    private final DelinquencyRangeRepository repositoryRange;
    private final DelinquencyBucketRepository repositoryBucket;
    private final DelinquencyBucketMappingsRepository repositoryBucketMappings;
    private final LoanDelinquencyTagHistoryRepository loanDelinquencyTagRepository;
    private final LoanRepositoryWrapper loanRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanDelinquencyDomainService loanDelinquencyDomainService;
    private final LoanInstallmentDelinquencyTagRepository loanInstallmentDelinquencyTagRepository;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final LoanDelinquencyActionRepository loanDelinquencyActionRepository;
    private final DelinquencyActionParseAndValidator delinquencyActionParseAndValidator;
    private final DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final DelinquencyWritePlatformServiceHelper delinquencyHelper;

    @Override
    public CommandProcessingResult createDelinquencyRange(JsonCommand command) {
        DelinquencyRangeData data = dataValidatorRange.validateAndParseUpdate(command);
        Map<String, Object> changes = new HashMap<>();
        DelinquencyRange delinquencyRange = createDelinquencyRange(data, changes);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(delinquencyRange.getId()).with(changes)
                .build();
    }

    @Override
    public CommandProcessingResult updateDelinquencyRange(Long delinquencyRangeId, JsonCommand command) {
        DelinquencyRangeData data = dataValidatorRange.validateAndParseUpdate(command);
        DelinquencyRange delinquencyRange = this.repositoryRange.getReferenceById(delinquencyRangeId);
        Map<String, Object> changes = new HashMap<>();
        delinquencyRange = updateDelinquencyRange(delinquencyRange, data, changes);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(delinquencyRange.getId()).with(changes)
                .build();
    }

    @Override
    public CommandProcessingResult deleteDelinquencyRange(Long delinquencyRangeId, JsonCommand command) {
        final DelinquencyRange delinquencyRange = repositoryRange.getReferenceById(delinquencyRangeId);
        if (delinquencyRange != null) {
            final Long delinquencyRangeLinked = this.loanDelinquencyTagRepository.countByDelinquencyRange(delinquencyRange);
            if (delinquencyRangeLinked > 0) {
                throw new PlatformDataIntegrityException("error.msg.data.integrity.issue.entity.linked",
                        "Data integrity issue with resource: " + delinquencyRange.getId());
            }
            repositoryRange.delete(delinquencyRange);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(delinquencyRange.getId()).build();
        }
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(delinquencyRangeId).build();
    }

    @Override
    public CommandProcessingResult createDelinquencyBucket(JsonCommand command) {
        DelinquencyBucketData data = dataValidatorBucket.validateAndParseUpdate(command);
        Map<String, Object> changes = new HashMap<>();
        DelinquencyBucket delinquencyBucket = createDelinquencyBucket(data, changes);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(delinquencyBucket.getId()).with(changes)
                .build();
    }

    @Override
    public CommandProcessingResult updateDelinquencyBucket(Long delinquencyBucketId, JsonCommand command) {
        DelinquencyBucketData data = dataValidatorBucket.validateAndParseUpdate(command);
        DelinquencyBucket delinquencyBucket = this.repositoryBucket.getReferenceById(delinquencyBucketId);

        Map<String, Object> changes = new HashMap<>();
        delinquencyBucket = updateDelinquencyBucket(delinquencyBucket, data, changes);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(delinquencyBucket.getId()).with(changes)
                .build();
    }

    @Override
    public CommandProcessingResult deleteDelinquencyBucket(Long delinquencyBucketId, JsonCommand command) {
        final DelinquencyBucket delinquencyBucket = repositoryBucket.getReferenceById(delinquencyBucketId);
        if (delinquencyBucket != null) {
            Long delinquencyBucketLinked = this.loanProductRepository.countByDelinquencyBucket(delinquencyBucket);
            if (delinquencyBucketLinked > 0) {
                throw new PlatformDataIntegrityException("error.msg.data.integrity.issue.entity.linked",
                        "Data integrity issue with resource: " + delinquencyBucket.getId());
            }
            repositoryBucket.delete(delinquencyBucket);
        }
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(delinquencyBucketId).build();
    }

    @Override
    public LoanScheduleDelinquencyData calculateDelinquencyData(LoanScheduleDelinquencyData loanScheduleDelinquencyData,
            List<LoanDelinquencyActionData> effectiveDelinquencyList) {
        Loan loan = loanScheduleDelinquencyData.getLoan();
        if (loan == null) {
            loan = this.loanRepository.findOneWithNotFoundDetection(loanScheduleDelinquencyData.getLoanId());
        }
        CollectionData collectionData = null;
        // If the Loan is not Active yet, return template data
        if (loan.isSubmittedAndPendingApproval() || loan.isApproved()) {
            collectionData = CollectionData.template();
        } else {
            collectionData = loanDelinquencyDomainService.getOverdueCollectionData(loan, effectiveDelinquencyList);
        }
        log.debug("Delinquency {}", collectionData);
        return new LoanScheduleDelinquencyData(loan.getId(), collectionData.getDelinquentDate(), collectionData.getDelinquentDays(), loan);
    }

    @Override
    public CommandProcessingResult applyDelinquencyTagToLoan(Long loanId, JsonCommand command) {
        Map<String, Object> changes = new HashMap<>();

        final Loan loan = this.loanRepository.findOneWithNotFoundDetection(loanId);
        final List<LoanDelinquencyAction> savedDelinquencyList = delinquencyReadPlatformService
                .retrieveLoanDelinquencyActions(loan.getId());
        List<LoanDelinquencyActionData> effectiveDelinquencyList = delinquencyEffectivePauseHelper
                .calculateEffectiveDelinquencyList(savedDelinquencyList);
        final DelinquencyBucket delinquencyBucket = loan.getLoanProduct().getDelinquencyBucket();
        if (delinquencyBucket != null) {
            final LoanDelinquencyData loanDelinquencyData = loanDelinquencyDomainService.getLoanDelinquencyData(loan,
                    effectiveDelinquencyList);
            // loan delinquent data
            final CollectionData collectionData = loanDelinquencyData.getLoanCollectionData();
            // loan installments delinquent data
            final Map<Long, CollectionData> installmentsCollectionData = loanDelinquencyData.getLoanInstallmentsCollectionData();
            log.debug("Delinquency {}", collectionData);

            changes = applyDelinquencyToLoanAndInstallments(loan, delinquencyBucket, collectionData, installmentsCollectionData);

        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loan.getId()) //
                .withEntityExternalId(loan.getExternalId()) //
                .with(changes) //
                .build(); //
    }

    @Override
    public void applyDelinquencyTagToLoan(LoanScheduleDelinquencyData loanDelinquencyData,
            List<LoanDelinquencyActionData> effectiveDelinquencyList) {
        final Loan loan = loanDelinquencyData.getLoan();
        if (loan.hasDelinquencyBucket()) {
            final DelinquencyBucket delinquencyBucket = loan.getLoanProduct().getDelinquencyBucket();
            final LoanDelinquencyData loanDelinquentData = loanDelinquencyDomainService.getLoanDelinquencyData(loan,
                    effectiveDelinquencyList);
            // loan delinquent data
            final CollectionData collectionData = loanDelinquentData.getLoanCollectionData();
            // loan installments delinquent data
            final Map<Long, CollectionData> installmentsCollectionData = loanDelinquentData.getLoanInstallmentsCollectionData();
            log.debug("Delinquency {}", collectionData);

            applyDelinquencyToLoanAndInstallments(loan, delinquencyBucket, collectionData, installmentsCollectionData);
        }
    }

    private Map<String, Object> applyDelinquencyToLoanAndInstallments(Loan loan, DelinquencyBucket delinquencyBucket,
            CollectionData collectionData, Map<Long, CollectionData> installmentsCollectionData) {
        // Order is important: first calculate loan level delinquency, then the installment level
        // delinquency for loan
        Map<String, Object> result = delinquencyHelper.applyDelinquencyForLoan(loan, delinquencyBucket, collectionData.getDelinquentDays());
        // delinquency for installments
        if (!installmentsCollectionData.isEmpty()) {
            delinquencyHelper.applyDelinquencyForLoanInstallments(loan, delinquencyBucket, installmentsCollectionData);
        }
        return result;
    }

    @Override
    @Transactional
    public CommandProcessingResult createDelinquencyAction(Long loanId, JsonCommand command) {
        final Loan loan = this.loanRepository.findOneWithNotFoundDetection(loanId);
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final List<LoanDelinquencyAction> savedDelinquencyList = delinquencyReadPlatformService.retrieveLoanDelinquencyActions(loanId);

        LoanDelinquencyAction parsedDelinquencyAction = delinquencyActionParseAndValidator.validateAndParseUpdate(command, loan,
                savedDelinquencyList, businessDate);

        parsedDelinquencyAction.setLoan(loan);
        LoanDelinquencyAction saved = loanDelinquencyActionRepository.saveAndFlush(parsedDelinquencyAction);
        // if backdated pause recalculate delinquency data
        if (DateUtils.isBefore(parsedDelinquencyAction.getStartDate(), businessDate)
                && DelinquencyAction.PAUSE.equals(parsedDelinquencyAction.getAction())) {
            recalculateLoanDelinquencyData(loan);
            // if pause end date is after current business date, loan delinquency pause flag is changed, emit event
            if (DateUtils.isAfter(parsedDelinquencyAction.getEndDate(), businessDate)) {
                businessEventNotifierService.notifyPostBusinessEvent(new LoanDelinquencyRangeChangeBusinessEvent(loan));
            }
        }
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAccountDelinquencyPauseChangedBusinessEvent(loan));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(saved.getId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .build();
    }

    private void recalculateLoanDelinquencyData(Loan loan) {
        List<LoanDelinquencyAction> savedDelinquencyList = delinquencyReadPlatformService.retrieveLoanDelinquencyActions(loan.getId());
        List<LoanDelinquencyActionData> effectiveDelinquencyList = delinquencyEffectivePauseHelper
                .calculateEffectiveDelinquencyList(savedDelinquencyList);

        CollectionData loanDelinquencyData = loanDelinquencyDomainService.getOverdueCollectionData(loan, effectiveDelinquencyList);
        LoanScheduleDelinquencyData loanScheduleDelinquencyData = new LoanScheduleDelinquencyData(loan.getId(),
                loanDelinquencyData.getDelinquentDate(), loanDelinquencyData.getDelinquentDays(), loan);
        if (loanScheduleDelinquencyData.getOverdueDays() > 0) {
            applyDelinquencyTagToLoan(loanScheduleDelinquencyData, effectiveDelinquencyList);
        } else {
            removeDelinquencyTagToLoan(loan);
        }
    }

    @Override
    public void removeDelinquencyTagToLoan(final Loan loan) {
        if (loan.isEnableInstallmentLevelDelinquency()) {
            cleanLoanInstallmentsDelinquencyTags(loan);
        }
        delinquencyHelper.setLoanDelinquencyTag(loan, null);
    }

    @Override
    public void cleanLoanDelinquencyTags(Loan loan) {
        List<LoanDelinquencyTagHistory> loanDelinquencyTags = this.loanDelinquencyTagRepository.findByLoan(loan);
        if (loanDelinquencyTags != null && loanDelinquencyTags.size() > 0) {
            this.loanDelinquencyTagRepository.deleteAll(loanDelinquencyTags);
        }
    }

    private DelinquencyRange createDelinquencyRange(DelinquencyRangeData data, Map<String, Object> changes) {
        Optional<DelinquencyRange> delinquencyRange = repositoryRange.findByClassification(data.getClassification());

        if (delinquencyRange.isEmpty()) {
            if (data.getMaximumAgeDays() != null && data.getMinimumAgeDays() > data.getMaximumAgeDays()) {
                final String errorMessage = "The age days values are invalid, the maximum age days can't be lower than minimum age days";
                throw new DelinquencyRangeInvalidAgesException(errorMessage, data.getMinimumAgeDays(), data.getMaximumAgeDays());
            }
            DelinquencyRange newDelinquencyRange = DelinquencyRange.instance(data.getClassification(), data.getMinimumAgeDays(),
                    data.getMaximumAgeDays());
            return repositoryRange.saveAndFlush(newDelinquencyRange);
        } else {
            throw new PlatformDataIntegrityException("error.msg.data.integrity.issue.entity.duplicated",
                    "Data integrity issue with resource: " + delinquencyRange.get().getId());
        }
    }

    private DelinquencyRange updateDelinquencyRange(DelinquencyRange delinquencyRange, DelinquencyRangeData data,
            Map<String, Object> changes) {
        if (!data.getClassification().equalsIgnoreCase(delinquencyRange.getClassification())) {
            delinquencyRange.setClassification(data.getClassification());
            changes.put(DelinquencyApiConstants.CLASSIFICATION_PARAM_NAME, data.getClassification());
        }
        if (!data.getMinimumAgeDays().equals(delinquencyRange.getMinimumAgeDays())) {
            delinquencyRange.setMinimumAgeDays(data.getMinimumAgeDays());
            changes.put(DelinquencyApiConstants.MINIMUMAGEDAYS_PARAM_NAME, data.getMinimumAgeDays());
        }
        if (!data.getMaximumAgeDays().equals(delinquencyRange.getMaximumAgeDays())) {
            delinquencyRange.setMaximumAgeDays(data.getMaximumAgeDays());
            changes.put(DelinquencyApiConstants.MAXIMUMAGEDAYS_PARAM_NAME, data.getMaximumAgeDays());
        }
        if (!changes.isEmpty()) {
            delinquencyRange = repositoryRange.saveAndFlush(delinquencyRange);
        }
        return delinquencyRange;
    }

    private DelinquencyBucket createDelinquencyBucket(DelinquencyBucketData data, Map<String, Object> changes) {
        Optional<DelinquencyBucket> delinquencyBucket = repositoryBucket.findByName(data.getName());

        if (delinquencyBucket.isEmpty()) {
            DelinquencyBucket newDelinquencyBucket = new DelinquencyBucket(data.getName());
            repositoryBucket.save(newDelinquencyBucket);
            setDelinquencyBucketMappings(newDelinquencyBucket, data);
            return newDelinquencyBucket;
        } else {
            throw new PlatformDataIntegrityException("error.msg.data.integrity.issue.entity.duplicated",
                    "Data integrity issue with resource: " + delinquencyBucket.get().getId());
        }
    }

    private DelinquencyBucket updateDelinquencyBucket(DelinquencyBucket delinquencyBucket, DelinquencyBucketData data,
            Map<String, Object> changes) {
        if (!data.getName().equalsIgnoreCase(delinquencyBucket.getName())) {
            delinquencyBucket.setName(data.getName());
            changes.put(DelinquencyApiConstants.NAME_PARAM_NAME, data.getName());
        }
        if (!changes.isEmpty()) {
            delinquencyBucket = repositoryBucket.save(delinquencyBucket);
        }
        setDelinquencyBucketMappings(delinquencyBucket, data);
        return delinquencyBucket;
    }

    private void setDelinquencyBucketMappings(DelinquencyBucket delinquencyBucket, DelinquencyBucketData data) {
        List<Long> rangeIds = new ArrayList<>();
        data.getRanges().forEach(dataRange -> {
            rangeIds.add(dataRange.getId());
        });

        List<DelinquencyRange> ranges = repositoryRange.findAllById(rangeIds);
        validateDelinquencyRanges(ranges);
        List<DelinquencyBucketMappings> bucketMappings = repositoryBucketMappings.findByDelinquencyBucket(delinquencyBucket);
        repositoryBucketMappings.deleteAll(bucketMappings);
        bucketMappings.clear();
        for (DelinquencyRange delinquencyRange : ranges) {
            bucketMappings.add(DelinquencyBucketMappings.instance(delinquencyBucket, delinquencyRange));
        }
        repositoryBucketMappings.saveAllAndFlush(bucketMappings);
    }

    private void validateDelinquencyRanges(List<DelinquencyRange> ranges) {
        // Sort the ranges based on the minAgeDays
        ranges = delinquencyHelper.sortDelinquencyRangesByMinAge(ranges);

        DelinquencyRange prevDelinquencyRange = null;
        for (DelinquencyRange delinquencyRange : ranges) {
            if (prevDelinquencyRange != null) {
                if (isOverlapped(prevDelinquencyRange, delinquencyRange)) {
                    final String errorMessage = "The delinquency ranges age days values are overlaped";
                    throw new DelinquencyBucketAgesOverlapedException(errorMessage, prevDelinquencyRange, delinquencyRange);
                }
            }
            prevDelinquencyRange = delinquencyRange;
        }
    }

    private boolean isOverlapped(DelinquencyRange o1, DelinquencyRange o2) {
        if (o2.getMaximumAgeDays() != null) { // Max Age undefined - Last one
            return Math.max(o1.getMinimumAgeDays(), o2.getMinimumAgeDays()) <= Math.min(o1.getMaximumAgeDays(), o2.getMaximumAgeDays());
        } else {
            return Math.max(o1.getMinimumAgeDays(), o2.getMinimumAgeDays()) <= Math.min(o1.getMaximumAgeDays(), o2.getMinimumAgeDays());
        }
    }

    private void cleanLoanInstallmentsDelinquencyTags(Loan loan) {
        loanInstallmentDelinquencyTagRepository.deleteAllLoanInstallmentsTags(loan.getId());
    }
}
