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
package org.apache.fineract.infrastructure.accountnumberformat.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public class AccountNumberFormatEnumerations {

    public final static Set<AccountNumberPrefixType> accountNumberPrefixesForClientAccounts = new HashSet<>(Arrays.asList(
            AccountNumberPrefixType.OFFICE_NAME, AccountNumberPrefixType.CLIENT_TYPE));
    public final static Set<AccountNumberPrefixType> accountNumberPrefixesForLoanAccounts = new HashSet<>(Arrays.asList(
            AccountNumberPrefixType.OFFICE_NAME, AccountNumberPrefixType.LOAN_PRODUCT_SHORT_NAME));
    public final static Set<AccountNumberPrefixType> accountNumberPrefixesForSavingsAccounts = new HashSet<>(Arrays.asList(
            AccountNumberPrefixType.OFFICE_NAME, AccountNumberPrefixType.SAVINGS_PRODUCT_SHORT_NAME));
    public final static Set<AccountNumberPrefixType> accountNumberPrefixesForCenters = new HashSet<>(Arrays.asList(
    		AccountNumberPrefixType.OFFICE_NAME));
    public final static Set<AccountNumberPrefixType> accountNumberPrefixesForGroups = new HashSet<>(Arrays.asList(
    		AccountNumberPrefixType.OFFICE_NAME));

    public enum AccountNumberPrefixType {
        OFFICE_NAME(1, "accountNumberPrefixType.officeName"), CLIENT_TYPE(101, "accountNumberPrefixType.clientType"), LOAN_PRODUCT_SHORT_NAME(
                201, "accountNumberPrefixType.loanProductShortName"), SAVINGS_PRODUCT_SHORT_NAME(301,
                "accountNumberPrefixType.savingsProductShortName");

        private final Integer value;
        private final String code;

        private AccountNumberPrefixType(final Integer value, final String code) {
            this.value = value;
            this.code = code;
        }

        public Integer getValue() {
            return this.value;
        }

        public String getCode() {
            return this.code;
        }

        private static final Map<Integer, AccountNumberPrefixType> intToEnumMap = new HashMap<>();
        private static int minValue;
        private static int maxValue;
        static {
            int i = 0;
            for (final AccountNumberPrefixType type : AccountNumberPrefixType.values()) {
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

        public static AccountNumberPrefixType fromInt(final int i) {
            final AccountNumberPrefixType type = intToEnumMap.get(Integer.valueOf(i));
            return type;
        }

        public static int getMinValue() {
            return minValue;
        }

        public static int getMaxValue() {
            return maxValue;
        }

    }

    public static EnumOptionData entityAccountType(final Integer accountTypeId) {
        return AccountNumberFormatEnumerations.entityAccountType(EntityAccountType.fromInt(accountTypeId));
    }

    public static List<EnumOptionData> entityAccountType(final EntityAccountType[] entityAccountTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final EntityAccountType accountType : entityAccountTypes) {
            optionDatas.add(entityAccountType(accountType));
        }
        return optionDatas;
    }

    public static EnumOptionData entityAccountType(final EntityAccountType accountType) {
        final EnumOptionData optionData = new EnumOptionData(accountType.getValue().longValue(), accountType.getCode(),
                accountType.toString());
        return optionData;
    }

    public static EnumOptionData accountNumberPrefixType(final Integer accountNumberPrefixTypeId) {
        return AccountNumberFormatEnumerations.entityAccountType(AccountNumberPrefixType.fromInt(accountNumberPrefixTypeId));
    }

    public static List<EnumOptionData> accountNumberPrefixType(final AccountNumberPrefixType[] accountNumberPrefixTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final AccountNumberPrefixType accountNumberPrefixType : accountNumberPrefixTypes) {
            optionDatas.add(entityAccountType(accountNumberPrefixType));
        }
        return optionDatas;
    }

    public static EnumOptionData entityAccountType(final AccountNumberPrefixType accountNumberPrefixType) {
        final EnumOptionData optionData = new EnumOptionData(accountNumberPrefixType.getValue().longValue(),
                accountNumberPrefixType.getCode(), accountNumberPrefixType.toString());
        return optionData;
    }

    public static List<EnumOptionData> accountNumberPrefixType(Object[] array) {
        AccountNumberPrefixType[] accountNumberPrefixTypes = Arrays.copyOf(array, array.length, AccountNumberPrefixType[].class);
        return accountNumberPrefixType(accountNumberPrefixTypes);
    }

}