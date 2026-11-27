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
package org.apache.fineract.portfolio.util;

import java.util.List;
import java.util.function.Predicate;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;

public final class InstallmentProcessingHelper {

    private InstallmentProcessingHelper() {}

    public static void addOneToInstallmentNumberFromInstallment(final List<LoanRepaymentScheduleInstallment> installments,
            final int installmentNumber) {
        installments.stream().filter(i -> i.getInstallmentNumber() != null && i.getInstallmentNumber() >= installmentNumber)
                .forEach(i -> i.setInstallmentNumber(i.getInstallmentNumber() + 1));
    }

    /**
     * Removes the installments that match the filter from the repayment schedule, together with their installment
     * charges, see {@link LoanRepaymentScheduleInstallment#removeInstallmentCharges()}.
     */
    public static void removeInstallmentsIf(final List<LoanRepaymentScheduleInstallment> installments,
            final Predicate<LoanRepaymentScheduleInstallment> filter) {
        installments.removeIf(installment -> {
            if (!filter.test(installment)) {
                return false;
            }
            installment.removeInstallmentCharges();
            return true;
        });
    }

    /**
     * Removes the installment from the repayment schedule, together with its installment charges, see
     * {@link LoanRepaymentScheduleInstallment#removeInstallmentCharges()}.
     */
    public static void removeInstallment(final List<LoanRepaymentScheduleInstallment> installments,
            final LoanRepaymentScheduleInstallment installment) {
        if (installments.remove(installment)) {
            installment.removeInstallmentCharges();
        }
    }
}
