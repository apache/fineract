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
import java.util.List;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.avro.loan.v1.DelinquencyRangeDataV1;
import org.apache.fineract.avro.loan.v1.LoanAccountDelinquencyRangeDataV1;
import org.apache.fineract.avro.loan.v1.LoanAmountDataV1;
import org.apache.fineract.avro.loan.v1.LoanChargeDataRangeViewV1;
import org.apache.fineract.avro.loan.v1.LoanInstallmentDelinquencyBucketDataV1;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDelinquencyRangeChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.generic.CurrencyDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanChargeDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.loan.LoanDelinquencyRangeDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroDateTimeMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class LoanDelinquencyRangeChangeBusinessEventSerializer implements BusinessEventSerializer {

    private final LoanReadPlatformService service;

    private final LoanDelinquencyRangeDataMapper mapper;

    private final LoanChargeReadPlatformService loanChargeReadPlatformService;

    private final DelinquencyReadPlatformService delinquencyReadPlatformService;

    private final LoanChargeDataMapper chargeMapper;

    private final CurrencyDataMapper currencyMapper;
    private final AvroDateTimeMapper dataTimeMapper;
    private final LoanInstallmentLevelDelinquencyEventProducer installmentLevelDelinquencyEventProducer;

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        LoanDelinquencyRangeChangeBusinessEvent event = (LoanDelinquencyRangeChangeBusinessEvent) rawEvent;
        LoanAccountData data = service.retrieveOne(event.get().getId());
        Long id = data.getId();
        String accountNumber = data.getAccountNo();
        String externalId = data.getExternalId().getValue();
        MonetaryCurrency loanCurrency = event.get().getCurrency();
        CollectionData delinquentData = delinquencyReadPlatformService.calculateLoanCollectionData(id);
        String delinquentDate = dataTimeMapper.mapLocalDate(delinquentData.getDelinquentDate());

        List<LoanChargeDataRangeViewV1> charges = loanChargeReadPlatformService.retrieveLoanCharges(id)//
                .stream()//
                .map(chargeMapper::mapRangeView)//
                .toList();
        LoanAmountDataV1 amount = LoanAmountDataV1.newBuilder()//
                .setPrincipalAmount(calculateDataSummary(event.get(),
                        (loan, installment) -> installment.getPrincipalOutstanding(loanCurrency).getAmount()))//
                .setFeeAmount(calculateDataSummary(event.get(),
                        (loan, installment) -> installment.getFeeChargesOutstanding(loanCurrency).getAmount()))//
                .setInterestAmount(calculateDataSummary(event.get(),
                        (loan, installment) -> installment.getInterestOutstanding(loanCurrency).getAmount()))//
                .setPenaltyAmount(calculateDataSummary(event.get(),
                        (loan, installment) -> installment.getPenaltyChargesOutstanding(loanCurrency).getAmount()))//
                .setTotalAmount(
                        calculateDataSummary(event.get(), (loan, installment) -> installment.getTotalOutstanding(loanCurrency).getAmount()))//
                .build();

        DelinquencyRangeDataV1 delinquencyRange = mapper.map(data.getDelinquencyRange());

        List<LoanInstallmentDelinquencyBucketDataV1> installmentsDelinquencyData = installmentLevelDelinquencyEventProducer
                .calculateInstallmentLevelDelinquencyData(event.get(), data.getCurrency());

        LoanAccountDelinquencyRangeDataV1.Builder builder = LoanAccountDelinquencyRangeDataV1.newBuilder();
        return builder//
                .setLoanId(id)//
                .setLoanAccountNo(accountNumber)//
                .setLoanExternalId(externalId)//
                .setDelinquencyRange(delinquencyRange)//
                .setCharges(charges)//
                .setAmount(amount)//
                .setCurrency(currencyMapper.map(data.getCurrency()))//
                .setDelinquentDate(delinquentDate)//
                .setInstallmentDelinquencyBuckets(installmentsDelinquencyData).build();
    }

    private BigDecimal calculateDataSummary(Loan loan, BiFunction<Loan, LoanRepaymentScheduleInstallment, BigDecimal> mapper) {
        return loan.getRepaymentScheduleInstallments().stream()
                .filter(installment -> DateUtils.isBeforeBusinessDate(installment.getDueDate()))
                .map(installment -> mapper.apply(loan, installment)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof LoanDelinquencyRangeChangeBusinessEvent;
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return LoanAccountDelinquencyRangeDataV1.class;
    }
}
