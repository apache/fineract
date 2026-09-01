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
package org.apache.fineract.portfolio.workingcapitalloan.serialization.serializer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalBreachDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanAccountDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanCollectionDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanDelinquencySchedulePeriodDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanDelinquencyScheduleTagDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanSummaryDataV1;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroDateTimeMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.AbstractBusinessEventWithCustomDataSerializer;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.ExternalEventCustomDataSerializer;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionTypeTotalHolder;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanBreachScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.mapper.WorkingCapitalLoanAccountDataMapper;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanApplicationReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanBusinessEventSerializer
        extends AbstractBusinessEventWithCustomDataSerializer<WorkingCapitalLoanBusinessEvent> implements BusinessEventSerializer {

    private static final List<LoanTransactionType> SUMMARY_TOTAL_TRANSACTION_TYPES = List.of(LoanTransactionType.PAYOUT_REFUND,
            LoanTransactionType.GOODWILL_CREDIT, LoanTransactionType.CHARGE_ADJUSTMENT, LoanTransactionType.CREDIT_BALANCE_REFUND,
            LoanTransactionType.REPAYMENT);
    private static final List<LoanTransactionType> PAYMENT_TRANSACTION_TYPES = List.of(LoanTransactionType.REPAYMENT,
            LoanTransactionType.PAYOUT_REFUND, LoanTransactionType.GOODWILL_CREDIT, LoanTransactionType.CHARGE_ADJUSTMENT);

    private final WorkingCapitalLoanApplicationReadPlatformService readPlatformService;
    private final WorkingCapitalLoanAccountDataMapper mapper;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleRepository rangeScheduleRepository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleMapper rangeScheduleMapper;
    private final WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository tagHistoryRepository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryMapper tagHistoryMapper;
    private final WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;
    private final WorkingCapitalLoanBreachScheduleMapper breachScheduleMapper;
    private final AvroDateTimeMapper avroDateTimeMapper;
    private final List<ExternalEventCustomDataSerializer<WorkingCapitalLoanBusinessEvent>> externalEventCustomDataSerializers;
    private final WorkingCapitalLoanChargeEnricher chargeEnricher;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof WorkingCapitalLoanBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        final WorkingCapitalLoanBusinessEvent event = (WorkingCapitalLoanBusinessEvent) rawEvent;
        final WorkingCapitalLoan loan = event.get();
        final WorkingCapitalLoanData data = readPlatformService.retrieveOne(loan.getId());
        final WorkingCapitalLoanAccountDataV1 result = mapper.map(data);
        if (result.getDisbursementDetails() != null) {
            result.getDisbursementDetails().forEach(detail -> detail.setNetDisbursalAmount(result.getNetDisbursalAmount()));
        }
        populateChargeAccruals(loan, result);
        populateChargeCustomData(event, result);
        populateChargeOffReason(data, result);
        populateSummaryTransactionTypeTotals(loan, result);
        populateDelinquencySchedule(loan, result);
        populateBreachSchedule(loan, result);
        result.setCustomData(collectCustomData(event));
        return result;
    }

    private void populateChargeAccruals(final WorkingCapitalLoan loan, final WorkingCapitalLoanAccountDataV1 result) {
        chargeEnricher.populateAccruals(loan, result.getCharges());
    }

    private void populateChargeCustomData(final WorkingCapitalLoanBusinessEvent event, final WorkingCapitalLoanAccountDataV1 result) {
        if (result.getCharges() == null) {
            return;
        }
        result.getCharges().forEach(charge -> charge.setCustomData(chargeEnricher.collectCustomData(event, charge.getId())));
    }

    private void populateChargeOffReason(final WorkingCapitalLoanData data, final WorkingCapitalLoanAccountDataV1 result) {
        final WorkingCapitalLoanSummaryDataV1 summary = result.getSummary();
        if (summary == null || data.getChargeOffReason() == null) {
            return;
        }
        summary.setChargeOffReasonId(data.getChargeOffReason().getId());
        summary.setChargeOffReason(data.getChargeOffReason().getName());
    }

    private void populateSummaryTransactionTypeTotals(final WorkingCapitalLoan loan, final WorkingCapitalLoanAccountDataV1 result) {
        final WorkingCapitalLoanSummaryDataV1 summary = result.getSummary();
        if (summary == null) {
            return;
        }
        final Map<LoanTransactionType, Map<Boolean, BigDecimal>> totals = transactionRepository
                .fetchTotalsPerTypeAndReversed(loan.getId(), SUMMARY_TOTAL_TRANSACTION_TYPES).stream()
                .collect(Collectors.groupingBy(TransactionTypeTotalHolder::transactionType,
                        Collectors.toMap(TransactionTypeTotalHolder::reversed, TransactionTypeTotalHolder::total)));
        summary.setTotalPayoutRefund(totalOf(totals, LoanTransactionType.PAYOUT_REFUND, false));
        summary.setTotalPayoutRefundReversed(totalOf(totals, LoanTransactionType.PAYOUT_REFUND, true));
        summary.setTotalGoodwillCredit(totalOf(totals, LoanTransactionType.GOODWILL_CREDIT, false));
        summary.setTotalGoodwillCreditReversed(totalOf(totals, LoanTransactionType.GOODWILL_CREDIT, true));
        summary.setTotalChargeAdjustment(totalOf(totals, LoanTransactionType.CHARGE_ADJUSTMENT, false));
        summary.setTotalChargeAdjustmentReversed(totalOf(totals, LoanTransactionType.CHARGE_ADJUSTMENT, true));
        summary.setTotalCreditBalanceRefund(totalOf(totals, LoanTransactionType.CREDIT_BALANCE_REFUND, false));
        summary.setTotalCreditBalanceRefundReversed(totalOf(totals, LoanTransactionType.CREDIT_BALANCE_REFUND, true));
        summary.setTotalRepaymentTransaction(totalOf(totals, LoanTransactionType.REPAYMENT, false));
        summary.setTotalRepaymentTransactionReversed(totalOf(totals, LoanTransactionType.REPAYMENT, true));
        summary.setTotalPayment(sumOf(totals, PAYMENT_TRANSACTION_TYPES, false));
        summary.setTotalPaymentReversed(sumOf(totals, PAYMENT_TRANSACTION_TYPES, true));
    }

    private BigDecimal totalOf(final Map<LoanTransactionType, Map<Boolean, BigDecimal>> totals, final LoanTransactionType transactionType,
            final boolean reversed) {
        return totals.getOrDefault(transactionType, Map.of()).getOrDefault(reversed, BigDecimal.ZERO);
    }

    private BigDecimal sumOf(final Map<LoanTransactionType, Map<Boolean, BigDecimal>> totals,
            final List<LoanTransactionType> transactionTypes, final boolean reversed) {
        return transactionTypes.stream().map(transactionType -> totalOf(totals, transactionType, reversed)).reduce(BigDecimal.ZERO,
                BigDecimal::add);
    }

    private void populateDelinquencySchedule(final WorkingCapitalLoan loan, final WorkingCapitalLoanAccountDataV1 result) {
        final WorkingCapitalLoanCollectionDataV1 delinquent = result.getDelinquent();
        if (delinquent == null) {
            return;
        }
        final List<WorkingCapitalLoanDelinquencySchedulePeriodDataV1> periods = mapper.mapDelinquencySchedule(
                rangeScheduleMapper.toDataList(rangeScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(loan.getId())));
        final Map<Integer, List<WorkingCapitalLoanDelinquencyScheduleTagDataV1>> tagsByPeriod = tagHistoryRepository
                .findByLoanIdAndLiftedOnDateIsNull(loan.getId()).stream()
                .collect(Collectors.groupingBy(tag -> tag.getRangeSchedule().getPeriodNumber(),
                        Collectors.mapping(tag -> mapper.map(tagHistoryMapper.mapForCollectionData(tag)), Collectors.toList())));
        periods.forEach(period -> period.setTags(tagsByPeriod.get(period.getPeriodNumber())));
        delinquent.setDelinquencySchedule(periods);
    }

    private void populateBreachSchedule(final WorkingCapitalLoan loan, final WorkingCapitalLoanAccountDataV1 result) {
        final WorkingCapitalBreachDataV1 breach = result.getBreach();
        if (breach == null) {
            return;
        }
        breach.setBreachSchedule(mapper.mapBreachSchedule(
                breachScheduleMapper.toDataList(breachScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(loan.getId()))));
        Optional.ofNullable(loan.getBalance()).ifPresent(balance -> breach.setBreachPastDueAmount(balance.getBreachPastDueAmount()));
    }

    @Override
    protected List<ExternalEventCustomDataSerializer<WorkingCapitalLoanBusinessEvent>> getExternalEventCustomDataSerializers() {
        return externalEventCustomDataSerializers;
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return WorkingCapitalLoanAccountDataV1.class;
    }
}
