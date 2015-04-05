/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.util.HashMap;
import java.util.Map;

public enum LoanPreCloseInterestCalculationStrategy {

    NONE(0, "loanPreCloseInterestCalculationStrategy.none"), //
    TILL_PRE_CLOSURE_DATE(1, "loanPreCloseInterestCalculationStrategy.tillPreClosureDate"), //
    TILL_REST_FREQUENCY_DATE(2, "loanPreCloseInterestCalculationStrategy.tillRestFrequencyDate");

    // REPAYMENT_PERIOD_DATE(3,
    // "loanPreCloseInterestCalculationStrategy.repaymentPeriodDate")

    private Integer value;
    private String code;

    private static final Map<Integer, LoanPreCloseInterestCalculationStrategy> intToEnumMap = new HashMap<>();
    private static int minValue;
    private static int maxValue;
    static {
        int i = 0;
        for (final LoanPreCloseInterestCalculationStrategy type : LoanPreCloseInterestCalculationStrategy.values()) {
            if (i == 0) {
                minValue = type.value;
            }
            intToEnumMap.put(type.value, type);
            if (minValue >= type.value) {
                minValue = type.value;
            }
            if (maxValue < type.value) {
                maxValue = type.value;
            }
            i = i + 1;
        }
    }

    public static LoanPreCloseInterestCalculationStrategy fromInt(final Integer ruleTypeValue) {
        final LoanPreCloseInterestCalculationStrategy type = intToEnumMap.get(ruleTypeValue);
        return type;
    }

    private LoanPreCloseInterestCalculationStrategy(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static int getMinValue() {
        return minValue;
    }

    public static int getMaxValue() {
        return maxValue;
    }

    public boolean calculateTillRestFrequencyEnabled() {
        return this.getValue().equals(LoanPreCloseInterestCalculationStrategy.TILL_REST_FREQUENCY_DATE.getValue());
    }

    public boolean calculateTillPreClosureDateEnabled() {
        return this.getValue().equals(LoanPreCloseInterestCalculationStrategy.TILL_PRE_CLOSURE_DATE.getValue());
    }
}
