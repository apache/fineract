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

import static org.apache.fineract.infrastructure.core.service.MeasuringUtil.measure;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetLoanDelinquencyTagsBusinessStep implements LoanCOBBusinessStep {

    private final LoanAccountDomainService loanAccountDomainService;

    @Override
    public Loan execute(Loan loan) {
        if (loan == null) {
            log.debug("Ignoring delinquency tag processing for null loan.");
            return null;
        }

        String externalId = Optional.ofNullable(loan.getExternalId()).map(ExternalId::getValue).orElse(null);
        measure(() -> {
            try {
                log.debug("Starting delinquency tag processing for loan with Id [{}], account number [{}], external Id [{}]", loan.getId(),
                        loan.getAccountNumber(), externalId);

                // Change the Action Context to DEFAULT for Business Date so that we can compare the loan due date to
                // the
                // current date and not the previous (COB) date.
                ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
                loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());
            } catch (RuntimeException re) {
                log.error(
                        "Received [{}] exception while processing delinquency tag for loan with Id [{}], account number [{}], external Id [{}]",
                        re.getMessage(), loan.getId(), loan.getAccountNumber(), externalId, re);

                throw re;
            } finally {
                // Change the Action Context back to COB to resume COB steps.
                ThreadLocalContextUtil.setActionContext(ActionContext.COB);
            }
        }, duration -> {
            log.debug("Ending delinquency tag processing for loan with Id [{}], account number [{}], external Id [{}], finished in [{}]ms",
                    loan.getId(), loan.getAccountNumber(), externalId, duration.toMillis());
        });

        return loan;
    }

    @Override
    public String getEnumStyledName() {
        return "LOAN_DELINQUENCY_CLASSIFICATION";
    }

    @Override
    public String getHumanReadableName() {
        return "Loan Delinquency Classification";
    }

}
