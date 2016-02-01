/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * INDIVIDUAL_LOAN: Individual loans are applicable only to clients. GROUP_LOAN:
 * * Group loans are applicable only to groups. And tracked only at group level.
 * JOINT_LIABILITY_LOAN: Joint liability loans are applicable only to clients
 * within a group. LINKED_LOAN: Loan is given to group, then later loan amount
 * is split into individual loans. Loan is tracked at both individual and group
 * level
 * 
 */
public enum LendingStrategy {

    INDIVIDUAL_LOAN(100, "lendingStrategy.individaulLoan", "individaulLoan"), //
    GROUP_LOAN(200, "lendingStrategy.groupLoan", "groupLoan"), //
    JOINT_LIABILITY_LOAN(300, "lendingStrategy.joinLiabilityLoan", "joinLiabilityLoan"), //
    LINKED_LOAN(400, "lendingStrategy.linkedLoan", "linkedLoan"), //
    INVALID(900, "lendingStrategy.invalid", "invalid");

    private Integer id;
    private String code;
    private String value;

    LendingStrategy(final Integer id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
    }

    private static final Map<Integer, LendingStrategy> intToEnumMap = new HashMap<>();
    private static int minValue;
    private static int maxValue;
    static {
        int i = 0;
        for (final LendingStrategy type : LendingStrategy.values()) {
            if (i == 0) {
                minValue = type.id;
            }
            intToEnumMap.put(type.id, type);
            if (minValue >= type.id) {
                minValue = type.id;
            }
            if (maxValue < type.id) {
                maxValue = type.id;
            }
            i = i + 1;
        }
    }

    public static LendingStrategy fromInt(final int i) {
        final LendingStrategy type = intToEnumMap.get(Integer.valueOf(i));
        return type;
    }

    public static int getMinValue() {
        return minValue;
    }

    public static int getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return name().toString();
    }

    public Integer getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getValue() {
        return this.value;
    }

}
