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
package org.apache.fineract.integrationtests.client.feign.modules;

public final class SavingsTestData {

    public static final String DATETIME_PATTERN = FeignTestConstants.DATETIME_PATTERN;
    public static final String LOCALE = FeignTestConstants.LOCALE;
    public static final String CURRENCY_CODE = "USD";
    public static final Double DEFAULT_CHARGE_AMOUNT = 100.0;

    private SavingsTestData() {}

    public static final class ChargeAppliesTo {

        public static final int SAVINGS = 2;

        private ChargeAppliesTo() {}
    }

    public static final class ChargeTimeType {

        public static final int WITHDRAWAL_FEE = 5;
        public static final int SAVINGS_ACTIVATION = 6;
        public static final int ANNUAL_FEE = 7;

        private ChargeTimeType() {}
    }

    public static final class ChargeCalculationType {

        public static final int FLAT = 1;

        private ChargeCalculationType() {}
    }

    public static final class InterestCompoundingPeriodType {

        public static final int DAILY = 1;
        public static final int MONTHLY = 4;
        public static final int QUARTERLY = 5;
        public static final int ANNUAL = 7;

        private InterestCompoundingPeriodType() {}
    }

    public static final class InterestPostingPeriodType {

        public static final int DAILY = 1;
        public static final int MONTHLY = 4;
        public static final int QUARTERLY = 5;
        public static final int ANNUAL = 7;

        private InterestPostingPeriodType() {}
    }

    public static final class InterestCalculationType {

        public static final int DAILY_BALANCE = 1;
        public static final int AVERAGE_DAILY_BALANCE = 2;

        private InterestCalculationType() {}
    }

    public static final class InterestCalculationDaysInYearType {

        public static final int DAYS_360 = 360;
        public static final int DAYS_365 = 365;

        private InterestCalculationDaysInYearType() {}
    }

    public static final class AccountingRule {

        public static final int NONE = 1;
        public static final int CASH_BASED = 2;
        public static final int ACCRUAL_PERIODIC = 3;

        private AccountingRule() {}
    }

    public static final class SavingsStatus {

        public static final int SUBMITTED = 100;
        public static final int APPROVED = 200;
        public static final int ACTIVE = 300;
        public static final int WITHDRAWN = 400;
        public static final int REJECTED = 500;
        public static final int CLOSED = 600;

        private SavingsStatus() {}
    }
}
