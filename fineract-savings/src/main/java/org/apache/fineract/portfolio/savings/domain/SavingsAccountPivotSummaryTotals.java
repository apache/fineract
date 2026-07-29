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
package org.apache.fineract.portfolio.savings.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Totals of a {@link SavingsAccountSummary} recalculation after an interest posting when backdated transactions are
 * allowed (pivot configuration), computed by
 * {@link SavingsAccountTransactionSummaryWrapper#calculatePivotSummaryTotals} and stored as-is by the summary.
 *
 * @param interestPostedTillDate
 *            date of the latest interest or overdraft interest posting, or {@code null} when there is none and the
 *            current value must be kept
 */
public record SavingsAccountPivotSummaryTotals(BigDecimal totalDeposits, BigDecimal totalWithdrawals, BigDecimal totalInterestPosted,
        BigDecimal totalOverdraftInterestDerived, BigDecimal totalWithholdTax, BigDecimal accountBalance,
        LocalDate interestPostedTillDate) {
}
