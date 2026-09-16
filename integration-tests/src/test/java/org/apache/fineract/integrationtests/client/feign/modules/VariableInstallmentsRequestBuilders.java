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
package org.apache.fineract.integrationtests.client.feign.modules;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.models.PostLoansLoanIdScheduleDeletedInstallment;
import org.apache.fineract.client.models.PostLoansLoanIdScheduleExceptions;
import org.apache.fineract.client.models.PostLoansLoanIdScheduleModifiedInstallment;
import org.apache.fineract.client.models.PostLoansLoanIdScheduleNewInstallment;
import org.apache.fineract.client.models.PostLoansLoanIdScheduleRequest;

/**
 * Loan term variation requests for variable installment loans. Declining balance products drive the schedule with
 * {@code installmentAmount}; flat products drive it with {@code principal}, so both are offered here.
 */
public final class VariableInstallmentsRequestBuilders {

    private VariableInstallmentsRequestBuilders() {}

    public static PostLoansLoanIdScheduleRequest variations(PostLoansLoanIdScheduleExceptions exceptions) {
        return new PostLoansLoanIdScheduleRequest()//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .exceptions(exceptions);
    }

    public static PostLoansLoanIdScheduleExceptions deleted(String... dueDates) {
        return new PostLoansLoanIdScheduleExceptions().deletedinstallments(deletedInstallments(dueDates));
    }

    public static List<PostLoansLoanIdScheduleDeletedInstallment> deletedInstallments(String... dueDates) {
        return List.of(dueDates).stream().map(date -> new PostLoansLoanIdScheduleDeletedInstallment().dueDate(date)).toList();
    }

    public static PostLoansLoanIdScheduleModifiedInstallment modifiedByInstallmentAmount(String dueDate, double installmentAmount) {
        return new PostLoansLoanIdScheduleModifiedInstallment().dueDate(dueDate).installmentAmount(BigDecimal.valueOf(installmentAmount));
    }

    public static PostLoansLoanIdScheduleModifiedInstallment modifiedByPrincipal(String dueDate, double principal) {
        return new PostLoansLoanIdScheduleModifiedInstallment().dueDate(dueDate).principal(BigDecimal.valueOf(principal));
    }

    public static PostLoansLoanIdScheduleNewInstallment addedByInstallmentAmount(String dueDate, double installmentAmount) {
        return new PostLoansLoanIdScheduleNewInstallment().dueDate(dueDate).installmentAmount(BigDecimal.valueOf(installmentAmount));
    }

    public static PostLoansLoanIdScheduleNewInstallment addedByPrincipal(String dueDate, double principal) {
        return new PostLoansLoanIdScheduleNewInstallment().dueDate(dueDate).principal(BigDecimal.valueOf(principal));
    }

    /** Moves an installment to a new due date, optionally restating its instalment amount. */
    public static PostLoansLoanIdScheduleModifiedInstallment movedByInstallmentAmount(String dueDate, String modifiedDueDate,
            Double installmentAmount) {
        PostLoansLoanIdScheduleModifiedInstallment modified = new PostLoansLoanIdScheduleModifiedInstallment().dueDate(dueDate)
                .modifiedDueDate(modifiedDueDate);
        return installmentAmount == null ? modified : modified.installmentAmount(BigDecimal.valueOf(installmentAmount));
    }

    /** Moves an installment to a new due date, optionally restating its principal. */
    public static PostLoansLoanIdScheduleModifiedInstallment movedByPrincipal(String dueDate, String modifiedDueDate, Double principal) {
        PostLoansLoanIdScheduleModifiedInstallment modified = new PostLoansLoanIdScheduleModifiedInstallment().dueDate(dueDate)
                .modifiedDueDate(modifiedDueDate);
        return principal == null ? modified : modified.principal(BigDecimal.valueOf(principal));
    }
}
