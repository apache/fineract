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

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.loanaccount.data.AmortizationAllocationBaseTransactionDTO;
import org.apache.fineract.portfolio.loanaccount.data.AmortizationAllocationMappingDTO;
import org.apache.fineract.portfolio.loanaccount.data.LoanAmortizationAllocationData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAmortizationAllocationData.AmortizationMappingData;
import org.apache.fineract.portfolio.loanaccount.domain.AmortizationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAmortizationAllocationMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAmortizationAllocationMappingRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.springframework.dao.EmptyResultDataAccessException;

@RequiredArgsConstructor
public class LoanAmortizationAllocationServiceImpl implements LoanAmortizationAllocationService {

    private final LoanAmortizationAllocationMappingRepository loanAmortizationAllocationMappingRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanCapitalizedIncomeBalanceRepository capitalizedIncomeBalanceRepository;
    private final LoanBuyDownFeeBalanceRepository buyDownFeeBalanceRepository;

    @Override
    public LoanAmortizationAllocationData retrieveLoanAmortizationAllocationsForBuyDownFeeTransaction(final Long loanTransactionId,
            final Long loanId) {
        final LoanTransaction loanTransaction = this.loanTransactionRepository.findByIdAndLoanId(loanTransactionId, loanId)
                .orElseThrow(() -> new LoanTransactionNotFoundException(loanTransactionId));
        if (!LoanTransactionType.BUY_DOWN_FEE.equals(loanTransaction.getTypeOf())) {
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.buydown.fee.transaction",
                    "Transaction with ID " + loanTransactionId + " is not a Buy Down Fee transaction");
        }
        return retrieveLoanAmortizationAllocationData(loanTransaction, loanId);
    }

    @Override
    public LoanAmortizationAllocationData retrieveLoanAmortizationAllocationsForCapitalizedIncomeTransaction(final Long loanTransactionId,
            final Long loanId) {
        final LoanTransaction loanTransaction = this.loanTransactionRepository.findByIdAndLoanId(loanTransactionId, loanId)
                .orElseThrow(() -> new LoanTransactionNotFoundException(loanTransactionId));
        if (!LoanTransactionType.CAPITALIZED_INCOME.equals(loanTransaction.getTypeOf())) {
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.capitalized.income.transaction",
                    "Transaction with ID " + loanTransactionId + " is not a Capitalized Income transaction");
        }
        return retrieveLoanAmortizationAllocationData(loanTransaction, loanId);
    }

    @Override
    public BigDecimal calculateAlreadyAmortizedAmount(final Long loanTransactionId, final Long loanId) {
        return loanAmortizationAllocationMappingRepository.calculateAlreadyAmortizedAmount(loanTransactionId, loanId);
    }

    private LoanAmortizationAllocationData retrieveLoanAmortizationAllocationData(final LoanTransaction loanTransaction,
            final Long loanId) {
        try {
            final Long loanTransactionId = loanTransaction.getId();
            final AmortizationAllocationBaseTransactionDTO baseTransactionInfo = getBaseTransactionInfo(loanTransaction, loanId);
            if (baseTransactionInfo == null) {
                throw new LoanTransactionNotFoundException(loanTransactionId);
            }

            final List<AmortizationAllocationMappingDTO> amortizationMappings = loanAmortizationAllocationMappingRepository
                    .findAmortizationMappingsByBaseTransactionAndLoan(loanTransactionId, loanId);

            final List<AmortizationMappingData> mappings = amortizationMappings.stream()
                    .map(dto -> AmortizationMappingData.builder().amortizationLoanTransactionId(dto.getAmortizationLoanTransactionId())
                            .amortizationLoanTransactionExternalId(dto.getAmortizationLoanTransactionExternalId())
                            .date(dto.getAmortizationDate()).type(dto.getAmortizationType()).amount(dto.getAmount()).build())
                    .toList();

            return new LoanAmortizationAllocationData(baseTransactionInfo.getLoanId(), baseTransactionInfo.getLoanExternalId(),
                    baseTransactionInfo.getBaseLoanTransactionId(), baseTransactionInfo.getBaseLoanTransactionDate(),
                    baseTransactionInfo.getBaseLoanTransactionAmount(), baseTransactionInfo.getUnrecognizedAmount(),
                    baseTransactionInfo.getChargedOffAmount(), baseTransactionInfo.getAdjustmentAmount(), mappings);
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanTransactionNotFoundException(loanTransaction.getId(), e);
        }
    }

    private AmortizationAllocationBaseTransactionDTO getBaseTransactionInfo(final LoanTransaction loanTransaction, final Long loanId) {
        if (loanTransaction.isBuyDownFee()) {
            return buyDownFeeBalanceRepository.findBaseTransactionInfo(loanTransaction.getId(), loanId);
        } else {
            return capitalizedIncomeBalanceRepository.findBaseTransactionInfo(loanTransaction.getId(), loanId);
        }
    }

    @Override
    public LoanAmortizationAllocationMapping createAmortizationAllocationMappingWithBaseLoanTransaction(
            final LoanTransaction loanTransaction, final BigDecimal amount, final AmortizationType amortizationType) {
        return new LoanAmortizationAllocationMapping(loanTransaction.getLoan().getId(), loanTransaction.getId(), null, null,
                amortizationType, amount);
    }

    @Override
    public void setAmortizationTransactionDataAndSaveAmortizationAllocationMapping(
            final LoanAmortizationAllocationMapping amortizationAllocationMapping, final LoanTransaction amortizationTransaction) {
        final LoanAmortizationAllocationMapping updatedMapping = new LoanAmortizationAllocationMapping(
                amortizationAllocationMapping.getLoanId(), amortizationAllocationMapping.getBaseLoanTransactionId(),
                amortizationTransaction.getTransactionDate(), amortizationTransaction.getId(),
                amortizationAllocationMapping.getAmortizationType(), amortizationAllocationMapping.getAmount());
        loanAmortizationAllocationMappingRepository.save(updatedMapping);
    }

}
