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
package org.apache.fineract.portfolio.savings.service;

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.closedOnDateParamName;

import java.util.Collection;

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountOnClosureType;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.data.DepositAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.data.FixedDepositAccountData;
import org.apache.fineract.portfolio.savings.data.RecurringDepositAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class DepositAccountPreMatureCalculationPlatformServiceImpl implements DepositAccountPreMatureCalculationPlatformService {

    private final FromJsonHelper fromJsonHelper;
    private final DepositAccountTransactionDataValidator depositAccountTransactionDataValidator;
    private final DepositAccountAssembler depositAccountAssembler;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;

    @Autowired
    public DepositAccountPreMatureCalculationPlatformServiceImpl(final FromJsonHelper fromJsonHelper,
            final DepositAccountTransactionDataValidator depositAccountTransactionDataValidator,
            final DepositAccountAssembler depositAccountAssembler,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService,
            final ConfigurationDomainService configurationDomainService, PaymentTypeReadPlatformService paymentTypeReadPlatformService) {
        this.fromJsonHelper = fromJsonHelper;
        this.depositAccountTransactionDataValidator = depositAccountTransactionDataValidator;
        this.depositAccountAssembler = depositAccountAssembler;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
        this.configurationDomainService = configurationDomainService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;

    }

    @Transactional
    @Override
    public DepositAccountData calculatePreMatureAmount(final Long accountId, final JsonQuery query,
            final DepositAccountType depositAccountType) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        this.depositAccountTransactionDataValidator.validatePreMatureAmountCalculation(query.json(), depositAccountType);
        final SavingsAccount account = this.depositAccountAssembler.assembleFrom(accountId, depositAccountType);

        DepositAccountData accountData = null;
        Collection<EnumOptionData> onAccountClosureOptions = SavingsEnumerations
                .depositAccountOnClosureType(new DepositAccountOnClosureType[] { DepositAccountOnClosureType.WITHDRAW_DEPOSIT,
                        DepositAccountOnClosureType.TRANSFER_TO_SAVINGS });
        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final Collection<SavingsAccountData> savingsAccountDatas = this.savingsAccountReadPlatformService.retrieveActiveForLookup(
                account.clientId(), DepositAccountType.SAVINGS_DEPOSIT);
        final JsonElement element = this.fromJsonHelper.parse(query.json());
        final LocalDate preMaturityDate = this.fromJsonHelper.extractLocalDateNamed(closedOnDateParamName, element);
        // calculate interest before one day of closure date
        final LocalDate interestCalculatedToDate = preMaturityDate.minusDays(1);
        final boolean isPreMatureClosure = true;

        if (depositAccountType.isFixedDeposit()) {
            final FixedDepositAccount fd = (FixedDepositAccount) account;
            accountData = FixedDepositAccountData.preClosureDetails(account.getId(), fd.calculatePreMatureAmount(interestCalculatedToDate,
                    isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth), onAccountClosureOptions,
                    paymentTypeOptions, savingsAccountDatas);
        } else if (depositAccountType.isRecurringDeposit()) {
            final RecurringDepositAccount rd = (RecurringDepositAccount) account;
            accountData = RecurringDepositAccountData.preClosureDetails(account.getId(), rd.calculatePreMatureAmount(
                    interestCalculatedToDate, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth),
                    onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas);
        }

        return accountData;
    }
}