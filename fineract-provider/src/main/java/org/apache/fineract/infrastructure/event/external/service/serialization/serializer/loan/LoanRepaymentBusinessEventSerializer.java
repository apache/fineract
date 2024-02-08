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
package org.apache.fineract.infrastructure.event.external.service.serialization.serializer.loan;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.avro.generic.v1.CurrencyDataV1;
import org.apache.fineract.avro.loan.v1.LoanRepaymentDueDataV1;
import org.apache.fineract.avro.loan.v1.RepaymentDueDataV1;
import org.apache.fineract.avro.loan.v1.RepaymentPastDueDataV1;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.repayment.LoanRepaymentBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanRepaymentPastDueDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroDateTimeMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.loanaccount.data.LoanRepaymentPastDueData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.service.LoanCalculateRepaymentPastDueService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanRepaymentBusinessEventSerializer implements BusinessEventSerializer {

    private final AvroDateTimeMapper dataTimeMapper;
    private final LoanRepaymentPastDueDataMapper pastDueDataMapper;
    private final LoanCalculateRepaymentPastDueService pastDueService;

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {

        LoanRepaymentBusinessEvent event = (LoanRepaymentBusinessEvent) rawEvent;
        LoanRepaymentScheduleInstallment repaymentInstallment = event.get();
        Loan loan = repaymentInstallment.getLoan();

        Long id = loan.getId();
        String accountNo = loan.getAccountNumber();
        String externalId = loan.getExternalId().getValue();
        MonetaryCurrency loanCurrency = loan.getCurrency();
        CurrencyDataV1 currency = CurrencyDataV1.newBuilder().setCode(loanCurrency.getCode())
                .setDecimalPlaces(loanCurrency.getDigitsAfterDecimal()).setInMultiplesOf(loanCurrency.getCurrencyInMultiplesOf()).build();

        RepaymentDueDataV1 repaymentDue = getRepaymentDueData(repaymentInstallment, loanCurrency);

        LoanRepaymentPastDueData pastDueData = pastDueService.retrieveLoanRepaymentPastDueAmountTillDate(loan);

        RepaymentPastDueDataV1 pastDue = pastDueDataMapper.map(pastDueData);

        LoanRepaymentDueDataV1 loanRepaymentDueDataV1 = LoanRepaymentDueDataV1.newBuilder().setLoanId(id).setLoanAccountNo(accountNo)
                .setLoanExternalId(externalId).setCurrency(currency).setInstallment(repaymentDue).setPastDueAmount(pastDue).build();
        return loanRepaymentDueDataV1;
    }

    private RepaymentDueDataV1 getRepaymentDueData(LoanRepaymentScheduleInstallment repaymentInstallment, MonetaryCurrency loanCurrency) {
        Integer installmentNumber = repaymentInstallment.getInstallmentNumber();
        String dueDate = dataTimeMapper.mapLocalDate(repaymentInstallment.getDueDate());
        BigDecimal principalAmountDue = repaymentInstallment.getPrincipalOutstanding(loanCurrency).getAmount();
        BigDecimal interestAmountDue = repaymentInstallment.getInterestOutstanding(loanCurrency).getAmount();
        BigDecimal feeChargeAmountDue = repaymentInstallment.getFeeChargesOutstanding(loanCurrency).getAmount();
        BigDecimal penaltyChargeAmountDue = repaymentInstallment.getPenaltyChargesOutstanding(loanCurrency).getAmount();
        BigDecimal totalAmountDue = repaymentInstallment.getTotalOutstanding(loanCurrency).getAmount();

        RepaymentDueDataV1 repaymentDue = new RepaymentDueDataV1(installmentNumber, dueDate, principalAmountDue, interestAmountDue,
                feeChargeAmountDue, penaltyChargeAmountDue, totalAmountDue);
        return repaymentDue;
    }

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof LoanRepaymentBusinessEvent;
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return LoanRepaymentDueDataV1.class;
    }
}
