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

package org.apache.fineract.portfolio.savings.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.Set;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.group.domain.Group;

@Entity
@Table(name = "gsim_accounts", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_number" }, name = "gsim_id") })
public final class GroupSavingsIndividualMonitoring extends AbstractPersistableCustom {

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "parent_deposit")
    private BigDecimal parentDeposit;

    @Column(name = "child_accounts_count")
    private Long childAccountsCount;

    @Column(name = "accepting_child")
    private Boolean isAcceptingChild;

    @OneToMany
    private Set<SavingsAccount> childSaving;

    @Column(name = "savings_status_id", nullable = false)
    private Integer savingsStatus;

    @Column(name = "application_id", nullable = true)
    private BigDecimal applicationId;

    private GroupSavingsIndividualMonitoring() {}

    private GroupSavingsIndividualMonitoring(String accountNumber, Group group, BigDecimal parentDeposit, Long childAccountsCount,
            Boolean isAcceptingChild, Integer savingsStatus, BigDecimal applicationId) {
        this.accountNumber = accountNumber;
        this.group = group;
        this.parentDeposit = parentDeposit;
        this.childAccountsCount = childAccountsCount;
        this.isAcceptingChild = isAcceptingChild;
        this.savingsStatus = savingsStatus;
        this.applicationId = applicationId;

    }

    public static GroupSavingsIndividualMonitoring getInstance(String accountNumber, Group group, BigDecimal parentDeposit,
            Long childAccountsCount, Boolean isAcceptingChild, Integer savingsStatus, BigDecimal applicationId) {
        return new GroupSavingsIndividualMonitoring(accountNumber, group, parentDeposit, childAccountsCount, isAcceptingChild,
                savingsStatus, applicationId);
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getParentDeposit() {
        return parentDeposit;
    }

    public void setParentDeposit(BigDecimal parentDeposit) {
        this.parentDeposit = parentDeposit;
    }

    public Long getChildAccountsCount() {
        return childAccountsCount;
    }

    public void setChildAccountsCount(Long childAccountsCount) {
        this.childAccountsCount = childAccountsCount;
    }

    public Boolean getIsAcceptingChild() {
        return isAcceptingChild;
    }

    public void setIsAcceptingChild(Boolean isAcceptingChild) {
        this.isAcceptingChild = isAcceptingChild;
    }

    public Set<SavingsAccount> getChildSaving() {
        return childSaving;
    }

    public void setChildSaving(Set<SavingsAccount> childSaving) {
        this.childSaving = childSaving;
    }

    public Integer getSavingsStatus() {
        return savingsStatus;
    }

    public void setSavingsStatus(Integer savingsStatus) {
        this.savingsStatus = savingsStatus;
    }
}
