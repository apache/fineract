/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.closedOnDateParamName;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.paymentdetail.PaymentDetailConstants;
import org.mifosplatform.portfolio.savings.DepositAccountOnClosureType;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.data.DepositAccountData;
import org.mifosplatform.portfolio.savings.data.DepositAccountTransactionDataValidator;
import org.mifosplatform.portfolio.savings.data.FixedDepositAccountData;
import org.mifosplatform.portfolio.savings.data.RecurringDepositAccountData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountData;
import org.mifosplatform.portfolio.savings.domain.DepositAccountAssembler;
import org.mifosplatform.portfolio.savings.domain.FixedDepositAccount;
import org.mifosplatform.portfolio.savings.domain.RecurringDepositAccount;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class DepositAccountPreMatureCalculationPlatformServiceImpl implements DepositAccountPreMatureCalculationPlatformService {

    private final FromJsonHelper fromJsonHelper;
    private final DepositAccountTransactionDataValidator depositAccountTransactionDataValidator;
    private final DepositAccountAssembler depositAccountAssembler;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;

    @Autowired
    public DepositAccountPreMatureCalculationPlatformServiceImpl(final FromJsonHelper fromJsonHelper,
            final DepositAccountTransactionDataValidator depositAccountTransactionDataValidator,
            final DepositAccountAssembler depositAccountAssembler, final CodeValueReadPlatformService codeValueReadPlatformService,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService) {
        this.fromJsonHelper = fromJsonHelper;
        this.depositAccountTransactionDataValidator = depositAccountTransactionDataValidator;
        this.depositAccountAssembler = depositAccountAssembler;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;

    }

    @Transactional
    @Override
    public DepositAccountData calculatePreMatureAmount(final Long accountId, final JsonQuery query,
            final DepositAccountType depositAccountType) {
        this.depositAccountTransactionDataValidator.validatePreMatureAmountCalculation(query.json(), depositAccountType);
        final SavingsAccount account = this.depositAccountAssembler.assembleFrom(accountId, depositAccountType);

        DepositAccountData accountData = null;
        Collection<EnumOptionData> onAccountClosureOptions = SavingsEnumerations.depositAccountOnClosureType(DepositAccountOnClosureType
                .values());
        final Collection<CodeValueData> paymentTypeOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(PaymentDetailConstants.paymentTypeCodeName);
        final Collection<SavingsAccountData> savingsAccountDatas = this.savingsAccountReadPlatformService.retrieveActiveForLookup(
                account.clientId(), DepositAccountType.SAVINGS_DEPOSIT);
        final JsonElement element = this.fromJsonHelper.parse(query.json());
        final LocalDate preMaturityDate = this.fromJsonHelper.extractLocalDateNamed(closedOnDateParamName, element);

        if (depositAccountType.isFixedDeposit()) {
            final FixedDepositAccount fd = (FixedDepositAccount) account;
            accountData = FixedDepositAccountData.preClosureDetails(account.getId(), fd.calculatePreMatureAmount(preMaturityDate),
                    onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas);
        } else if (depositAccountType.isRecurringDeposit()) {
            final RecurringDepositAccount rd = (RecurringDepositAccount) account;
            accountData = RecurringDepositAccountData.preClosureDetails(account.getId(), rd.calculatePreMatureAmount(preMaturityDate),
                    onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas);
        }

        return accountData;
    }
}