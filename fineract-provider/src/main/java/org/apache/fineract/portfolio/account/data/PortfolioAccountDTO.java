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
package org.apache.fineract.portfolio.account.data;

public class PortfolioAccountDTO {

    private final Integer accountTypeId;
    private final Long clientId;
    private Long groupId;
    private final String currencyCode;
    private final long[] accountStatus;
    private final Integer depositType;
    private final boolean excludeOverDraftAccounts;

    public PortfolioAccountDTO(final Integer accountTypeId, final Long clientId, final String currencyCode, final long[] accountStatus,
            final Integer depositType, final boolean excludeOverDraftAccounts) {
        this.accountTypeId = accountTypeId;
        this.clientId = clientId;
        this.currencyCode = currencyCode;
        this.accountStatus = accountStatus;
        this.depositType = depositType;
        this.excludeOverDraftAccounts = excludeOverDraftAccounts;
    }

    public PortfolioAccountDTO(final Integer accountTypeId, final Long clientId, final long[] accountStatus) {
        this.accountTypeId = accountTypeId;
        this.clientId = clientId;
        this.currencyCode = null;
        this.accountStatus = accountStatus;
        this.depositType = null;
        this.excludeOverDraftAccounts = false;
    }
    
    public PortfolioAccountDTO(final Integer accountTypeId, final Long clientId, final String currencyCode, final long[] accountStatus,
            final Integer depositType) {
        this.accountTypeId = accountTypeId;
        this.clientId = clientId;
        this.currencyCode = currencyCode;
        this.accountStatus = accountStatus;
        this.depositType = depositType;
        this.excludeOverDraftAccounts = false;
    }

    public Integer getAccountTypeId() {
        return this.accountTypeId;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public long[] getAccountStatus() {
        return this.accountStatus;
    }

    public Integer getDepositType() {
        return this.depositType;
    }

    public boolean isExcludeOverDraftAccounts() {
        return this.excludeOverDraftAccounts;
    }
    
    public Long getGroupId() {
        return this.groupId;
    }
    
    public void setGroupId(final Long groupId) {
        this.groupId = groupId;
    }

}
