/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;

/**
 * Helper class for creating loan schedule data suitable for testing.
 */
public class LoanScheduleTestDataHelper {

    /**
     * Creates brand new three installment loan:
     * 
     * For example: with firstDueDate = 02 July 2011
     * 
     * Date Principal Interest Interest Waived
     * ==================================
     * ================================================ 02 July 2011 1,000 200 0
     * 02 August 2011 1,000 200 0 02 September 2011 1,000 200 0
     */
    public static List<LoanRepaymentScheduleInstallment> createSimpleLoanSchedule(final LocalDate firstDueDate,
            final MonetaryCurrency currency) {
        final LoanRepaymentScheduleInstallment firstInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
                .withInstallmentNumber(1).withDueDate(firstDueDate).withPrincipal("1000.00").withInterest("200.00").build();

        final LoanRepaymentScheduleInstallment secondInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
                .withInstallmentNumber(2).withDueDate(firstDueDate.plusMonths(1)).withPrincipal("1000.00").withInterest("200.00").build();

        final LoanRepaymentScheduleInstallment thirdInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
                .withInstallmentNumber(3).withDueDate(firstDueDate.plusMonths(2)).withPrincipal("1000.00").withInterest("200.00").build();

        return Arrays.asList(firstInstallment, secondInstallment, thirdInstallment);
    }

    /**
     * Creates three installment loan with first installment fully completed:
     * 
     * For example: with firstDueDate = 02 July 2011
     * 
     * Date Principal Interest Interest Waived Completed
     * ========================
     * ==================================================
     * ====================================== 02 July 2011 1,000 200 0 true
     * (principal paid, interest paid) 02 August 2011 1,000 200 0 false 02
     * September 2011 1,000 200 0 false
     */
    public static List<LoanRepaymentScheduleInstallment> createSimpleLoanScheduleWithFirstInstallmentFullyPaid(
            final LocalDate firstDueDate, final MonetaryCurrency currency) {

        final LoanRepaymentScheduleInstallment firstInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
                .withInstallmentNumber(1).withDueDate(firstDueDate).withPrincipal("1000.00").withInterest("200.00").completed().build();

        final LoanRepaymentScheduleInstallment secondInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
                .withInstallmentNumber(2).withDueDate(firstDueDate.plusMonths(1)).withPrincipal("1000.00").withInterest("200.00").build();

        final LoanRepaymentScheduleInstallment thirdInstallment = new LoanRepaymentScheduleInstallmentBuilder(currency)
                .withInstallmentNumber(3).withDueDate(firstDueDate.plusMonths(2)).withPrincipal("1000.00").withInterest("200.00").build();

        return Arrays.asList(firstInstallment, secondInstallment, thirdInstallment);
    }

}
