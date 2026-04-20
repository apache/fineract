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
package org.apache.fineract.portfolio.account.service;

import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.statusParamName;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.StandingInstructionCreateRequest;
import org.apache.fineract.portfolio.account.data.StandingInstructionCreateResponse;
import org.apache.fineract.portfolio.account.data.StandingInstructionDeleteRequest;
import org.apache.fineract.portfolio.account.data.StandingInstructionUpdateRequest;
import org.apache.fineract.portfolio.account.data.StandingInstructionUpdateResponse;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.StandingInstructionAssembler;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.exception.StandingInstructionNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class StandingInstructionWritePlatformServiceImpl implements StandingInstructionWritePlatformService {

    private final StandingInstructionAssembler standingInstructionAssembler;
    private final AccountTransferDetailRepository accountTransferDetailRepository;
    private final StandingInstructionRepository standingInstructionRepository;

    @Transactional
    @Override
    public StandingInstructionCreateResponse create(final StandingInstructionCreateRequest request) {
        final PortfolioAccountType fromAccountType = PortfolioAccountType.fromInt(request.getFromAccountType());
        final PortfolioAccountType toAccountType = PortfolioAccountType.fromInt(request.getToAccountType());

        Long standingInstructionId = null;
        try {
            if (isSavingsToSavingsAccountTransfer(fromAccountType, toAccountType)) {
                final AccountTransferDetails standingInstruction = this.standingInstructionAssembler
                        .assembleSavingsToSavingsTransfer(request);
                this.accountTransferDetailRepository.saveAndFlush(standingInstruction);
                standingInstructionId = standingInstruction.accountTransferStandingInstruction().getId();
            } else if (isSavingsToLoanAccountTransfer(fromAccountType, toAccountType)) {
                final AccountTransferDetails standingInstruction = this.standingInstructionAssembler.assembleSavingsToLoanTransfer(request);
                this.accountTransferDetailRepository.saveAndFlush(standingInstruction);
                standingInstructionId = standingInstruction.accountTransferStandingInstruction().getId();
            } else if (isLoanToSavingsAccountTransfer(fromAccountType, toAccountType)) {
                final AccountTransferDetails standingInstruction = this.standingInstructionAssembler.assembleLoanToSavingsTransfer(request);
                this.accountTransferDetailRepository.saveAndFlush(standingInstruction);
                standingInstructionId = standingInstruction.accountTransferStandingInstruction().getId();
            }
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleDataIntegrityIssues(request.getName(), throwable, dve);
            return StandingInstructionCreateResponse.builder().build();
        }

        return StandingInstructionCreateResponse.builder().resourceId(standingInstructionId).clientId(request.getFromClientId()).build();
    }

    @Override
    public StandingInstructionUpdateResponse update(final StandingInstructionUpdateRequest request) {
        final AccountTransferStandingInstruction standingInstruction = this.standingInstructionRepository.findById(request.getId())
                .orElseThrow(() -> new StandingInstructionNotFoundException(request.getId()));

        final Locale locale = parseLocale(request.getLocale());
        final DateTimeFormatter dateFmt = parseDateFormat(request.getDateFormat(), locale);

        final LocalDate validFrom = StringUtils.isBlank(request.getValidFrom()) ? null : LocalDate.parse(request.getValidFrom(), dateFmt);
        final LocalDate validTill = StringUtils.isBlank(request.getValidTill()) ? null : LocalDate.parse(request.getValidTill(), dateFmt);

        MonthDay recurrenceOnMonthDay = null;
        if (!StringUtils.isBlank(request.getRecurrenceOnMonthDay())) {
            final DateTimeFormatter monthDayFmt = parseMonthDayFormat(request.getMonthDayFormat(), locale);
            recurrenceOnMonthDay = MonthDay.parse(request.getRecurrenceOnMonthDay(), monthDayFmt);
        }

        final Map<String, Object> actualChanges = standingInstruction.update(validFrom, validTill, request.getAmount(), request.getStatus(),
                request.getPriority(), request.getInstructionType(), request.getRecurrenceType(), request.getRecurrenceFrequency(),
                request.getRecurrenceInterval(), recurrenceOnMonthDay);

        return StandingInstructionUpdateResponse.builder().resourceId(request.getId()).changes(actualChanges).build();
    }

    @Override
    public StandingInstructionUpdateResponse delete(final StandingInstructionDeleteRequest request) {
        final AccountTransferStandingInstruction standingInstruction = this.standingInstructionRepository.findById(request.getId())
                .orElseThrow();
        standingInstruction.delete();

        final Map<String, Object> actualChanges = new HashMap<>();
        actualChanges.put(statusParamName, StandingInstructionStatus.DELETED.getValue());
        return StandingInstructionUpdateResponse.builder().resourceId(request.getId()).changes(actualChanges).build();
    }

    private void handleDataIntegrityIssues(final String name, final Throwable realCause, final NonTransientDataAccessException dve) {
        if (realCause.getMessage().contains("name")) {
            throw new PlatformDataIntegrityException("error.msg.standinginstruction.duplicate.name",
                    "Standinginstruction with name `" + name + "` already exists", "name", name);
        }
        log.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    private boolean isLoanToSavingsAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return PortfolioAccountType.LOAN.equals(fromAccountType) && PortfolioAccountType.SAVINGS.equals(toAccountType);
    }

    private boolean isSavingsToLoanAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return PortfolioAccountType.SAVINGS.equals(fromAccountType) && PortfolioAccountType.LOAN.equals(toAccountType);
    }

    private boolean isSavingsToSavingsAccountTransfer(final PortfolioAccountType fromAccountType,
            final PortfolioAccountType toAccountType) {
        return PortfolioAccountType.SAVINGS.equals(fromAccountType) && PortfolioAccountType.SAVINGS.equals(toAccountType);
    }

    private static Locale parseLocale(final String localeStr) {
        return StringUtils.isBlank(localeStr) ? Locale.getDefault() : JsonParserHelper.localeFromString(localeStr);
    }

    private static DateTimeFormatter parseDateFormat(final String dateFormat, final Locale locale) {
        return StringUtils.isBlank(dateFormat) ? DateTimeFormatter.ISO_LOCAL_DATE.withLocale(locale)
                : DateTimeFormatter.ofPattern(dateFormat).withLocale(locale);
    }

    private static DateTimeFormatter parseMonthDayFormat(final String monthDayFormat, final Locale locale) {
        return StringUtils.isBlank(monthDayFormat) ? DateTimeFormatter.ofPattern("--MM-dd").withLocale(locale)
                : DateTimeFormatter.ofPattern(monthDayFormat).withLocale(locale);
    }
}
