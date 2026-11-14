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
package org.apache.fineract.portfolio.loanaccount.service;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;

/**
 * Entry point for loan charge write operations. The work is done by {@link LoanChargeAddUpdateWritePlatformService},
 * {@link LoanChargeWaiverWritePlatformService}, {@link LoanChargePaymentWritePlatformService} and
 * {@link LoanOverdueChargeWritePlatformService}.
 */
@RequiredArgsConstructor
public class LoanChargeWritePlatformServiceImpl implements LoanChargeWritePlatformService {

    private final LoanChargeAddUpdateWritePlatformService loanChargeAddUpdateWritePlatformService;
    private final LoanChargeWaiverWritePlatformService loanChargeWaiverWritePlatformService;
    private final LoanChargePaymentWritePlatformService loanChargePaymentWritePlatformService;
    private final LoanOverdueChargeWritePlatformService loanOverdueChargeWritePlatformService;

    @Override
    public CommandProcessingResult addLoanCharge(final Long loanId, final JsonCommand command) {
        return this.loanChargeAddUpdateWritePlatformService.addLoanCharge(loanId, command);
    }

    @Override
    public CommandProcessingResult loanChargeRefund(final Long loanId, final JsonCommand command) {
        return this.loanChargePaymentWritePlatformService.loanChargeRefund(loanId, command);
    }

    @Override
    public CommandProcessingResult undoWaiveLoanCharge(final JsonCommand command) {
        return this.loanChargeWaiverWritePlatformService.undoWaiveLoanCharge(command);
    }

    @Override
    public CommandProcessingResult updateLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {
        return this.loanChargeAddUpdateWritePlatformService.updateLoanCharge(loanId, loanChargeId, command);
    }

    @Override
    public CommandProcessingResult waiveLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {
        return this.loanChargeWaiverWritePlatformService.waiveLoanCharge(loanId, loanChargeId, command);
    }

    @Override
    public CommandProcessingResult deleteLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {
        return this.loanChargeAddUpdateWritePlatformService.deleteLoanCharge(loanId, loanChargeId, command);
    }

    @Override
    public CommandProcessingResult payLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command,
            final boolean isChargeIdIncludedInJson) {
        return this.loanChargePaymentWritePlatformService.payLoanCharge(loanId, loanChargeId, command, isChargeIdIncludedInJson);
    }

    @Override
    public CommandProcessingResult adjustmentForLoanCharge(final Long loanId, final Long loanChargeId, final JsonCommand command) {
        return this.loanChargePaymentWritePlatformService.adjustmentForLoanCharge(loanId, loanChargeId, command);
    }

    @Override
    public CommandProcessingResult deactivateOverdueLoanCharge(final Long loanId, final JsonCommand command) {
        return this.loanOverdueChargeWritePlatformService.deactivateOverdueLoanCharge(loanId, command);
    }

    @Override
    public void applyOverdueChargesForLoan(final Long loanId, final Collection<OverdueLoanScheduleData> overdueLoanScheduleDataList) {
        this.loanOverdueChargeWritePlatformService.applyOverdueChargesForLoan(loanId, overdueLoanScheduleDataList);
    }
}
