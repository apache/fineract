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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class LoanRepaymentScheduleInstallmentData {

    private Long id;

    private Integer installmentId;

    private LocalDate date;

    private BigDecimal amount;

    private LoanRepaymentScheduleInstallmentData(final Long id, final Integer installmentId, final LocalDate date,
            final BigDecimal amount) {
        this.amount = amount;
        this.date = date;
        this.installmentId = installmentId;
        this.id = id;
    }

    public static LoanRepaymentScheduleInstallmentData instanceOf(final Long id, final Integer installmentId, final LocalDate date,
            final BigDecimal amount) {
        return new LoanRepaymentScheduleInstallmentData(id, installmentId, date, amount);
    }

}
