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

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.data.LoanPointInTimeData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.arrears.LoanArrearsData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanPointInTimeServiceImpl implements LoanPointInTimeService {

    private final LoanUtilService loanUtilService;
    private final LoanScheduleService loanScheduleService;
    private final LoanAssembler loanAssembler;
    private final LoanPointInTimeData.Mapper dataMapper;
    private final EntityManager entityManager;
    private final LoanArrearsAgingService arrearsAgingService;

    @Override
    public LoanPointInTimeData retrieveAt(Long loanId, LocalDate date) {
        entityManager.setFlushMode(FlushModeType.COMMIT);
        validateSingularRetrieval(loanId, date);

        // Note: since everything is running in a readOnly transaction
        // whatever we modify on the loan is not going to be propagated to the DB
        // Note2: Interest is always calculated against the current date of the system so we need to roll time back
        HashMap<BusinessDateType, LocalDate> originalBDs = ThreadLocalContextUtil.getBusinessDates();
        try {
            ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, date)));

            Loan loan = loanAssembler.assembleFrom(loanId);
            removeAfterDateTransactions(loan, date);
            removeAfterDateCharges(loan, date);

            ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null, null);
            loanScheduleService.recalculateSchedule(loan, scheduleGeneratorDTO);

            LoanArrearsData arrearsData = arrearsAgingService.calculateArrearsForLoan(loan);

            LoanPointInTimeData result = dataMapper.map(loan);
            result.setArrears(arrearsData);
            return result;
        } finally {
            entityManager.clear();
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            ThreadLocalContextUtil.setBusinessDates(originalBDs);
        }
    }

    private void removeAfterDateCharges(Loan loan, LocalDate date) {
        loan.removeCharges(c -> DateUtils.isAfter(c.getEffectiveDueDate(), date));
    }

    private void removeAfterDateTransactions(Loan loan, LocalDate date) {
        loan.removeLoanTransactions(tx -> DateUtils.isAfter(tx.getTransactionDate(), date));
    }

    private void validateSingularRetrieval(Long loanId, LocalDate date) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        baseDataValidator.reset().parameter("loanId").value(loanId).notNull();
        baseDataValidator.reset().parameter("date").value(date).notNull().notBlank();
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    @Override
    public List<LoanPointInTimeData> retrieveAt(List<Long> loanIds, LocalDate date) {
        validateBulkRetrieval(loanIds, date);
        List<LoanPointInTimeData> result = loanIds.stream().map(loanId -> retrieveAt(loanId, date)).toList();
        TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
        return result;
    }

    private void validateBulkRetrieval(List<Long> loanIds, LocalDate date) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
        baseDataValidator.reset().parameter("loanIds").value(loanIds).notNull().listNotEmpty();
        baseDataValidator.reset().parameter("date").value(date).notNull().notBlank();
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
