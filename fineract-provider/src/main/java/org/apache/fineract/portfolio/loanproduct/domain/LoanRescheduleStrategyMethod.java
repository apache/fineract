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

import java.util.HashMap;
import java.util.Map;

/***
 * * People typically use either of the following settings when defining interest
 * recalculation method:
 * <ul>
 * <li>RESCHEDULE_NEXT_REPAYMENTS</li>
 * <li>REDUCE_NUMBER_OF_INSTALLMENTS</li>
 * <li>REDUCE_EMI_AMOUNT</li>
 * </ul>
 */

public enum LoanRescheduleStrategyMethod {

    INVALID(0, "loanRescheduleStrategyMethod.invalid"), //
    RESCHEDULE_NEXT_REPAYMENTS(1, "loanRescheduleStrategyMethod.reschedule.next.repayments"), //
    REDUCE_NUMBER_OF_INSTALLMENTS(2, "loanRescheduleStrategyMethod.reduce.number.of.installments"), //
    REDUCE_EMI_AMOUNT(3, "loanRescheduleStrategyMethod.reduce.emi.amount");

    private final Integer value;
    private final String code;
    private static final Map<Integer, LoanRescheduleStrategyMethod> intToEnumMap = new HashMap<>();

    static {
        for (final LoanRescheduleStrategyMethod type : LoanRescheduleStrategyMethod.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static LoanRescheduleStrategyMethod fromInt(final Integer ruleTypeValue) {
        final LoanRescheduleStrategyMethod type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private LoanRescheduleStrategyMethod(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }
}
