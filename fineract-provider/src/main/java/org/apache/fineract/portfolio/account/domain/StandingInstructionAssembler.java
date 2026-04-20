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
package org.apache.fineract.portfolio.account.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.account.data.StandingInstructionCreateRequest;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StandingInstructionAssembler {

    private final AccountTransferDetailAssembler accountTransferDetailAssembler;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final LoanAssembler loanAssembler;

    @Autowired
    public StandingInstructionAssembler(final AccountTransferDetailAssembler accountTransferDetailAssembler,
            final SavingsAccountAssembler savingsAccountAssembler, final LoanAssembler loanAssembler) {
        this.accountTransferDetailAssembler = accountTransferDetailAssembler;
        this.savingsAccountAssembler = savingsAccountAssembler;
        this.loanAssembler = loanAssembler;
    }

    public AccountTransferDetails assembleSavingsToSavingsTransfer(final StandingInstructionCreateRequest request) {
        final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(request.getFromAccountId(), false);
        final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(request.getToAccountId(), false);
        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler
                .assembleSavingsToSavingsTransfer(fromSavingsAccount, toSavingsAccount, request.getTransferType());
        assembleStandingInstruction(request, accountTransferDetails);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleSavingsToLoanTransfer(final StandingInstructionCreateRequest request) {
        final SavingsAccount fromSavingsAccount = this.savingsAccountAssembler.assembleFrom(request.getFromAccountId(), false);
        final Loan toLoanAccount = this.loanAssembler.assembleFrom(request.getToAccountId());
        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler
                .assembleSavingsToLoanTransfer(fromSavingsAccount, toLoanAccount, request.getTransferType());
        assembleStandingInstruction(request, accountTransferDetails);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleLoanToSavingsTransfer(final StandingInstructionCreateRequest request) {
        final Loan fromLoanAccount = this.loanAssembler.assembleFrom(request.getFromAccountId());
        final SavingsAccount toSavingsAccount = this.savingsAccountAssembler.assembleFrom(request.getToAccountId(), false);
        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler
                .assembleLoanToSavingsTransfer(fromLoanAccount, toSavingsAccount, request.getTransferType());
        assembleStandingInstruction(request, accountTransferDetails);
        return accountTransferDetails;
    }

    public void assembleStandingInstruction(final StandingInstructionCreateRequest request,
            final AccountTransferDetails accountTransferDetails) {
        final Locale locale = parseLocale(request.getLocale());
        final DateTimeFormatter dateFmt = parseDateFormat(request.getDateFormat(), locale);

        final LocalDate validFrom = LocalDate.parse(request.getValidFrom(), dateFmt);
        final LocalDate validTill = StringUtils.isBlank(request.getValidTill()) ? null : LocalDate.parse(request.getValidTill(), dateFmt);

        BigDecimal amount = null;
        if (request.getAmount() != null && accountTransferDetails.fromSavingsAccount() != null) {
            final Money monetaryAmount = Money.of(accountTransferDetails.fromSavingsAccount().getCurrency(), request.getAmount());
            amount = monetaryAmount.getAmount();
        }

        MonthDay recurrenceOnMonthDay = null;
        if (!StringUtils.isBlank(request.getRecurrenceOnMonthDay())) {
            final DateTimeFormatter monthDayFmt = parseMonthDayFormat(request.getMonthDayFormat(), locale);
            recurrenceOnMonthDay = MonthDay.parse(request.getRecurrenceOnMonthDay(), monthDayFmt);
        }

        AccountTransferStandingInstruction standingInstruction = AccountTransferStandingInstruction.create(accountTransferDetails,
                request.getName(), request.getPriority(), request.getInstructionType(), request.getStatus(), amount, validFrom, validTill,
                request.getRecurrenceType(), request.getRecurrenceFrequency(), request.getRecurrenceInterval(), recurrenceOnMonthDay);
        accountTransferDetails.updateAccountTransferStandingInstruction(standingInstruction);
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
