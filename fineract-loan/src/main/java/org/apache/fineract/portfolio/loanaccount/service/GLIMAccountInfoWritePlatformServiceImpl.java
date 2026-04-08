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

package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.GroupLoanIndividualMonitoringAccount;

@RequiredArgsConstructor
public class GLIMAccountInfoWritePlatformServiceImpl implements GLIMAccountInfoWritePlatformService {

    private final GLIMAccountInfoRepository glimAccountRepository;

    @Override
    public GroupLoanIndividualMonitoringAccount createGLIMAccount(String accountNumber, Group group, BigDecimal principalAmount,
            Long childAccountsCount, Boolean isAcceptingChild, Integer loanStatus, BigDecimal applicationId) {

        GroupLoanIndividualMonitoringAccount glimAccountInfo = GroupLoanIndividualMonitoringAccount.getInstance(accountNumber, group,
                principalAmount, childAccountsCount, isAcceptingChild, loanStatus, applicationId);

        return this.glimAccountRepository.save(glimAccountInfo);
    }

    @Override
    public void setIsAcceptingChild(GroupLoanIndividualMonitoringAccount glimAccount) {
        glimAccount.setIsAcceptingChild(true);
        glimAccountRepository.save(glimAccount);

    }

    @Override
    public void resetIsAcceptingChild(GroupLoanIndividualMonitoringAccount glimAccount) {
        glimAccount.setIsAcceptingChild(false);
        glimAccountRepository.save(glimAccount);

    }

    @Override
    public void incrementChildAccountCount(GroupLoanIndividualMonitoringAccount glimAccount) {
        long count = glimAccount.getChildAccountsCount();
        glimAccount.setChildAccountsCount(count + 1);
        glimAccountRepository.save(glimAccount);

    }

}
