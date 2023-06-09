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
import java.util.Objects;
import lombok.Getter;

/**
 * Immutable data object representing the total unpaid charge details
 */
@Getter
public class UnpaidChargeData {

    private static final String NULL_VALUE_IS_NOT_ALLOWED = "Null value is not allowed";
    private final Long chargeId;
    private final String chargeName;
    private final BigDecimal outstandingAmount;

    public UnpaidChargeData(Long chargeId, String chargeName, BigDecimal outstandingAmount) {
        this.chargeId = Objects.requireNonNull(chargeId, NULL_VALUE_IS_NOT_ALLOWED);
        this.chargeName = Objects.requireNonNull(chargeName, NULL_VALUE_IS_NOT_ALLOWED);
        this.outstandingAmount = Objects.requireNonNull(outstandingAmount, NULL_VALUE_IS_NOT_ALLOWED);
    }
}
