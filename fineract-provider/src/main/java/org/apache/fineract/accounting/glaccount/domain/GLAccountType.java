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
package org.apache.fineract.accounting.glaccount.domain;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

import java.util.HashMap;
import java.util.Map;

public enum GLAccountType {
    ASSET(1, "accountType.asset"), LIABILITY(2, "accountType.liability"), EQUITY(3, "accountType.equity"), INCOME(4, "accountType.income"), EXPENSE(
            5, "accountType.expense");

    private final Integer value;
    private final String code;

    private GLAccountType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    private static final Map<Integer, GLAccountType> intToEnumMap = new HashMap<>();
    private static int minValue;
    private static int maxValue;
    static {
        int i = 0;
        for (final GLAccountType type : GLAccountType.values()) {
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
    public static EnumOptionData fromString(String accountType){
        Long accountTypeId=null;
        if (accountType!=null && accountType.equalsIgnoreCase(ASSET.toString())){
            accountTypeId=1L;
            return new EnumOptionData(accountTypeId,null,null);
        }else if(accountType!=null && accountType.equalsIgnoreCase(LIABILITY.toString())) {
            accountTypeId = 2L;
            return new EnumOptionData(accountTypeId, null, null);
        }else if(accountType!=null && accountType.equalsIgnoreCase(EQUITY.toString())){
            accountTypeId=3L;
            return new EnumOptionData(accountTypeId,null,null);
        }else if(accountType!=null && accountType.equalsIgnoreCase(INCOME.toString())) {
            accountTypeId = 4L;
            return new EnumOptionData(accountTypeId, null, null);
        }else if(accountType!=null && accountType.equalsIgnoreCase(EXPENSE.toString())){
            accountTypeId=5L;
            return new EnumOptionData(accountTypeId,null,null);
        }else {
            return null;
        }
    }

    public static GLAccountType fromInt(final int i) {
        final GLAccountType type = intToEnumMap.get(Integer.valueOf(i));
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

    public boolean isAssetType() {
        return this.value.equals(GLAccountType.ASSET.getValue());
    }

    public boolean isLiabilityType() {
        return this.value.equals(GLAccountType.LIABILITY.getValue());
    }

    public boolean isEquityType() {
        return this.value.equals(GLAccountType.EQUITY.getValue());
    }

    public boolean isIncomeType() {
        return this.value.equals(GLAccountType.INCOME.getValue());
    }

    public boolean isExpenseType() {
        return this.value.equals(GLAccountType.EXPENSE.getValue());
    }

}
