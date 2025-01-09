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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariations;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanTermVariationsRepository;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Transactional
public class InterestPauseWritePlatformServiceImpl implements InterestPauseWritePlatformService {

    private final LoanTermVariationsRepository loanTermVariationsRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final PlatformSecurityContext context;

    @Override
    public CommandProcessingResult createInterestPause(ExternalId loanExternalId, String startDateString, String endDateString,
            String dateFormat, String locale) {
        final LocalDate startDate = parseDate(startDateString, dateFormat, locale);
        final LocalDate endDate = parseDate(endDateString, dateFormat, locale);

        return processInterestPause(() -> loanRepositoryWrapper.findOneWithNotFoundDetection(loanExternalId), startDate, endDate,
                dateFormat, locale);
    }

    @Override
    public CommandProcessingResult createInterestPause(Long loanId, String startDateString, String endDateString, String dateFormat,
            String locale) {
        final LocalDate startDate = parseDate(startDateString, dateFormat, locale);
        final LocalDate endDate = parseDate(endDateString, dateFormat, locale);

        return processInterestPause(() -> loanRepositoryWrapper.findOneWithNotFoundDetection(loanId), startDate, endDate, dateFormat,
                locale);
    }

    @Override
    public CommandProcessingResult deleteInterestPause(Long loanId, Long variationId) {
        LoanTermVariations variation = loanTermVariationsRepository
                .findByIdAndLoanIdAndTermType(variationId, loanId, INTEREST_PAUSE.getValue())
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.variation.not.found",
                        "Variation not found for the given loan ID"));

        loanTermVariationsRepository.delete(variation);

        return new CommandProcessingResultBuilder().withEntityId(variationId).build();
    }

    @Override
    public CommandProcessingResult updateInterestPause(Long loanId, Long variationId, String startDateString, String endDateString,
            String dateFormat, String locale) {
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);

        LocalDate startDate = parseDate(startDateString, dateFormat, locale);
        LocalDate endDate = parseDate(endDateString, dateFormat, locale);

        validateInterestPauseDates(loan, startDate, endDate, dateFormat, locale);

        LoanTermVariations variation = loanTermVariationsRepository
                .findByIdAndLoanIdAndTermType(variationId, loanId, INTEREST_PAUSE.getValue())
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.variation.not.found",
                        "Variation not found for the given loan ID"));

        validateVariations(loan, variation);

        variation.setTermApplicableFrom(startDate);
        variation.setDateValue(endDate);

        LoanTermVariations updatedVariation = loanTermVariationsRepository.save(variation);

        return new CommandProcessingResultBuilder().withEntityId(updatedVariation.getId())
                .with(Map.of("startDate", startDate.toString(), "endDate", endDate.toString())).build();
    }

    private CommandProcessingResult processInterestPause(Supplier<Loan> loanSupplier, LocalDate startDate, LocalDate endDate,
            String dateFormat, String locale) {
        final Loan loan = loanSupplier.get();

        validateInterestPauseDates(loan, startDate, endDate, dateFormat, locale);

        LoanTermVariations variation = new LoanTermVariations(INTEREST_PAUSE.getValue(), startDate, null, endDate, false, loan);

        LoanTermVariations savedVariation = loanTermVariationsRepository.saveAndFlush(variation);

        return new CommandProcessingResultBuilder().withEntityId(savedVariation.getId()).build();
    }

    private void validateInterestPauseDates(Loan loan, LocalDate startDate, LocalDate endDate, String dateFormat, String locale) {

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

        if (!Objects.equals(loan.getLoanStatus(), ACTIVE.getValue())) {
            throw new GeneralPlatformDomainRuleException("loan.must.be.active",
                    "Operations on interest pauses are restricted to active loans.");
        }

        if (!PROGRESSIVE.equals(loan.getLoanRepaymentScheduleDetail().getLoanScheduleType())) {
            throw new GeneralPlatformDomainRuleException("loan.must.be.progressive",
                    "Interest pause is only supported for progressive loans.");
        }

        if (!loan.getLoanRepaymentScheduleDetail().isInterestRecalculationEnabled()) {
            throw new GeneralPlatformDomainRuleException("loan.must.have.recalculate.interest.enabled",
                    "Interest pause is only supported for loans with recalculate interest enabled.");
        }
    }

    private void validateVariations(Loan loan, LoanTermVariations variation) {
        if (variation == null) {
            throw new GeneralPlatformDomainRuleException("interest.pause.not.found",
                    "The specified interest pause does not exist for the given loan.");
        }

        List<LoanTermVariations> existingVariations = loan.getLoanTermVariations();
        for (LoanTermVariations existingVariation : existingVariations) {
            if (!existingVariation.equals(variation) && variation.getTermApplicableFrom().isBefore(existingVariation.getDateValue())
                    && variation.getDateValue().isAfter(existingVariation.getTermApplicableFrom())) {
                throw new GeneralPlatformDomainRuleException("interest.pause.overlapping", "Overlapping interest pauses are not allowed.");
            }
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
