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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanAddChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanDeleteChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanUpdateChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;

/**
 * Down payment fees are created, changed and deactivated by the system in several commands (application, disbursement,
 * undo, close). A command captures the fees before it changes the loan and, once the loan is saved, raises one charge
 * event per fee it changed: {@link LoanAddChargeBusinessEvent} for a created fee, {@link LoanUpdateChargeBusinessEvent}
 * when the amount, due date or paid status changed or the fee was reactivated, {@link LoanDeleteChargeBusinessEvent}
 * when it was deactivated or deleted.
 */
@RequiredArgsConstructor
public class LoanDownPaymentFeeEventNotifier {

    private final BusinessEventNotifierService businessEventNotifierService;

    public Map<Long, FeeState> captureFees(final Loan loan) {
        final Map<Long, FeeState> fees = new HashMap<>();
        for (final LoanCharge fee : loan.getLoanCharges()) {
            if (fee.isDueAtDownPayment() && fee.getId() != null) {
                fees.put(fee.getId(), FeeState.of(fee));
            }
        }
        return fees;
    }

    public void notifyFeeChanges(final Loan loan, final Map<Long, FeeState> feesBefore) {
        final Set<Long> feesAfter = new HashSet<>();
        for (final LoanCharge fee : loan.getLoanCharges()) {
            if (!fee.isDueAtDownPayment() || fee.getId() == null) {
                continue;
            }
            feesAfter.add(fee.getId());
            final FeeState before = feesBefore.get(fee.getId());
            if (before == null) {
                if (fee.isActive()) {
                    businessEventNotifierService.notifyPostBusinessEvent(new LoanAddChargeBusinessEvent(fee));
                }
            } else if (before.active() && !fee.isActive()) {
                businessEventNotifierService.notifyPostBusinessEvent(new LoanDeleteChargeBusinessEvent(fee));
            } else if (fee.isActive() && before.isChangedIn(fee)) {
                businessEventNotifierService.notifyPostBusinessEvent(new LoanUpdateChargeBusinessEvent(fee));
            }
        }
        for (final Map.Entry<Long, FeeState> before : feesBefore.entrySet()) {
            if (!feesAfter.contains(before.getKey()) && before.getValue().active()) {
                businessEventNotifierService.notifyPostBusinessEvent(new LoanDeleteChargeBusinessEvent(before.getValue().fee()));
            }
        }
    }

    public record FeeState(LoanCharge fee, boolean active, Money amount, LocalDate dueDate, boolean paid) {

        static FeeState of(final LoanCharge fee) {
            return new FeeState(fee, fee.isActive(), fee.getAmount(fee.getLoan().getCurrency()), fee.getDueLocalDate(), fee.isPaid());
        }

        boolean isChangedIn(final LoanCharge fee) {
            return active != fee.isActive() || paid != fee.isPaid() || !DateUtils.isEqual(dueDate, fee.getDueLocalDate())
                    || !MathUtil.isEqualTo(amount, fee.getAmount(fee.getLoan().getCurrency()));
        }
    }
}
