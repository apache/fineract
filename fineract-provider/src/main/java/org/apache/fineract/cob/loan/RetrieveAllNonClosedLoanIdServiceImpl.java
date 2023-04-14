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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.data.LoanCOBParameter;
import org.apache.fineract.cob.data.LoanIdAndLastClosedBusinessDate;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;

@RequiredArgsConstructor
public class RetrieveAllNonClosedLoanIdServiceImpl implements RetrieveLoanIdService {

    private final LoanRepository loanRepository;

    @Override
    public LoanCOBParameter retrieveMinAndMaxLoanIdsNDaysBehind(Long numberOfDays, LocalDate businessDate) {
        return loanRepository.findMinAndMaxNonClosedLoanIdsByLastClosedBusinessDate(businessDate.minusDays(numberOfDays));
    }

    @Override
    public List<LoanIdAndLastClosedBusinessDate> retrieveLoanIdsBehindDateOrNull(LocalDate businessDate, List<Long> loanIds) {
        return loanRepository.findAllNonClosedLoansBehindOrNullByLoanIds(businessDate, loanIds);
    }

    @Override
    public List<LoanIdAndLastClosedBusinessDate> retrieveLoanIdsOldestCobProcessed(LocalDate businessDate) {
        return loanRepository.findOldestCOBProcessedLoan(businessDate);
    }

    @Override
    public List<Long> retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(LoanCOBParameter loanCOBParameter) {
        return loanRepository.findAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(loanCOBParameter.getMinLoanId(),
                loanCOBParameter.getMaxLoanId(),
                ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE).minusDays(LoanCOBConstant.NUMBER_OF_DAYS_BEHIND));
    }

}
