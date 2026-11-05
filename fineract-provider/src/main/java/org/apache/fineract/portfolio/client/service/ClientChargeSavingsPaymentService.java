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
package org.apache.fineract.portfolio.client.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.client.domain.ClientCharge;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientChargeSavingsPaymentService {

    private final PlatformSecurityContext context;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final SavingsAccountDomainService savingsAccountDomainService;

    @Transactional
    public SavingsAccountTransaction withdraw(Long savingsAccountId, ClientCharge charge, Money amount, LocalDate date,
            DateTimeFormatter formatter) {
        context.authenticatedUser().validateHasPermissionTo("WITHDRAWAL_SAVINGSACCOUNT");
        SavingsAccount account = savingsAccountAssembler.assembleFromInUserScope(savingsAccountId, false);
        if (account.getClient() != null && account.getClient().isNotActive()) {
            throw new ClientNotActiveException(account.getClient().getId());
        }
        if (account.group() != null && account.group().isNotActive()) {
            throw new GroupNotActiveException(account.group().getId());
        }
        validateFunding(account, charge);
        SavingsTransactionBooleanValues values = new SavingsTransactionBooleanValues(true, true,
                account.isWithdrawalFeeApplicableForTransfer(), false, false);
        return savingsAccountDomainService.handleWithdrawal(account, formatter, date, amount.getAmount(), null, values, false);
    }

    static void validateFunding(SavingsAccount account, ClientCharge charge) {
        if (!account.getCurrency().getCode().equals(charge.getCurrency().getCode())) {
            throw new GeneralPlatformDomainRuleException("error.msg.client.charge.savings.currency.mismatch",
                    "The savings account and client charge must have the same currency");
        }
        if (!account.officeId().equals(charge.getOfficeId())) {
            throw new GeneralPlatformDomainRuleException("error.msg.client.charge.savings.cross.office.not.supported",
                    "Savings-funded client charge payments between offices are not supported");
        }
        boolean savingsAccounting = Boolean.TRUE.equals(account.isCashBasedAccountingEnabledOnSavingsProduct())
                || Boolean.TRUE.equals(account.isAccrualBasedAccountingEnabledOnSavingsProduct());
        if (savingsAccounting != (charge.getCharge().getAccount() != null)) {
            throw new GeneralPlatformDomainRuleException("error.msg.client.charge.savings.accounting.mismatch",
                    "Accounting must be enabled on both the savings product and client charge, or disabled on both");
        }
    }
}
