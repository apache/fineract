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
package org.apache.fineract.portfolio.savings.data;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SavingsAccountTransactionToUpdateSummary {

    private final BigDecimal amount;
    private final int typeOf;
    private final boolean isReversalTransaction;
    private final boolean isDepositAndNotReversed;
    private final boolean isDividendPayoutAndNotReversed;
    private final boolean isWithdrawal;
    private final boolean isNotReversed;
    private final boolean isWithdrawalFeeAndNotReversed;
    private final boolean isAnnualFeeAndNotReversed;
    private final boolean isWaiveFeeChargeAndNotReversed;
    private final boolean isWaivePenaltyChargeAndNotReversed;
    private final boolean isFeeChargeAndNotReversed;
    private final boolean isPenaltyChargeAndNotReversed;
    private final boolean isOverdraftInterestAndNotReversed;
    private final boolean isWithHoldTaxAndNotReversed;
}
