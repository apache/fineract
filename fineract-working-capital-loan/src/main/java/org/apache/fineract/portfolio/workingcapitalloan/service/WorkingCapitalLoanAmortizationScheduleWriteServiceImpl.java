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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleGenerateRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: This is a temporary testing implementation. In the real flow, the amortization schedule
// will be generated and saved as part of the loan lifecycle (approve/disburse) — not via a
// standalone endpoint. The parameters will come from the loan entity + product, not from the
// request body. Replace this once the full WCL lifecycle is implemented.
@Service
@RequiredArgsConstructor
@Transactional
public class WorkingCapitalLoanAmortizationScheduleWriteServiceImpl implements WorkingCapitalLoanAmortizationScheduleWriteService {

    private static final MonetaryCurrency DEFAULT_CURRENCY = new MonetaryCurrency("USD", 2, null);

    private final WorkingCapitalLoanRepository loanRepository;
    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;

    @Override
    public void generateAndSaveAmortizationSchedule(final Long loanId, final ProjectedAmortizationScheduleGenerateRequest request) {
        final WorkingCapitalLoan loan = loanRepository.findById(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        final MathContext mc = MoneyHelper.getMathContext();

        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(//
                request.getOriginationFeeAmount(), //
                request.getNetDisbursementAmount(), //
                request.getTotalPaymentValue(), //
                request.getPeriodPaymentRate(), //
                request.getNpvDayCount(), //
                request.getExpectedDisbursementDate(), //
                mc, DEFAULT_CURRENCY);

        scheduleRepositoryWrapper.writeModel(loan, model);
    }

    @Override
    public void generateAndSaveAmortizationScheduleOnDisbursement(final WorkingCapitalLoan loan, final BigDecimal disbursedAmount,
            final LocalDate disbursementDate) {
        Validate.notNull(loan, "loan must not be null");
        Validate.notNull(disbursedAmount, "disbursedAmount must not be null");
        Validate.notNull(disbursementDate, "disbursementDate must not be null");

        final MathContext mc = MoneyHelper.getMathContext();
        final BigDecimal discount = loan.getLoanProductRelatedDetails() != null && loan.getLoanProductRelatedDetails().getDiscount() != null
                ? loan.getLoanProductRelatedDetails().getDiscount()
                : BigDecimal.ZERO;
        final BigDecimal totalPayment = loan.getBalance() != null && loan.getBalance().getTotalPayment() != null
                ? loan.getBalance().getTotalPayment()
                : BigDecimal.ZERO;
        final BigDecimal periodPaymentRate = loan.getLoanProductRelatedDetails() != null
                ? loan.getLoanProductRelatedDetails().getPeriodPaymentRate()
                : null;
        final Integer npvDayCount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getNpvDayCount()
                : null;

        Validate.isTrue(totalPayment.signum() > 0, "totalPayment must be positive");
        Validate.notNull(periodPaymentRate, "periodPaymentRate must not be null");
        Validate.notNull(npvDayCount, "npvDayCount must not be null");

        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(discount, disbursedAmount,
                totalPayment, periodPaymentRate, npvDayCount, disbursementDate, mc, resolveCurrency(loan));
        scheduleRepositoryWrapper.writeModel(loan, model);
    }

    @Override
    public void regenerateAmortizationScheduleOnUndoDisbursal(final WorkingCapitalLoan loan) {
        Validate.notNull(loan, "loan must not be null");

        final MathContext mc = MoneyHelper.getMathContext();
        final BigDecimal discount = loan.getLoanProductRelatedDetails() != null && loan.getLoanProductRelatedDetails().getDiscount() != null
                ? loan.getLoanProductRelatedDetails().getDiscount()
                : BigDecimal.ZERO;
        final BigDecimal totalPayment = loan.getBalance() != null && loan.getBalance().getTotalPayment() != null
                ? loan.getBalance().getTotalPayment()
                : BigDecimal.ZERO;
        final BigDecimal periodPaymentRate = loan.getLoanProductRelatedDetails() != null
                ? loan.getLoanProductRelatedDetails().getPeriodPaymentRate()
                : null;
        final Integer npvDayCount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getNpvDayCount()
                : null;

        final WorkingCapitalLoanDisbursementDetails detail = loan.getDisbursementDetails() != null
                && !loan.getDisbursementDetails().isEmpty() ? loan.getDisbursementDetails().getFirst() : null;
        final LocalDate expectedDisbursementDate = detail != null ? detail.getExpectedDisbursementDate() : null;
        final BigDecimal expectedAmount = detail != null && detail.getExpectedAmount() != null ? detail.getExpectedAmount()
                : loan.getApprovedPrincipal();

        Validate.isTrue(totalPayment.signum() > 0, "totalPayment must be positive");
        Validate.notNull(periodPaymentRate, "periodPaymentRate must not be null");
        Validate.notNull(npvDayCount, "npvDayCount must not be null");
        Validate.notNull(expectedDisbursementDate, "expectedDisbursementDate must not be null");
        Validate.notNull(expectedAmount, "expectedAmount must not be null");

        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(discount, expectedAmount, totalPayment,
                periodPaymentRate, npvDayCount, expectedDisbursementDate, mc, resolveCurrency(loan));
        scheduleRepositoryWrapper.writeModel(loan, model);
    }

    private MonetaryCurrency resolveCurrency(final WorkingCapitalLoan loan) {
        if (loan.getLoanProductRelatedDetails() != null && loan.getLoanProductRelatedDetails().getCurrency() != null) {
            return loan.getLoanProductRelatedDetails().getCurrency();
        }
        if (loan.getLoanProduct() != null && loan.getLoanProduct().getCurrency() != null) {
            return loan.getLoanProduct().getCurrency();
        }
        return DEFAULT_CURRENCY;
    }
}
