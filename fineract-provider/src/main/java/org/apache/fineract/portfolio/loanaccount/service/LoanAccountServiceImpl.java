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

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

@RequiredArgsConstructor
public class LoanAccountServiceImpl implements LoanAccountService {

    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanTransactionRepository loanTransactionRepository;

    @Override
    public LoanTransaction saveLoanTransactionWithDataIntegrityViolationChecks(LoanTransaction newRepaymentTransaction) {
        try {
            return this.loanTransactionRepository.saveAndFlush(newRepaymentTransaction);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            raiseValidationExceptionForUniqueConstraintViolation(e);
            throw e;
        }
    }

    @Override
    public Loan saveAndFlushLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            return this.loanRepositoryWrapper.saveAndFlush(loan);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            raiseValidationExceptionForUniqueConstraintViolation(e);
            throw e;
        }
    }

    @Override
    public Loan saveLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            return this.loanRepositoryWrapper.save(loan);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            raiseValidationExceptionForUniqueConstraintViolation(e);
            throw e;
        }
    }

    private void raiseValidationExceptionForUniqueConstraintViolation(Exception e) {
        final Throwable realCause = e.getCause();
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
        if (realCause.getMessage().toLowerCase().contains("external_id_unique") || realCause.getMessage()
                .contains("duplicate key value violates unique constraint \"m_loan_transaction_external_id_key\"")) {
            baseDataValidator.reset().parameter("externalId").failWithCode("value.must.be.unique");
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors, e);
        }
    }

}
