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
package org.apache.fineract.portfolio.savings;

public class SavingsTransactionBooleanValues {

    private final boolean isAccountTransfer;
    private final boolean isRegularTransaction;
    private final boolean isApplyWithdrawFee;
    private final boolean isInterestTransfer;
    private final boolean isExceptionForBalanceCheck;

    public SavingsTransactionBooleanValues(final boolean isAccountTransfer, final boolean isRegularTransaction,
            final boolean isApplyWithdrawFee, final boolean isInterestTransfer, final boolean isExceptionForBalanceCheck) {

        this.isAccountTransfer = isAccountTransfer;
        this.isRegularTransaction = isRegularTransaction;
        this.isApplyWithdrawFee = isApplyWithdrawFee;
        this.isInterestTransfer = isInterestTransfer;
        this.isExceptionForBalanceCheck = isExceptionForBalanceCheck;
    }

    public boolean isAccountTransfer() {
        return this.isAccountTransfer;
    }

    public boolean isRegularTransaction() {
        return this.isRegularTransaction;
    }

    public boolean isApplyWithdrawFee() {
        return this.isApplyWithdrawFee;
    }

    public boolean isInterestTransfer() {
        return this.isInterestTransfer;
    }

    public boolean isExceptionForBalanceCheck() {
        return this.isExceptionForBalanceCheck;
    }

}
