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

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingCapitalLoanSummaryData implements Serializable {

    private CurrencyData currency;

    // Principal
    private BigDecimal principal;
    private BigDecimal principalPaid;
    private BigDecimal principalAdjustment;
    private BigDecimal principalOutstanding;

    // Fee
    private BigDecimal fee;
    private BigDecimal feePaid;
    private BigDecimal feeOutstanding;

    // Penalty
    private BigDecimal penalty;
    private BigDecimal penaltyPaid;
    private BigDecimal penaltyOutstanding;

    // Income recognition
    private BigDecimal realizedIncomeFromDiscountFee;
    private BigDecimal unrealizedIncomeFromDiscountFee;

    // Overpayment
    private BigDecimal overpayment;

    // Aggregates
    private BigDecimal totalDisbursement;
    private BigDecimal totalDiscountFee;
    private BigDecimal totalDiscountFeeAdjustment;
    private BigDecimal totalExpectedRepayment;
    private BigDecimal totalRepayment;
    private BigDecimal totalOutstanding;
}
