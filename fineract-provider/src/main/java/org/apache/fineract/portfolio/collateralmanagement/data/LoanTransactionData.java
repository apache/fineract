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
package org.apache.fineract.portfolio.collateralmanagement.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class LoanTransactionData {

    private final BigDecimal lastRepayment;

    private final BigDecimal remainingAmount;

    private final Long loanId;

    private final LocalDateTime lastRepaymentDate;

    private LoanTransactionData(final Long loanId, final LocalDateTime lastRepaymentDate, final BigDecimal remainingAmount,
            final BigDecimal lastRepayment) {
        this.lastRepayment = lastRepayment;
        this.lastRepaymentDate = lastRepaymentDate;
        this.remainingAmount = remainingAmount;
        this.loanId = loanId;
    }

    public static LoanTransactionData instance(final Long loanId, final LocalDateTime lastRepaymentDate, final BigDecimal remainingAmount,
            final BigDecimal lastRepayment) {
        return new LoanTransactionData(loanId, lastRepaymentDate, remainingAmount, lastRepayment);
    }
}
