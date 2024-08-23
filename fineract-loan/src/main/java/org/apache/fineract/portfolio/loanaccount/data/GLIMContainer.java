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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.portfolio.accountdetails.data.LoanAccountSummaryData;

public class GLIMContainer {

    private final BigDecimal glimId;

    private final BigDecimal groupId;

    private final String accountNumber;

    private final List<LoanAccountSummaryData> childGLIMAccounts;

    private final BigDecimal parentPrincipalAmount;

    private final String loanStatus;

    public GLIMContainer(final BigDecimal glimId, final BigDecimal groupId, final String accountNumber,
            final List<LoanAccountSummaryData> childGLIMAccounts, final BigDecimal parentPrincipalAmount, final String loanStatus) {
        this.glimId = glimId;
        this.groupId = groupId;
        this.accountNumber = accountNumber;
        this.childGLIMAccounts = childGLIMAccounts;
        this.parentPrincipalAmount = parentPrincipalAmount;
        this.loanStatus = loanStatus;

    }

    public BigDecimal getGlimId() {
        return glimId;
    }

    public BigDecimal getGroupId() {
        return groupId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public List<LoanAccountSummaryData> getChildGLIMAccounts() {
        return childGLIMAccounts;
    }

    public BigDecimal getParentPrincipalAmount() {
        return parentPrincipalAmount;
    }

    public String getLoanStatus() {
        return loanStatus;
    }
}
