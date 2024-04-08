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
package org.apache.fineract.test.data;

public enum ChargeTimeType {

    DISBURSEMENT(1), SPECIFIED_DUE_DATE(2), SAVINGS_ACTIVATION(3), WITHDRAWAL_FEE(5), ANNUAL_FEE(6), MONTHLY_FEE(7), INSTALLMENT_FEE(
            8), OVERDUE_FEES(9), OVERDRAFT_FEE(10), WEEKLY_FEE(11), TRANCHE_DISBURSEMENT(
                    12), SHARE_ACCOUNT_ACTIVATE(13), SHARE_PURCHASE(14), SHARE_REDEEM(15), SAVING_NO_ACTIVITY_FEE(16);

    public final Integer value;

    ChargeTimeType(Integer value) {
        this.value = value;
    }
}
