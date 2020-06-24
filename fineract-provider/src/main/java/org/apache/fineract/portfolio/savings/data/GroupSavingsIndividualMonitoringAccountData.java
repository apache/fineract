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

package org.apache.fineract.portfolio.savings.data;

import java.math.BigDecimal;

public final class GroupSavingsIndividualMonitoringAccountData {

    private final BigDecimal gsimId;

    private final BigDecimal groupId;

    private final BigDecimal clientId;

    private final String accountNumber;

    private final BigDecimal childAccountId;

    private final String childAccountNumber;

    private final BigDecimal childDeposit;

    private final BigDecimal parentDeposit;

    private final Long childAccountsCount;

    private final String savingsStatus;

    private GroupSavingsIndividualMonitoringAccountData(final BigDecimal glimId, final BigDecimal groupId, final BigDecimal clientId,
            final String accountNumber, final BigDecimal childAccountId, final String childAccountNumber, final BigDecimal childDeposit,
            final BigDecimal parentDeposit, final Long childAccountsCount, final String savingsStatus) {
        this.gsimId = glimId;
        this.groupId = groupId;
        this.clientId = clientId;
        this.accountNumber = accountNumber;
        this.childAccountId = childAccountId;
        this.childAccountNumber = childAccountNumber;
        this.childDeposit = childDeposit;
        this.parentDeposit = parentDeposit;
        this.childAccountsCount = childAccountsCount;
        this.savingsStatus = savingsStatus;
    }

    public static GroupSavingsIndividualMonitoringAccountData getInstance(final BigDecimal glimId, final BigDecimal groupId,
            final String accountNumber, final String childAccountNumber, final BigDecimal childDeposit, final BigDecimal parentDeposit,
            final Long childAccountsCount, final String savingsStatus) {
        return new GroupSavingsIndividualMonitoringAccountData(glimId, groupId, null, accountNumber, null, childAccountNumber, childDeposit,
                parentDeposit, childAccountsCount, savingsStatus);
    }

    public static GroupSavingsIndividualMonitoringAccountData getInstance1(final BigDecimal glimId, final BigDecimal groupId,
            final String accountNumber, final BigDecimal parentDeposit, final String savingsStatus) {
        return new GroupSavingsIndividualMonitoringAccountData(glimId, groupId, null, accountNumber, null, null, null, parentDeposit, null,
                savingsStatus);
    }

    public static GroupSavingsIndividualMonitoringAccountData getInstance2(final BigDecimal glimId, final BigDecimal groupId,
            final BigDecimal clientId, final String accountNumber, final BigDecimal childAccountId, final String childAccountNumber,
            final BigDecimal childDeposit, final BigDecimal parentDeposit, final Long childAccountsCount, final String savingsStatus) {
        return new GroupSavingsIndividualMonitoringAccountData(glimId, groupId, clientId, accountNumber, childAccountId, childAccountNumber,
                childDeposit, parentDeposit, childAccountsCount, savingsStatus);
    }

    public BigDecimal getGsimId() {
        return gsimId;
    }

    public BigDecimal getGroupId() {
        return groupId;
    }

    public BigDecimal getClientId() {
        return clientId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getChildAccountId() {
        return childAccountId;
    }

    public String getChildAccountNumber() {
        return childAccountNumber;
    }

    public BigDecimal getChildDeposit() {
        return childDeposit;
    }

    public BigDecimal getParentDeposit() {
        return parentDeposit;
    }

    public Long getChildAccountsCount() {
        return childAccountsCount;
    }

    public String getSavingsStatus() {
        return savingsStatus;
    }

}
