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
package org.apache.fineract.portfolio.loanproduct.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class LoanProductTrancheDetails {

    @Column(name = "allow_multiple_disbursals")
    private boolean multiDisburseLoan;

    @Column(name = "max_disbursals")
    private Integer maxTrancheCount;

    @Column(name = "max_outstanding_loan_balance", scale = 6, precision = 19)
    private BigDecimal outstandingLoanBalance;

    protected LoanProductTrancheDetails() {
        // TODO Auto-generated constructor stub
    }

    public LoanProductTrancheDetails(final boolean multiDisburseLoan, final Integer maxTrancheCount,
            final BigDecimal outstandingLoanBalance) {
        this.multiDisburseLoan = multiDisburseLoan;
        this.maxTrancheCount = maxTrancheCount;
        this.outstandingLoanBalance = outstandingLoanBalance;
    }

}
