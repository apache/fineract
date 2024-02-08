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
package org.apache.fineract.portfolio.repaymentwithpostdatedchecks.starter;

import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksAssembler;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksReadPlatformService;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksReadPlatformServiceImpl;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksWritePlatformService;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksWritePlatformServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepaymentWithPostDatedChecksConfiguration {

    @Bean
    @ConditionalOnMissingBean(RepaymentWithPostDatedChecksAssembler.class)
    public RepaymentWithPostDatedChecksAssembler repaymentWithPostDatedChecksAssembler(FromJsonHelper fromJsonHelper) {
        return new RepaymentWithPostDatedChecksAssembler(fromJsonHelper);
    }

    @Bean
    @ConditionalOnMissingBean(RepaymentWithPostDatedChecksReadPlatformService.class)
    public RepaymentWithPostDatedChecksReadPlatformService repaymentWithPostDatedChecksReadPlatformService(
            PostDatedChecksRepository postDatedChecksRepository, LoanRepository loanRepository,
            LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository) {
        return new RepaymentWithPostDatedChecksReadPlatformServiceImpl(postDatedChecksRepository, loanRepository,
                loanRepaymentScheduleInstallmentRepository);
    }

    @Bean
    @ConditionalOnMissingBean(RepaymentWithPostDatedChecksWritePlatformService.class)
    public RepaymentWithPostDatedChecksWritePlatformService repaymentWithPostDatedChecksWritePlatformService(
            PostDatedChecksRepository postDatedChecksRepository, FromJsonHelper fromApiJsonHelper, LoanRepository loanRepository) {
        return new RepaymentWithPostDatedChecksWritePlatformServiceImpl(postDatedChecksRepository, fromApiJsonHelper, loanRepository);
    }
}
