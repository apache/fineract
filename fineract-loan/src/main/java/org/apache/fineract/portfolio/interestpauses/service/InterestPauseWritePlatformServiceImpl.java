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
package org.apache.fineract.portfolio.interestpauses.service;

import static org.apache.fineract.portfolio.loanaccount.domain.LoanStatus.ACTIVE;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType.INTEREST_PAUSE;
import static org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType.PROGRESSIVE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariations;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanTermVariationsRepository;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.ReplayedTransactionBusinessEventService;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Transactional
public class InterestPauseWritePlatformServiceImpl implements InterestPauseWritePlatformService {

    private final LoanTermVariationsRepository loanTermVariationsRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanAssembler loanAssembler;
    private final LoanAccountDomainService loanAccountDomainService;
    private final AccountTransfersService accountTransfersService;
    private final ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService;

    @Override
    public CommandProcessingResult createInterestPause(final ExternalId loanExternalId, final String startDateString,
            final String endDateString, final String dateFormat, final String locale) {
        final LocalDate startDate = parseDate(startDateString, dateFormat, locale);
        final LocalDate endDate = parseDate(endDateString, dateFormat, locale);
        final Loan loan = loanAssembler.assembleFrom(loanExternalId, false);

        return processInterestPause(loan, startDate, endDate, dateFormat, locale);
    }

    @Override
    public CommandProcessingResult createInterestPause(final Long loanId, final String startDateString, final String endDateString,
            final String dateFormat, final String locale) {
        final LocalDate startDate = parseDate(startDateString, dateFormat, locale);
        final LocalDate endDate = parseDate(endDateString, dateFormat, locale);
        final Loan loan = loanAssembler.assembleFrom(loanId, false);

        return processInterestPause(loan, startDate, endDate, dateFormat, locale);
    }

    @Override
    public CommandProcessingResult deleteInterestPause(ExternalId loanExternalId, Long variationId) {
        return processDeleteInterestPause(loanRepositoryWrapper.findOneWithNotFoundDetection(loanExternalId), variationId);
    }

    @Override
    public CommandProcessingResult deleteInterestPause(Long loanId, Long variationId) {
        return processDeleteInterestPause(loanRepositoryWrapper.findOneWithNotFoundDetection(loanId), variationId);
    }

    @Override
    public CommandProcessingResult updateInterestPause(ExternalId loanExternalId, Long variationId, String startDateString,
            String endDateString, String dateFormat, String locale) {
        return processUpdateInterestPause(loanRepositoryWrapper.findOneWithNotFoundDetection(loanExternalId), variationId, startDateString,
                endDateString, dateFormat, locale);
    }

    @Override
    public CommandProcessingResult updateInterestPause(Long loanId, Long variationId, String startDateString, String endDateString,
            String dateFormat, String locale) {
        return processUpdateInterestPause(loanRepositoryWrapper.findOneWithNotFoundDetection(loanId), variationId, startDateString,
                endDateString, dateFormat, locale);
    }

