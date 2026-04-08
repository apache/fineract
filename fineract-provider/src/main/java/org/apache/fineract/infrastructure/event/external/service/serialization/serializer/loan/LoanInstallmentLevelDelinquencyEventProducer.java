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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.avro.loan.v1.DelinquencyRangeDataV1;
import org.apache.fineract.avro.loan.v1.LoanAmountDataV1;
import org.apache.fineract.avro.loan.v1.LoanChargeDataRangeViewV1;
import org.apache.fineract.avro.loan.v1.LoanInstallmentDelinquencyBucketDataV1;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.generic.CurrencyDataMapper;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.delinquency.data.LoanInstallmentDelinquencyTagData;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanInstallmentLevelDelinquencyEventProducer {

    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final CurrencyDataMapper currencyMapper;

    public List<LoanInstallmentDelinquencyBucketDataV1> calculateInstallmentLevelDelinquencyData(Loan loan, CurrencyData currency) {
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
                                    .map(installment -> installment.getPrincipalOutstanding(loan.getCurrency()).getAmount())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))//
                            .setFeeAmount(delinquentInstallmentsInSameRange.stream()
                                    .map(installment -> installment.getFeeChargesOutstanding(loan.getCurrency()).getAmount())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))//
                            .setInterestAmount(delinquentInstallmentsInSameRange.stream()
                                    .map(installment -> installment.getInterestOutstanding(loan.getCurrency()).getAmount())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))//
                            .setPenaltyAmount(delinquentInstallmentsInSameRange.stream()
                                    .map(installment -> installment.getPenaltyChargesOutstanding(loan.getCurrency()).getAmount())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))//
                            .setTotalAmount(delinquentInstallmentsInSameRange.stream()
                                    .map(installment -> installment.getTotalOutstanding(loan.getCurrency()).getAmount())
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
                                .setName(charge.name()).setAmount(charge.amountOutstanding()).setCurrency(currencyMapper.map(currency))
                                .build();
                        charges.add(chargeData);
                    }

                    LoanInstallmentDelinquencyTagData.InstallmentDelinquencyRange delinquencyRange = installmentDelinquencyTagData
                            .getValue().get(0).getDelinquencyRange();

                    DelinquencyRangeDataV1 delinquencyRangeDataV1 = DelinquencyRangeDataV1.newBuilder().setId(delinquencyRange.getId())
                            .setClassification(delinquencyRange.getClassification()).setMinimumAgeDays(delinquencyRange.getMinimumAgeDays())
                            .setMaximumAgeDays(delinquencyRange.getMaximumAgeDays()).build();

                    LoanInstallmentDelinquencyBucketDataV1 installmentDelinquencyBucketDataV1 = LoanInstallmentDelinquencyBucketDataV1
                            .newBuilder().setDelinquencyRange(delinquencyRangeDataV1).setAmount(amount).setCharges(charges)
                            .setCurrency(currencyMapper.map(currency)).build();

                    loanInstallmentDelinquencyData.add(installmentDelinquencyBucketDataV1);
                }
            }
        }
        return loanInstallmentDelinquencyData;
    }
}
