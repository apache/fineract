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

import jakarta.persistence.FlushModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.persistence.FlushModeHandler;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.ProgressiveLoanModel;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.ProgressiveTransactionCtx;
import org.apache.fineract.portfolio.loanaccount.repository.ProgressiveLoanModelRepository;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterestScheduleModelRepositoryWrapperImpl implements InterestScheduleModelRepositoryWrapper {

    private final ProgressiveLoanModelRepository loanModelRepository;
    private final ProgressiveLoanInterestScheduleModelParserService progressiveLoanInterestScheduleModelParserService;
    private final AdvancedPaymentScheduleTransactionProcessor advancedPaymentScheduleTransactionProcessor;
    private final FlushModeHandler flushModeHandler;

    @Transactional
    @Override
    public ProgressiveLoanInterestScheduleModel writeInterestScheduleModel(Loan loan, ProgressiveLoanInterestScheduleModel model) {
        if (model == null) {
            return null;
        }
        String jsonModel = progressiveLoanInterestScheduleModelParserService.toJson(model);
        flushModeHandler.withFlushMode(FlushModeType.COMMIT, () -> {
            ProgressiveLoanModel progressiveLoanModel = loanModelRepository.findOneByLoanId(loan.getId()).orElseGet(() -> {
                ProgressiveLoanModel plm = new ProgressiveLoanModel();
                plm.setLoan(loan);
                return plm;
            });
            progressiveLoanModel.setBusinessDate(ThreadLocalContextUtil.getBusinessDate());
            progressiveLoanModel.setLastModifiedDate(DateUtils.getAuditOffsetDateTime());
            progressiveLoanModel.setJsonModel(jsonModel);
            loanModelRepository.save(progressiveLoanModel);
        });
        return model;
    }

    @Override
    public Optional<ProgressiveLoanModel> findOneByLoanId(Long loanId) {
        final Optional[] progressiveLoanModel = new Optional[1];
        flushModeHandler.withFlushMode(FlushModeType.COMMIT, () -> {
            progressiveLoanModel[0] = loanModelRepository.findOneByLoanId(loanId);
        });
        return progressiveLoanModel[0];
    }

    @Override
    public Optional<ProgressiveLoanModel> findOneByLoan(Loan loan) {
        AtomicReference<Optional<ProgressiveLoanModel>> progressiveLoanModelRef = new AtomicReference<>();
        flushModeHandler.withFlushMode(FlushModeType.COMMIT, () -> {
            progressiveLoanModelRef.set(loanModelRepository.findOneByLoan(loan));
        });
        return progressiveLoanModelRef.get();
    }

    @Override
    public Optional<ProgressiveLoanInterestScheduleModel> extractModel(Optional<ProgressiveLoanModel> progressiveLoanModel) {
        return progressiveLoanModel.map(ProgressiveLoanModel::getJsonModel) //
                .map(jsonModel -> progressiveLoanInterestScheduleModelParserService.fromJson(jsonModel,
                        progressiveLoanModel.get().getLoan().getLoanProductRelatedDetail(), MoneyHelper.getMathContext(),
                        progressiveLoanModel.get().getLoan().getLoanProductRelatedDetail().getInstallmentAmountInMultiplesOf()));
    }

    @Override
    public boolean hasValidModelForDate(Long loanId, LocalDate targetDate) {
        Optional<ProgressiveLoanModel> progressiveLoanModel = findOneByLoanId(loanId);
        LocalDate modelUpdatedTilDate = progressiveLoanModel.map(ProgressiveLoanModel::getBusinessDate).orElse(null);
        return progressiveLoanModel.isPresent() && !targetDate.isBefore(modelUpdatedTilDate);
    }

    @Override
    public Optional<ProgressiveLoanInterestScheduleModel> getSavedModel(Loan loan, LocalDate businessDate) {
        Optional<ProgressiveLoanModel> progressiveLoanModel = findOneByLoanId(loan.getId());
        Optional<ProgressiveLoanInterestScheduleModel> savedModel;
        if (progressiveLoanModel.isPresent() && !progressiveLoanModel.get().getBusinessDate().isAfter(businessDate)) {
            savedModel = extractModel(progressiveLoanModel);
            if (savedModel.isPresent() && progressiveLoanModel.get().getBusinessDate().isBefore(businessDate)) {
                ProgressiveTransactionCtx ctx = new ProgressiveTransactionCtx(loan.getCurrency(), loan.getRepaymentScheduleInstallments(),
                        Set.of(), new MoneyHolder(loan.getTotalOverpaidAsMoney()), new ChangedTransactionDetail(), savedModel.get());
                ctx.setChargedOff(loan.isChargedOff());
                ctx.setContractTerminated(loan.isContractTermination());
                advancedPaymentScheduleTransactionProcessor.recalculateInterestForDate(businessDate, ctx);
            }
        } else {
            savedModel = Optional.empty();
        }
        return savedModel;
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
