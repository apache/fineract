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
package org.apache.fineract.accounting.common;

public final class AccountingValidations {

    private AccountingValidations() {}

    public static boolean isCashBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.CASH_BASED.getValue().equals(accountingRuleType);
    }

    public static boolean isAccrualPeriodicBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.ACCRUAL_PERIODIC.getValue().equals(accountingRuleType);
    }

    public static boolean isUpfrontAccrualAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.ACCRUAL_UPFRONT.getValue().equals(accountingRuleType);
    }

    public static boolean isAccrualBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.ACCRUAL_PERIODIC.getValue().equals(accountingRuleType)
                || AccountingRuleType.ACCRUAL_UPFRONT.getValue().equals(accountingRuleType);
    }

    public static boolean isCashOrAccrualBasedAccounting(final Integer accountingRuleType) {
        return isCashBasedAccounting(accountingRuleType) || isAccrualBasedAccounting(accountingRuleType);
    }

}
