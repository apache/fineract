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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.fineract.portfolio.loanaccount.data.LoanPrincipalRelatedDataHolder;

/**
 * Immutable data object representing a subset of loan transaction data.
 */
@Getter
@EqualsAndHashCode
public class LoanTransactionRepaymentPeriodData implements LoanPrincipalRelatedDataHolder, Serializable {

    private final Long transactionId;
    private final Long loanId;
    private final LocalDate date;
    private final boolean reversed;
    private final BigDecimal amount;
    private final BigDecimal unrecognizedAmount;
    private final BigDecimal feeChargesPortion;

    public LoanTransactionRepaymentPeriodData(Long transactionId, Long loanId, LocalDate date, boolean reversed, BigDecimal amount,
            BigDecimal unrecognizedAmount, BigDecimal feeChargesPortion) {
        this.transactionId = transactionId;
        this.loanId = loanId;
        this.date = date;
        this.reversed = reversed;
        this.amount = amount;
        this.unrecognizedAmount = unrecognizedAmount;
        this.feeChargesPortion = feeChargesPortion;
    }
}
