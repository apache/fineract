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
package org.apache.fineract.portfolio.account.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Optional;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

/**
 * Immutable data object representing a savings account.
 */
@SuppressWarnings("unused")
public final class StandingInstructionData {

    @Getter
    private final Long id;
    @Getter
    private final Long accountDetailId;
    @Getter
    private final String name;
    private final OfficeData fromOffice;
    @Getter
    private final ClientData fromClient;
    private final EnumOptionData fromAccountType;
    @Getter
    private final PortfolioAccountData fromAccount;
    private final OfficeData toOffice;
    @Getter
    private final ClientData toClient;
    private final EnumOptionData toAccountType;
    @Getter
    private final PortfolioAccountData toAccount;
    private final EnumOptionData transferType;
    private final EnumOptionData priority;
    private final EnumOptionData instructionType;
    private final EnumOptionData status;
    @Getter
    private final BigDecimal amount;
    @Getter
    private final LocalDate validFrom;
    private final LocalDate validTill;
    private final EnumOptionData recurrenceType;
    private final EnumOptionData recurrenceFrequency;
    @Getter
    private final Integer recurrenceInterval;
    private final MonthDay recurrenceOnMonthDay;
    private final Page<AccountTransferData> transactions;

    private final Collection<OfficeData> fromOfficeOptions;
    private final Collection<ClientData> fromClientOptions;
    private final Collection<EnumOptionData> fromAccountTypeOptions;
    private final Collection<PortfolioAccountData> fromAccountOptions;
    private final Collection<OfficeData> toOfficeOptions;
    private final Collection<ClientData> toClientOptions;
    private final Collection<EnumOptionData> toAccountTypeOptions;
    private final Collection<PortfolioAccountData> toAccountOptions;
    private final Collection<EnumOptionData> transferTypeOptions;
    private final Collection<EnumOptionData> statusOptions;
    private final Collection<EnumOptionData> instructionTypeOptions;
    private final Collection<EnumOptionData> priorityOptions;
    private final Collection<EnumOptionData> recurrenceTypeOptions;
    private final Collection<EnumOptionData> recurrenceFrequencyOptions;

    public static StandingInstructionData template(final Collection<OfficeData> fromOfficeOptions,
            final Collection<ClientData> fromClientOptions, final Collection<EnumOptionData> fromAccountTypeOptions,
            final Collection<PortfolioAccountData> fromAccountOptions, final Collection<OfficeData> toOfficeOptions,
            final Collection<ClientData> toClientOptions, final Collection<EnumOptionData> toAccountTypeOptions,
            final Collection<PortfolioAccountData> toAccountOptions, final Collection<EnumOptionData> transferTypeOptions,
            final Collection<EnumOptionData> statusOptions, final Collection<EnumOptionData> instructionTypeOptions,
            final Collection<EnumOptionData> priorityOptions, final Collection<EnumOptionData> recurrenceTypeOptions,
            final Collection<EnumOptionData> recurrenceFrequencyOptions) {

        final Long id = null;
        final Long accountDetailId = null;
        final String name = null;
        final OfficeData fromOffice = null;
        final OfficeData toOffice = null;
        final ClientData fromClient = null;
        final ClientData toClient = null;
        final EnumOptionData fromAccountType = null;
        final PortfolioAccountData fromAccount = null;
        final EnumOptionData toAccountType = null;
        final PortfolioAccountData toAccount = null;
        final EnumOptionData transferType = null;
        final EnumOptionData priority = null;
        final EnumOptionData instructionType = null;
        final EnumOptionData status = null;
        final BigDecimal amount = null;
        final LocalDate validFrom = null;
        final LocalDate validTill = null;
        final EnumOptionData recurrenceType = null;
        final EnumOptionData recurrenceFrequency = null;
        final Integer recurrenceInterval = null;
        final MonthDay recurrenceOnMonthDay = null;
        final Page<AccountTransferData> transactions = null;

        return new StandingInstructionData(id, accountDetailId, name, fromOffice, fromClient, fromAccountType, fromAccount, toOffice,
                toClient, toAccountType, toAccount, transferType, priority, instructionType, status, amount, validFrom, validTill,
                recurrenceType, recurrenceFrequency, recurrenceInterval, recurrenceOnMonthDay, transactions, fromOfficeOptions,
                fromClientOptions, fromAccountTypeOptions, fromAccountOptions, toOfficeOptions, toClientOptions, toAccountTypeOptions,
                toAccountOptions, transferTypeOptions, statusOptions, instructionTypeOptions, priorityOptions, recurrenceTypeOptions,
                recurrenceFrequencyOptions);
    }

