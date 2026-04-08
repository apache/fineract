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

package org.apache.fineract.portfolio.loanaccount.domain;

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
@Table(name = "glim_accounts", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_number" }, name = "FK_glim_id") })
public class GroupLoanIndividualMonitoringAccount extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "principal_amount")
    private BigDecimal principalAmount;

    @Column(name = "child_accounts_count")
    private Long childAccountsCount;

    @Column(name = "accepting_child")
    private Boolean isAcceptingChild;

    @OneToMany
    private Set<Loan> childLoan;

    @Column(name = "loan_status_id", nullable = false)
    private Integer loanStatus;

    @Column(name = "application_id", nullable = true)
    private BigDecimal applicationId;

    protected GroupLoanIndividualMonitoringAccount() {}

    private GroupLoanIndividualMonitoringAccount(String accountNumber, Group group, BigDecimal principalAmount, Long childAccountsCount,
            Boolean isAcceptingChild, Integer loanStatus, BigDecimal applicationId) {
        this.accountNumber = accountNumber;
        this.group = group;
        this.principalAmount = principalAmount;
        this.childAccountsCount = childAccountsCount;
        this.isAcceptingChild = isAcceptingChild;
        this.loanStatus = loanStatus;
        this.applicationId = applicationId;
    }

    public static GroupLoanIndividualMonitoringAccount getInstance(String accountNumber, Group group, BigDecimal principalAmount,
            Long childAccountsCount, Boolean isAcceptingChild, Integer loanStatus, BigDecimal applicationId) {
        return new GroupLoanIndividualMonitoringAccount(accountNumber, group, principalAmount, childAccountsCount, isAcceptingChild,
                loanStatus, applicationId);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Set<Loan> getChildLoan() {
        return childLoan;
    }

    public void setChildLoan(Set<Loan> childLoan) {
        this.childLoan = childLoan;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
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

    public Integer getLoanStatus() {
        return loanStatus;
    }

    public void setLoanStatus(Integer loanStatus) {
        this.loanStatus = loanStatus;
    }

    public BigDecimal getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(BigDecimal applicationId) {
        this.applicationId = applicationId;
    }
}
