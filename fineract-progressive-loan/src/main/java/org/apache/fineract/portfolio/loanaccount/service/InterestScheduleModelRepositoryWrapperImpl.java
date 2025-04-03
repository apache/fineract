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

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.ProgressiveLoanModel;
import org.apache.fineract.portfolio.loanaccount.repository.ProgressiveLoanModelRepository;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterestScheduleModelRepositoryWrapperImpl implements InterestScheduleModelRepositoryWrapper {

    private final ProgressiveLoanModelRepository loanModelRepository;
    private final ProgressiveLoanInterestScheduleModelParserService progressiveLoanInterestScheduleModelParserService;

    @Transactional
    @Override
    public String writeInterestScheduleModel(Loan loan, ProgressiveLoanInterestScheduleModel model) {
        if (model == null) {
            return null;
        }
        String jsonModel = progressiveLoanInterestScheduleModelParserService.toJson(model);
        ProgressiveLoanModel progressiveLoanModel = loanModelRepository.findOneByLoanId(loan.getId()).orElseGet(() -> {
            ProgressiveLoanModel plm = new ProgressiveLoanModel();
            plm.setLoan(loan);
            return plm;
        });
        progressiveLoanModel.setBusinessDate(ThreadLocalContextUtil.getBusinessDate());
        progressiveLoanModel.setLastModifiedDate(DateUtils.getAuditOffsetDateTime());
        progressiveLoanModel.setJsonModel(jsonModel);
        loanModelRepository.save(progressiveLoanModel);
        return jsonModel;
    }

    @Override
    public Optional<ProgressiveLoanInterestScheduleModel> readProgressiveLoanInterestScheduleModel(final Long loanId,
            final LoanProductMinimumRepaymentScheduleRelatedDetail detail, final Integer installmentAmountInMultipliesOf) {
        return loanModelRepository.findOneByLoanId(loanId) //
                .map(ProgressiveLoanModel::getJsonModel) //
                .map(jsonModel -> progressiveLoanInterestScheduleModelParserService.fromJson(jsonModel, detail,
                        MoneyHelper.getMathContext(), installmentAmountInMultipliesOf)); //
    }
}
