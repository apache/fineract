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
package org.apache.fineract.cob.loan;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualActivityProcessingService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccrualActivityPostingBusinessStep implements LoanCOBBusinessStep {

    private final LoanAccrualActivityProcessingService loanAccrualActivityProcessingService;

    @Override
    public Loan execute(Loan loan) {
        log.debug("start processing loan accrual activity posting on installment due date with id [{}]", loan.getId());
        final LocalDate currentDate = DateUtils.getBusinessLocalDate();

        // check if loan capable for posting
        loanAccrualActivityProcessingService.makeAccrualActivityTransaction(loan, currentDate);

        log.debug("end processing loan accrual activity posting on installment due date with id [{}]", loan.getId());
        return loan;
    }

    @Override
    public String getEnumStyledName() {
        return "ACCRUAL_ACTIVITY_POSTING";
    }

    @Override
    public String getHumanReadableName() {
        return "Accrual Activity Posting on Installment Due Date";
    }

}
