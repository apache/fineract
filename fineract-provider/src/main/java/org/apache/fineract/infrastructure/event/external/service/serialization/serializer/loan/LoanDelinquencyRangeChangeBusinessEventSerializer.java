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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
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
import org.apache.fineract.portfolio.delinquency.data.LoanInstallmentDelinquencyTagData;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
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

        List<LoanInstallmentDelinquencyBucketDataV1> installmentsDelinquencyData = calculateInstallmentLevelDelinquencyData(event.get(),
                data);

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

    private List<LoanInstallmentDelinquencyBucketDataV1> calculateInstallmentLevelDelinquencyData(Loan loan, LoanAccountData data) {
        List<LoanInstallmentDelinquencyBucketDataV1> loanInstallmentDelinquencyData = new ArrayList<>();
        if (loan.isEnableInstallmentLevelDelinquency()) {
            Collection<LoanInstallmentDelinquencyTagData> installmentDelinquencyTags = delinquencyReadPlatformService
                    .retrieveLoanInstallmentsCurrentDelinquencyTag(loan.getId());
            if (installmentDelinquencyTags != null && installmentDelinquencyTags.size() > 0) {
                // group installments that are in same range
                Map<Long, List<LoanInstallmentDelinquencyTagData>> installmentsInSameRange = installmentDelinquencyTags.stream().collect(
                        Collectors.groupingBy(installmentDelnquencyTags -> installmentDelnquencyTags.getDelinquencyRange().getId()));
                // for installments in each range, get details from loan repayment schedule installment, add amounts,
                // list charges
                for (Map.Entry<Long, List<LoanInstallmentDelinquencyTagData>> installmentDelinquencyTagData : installmentsInSameRange
                        .entrySet()) {
                    // get installments details
                    List<LoanRepaymentScheduleInstallment> delinquentInstallmentsInSameRange = loan.getRepaymentScheduleInstallments()
                            .stream().filter(installment -> installmentDelinquencyTagData.getValue().stream()
                                    .anyMatch(installmentTag -> installmentTag.getId().equals(installment.getId())))
                            .toList();
                    // add amounts
                    LoanAmountDataV1 amount = LoanAmountDataV1.newBuilder()//
                            .setPrincipalAmount(delinquentInstallmentsInSameRange.stream()
                                    .map(instlment -> instlment.getPrincipalOutstanding(loan.getCurrency()).getAmount())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))//
                            .setFeeAmount(delinquentInstallmentsInSameRange.stream()
                                    .map(instlment -> instlment.getFeeChargesOutstanding(loan.getCurrency()).getAmount())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))//
                            .setInterestAmount(delinquentInstallmentsInSameRange.stream()
                                    .map(instlment -> instlment.getInterestOutstanding(loan.getCurrency()).getAmount())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))//
                            .setPenaltyAmount(delinquentInstallmentsInSameRange.stream()
                                    .map(instlment -> instlment.getPenaltyChargesOutstanding(loan.getCurrency()).getAmount())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))//
                            .setTotalAmount(delinquentInstallmentsInSameRange.stream()
                                    .map(instlment -> instlment.getTotalOutstanding(loan.getCurrency()).getAmount())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))//
                            .build();

                    // get list of charges for installments in same range
                    List<LoanCharge> chargesForInstallmentsInSameRange = loan.getLoanCharges().stream().filter(loanCharge -> !loanCharge
                            .isPaid()
                            && delinquentInstallmentsInSameRange.stream().anyMatch(installmentForCharge -> (DateUtils
                                    .isAfter(loanCharge.getEffectiveDueDate(), installmentForCharge.getFromDate())
                                    || DateUtils.isEqual(loanCharge.getEffectiveDueDate(), installmentForCharge.getFromDate()))
                                    && (DateUtils.isBefore(loanCharge.getEffectiveDueDate(), installmentForCharge.getDueDate())
                                            || DateUtils.isEqual(loanCharge.getEffectiveDueDate(), installmentForCharge.getDueDate()))))
                            .toList();

                    List<LoanChargeDataRangeViewV1> charges = new ArrayList<>();
                    for (LoanCharge charge : chargesForInstallmentsInSameRange) {
                        LoanChargeDataRangeViewV1 chargeData = LoanChargeDataRangeViewV1.newBuilder().setId(charge.getId())
                                .setName(charge.name()).setAmount(charge.amountOutstanding())
                                .setCurrency(currencyMapper.map(data.getCurrency())).build();
                        charges.add(chargeData);
                    }

                    LoanInstallmentDelinquencyTagData.InstallmentDelinquencyRange delinquencyRange = installmentDelinquencyTagData
                            .getValue().get(0).getDelinquencyRange();

                    DelinquencyRangeDataV1 delinquencyRangeDataV1 = DelinquencyRangeDataV1.newBuilder().setId(delinquencyRange.getId())
                            .setClassification(delinquencyRange.getClassification()).setMinimumAgeDays(delinquencyRange.getMinimumAgeDays())
                            .setMaximumAgeDays(delinquencyRange.getMaximumAgeDays()).build();

                    LoanInstallmentDelinquencyBucketDataV1 installmentDelinquencyBucketDataV1 = LoanInstallmentDelinquencyBucketDataV1
                            .newBuilder().setDelinquencyRange(delinquencyRangeDataV1).setAmount(amount).setCharges(charges)
                            .setCurrency(currencyMapper.map(data.getCurrency())).build();

                    loanInstallmentDelinquencyData.add(installmentDelinquencyBucketDataV1);
                }
            }
        }
        return loanInstallmentDelinquencyData;
    }

    private BigDecimal calculateDataSummary(Loan loan, BiFunction<Loan, LoanRepaymentScheduleInstallment, BigDecimal> mapper) {
        return loan.getRepaymentScheduleInstallments().stream().map(installment -> mapper.apply(loan, installment)).reduce(BigDecimal.ZERO,
                BigDecimal::add);

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
