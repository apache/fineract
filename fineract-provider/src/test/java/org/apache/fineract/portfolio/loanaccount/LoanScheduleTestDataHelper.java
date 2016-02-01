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
package org.apache.fineract.portfolio.loanaccount;

import java.util.Arrays;
import java.util.List;

import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.joda.time.LocalDate;

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