    public static StandingInstructionData instance(final Long id, final Long accountDetailId, final String name,
            final OfficeData fromOffice, final OfficeData toOffice, final ClientData fromClient, final ClientData toClient,
            final EnumOptionData fromAccountType, final PortfolioAccountData fromAccount, final EnumOptionData toAccountType,
            final PortfolioAccountData toAccount, final EnumOptionData transferType, final EnumOptionData priority,
            final EnumOptionData instructionType, final EnumOptionData status, final BigDecimal amount, final LocalDate validFrom,
            final LocalDate validTill, final EnumOptionData recurrenceType, final EnumOptionData recurrenceFrequency,
            final Integer recurrenceInterval, final MonthDay recurrenceOnMonthDay) {
        final Page<AccountTransferData> transactions = null;
        final Collection<OfficeData> fromOfficeOptions = null;
        final Collection<ClientData> fromClientOptions = null;
        final Collection<EnumOptionData> fromAccountTypeOptions = null;
        final Collection<PortfolioAccountData> fromAccountOptions = null;
        final Collection<OfficeData> toOfficeOptions = null;
        final Collection<ClientData> toClientOptions = null;
        final Collection<EnumOptionData> toAccountTypeOptions = null;
        final Collection<PortfolioAccountData> toAccountOptions = null;
        final Collection<EnumOptionData> transferTypeOptions = null;
        final Collection<EnumOptionData> statusOptions = null;
        final Collection<EnumOptionData> instructionTypeOptions = null;
        final Collection<EnumOptionData> priorityOptions = null;
        final Collection<EnumOptionData> recurrenceTypeOptions = null;
        final Collection<EnumOptionData> recurrenceFrequencyOptions = null;

        return new StandingInstructionData(id, accountDetailId, name, fromOffice, fromClient, fromAccountType, fromAccount, toOffice,
                toClient, toAccountType, toAccount, transferType, priority, instructionType, status, amount, validFrom, validTill,
                recurrenceType, recurrenceFrequency, recurrenceInterval, recurrenceOnMonthDay, transactions, fromOfficeOptions,
                fromClientOptions, fromAccountTypeOptions, fromAccountOptions, toOfficeOptions, toClientOptions, toAccountTypeOptions,
                toAccountOptions, transferTypeOptions, statusOptions, instructionTypeOptions, priorityOptions, recurrenceTypeOptions,
                recurrenceFrequencyOptions);
    }

    public static StandingInstructionData withTemplateData(StandingInstructionData instructionData, StandingInstructionData templateData) {
        return new StandingInstructionData(instructionData.id, instructionData.accountDetailId, instructionData.name,
                instructionData.fromOffice, instructionData.fromClient, instructionData.fromAccountType, instructionData.fromAccount,
                instructionData.toOffice, instructionData.toClient, instructionData.toAccountType, instructionData.toAccount,
                instructionData.transferType, instructionData.priority, instructionData.instructionType, instructionData.status,
                instructionData.amount, instructionData.validFrom, instructionData.validTill, instructionData.recurrenceType,
                instructionData.recurrenceFrequency, instructionData.recurrenceInterval, instructionData.recurrenceOnMonthDay,
                instructionData.transactions, templateData.fromOfficeOptions, templateData.fromClientOptions,
                templateData.fromAccountTypeOptions, templateData.fromAccountOptions, templateData.toOfficeOptions,
                templateData.toClientOptions, templateData.toAccountTypeOptions, templateData.toAccountOptions,
                templateData.transferTypeOptions, templateData.statusOptions, templateData.instructionTypeOptions,
                templateData.priorityOptions, templateData.recurrenceTypeOptions, templateData.recurrenceFrequencyOptions);
    }

