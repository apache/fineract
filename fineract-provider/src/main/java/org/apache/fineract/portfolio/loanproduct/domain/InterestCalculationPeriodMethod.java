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

public enum InterestCalculationPeriodMethod {
    DAILY(0, "interestCalculationPeriodType.daily"), //
    SAME_AS_REPAYMENT_PERIOD(1, "interestCalculationPeriodType.same.as.repayment.period"), //
    INVALID(2, "interestCalculationPeriodType.invalid");

    private final Integer value;
    private final String code;

    private InterestCalculationPeriodMethod(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static InterestCalculationPeriodMethod fromInt(final Integer selectedMethod) {

        InterestCalculationPeriodMethod repaymentMethod = null;
        switch (selectedMethod) {
            case 0:
                repaymentMethod = InterestCalculationPeriodMethod.DAILY;
            break;
            case 1:
                repaymentMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
            break;
            default:
                repaymentMethod = InterestCalculationPeriodMethod.INVALID;
            break;
        }
        return repaymentMethod;
    }

    public boolean isDaily() {
        return this.value.equals(InterestCalculationPeriodMethod.DAILY.getValue());
    }

}