    private CommandProcessingResult processDeleteInterestPause(Loan loan, Long variationId) {
        validateActiveLoan(loan);

        LoanTermVariations variation = loanTermVariationsRepository
                .findByIdAndLoanIdAndTermType(variationId, loan.getId(), INTEREST_PAUSE.getValue())
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.variation.not.found",
                        "Variation not found for the given loan ID"));

        loanTermVariationsRepository.delete(variation);

        return new CommandProcessingResultBuilder().withEntityId(variationId).build();
    }

    private CommandProcessingResult processUpdateInterestPause(Loan loan, Long variationId, String startDateString, String endDateString,
            String dateFormat, String locale) {
        LocalDate startDate = parseDate(startDateString, dateFormat, locale);
        LocalDate endDate = parseDate(endDateString, dateFormat, locale);

        validateActiveLoan(loan);

        LoanTermVariations variation = loanTermVariationsRepository
                .findByIdAndLoanIdAndTermType(variationId, loan.getId(), INTEREST_PAUSE.getValue())
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.variation.not.found",
                        "Variation not found for the given loan ID"));

        validateInterestPauseDates(loan, startDate, endDate, dateFormat, locale, variation.getId());

        variation.setTermApplicableFrom(startDate);
        variation.setDateValue(endDate);

        LoanTermVariations updatedVariation = loanTermVariationsRepository.save(variation);

        return new CommandProcessingResultBuilder().withEntityId(updatedVariation.getId())
                .with(Map.of("startDate", startDate.toString(), "endDate", endDate.toString())).build();
    }

    private CommandProcessingResult processInterestPause(final Loan loan, final LocalDate startDate, final LocalDate endDate,
            String dateFormat, String locale) {
        validateActiveLoan(loan);
        validateInterestPauseDates(loan, startDate, endDate, dateFormat, locale, null);

        final LoanTermVariations variation = new LoanTermVariations(INTEREST_PAUSE.getValue(), startDate, BigDecimal.ZERO, endDate, false,
                loan);

        final LoanTermVariations savedVariation = loanTermVariationsRepository.saveAndFlush(variation);

        loan.getLoanTermVariations().add(savedVariation);
        final ChangedTransactionDetail changedTransactionDetail = loan.reprocessTransactions();
        if (changedTransactionDetail != null) {
            for (final Map.Entry<Long, LoanTransaction> mapEntry : changedTransactionDetail.getNewTransactionMappings().entrySet()) {
                loanAccountDomainService.saveLoanTransactionWithDataIntegrityViolationChecks(mapEntry.getValue());
                accountTransfersService.updateLoanTransaction(mapEntry.getKey(), mapEntry.getValue());
            }
            replayedTransactionBusinessEventService.raiseTransactionReplayedEvents(changedTransactionDetail);
        }

        return new CommandProcessingResultBuilder().withEntityId(savedVariation.getId()).build();
    }

    private void validateInterestPauseDates(Loan loan, LocalDate startDate, LocalDate endDate, String dateFormat, String locale,
            Long currentVariationId) {

        validateOrThrow(baseDataValidator -> {
            baseDataValidator.reset().parameter("startDate").value(startDate).notBlank();
            baseDataValidator.reset().parameter("endDate").value(endDate).notBlank();
            baseDataValidator.reset().parameter("dateFormat").value(dateFormat).notBlank();
            baseDataValidator.reset().parameter("locale").value(locale).notBlank();
        });

        if (startDate.isBefore(loan.getSubmittedOnDate())) {
            throw new GeneralPlatformDomainRuleException("interest.pause.start.date.before.loan.start.date",
                    String.format("Interest pause start date (%s) cannot be earlier than loan start date (%s).", startDate,
                            loan.getSubmittedOnDate()),
                    startDate, loan.getSubmittedOnDate());
        }

        if (endDate.isAfter(loan.getMaturityDate())) {
            throw new GeneralPlatformDomainRuleException("interest.pause.end.date.after.loan.maturity.date", String
                    .format("Interest pause end date (%s) cannot be later than loan maturity date (%s).", endDate, loan.getMaturityDate()),
                    endDate, loan.getMaturityDate());
        }

        if (endDate.isBefore(startDate)) {
            throw new GeneralPlatformDomainRuleException("interest.pause.end.date.before.start.date", String
                    .format("Interest pause end date (%s) must not be before the interest pause start date (%s).", endDate, startDate),
                    endDate, startDate);
        }

        if (!PROGRESSIVE.equals(loan.getLoanRepaymentScheduleDetail().getLoanScheduleType())) {
            throw new GeneralPlatformDomainRuleException("loan.must.be.progressive",
                    "Interest pause is only supported for progressive loans.");
        }

        if (!loan.getLoanRepaymentScheduleDetail().isInterestRecalculationEnabled()) {
            throw new GeneralPlatformDomainRuleException("loan.must.have.recalculate.interest.enabled",
                    "Interest pause is only supported for loans with recalculate interest enabled.");
        }

        List<LoanTermVariations> existingVariations = loan.getLoanTermVariations();
        for (LoanTermVariations existingVariation : existingVariations) {
            if (currentVariationId == null || !existingVariation.getId().equals(currentVariationId)) {
                if (Objects.equals(existingVariation.getTermType().getValue(), INTEREST_PAUSE.getValue())) {
                    if (!(endDate.isBefore(existingVariation.getTermApplicableFrom())
                            || startDate.isAfter(existingVariation.getDateValue()))) {
                        throw new GeneralPlatformDomainRuleException("interest.pause.overlapping",
                                "Overlapping interest pauses are not allowed.");
                    }
                }
            }
        }
    }

    private void validateActiveLoan(Loan loan) {
        if (!Objects.equals(loan.getLoanStatus(), ACTIVE.getValue())) {
            throw new GeneralPlatformDomainRuleException("loan.must.be.active",
                    "Operations on interest pauses are restricted to active loans.");
        }
    }

    private LocalDate parseDate(String date, String dateFormat, String locale) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat, Locale.forLanguageTag(locale));
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new PlatformApiDataValidationException("validation.msg.invalid.date.format",
                    String.format("Invalid date format. Provided: %s, Expected format: %s, Locale: %s", date, dateFormat, locale),
                    e.getMessage(), e);
        }
    }

    private void validateOrThrow(Consumer<DataValidatorBuilder> baseDataValidator) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(dataValidationErrors).resource("InterestPause");

        baseDataValidator.accept(dataValidatorBuilder);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
