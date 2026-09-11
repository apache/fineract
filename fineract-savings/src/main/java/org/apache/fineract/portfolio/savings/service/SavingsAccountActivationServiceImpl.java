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

import java.math.MathContext;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;

/**
 * Default implementation of {@link SavingsAccountActivationService}. The bodies of {@code processAccountUponActivation}
 * and {@code payActivationCharges} were extracted from {@code SavingsAccount}; behaviour is intentionally unchanged.
 * The interest posting that may follow activation charge payment is delegated to
 * {@link SavingsAccountPostInterestService}.
 */
@RequiredArgsConstructor
public class SavingsAccountActivationServiceImpl implements SavingsAccountActivationService {

    private final SavingsAccountPostInterestService savingsAccountPostInterestService;
    private final SavingsHelper savingsHelper;

    @Override
    public void processAccountUponActivation(final SavingsAccount account, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth) {
        // update annual fee due date
        for (SavingsAccountCharge charge : account.charges()) {
            charge.updateToNextDueDateFrom(account.getActivationDate());
        }

        // auto pay the activation time charges (No need of checking the pivot date
        // config)
        payActivationCharges(account, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, false);
        // TODO : AA add activation charges to actual changes list
    }

    private void payActivationCharges(final SavingsAccount account, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth, final boolean backdatedTxnsAllowedTill) {
        boolean isSavingsChargeApplied = false;
        boolean postReversals = false;
        UUID refNo = UUID.randomUUID();
        for (SavingsAccountCharge savingsAccountCharge : account.charges()) {
            if (savingsAccountCharge.isSavingsActivation()) {
                isSavingsChargeApplied = true;
                account.payCharge(savingsAccountCharge, savingsAccountCharge.getAmountOutstanding(account.getCurrency()),
                        account.getActivationDate(), backdatedTxnsAllowedTill, refNo.toString());
            }
        }

        if (isSavingsChargeApplied) {
            final MathContext mc = MathContext.DECIMAL64;
            boolean isInterestTransfer = false;
            LocalDate postInterestAsOnDate = null;
            if (account.isBeforeLastPostingPeriod(account.getActivationDate(), backdatedTxnsAllowedTill)) {
                final LocalDate today = DateUtils.getBusinessLocalDate();
                savingsAccountPostInterestService.postInterest(account, mc, today, isInterestTransfer,
                        isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestAsOnDate,
                        backdatedTxnsAllowedTill, postReversals);
            } else {
                final LocalDate today = DateUtils.getBusinessLocalDate();
                account.calculateInterestUsing(mc, today, isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd,
                        financialYearBeginningMonth, postInterestAsOnDate, backdatedTxnsAllowedTill, postReversals, this.savingsHelper);
            }
        }
    }
}
