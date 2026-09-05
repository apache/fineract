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

import java.math.BigDecimal;

/**
 * Constants for fixed and recurring deposit products and accounts. The types shared with plain savings -- interest
 * compounding, posting, calculation, days-in-year and period frequency -- live in {@link SavingsTestData}.
 */
public final class DepositTestData {

    public static final String MONTH_DAY_FORMAT = "dd MMM";
    public static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("100000");
    public static final BigDecimal RECURRING_DEPOSIT_AMOUNT = new BigDecimal("2000");

    private DepositTestData() {}

    public static final class PreClosurePenalInterestOnType {

        public static final int WHOLE_TERM = 1;
        public static final int TILL_PREMATURE_WITHDRAWAL = 2;

        private PreClosurePenalInterestOnType() {}
    }

    /** Value of {@code onAccountClosureId} on a premature close, and of {@code maturityInstructionId} on an account. */
    public static final class AccountClosureType {

        public static final int WITHDRAW_DEPOSIT = 100;
        public static final int TRANSFER_TO_SAVINGS = 200;
        public static final int REINVEST = 300;
        public static final int REINVEST_PRINCIPAL_ONLY = 400;

        private AccountClosureType() {}
    }
}
