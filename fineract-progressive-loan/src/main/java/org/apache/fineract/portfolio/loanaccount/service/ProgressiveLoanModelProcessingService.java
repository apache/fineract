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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.repository.ProgressiveLoanModelRepository;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProgressiveLoanModelProcessingService {

    private static final List<LoanStatus> allowedLoanStatuses = List.of(LoanStatus.ACTIVE, LoanStatus.CLOSED_OBLIGATIONS_MET,
            LoanStatus.CLOSED_WRITTEN_OFF, LoanStatus.OVERPAID);
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ProgressiveLoanModelRecalculationService modelProcessingService;
    private final InterestScheduleModelRepositoryWrapper modelRepositoryWrapper;
    private final ProgressiveLoanModelRepository progressiveLoanModelRepository;

    public boolean hasValidModel(Long loanId, String modelVersion) {
        return progressiveLoanModelRepository.hasValidModel(loanId, modelVersion);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recalculateModelAndSave(Long loanId) {
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        ProgressiveLoanInterestScheduleModel recalculatedModel = modelProcessingService.getRecalculatedModel(loan.getId(),
                ThreadLocalContextUtil.getBusinessDate());
        if (recalculatedModel != null) {
            modelRepositoryWrapper.writeInterestScheduleModel(loan, recalculatedModel);
        }
    }

    public boolean allowedLoanStatuses(Long loanId) {
        return loanRepositoryWrapper.isLoanInAllowedStatus(loanId, allowedLoanStatuses);
    }
}