    private StandingInstructionData(final Long id, final Long accountDetailId, final String name, final OfficeData fromOffice,
            final ClientData fromClient, final EnumOptionData fromAccountType, final PortfolioAccountData fromAccount,
            final OfficeData toOffice, final ClientData toClient, final EnumOptionData toAccountType, final PortfolioAccountData toAccount,
            final EnumOptionData transferType, final EnumOptionData priority, final EnumOptionData instructionType,
            final EnumOptionData status, final BigDecimal amount, final LocalDate validFrom, LocalDate validTill,
            final EnumOptionData recurrenceType, final EnumOptionData recurrenceFrequency, final Integer recurrenceInterval,
            final MonthDay recurrenceOnMonthDay, final Page<AccountTransferData> transactions,
            final Collection<OfficeData> fromOfficeOptions, final Collection<ClientData> fromClientOptions,
            final Collection<EnumOptionData> fromAccountTypeOptions, final Collection<PortfolioAccountData> fromAccountOptions,
            final Collection<OfficeData> toOfficeOptions, final Collection<ClientData> toClientOptions,
            final Collection<EnumOptionData> toAccountTypeOptions, final Collection<PortfolioAccountData> toAccountOptions,
            final Collection<EnumOptionData> transferTypeOptions, final Collection<EnumOptionData> statusOptions,
            final Collection<EnumOptionData> instructionTypeOptions, final Collection<EnumOptionData> priorityOptions,
            final Collection<EnumOptionData> recurrenceTypeOptions, final Collection<EnumOptionData> recurrenceFrequencyOptions) {
        this.id = id;
        this.accountDetailId = accountDetailId;
        this.name = name;
        this.fromOffice = fromOffice;
        this.fromClient = fromClient;
        this.fromAccountType = fromAccountType;
        this.fromAccount = fromAccount;
        this.toOffice = toOffice;
        this.toClient = toClient;
        this.toAccountType = toAccountType;
        this.toAccount = toAccount;
        this.transferType = transferType;

        this.priority = priority;
        this.instructionType = instructionType;
        this.status = status;
        this.amount = amount;
        this.validFrom = validFrom;
        this.validTill = validTill;
        this.recurrenceType = recurrenceType;
        this.recurrenceFrequency = recurrenceFrequency;
        this.recurrenceInterval = recurrenceInterval;
        this.recurrenceOnMonthDay = recurrenceOnMonthDay;

        this.fromOfficeOptions = fromOfficeOptions;
        this.fromClientOptions = fromClientOptions;
        this.fromAccountTypeOptions = fromAccountTypeOptions;
        this.fromAccountOptions = fromAccountOptions;
        this.toOfficeOptions = toOfficeOptions;
        this.toClientOptions = toClientOptions;
        this.toAccountTypeOptions = toAccountTypeOptions;
        this.toAccountOptions = toAccountOptions;
        this.transferTypeOptions = transferTypeOptions;
        this.statusOptions = statusOptions;
        this.instructionTypeOptions = instructionTypeOptions;
        this.priorityOptions = priorityOptions;
        this.recurrenceTypeOptions = recurrenceTypeOptions;
        this.recurrenceFrequencyOptions = recurrenceFrequencyOptions;
        this.transactions = transactions;
    }

    public static StandingInstructionData template(OfficeData fromOffice, ClientData fromClient, EnumOptionData fromAccountType,
            PortfolioAccountData fromAccount, LocalDate transferDate, OfficeData toOffice, ClientData toClient,
            EnumOptionData toAccountType, PortfolioAccountData toAccount, final Collection<OfficeData> fromOfficeOptions,
            final Collection<ClientData> fromClientOptions, final Collection<EnumOptionData> fromAccountTypeOptions,
            final Collection<PortfolioAccountData> fromAccountOptions, final Collection<OfficeData> toOfficeOptions,
            final Collection<ClientData> toClientOptions, final Collection<EnumOptionData> toAccountTypeOptions,
            final Collection<PortfolioAccountData> toAccountOptions, final Collection<EnumOptionData> transferTypeOptions,
            final Collection<EnumOptionData> statusOptions, final Collection<EnumOptionData> instructionTypeOptions,
            final Collection<EnumOptionData> priorityOptions, final Collection<EnumOptionData> recurrenceTypeOptions,
            final Collection<EnumOptionData> recurrenceFrequencyOptions) {
        final Long id = null;
        final Long accountDetailId = null;
        final String name = null;
        final EnumOptionData transferType = null;
        final EnumOptionData priority = null;
        final EnumOptionData instructionType = null;
        final EnumOptionData status = null;
        final BigDecimal amount = null;
        final LocalDate validFrom = null;
        final LocalDate validTill = null;
        final EnumOptionData recurrenceType = null;
        final EnumOptionData recurrenceFrequency = null;
        final Integer recurrenceInterval = null;
        final MonthDay recurrenceOnMonthDay = null;
        final Page<AccountTransferData> transactions = null;

        return new StandingInstructionData(id, accountDetailId, name, fromOffice, fromClient, fromAccountType, fromAccount, toOffice,
                toClient, toAccountType, toAccount, transferType, priority, instructionType, status, amount, validFrom, validTill,
                recurrenceType, recurrenceFrequency, recurrenceInterval, recurrenceOnMonthDay, transactions, fromOfficeOptions,
                fromClientOptions, fromAccountTypeOptions, fromAccountOptions, toOfficeOptions, toClientOptions, toAccountTypeOptions,
                toAccountOptions, transferTypeOptions, statusOptions, instructionTypeOptions, priorityOptions, recurrenceTypeOptions,
                recurrenceFrequencyOptions);
    }

