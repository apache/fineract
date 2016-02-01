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
