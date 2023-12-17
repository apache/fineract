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
import java.util.Collections;
import java.util.Comparator;
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
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTag;
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
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
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
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanDelinquencyDomainService loanDelinquencyDomainService;
    private final LoanInstallmentDelinquencyTagRepository loanInstallmentDelinquencyTagRepository;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final LoanDelinquencyActionRepository loanDelinquencyActionRepository;
    private final DelinquencyActionParseAndValidator delinquencyActionParseAndValidator;
    private final DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;

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
        final CollectionData collectionData = loanDelinquencyDomainService.getOverdueCollectionData(loan, effectiveDelinquencyList);
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
            // delinquency for installments
            if (installmentsCollectionData.size() > 0) {
                applyDelinquencyDetailsForLoanInstallments(loan, delinquencyBucket, installmentsCollectionData);
            }
            // delinquency for loan
            changes = lookUpDelinquencyRange(loan, delinquencyBucket, collectionData.getDelinquentDays());

        }
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loan.getId())
                .withEntityExternalId(loan.getExternalId()).with(changes).build();
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
            // delinquency for installments
            if (installmentsCollectionData.size() > 0) {
                applyDelinquencyDetailsForLoanInstallments(loan, delinquencyBucket, installmentsCollectionData);
            }
            log.debug("Delinquency {}", collectionData);
            // delinquency for loan
            lookUpDelinquencyRange(loan, delinquencyBucket, collectionData.getDelinquentDays());
        }
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
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()) //
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
        setLoanDelinquencyTag(loan, null);
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
        ranges = sortDelinquencyRangesByMinAge(ranges);

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

    private Map<String, Object> lookUpDelinquencyRange(final Loan loan, final DelinquencyBucket delinquencyBucket, long overdueDays) {
        Map<String, Object> changes = new HashMap<>();

        if (overdueDays <= 0) { // No Delinquency
            log.debug("Loan {} without delinquency range with {} days", loan.getId(), overdueDays);
            changes = setLoanDelinquencyTag(loan, null);

        } else {
            // Sort the ranges based on the minAgeDays
            final List<DelinquencyRange> ranges = sortDelinquencyRangesByMinAge(delinquencyBucket.getRanges());

            for (final DelinquencyRange delinquencyRange : ranges) {
                if (delinquencyRange.getMaximumAgeDays() == null) { // Last Range in the Bucket
                    if (delinquencyRange.getMinimumAgeDays() <= overdueDays) {
                        log.debug("Loan {} with delinquency range {} with {} days", loan.getId(), delinquencyRange.getClassification(),
                                overdueDays);
                        changes = setLoanDelinquencyTag(loan, delinquencyRange.getId());
                        break;
                    }
                } else {
                    if (delinquencyRange.getMinimumAgeDays() <= overdueDays && delinquencyRange.getMaximumAgeDays() >= overdueDays) {
                        log.debug("Loan {} with delinquency range {} with {} days", loan.getId(), delinquencyRange.getClassification(),
                                overdueDays);
                        changes = setLoanDelinquencyTag(loan, delinquencyRange.getId());
                        break;
                    }
                }
            }
        }
        changes.put("overdueDays", overdueDays);
        return changes;
    }

    private Map<String, Object> setLoanDelinquencyTag(Loan loan, Long delinquencyRangeId) {
        Map<String, Object> changes = new HashMap<>();
        List<LoanDelinquencyTagHistory> loanDelinquencyTagHistory = new ArrayList<>();
        final LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        Optional<LoanDelinquencyTagHistory> optLoanDelinquencyTag = this.loanDelinquencyTagRepository.findByLoanAndLiftedOnDate(loan, null);
        // The delinquencyRangeId in null means just goes out from Delinquency
        LoanDelinquencyTagHistory loanDelinquencyTagPrev = null;
        if (delinquencyRangeId == null) {
            // The Loan will go out from Delinquency
            if (optLoanDelinquencyTag.isPresent()) {
                loanDelinquencyTagPrev = optLoanDelinquencyTag.get();
                loanDelinquencyTagPrev.setLiftedOnDate(transactionDate);
                loanDelinquencyTagHistory.add(loanDelinquencyTagPrev);
                changes.put("previous", loanDelinquencyTagPrev.getDelinquencyRange());
                // event when loan goes out of delinquency we do not calculate at
                // installment level and remove all installment tags, so event needs to raised here.
                if (loan.isEnableInstallmentLevelDelinquency()) {
                    businessEventNotifierService.notifyPostBusinessEvent(new LoanDelinquencyRangeChangeBusinessEvent(loan));
                }
            }
        } else {
            if (optLoanDelinquencyTag.isPresent()) {
                loanDelinquencyTagPrev = optLoanDelinquencyTag.get();
            }
            // If the Delinquency Tag has not changed
            if (loanDelinquencyTagPrev != null && loanDelinquencyTagPrev.getDelinquencyRange().getId().equals(delinquencyRangeId)) {
                changes.put("current", loanDelinquencyTagPrev.getDelinquencyRange());
            } else {
                // The previous Loan Delinquency Tag will set as Lifted
                if (loanDelinquencyTagPrev != null) {
                    loanDelinquencyTagPrev.setLiftedOnDate(transactionDate);
                    loanDelinquencyTagHistory.add(loanDelinquencyTagPrev);
                    changes.put("previous", loanDelinquencyTagPrev.getDelinquencyRange());
                }

                final DelinquencyRange delinquencyRange = repositoryRange.getReferenceById(delinquencyRangeId);
                LoanDelinquencyTagHistory loanDelinquencyTag = new LoanDelinquencyTagHistory(delinquencyRange, loan, transactionDate, null);
                loanDelinquencyTagHistory.add(loanDelinquencyTag);
                changes.put("current", loanDelinquencyTag.getDelinquencyRange());
            }
        }
        if (loanDelinquencyTagHistory.size() > 0) {
            this.loanDelinquencyTagRepository.saveAllAndFlush(loanDelinquencyTagHistory);
            // if installment level delinquency is enabled event will be raised at installment level calculation, no
            // need to raise the event here
            if (!loan.isEnableInstallmentLevelDelinquency()) {
                businessEventNotifierService.notifyPostBusinessEvent(new LoanDelinquencyRangeChangeBusinessEvent(loan));
            }
        }
        return changes;
    }

    private List<DelinquencyRange> sortDelinquencyRangesByMinAge(List<DelinquencyRange> ranges) {
        final Comparator<DelinquencyRange> orderByMinAge = new Comparator<DelinquencyRange>() {

            @Override
            public int compare(DelinquencyRange o1, DelinquencyRange o2) {
                return o1.getMinimumAgeDays().compareTo(o2.getMinimumAgeDays());
            }
        };
        Collections.sort(ranges, orderByMinAge);
        return ranges;
    }

    private void applyDelinquencyDetailsForLoanInstallments(final Loan loan, final DelinquencyBucket delinquencyBucket,
            final Map<Long, CollectionData> installmentsCollectionData) {
        boolean isDelinquencyRangeChangedForAnyOfInstallment = false;
        for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            if (installmentsCollectionData.containsKey(installment.getId())) {
                boolean isDelinquencySetForInstallment = setInstallmentDelinquencyDetails(loan, installment, delinquencyBucket,
                        installmentsCollectionData.get(installment.getId()));
                isDelinquencyRangeChangedForAnyOfInstallment = isDelinquencyRangeChangedForAnyOfInstallment
                        || isDelinquencySetForInstallment;
            }
        }
        // remove tags for non existing installments that got deleted due to re-schedule
        removeDelinquencyTagsForNonExistingInstallments(loan.getId());
        // raise event if there is any change at installment level delinquency
        if (isDelinquencyRangeChangedForAnyOfInstallment) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanDelinquencyRangeChangeBusinessEvent(loan));
        }

    }

    private boolean setInstallmentDelinquencyDetails(final Loan loan, final LoanRepaymentScheduleInstallment installment,
            final DelinquencyBucket delinquencyBucket, final CollectionData installmentDelinquencyData) {
        DelinquencyRange delinquencyRangeForInstallment = getInstallmentDelinquencyRange(delinquencyBucket,
                installmentDelinquencyData.getDelinquentDays());
        return setDelinquencyDetailsForInstallment(loan, installment, installmentDelinquencyData, delinquencyRangeForInstallment);
    }

    private DelinquencyRange getInstallmentDelinquencyRange(final DelinquencyBucket delinquencyBucket, Long overDueDays) {
        DelinquencyRange delinquencyRangeForInstallment = null;
        if (overDueDays > 0) {
            // Sort the ranges based on the minAgeDays
            final List<DelinquencyRange> ranges = sortDelinquencyRangesByMinAge(delinquencyBucket.getRanges());
            for (final DelinquencyRange delinquencyRange : ranges) {
                if (delinquencyRange.getMaximumAgeDays() == null) { // Last Range in the Bucket
                    if (delinquencyRange.getMinimumAgeDays() <= overDueDays) {
                        delinquencyRangeForInstallment = delinquencyRange;
                        break;
                    }
                } else {
                    if (delinquencyRange.getMinimumAgeDays() <= overDueDays && delinquencyRange.getMaximumAgeDays() >= overDueDays) {
                        delinquencyRangeForInstallment = delinquencyRange;
                        break;
                    }
                }
            }

        }
        return delinquencyRangeForInstallment;
    }

    private boolean setDelinquencyDetailsForInstallment(final Loan loan, final LoanRepaymentScheduleInstallment installment,
            CollectionData installmentDelinquencyData, final DelinquencyRange delinquencyRangeForInstallment) {
        List<LoanInstallmentDelinquencyTag> installmentDelinquencyTags = new ArrayList<>();
        LocalDate delinquencyCalculationDate = DateUtils.getBusinessLocalDate();
        boolean isDelinquencyRangeChanged = false;

        LoanInstallmentDelinquencyTag previousInstallmentDelinquencyTag = loanInstallmentDelinquencyTagRepository
                .findByLoanAndInstallment(loan, installment).orElse(null);

        if (delinquencyRangeForInstallment == null) {
            // if currentInstallmentDelinquencyTag exists and range is null, installment is out of delinquency, delete
            // delinquency details
            if (previousInstallmentDelinquencyTag != null) {
                // event installment out of delinquency
                loanInstallmentDelinquencyTagRepository.delete(previousInstallmentDelinquencyTag);
                isDelinquencyRangeChanged = true;
            }
        } else {
            LoanInstallmentDelinquencyTag installmentDelinquency = null;
            if (previousInstallmentDelinquencyTag != null) {
                if (!previousInstallmentDelinquencyTag.getDelinquencyRange().getId().equals(delinquencyRangeForInstallment.getId())) {
                    // if current delinquency range exists and there is range change, delete previous delinquency
                    // details and add new range details
                    installmentDelinquency = new LoanInstallmentDelinquencyTag(delinquencyRangeForInstallment, loan, installment,
                            delinquencyCalculationDate, null, previousInstallmentDelinquencyTag.getFirstOverdueDate(),
                            installmentDelinquencyData.getDelinquentAmount());
                    loanInstallmentDelinquencyTagRepository.delete(previousInstallmentDelinquencyTag);
                    // event installment delinquency range change
                    isDelinquencyRangeChanged = true;
                } else {
                    previousInstallmentDelinquencyTag.setOutstandingAmount(installmentDelinquencyData.getDelinquentAmount());
                    installmentDelinquency = previousInstallmentDelinquencyTag;
                }
            } else {
                // add new range, first time delinquent
                installmentDelinquency = new LoanInstallmentDelinquencyTag(delinquencyRangeForInstallment, loan, installment,
                        delinquencyCalculationDate, null, installmentDelinquencyData.getDelinquentDate(),
                        installmentDelinquencyData.getDelinquentAmount());
                // event installment delinquent
                isDelinquencyRangeChanged = true;
            }

            if (installmentDelinquency != null) {
                installmentDelinquencyTags.add(installmentDelinquency);
            }

        }

        if (installmentDelinquencyTags.size() > 0) {
            loanInstallmentDelinquencyTagRepository.saveAllAndFlush(installmentDelinquencyTags);
        }
        return isDelinquencyRangeChanged;
    }

    private void cleanLoanInstallmentsDelinquencyTags(Loan loan) {
        loanInstallmentDelinquencyTagRepository.deleteAllLoanInstallmentsTags(loan.getId());
    }

    private void removeDelinquencyTagsForNonExistingInstallments(Long loanId) {
        List<LoanInstallmentDelinquencyTag> currentLoanInstallmentDelinquencyTags = loanInstallmentDelinquencyTagRepository
                .findByLoanId(loanId);
        if (currentLoanInstallmentDelinquencyTags != null && currentLoanInstallmentDelinquencyTags.size() > 0) {
            List<Long> loanInstallmentTagsForDelete = currentLoanInstallmentDelinquencyTags.stream()
                    .filter(tag -> tag.getInstallment() == null).map(tag -> tag.getId()).toList();
            if (loanInstallmentTagsForDelete.size() > 0) {
                loanInstallmentDelinquencyTagRepository.deleteAllLoanInstallmentsTagsByIds(loanInstallmentTagsForDelete);
            }
        }
    }
}
