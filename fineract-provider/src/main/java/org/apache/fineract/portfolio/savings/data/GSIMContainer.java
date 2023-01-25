
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
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.apache.fineract.portfolio.accountdetails.data.SavingsSummaryCustom;

@Entity
public class GSIMContainer {

    private BigDecimal gsimId;

    private BigDecimal groupId;

    private String accountNumber;

    private List<SavingsSummaryCustom> childGSIMAccounts;

    private BigDecimal parentBalance;

    private String savingsStatus;
    @Id
    private Long id;
    private String name;
    private Long officeId;

    public GSIMContainer() {}

    public GSIMContainer(final BigDecimal gsimId, final BigDecimal groupId, final String accountNumber,
            final List<SavingsSummaryCustom> childGSIMAccounts, final BigDecimal parentBalance, final String savingsStatus) {
        this.gsimId = gsimId;
        this.groupId = groupId;
        this.accountNumber = accountNumber;
        this.childGSIMAccounts = childGSIMAccounts;
        this.parentBalance = parentBalance;
        this.savingsStatus = savingsStatus;

    }

    public BigDecimal getGsimId() {
        return gsimId;
    }

    public BigDecimal getGroupId() {
        return groupId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public List<SavingsSummaryCustom> getChildGSIMAccounts() {
        return childGSIMAccounts;
    }

    public BigDecimal getparentBalance() {
        return parentBalance;
    }

    public String getSavingsStatus() {
        return savingsStatus;
    }

    public BigDecimal getParentBalance() {
        return parentBalance;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }
}