    public static StandingInstructionData withTransferData(StandingInstructionData instructionData,
            final Page<AccountTransferData> transactions) {
        return new StandingInstructionData(instructionData.id, instructionData.accountDetailId, instructionData.name,
                instructionData.fromOffice, instructionData.fromClient, instructionData.fromAccountType, instructionData.fromAccount,
                instructionData.toOffice, instructionData.toClient, instructionData.toAccountType, instructionData.toAccount,
                instructionData.transferType, instructionData.priority, instructionData.instructionType, instructionData.status,
                instructionData.amount, instructionData.validFrom, instructionData.validTill, instructionData.recurrenceType,
                instructionData.recurrenceFrequency, instructionData.recurrenceInterval, instructionData.recurrenceOnMonthDay, transactions,
                instructionData.fromOfficeOptions, instructionData.fromClientOptions, instructionData.fromAccountTypeOptions,
                instructionData.fromAccountOptions, instructionData.toOfficeOptions, instructionData.toClientOptions,
                instructionData.toAccountTypeOptions, instructionData.toAccountOptions, instructionData.transferTypeOptions,
                instructionData.statusOptions, instructionData.instructionTypeOptions, instructionData.priorityOptions,
                instructionData.recurrenceTypeOptions, instructionData.recurrenceFrequencyOptions);
    }

    public StandingInstructionType getInstructionType() {
        return Optional.ofNullable(this.instructionType).map(e -> StandingInstructionType.fromInt(e.getId().intValue())).orElse(null);

    }

    public AccountTransferRecurrenceType getRecurrenceType() {
        return Optional.ofNullable(this.recurrenceType).map(e -> AccountTransferRecurrenceType.fromInt(e.getId().intValue())).orElse(null);
    }

    public PeriodFrequencyType getRecurrenceFrequency() {
        return Optional.ofNullable(this.recurrenceFrequency).map(e -> PeriodFrequencyType.fromInt(e.getId().intValue())).orElse(null);
    }

    public PortfolioAccountType getFromAccountType() {
        return Optional.ofNullable(this.fromAccountType).map(e -> PortfolioAccountType.fromInt(e.getId().intValue())).orElse(null);
    }

    public PortfolioAccountType getToAccountType() {
        return Optional.ofNullable(this.toAccountType).map(e -> PortfolioAccountType.fromInt(e.getId().intValue())).orElse(null);
    }

    public AccountTransferType getTransferType() {
        return Optional.ofNullable(this.transferType).map(e -> AccountTransferType.fromInt(e.getId().intValue())).orElse(null);
    }

    public Integer getRecurrenceOnDay() {
        return Optional.ofNullable(this.recurrenceOnMonthDay).map(monthDay -> monthDay.get(ChronoField.DAY_OF_MONTH)).orElse(0);
    }

    public Integer getRecurrenceOnMonth() {
        return Optional.ofNullable(this.recurrenceOnMonthDay).map(monthDay -> monthDay.get(ChronoField.MONTH_OF_YEAR)).orElse(0);
    }

    public Integer toTransferType() {
        Integer transferType = null;
        AccountTransferType accountTransferType = getTransferType();
        if (accountTransferType.isChargePayment()) {
            transferType = LoanTransactionType.CHARGE_PAYMENT.getValue();
        } else if (accountTransferType.isLoanRepayment()) {
            transferType = LoanTransactionType.REPAYMENT.getValue();
        }
        return transferType;
    }

}
