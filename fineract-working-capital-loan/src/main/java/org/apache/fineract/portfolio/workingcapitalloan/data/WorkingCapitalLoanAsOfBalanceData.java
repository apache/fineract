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
package org.apache.fineract.portfolio.workingcapitalloan.data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The outstanding buckets of a Working Capital loan as of a past date, mirroring the getters on
 * {@code WorkingCapitalLoanBalance} field for field.
 *
 * <p>
 * Only the charged side is date-scoped. What has been paid, written off or recovered keeps its current value, so the
 * quote stays an answer to "what closes this loan" rather than "what was owed that day" - the same thing the core loan
 * module's payoff quote answers. {@link #overpaymentAmount} and {@link #writtenOffOutstanding} are therefore carried
 * through from the stored balance unchanged and do not vary with the date at all.
 */
public record WorkingCapitalLoanAsOfBalanceData(LocalDate asOfDate, BigDecimal principalOutstanding, BigDecimal feeOutstanding,
        BigDecimal penaltyOutstanding, BigDecimal totalOutstanding, BigDecimal overpaymentAmount, BigDecimal writtenOffOutstanding) {
}
