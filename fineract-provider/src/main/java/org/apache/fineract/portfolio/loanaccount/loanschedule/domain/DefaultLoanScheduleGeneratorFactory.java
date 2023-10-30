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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultLoanScheduleGeneratorFactory implements LoanScheduleGeneratorFactory {

    private final ProgressiveLoanScheduleGenerator progressiveLoanScheduleGenerator;
    private final CumulativeFlatInterestLoanScheduleGenerator cumulativeFlatInterestLoanScheduleGenerator;
    private final CumulativeDecliningBalanceInterestLoanScheduleGenerator cumulativeDecliningBalanceInterestLoanScheduleGenerator;

    @Override
    public LoanScheduleGenerator create(final LoanScheduleType loanScheduleType, final InterestMethod interestMethod) {
        return switch (loanScheduleType) {
            case CUMULATIVE -> cumulativeLoanScheduleGenerator(interestMethod);
            case PROGRESSIVE -> progressiveLoanScheduleGenerator(interestMethod);
        };
    }

    private LoanScheduleGenerator cumulativeLoanScheduleGenerator(final InterestMethod interestMethod) {
        return switch (interestMethod) {
            case FLAT -> cumulativeFlatInterestLoanScheduleGenerator;
            case DECLINING_BALANCE -> cumulativeDecliningBalanceInterestLoanScheduleGenerator;
            case INVALID -> null;
        };
    }

    private LoanScheduleGenerator progressiveLoanScheduleGenerator(final InterestMethod interestMethod) {
        return progressiveLoanScheduleGenerator;
    }

}
