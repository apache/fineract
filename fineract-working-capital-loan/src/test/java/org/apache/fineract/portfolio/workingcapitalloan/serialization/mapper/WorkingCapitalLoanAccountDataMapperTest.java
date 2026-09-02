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
package org.apache.fineract.portfolio.workingcapitalloan.serialization.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.avro.generic.v1.CurrencyDataV1;
import org.apache.fineract.avro.generic.v1.StringEnumOptionDataV1;
import org.apache.fineract.avro.loan.v1.LoanApplicationTimelineDataV1;
import org.apache.fineract.avro.loan.v1.LoanStatusEnumDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalBreachDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanAccountDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanBreachSchedulePeriodDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanChargeDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanCollectionDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanDelinquencyDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanDelinquencySchedulePeriodDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanDisbursementDetailDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanSummaryDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalNearBreachDataV1;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroDateTimeMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.ExternalIdMapper;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.loanaccount.data.DelinquencyPausePeriod;
import org.apache.fineract.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanorigination.data.LoanOriginatorData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargeData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCollectionData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDisbursementDetailData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanSummaryData;
import org.apache.fineract.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachData;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;
import org.junit.jupiter.api.Test;

class WorkingCapitalLoanAccountDataMapperTest {

    private final WorkingCapitalLoanAccountDataMapper mapper = new WorkingCapitalLoanAccountDataMapperImpl(new AvroDateTimeMapper(),
            new ExternalIdMapper());

    @Test
    void map_nullSource_returnsNull() {
        assertNull(mapper.map((WorkingCapitalLoanData) null));
    }

    @Test
    void map_loanData_coversAllTopLevelFields() {
        final WorkingCapitalLoanData source = fullLoanData();

        final WorkingCapitalLoanAccountDataV1 result = mapper.map(source);

        assertNotNull(result);
        assertEquals(101L, result.getId());
        assertEquals("WC-000101", result.getAccountNo());
        assertEquals("ext-101", result.getExternalId());
        assertEquals(11L, result.getClientId());
        assertEquals("CL-11", result.getClientAccountNo());
        assertEquals("John Doe", result.getClientName());
        assertEquals(22L, result.getClientOfficeId());
        assertEquals("client-ext", result.getClientExternalId());
        assertEquals(33L, result.getLoanProductId());
        assertEquals("WC Product", result.getLoanProductName());
        assertEquals("WC product description", result.getLoanProductDescription());
        assertEquals(44L, result.getFundId());
        assertEquals("Main Fund", result.getFundName());
        assertEquals(new BigDecimal("1000.00"), result.getPrincipal());
        assertEquals(new BigDecimal("1100.00"), result.getApprovedPrincipal());
        assertEquals(new BigDecimal("1200.00"), result.getProposedPrincipal());
        assertEquals(new BigDecimal("980.00"), result.getNetDisbursalAmount());
        assertEquals(90, result.getNumberOfRepayments());
        assertEquals(30, result.getRepaymentEvery());
        assertEquals(7, result.getLoanProductCounter());
        assertEquals(360, result.getNpvDayCount());
        assertEquals(new BigDecimal("2.50"), result.getPaymentRate());
        assertEquals(new BigDecimal("12.00"), result.getDiscountFee());
        assertEquals(new BigDecimal("13.00"), result.getProposedDiscountFee());
        assertEquals(new BigDecimal("14.00"), result.getApprovedDiscountFee());
        assertEquals(new BigDecimal("120.00"), result.getPeriodPaymentAmount());
        assertEquals(new BigDecimal("5000.00"), result.getTotalPaymentVolume());
        assertEquals(Boolean.TRUE, result.getChargedOff());
        assertEquals(Boolean.TRUE, result.getEnableInstallmentLevelDelinquency());

        // actualNoTerm duplicates numberOfRepayments
        assertEquals(90, result.getActualNoTerm());

        // delinquencyGraceDays is exposed under the legacy loan field name
        assertEquals(7, result.getGraceOnArrearsAgeing());

        // totalOverpaid is pulled from the nested summary.overpayment
        assertEquals(new BigDecimal("9.99"), result.getTotalOverpaid());

        // EIR values are re-scaled to the fixed avro decimal scale (8, HALF_UP)
        assertEquals(new BigDecimal("0.05000000"), result.getDailyEir());
        assertEquals(8, result.getDailyEir().scale());
        assertEquals(new BigDecimal("18.25000000"), result.getCalculatedAnnualEir());
        assertEquals(8, result.getCalculatedAnnualEir().scale());
        assertEquals("2024-02-01", result.getLastClosedBusinessDate());
        assertStringEnum(source.getAmortizationType(), result.getAmortizationType());
        assertStringEnum(source.getRepaymentFrequencyType(), result.getRepaymentFrequencyType());
        assertNotNull(result.getStatus());
        assertNotNull(result.getTimeline());
        assertNotNull(result.getSummary());
        assertNotNull(result.getDelinquent());
        assertNotNull(result.getDelinquency());
        assertNotNull(result.getBreach());
        assertEquals(1, result.getCharges().size());
        assertEquals(1, result.getDisbursementDetails().size());
        assertEquals(1, result.getOriginators().size());

        // serializer-only fields stay unmapped
        assertNull(result.getOverpaidOnDate());
        assertNull(result.getCustomData());
    }

    @Test
    void map_loanData_mapsStatusFully() {
        final LoanStatusEnumDataV1 status = mapper.map(fullLoanData()).getStatus();

        assertNotNull(status);
        assertEquals(300, status.getId());
        assertEquals("loanStatusType.active", status.getCode());
        assertEquals("Active", status.getValue());
        assertEquals(Boolean.FALSE, status.getPendingApproval());
        assertEquals(Boolean.FALSE, status.getWaitingForDisbursal());
        assertEquals(Boolean.TRUE, status.getActive());
        assertEquals(Boolean.FALSE, status.getClosedObligationsMet());
        assertEquals(Boolean.FALSE, status.getClosedWrittenOff());
        assertEquals(Boolean.FALSE, status.getClosedRescheduled());
        assertEquals(Boolean.FALSE, status.getClosed());
        assertEquals(Boolean.FALSE, status.getOverpaid());
    }

    @Test
    void map_loanData_mapsTimelineFully() {
        final LoanApplicationTimelineDataV1 timeline = mapper.map(fullLoanData()).getTimeline();

        assertNotNull(timeline);
        assertEquals("2024-01-01", timeline.getSubmittedOnDate());
        assertEquals("sub-user", timeline.getSubmittedByUsername());
        assertEquals("sub-first", timeline.getSubmittedByFirstname());
        assertEquals("sub-last", timeline.getSubmittedByLastname());
        assertEquals("2024-01-02", timeline.getRejectedOnDate());
        assertEquals("rej-user", timeline.getRejectedByUsername());
        assertEquals("rej-first", timeline.getRejectedByFirstname());
        assertEquals("rej-last", timeline.getRejectedByLastname());
        assertEquals("2024-01-03", timeline.getWithdrawnOnDate());
        assertEquals("wd-user", timeline.getWithdrawnByUsername());
        assertEquals("wd-first", timeline.getWithdrawnByFirstname());
        assertEquals("wd-last", timeline.getWithdrawnByLastname());
        assertEquals("2024-01-04", timeline.getApprovedOnDate());
        assertEquals("app-user", timeline.getApprovedByUsername());
        assertEquals("app-first", timeline.getApprovedByFirstname());
        assertEquals("app-last", timeline.getApprovedByLastname());
        assertEquals("2024-01-05", timeline.getExpectedDisbursementDate());
        assertEquals("2024-01-06", timeline.getActualDisbursementDate());
        assertEquals("dis-user", timeline.getDisbursedByUsername());
        assertEquals("dis-first", timeline.getDisbursedByFirstname());
        assertEquals("dis-last", timeline.getDisbursedByLastname());
        assertEquals("2024-01-07", timeline.getClosedOnDate());
        assertEquals("cl-user", timeline.getClosedByUsername());
        assertEquals("cl-first", timeline.getClosedByFirstname());
        assertEquals("cl-last", timeline.getClosedByLastname());
        assertEquals("2024-01-08", timeline.getExpectedMaturityDate());
        assertEquals("2024-01-09", timeline.getWriteOffOnDate());
        assertEquals("wo-user", timeline.getWriteOffByUsername());
        assertEquals("wo-first", timeline.getWriteOffByFirstname());
        assertEquals("wo-last", timeline.getWriteOffByLastname());
        assertEquals("2024-01-10", timeline.getChargedOffOnDate());
        assertEquals("co-user", timeline.getChargedOffByUsername());
        assertEquals("co-first", timeline.getChargedOffByFirstname());
        assertEquals("co-last", timeline.getChargedOffByLastname());
    }

    @Test
    void map_loanData_mapsOriginatorsFully() {
        final var originator = mapper.map(fullLoanData()).getOriginators().getFirst();

        assertNotNull(originator);
        assertEquals(70L, originator.getId());
        assertEquals("orig-ext", originator.getExternalId());
        assertEquals("Origin Bank", originator.getName());
        assertEquals("ACTIVE", originator.getStatus());

        assertNotNull(originator.getOriginatorType());
        assertEquals(71L, originator.getOriginatorType().getId());
        assertEquals("Broker", originator.getOriginatorType().getName());
        assertEquals(1, originator.getOriginatorType().getPosition());
        assertEquals("originator type", originator.getOriginatorType().getDescription());
        assertEquals(Boolean.TRUE, originator.getOriginatorType().getActive());
        assertEquals(Boolean.FALSE, originator.getOriginatorType().getMandatory());

        assertNotNull(originator.getChannelType());
        assertEquals(72L, originator.getChannelType().getId());
        assertEquals("Online", originator.getChannelType().getName());
    }

    @Test
    void map_loanData_mapsDelinquencyFully() {
        final WorkingCapitalLoanData source = fullLoanData();

        final WorkingCapitalLoanDelinquencyDataV1 delinquency = mapper.map(source).getDelinquency();

        assertNotNull(delinquency);
        assertStringEnum(source.getDelinquencyStartType(), delinquency.getDelinquencyStartType());
        assertEquals("2024-01-05", delinquency.getDelinquencyStartDate());
    }

    @Test
    void map_loanData_mapsBreachWithNearBreachFully() {
        final WorkingCapitalBreachDataV1 breach = mapper.map(fullLoanData()).getBreach();

        assertNotNull(breach);
        assertEquals(80L, breach.getId());
        assertEquals("Breach rule", breach.getName());
        assertEquals(2, breach.getBreachFrequency());
        assertStringEnum(fullBreach().getBreachFrequencyType(), breach.getBreachFrequencyType());
        assertStringEnum(fullBreach().getBreachAmountCalculationType(), breach.getBreachAmountCalculationType());
        assertEquals(new BigDecimal("500.00"), breach.getBreachAmount());

        // loan-level fields lifted into the breach record
        assertEquals(3, breach.getBreachGraceDays());
        assertEquals("2024-01-06", breach.getBreachStartDate());

        final WorkingCapitalNearBreachDataV1 nearBreach = breach.getNearBreach();
        assertNotNull(nearBreach);
        assertEquals(81L, nearBreach.getId());
        assertEquals("Near breach rule", nearBreach.getName());
        assertEquals(1, nearBreach.getFrequency());
        assertStringEnum(fullNearBreach().getFrequencyType(), nearBreach.getFrequencyType());
        assertEquals(new BigDecimal("450.00"), nearBreach.getThreshold());

        // serializer-only fields stay unmapped
        assertNull(breach.getBreachSchedule());
        assertNull(breach.getBreachPastDueAmount());
    }

    @Test
    void map_loanData_withoutBreachConfig_stillCreatesBreachRecordFromLoanFields() {
        final WorkingCapitalLoanData source = fullLoanData();
        source.setBreach(null);
        source.setNearBreach(null);

        final WorkingCapitalBreachDataV1 breach = mapper.map(source).getBreach();

        assertNotNull(breach);
        assertNull(breach.getId());
        assertNull(breach.getName());
        assertNull(breach.getNearBreach());
        assertEquals(3, breach.getBreachGraceDays());
        assertEquals("2024-01-06", breach.getBreachStartDate());
    }

    @Test
    void map_summary_coversAllFields() {
        final WorkingCapitalLoanSummaryData source = fullSummary();

        final WorkingCapitalLoanSummaryDataV1 result = mapper.map(source);

        assertNotNull(result);
        assertEquals(new BigDecimal("1000"), result.getTotalPrincipal());
        assertEquals(new BigDecimal("950"), result.getPrincipalDisbursed());
        assertEquals(new BigDecimal("50"), result.getFeeChargesCharged());
        assertEquals(new BigDecimal("20"), result.getFeeChargesPaid());
        assertEquals(new BigDecimal("30"), result.getFeeChargesOutstanding());
        assertEquals(new BigDecimal("10"), result.getPenaltyChargesCharged());
        assertEquals(new BigDecimal("4"), result.getPenaltyChargesPaid());
        assertEquals(new BigDecimal("6"), result.getPenaltyChargesOutstanding());
        assertEquals(new BigDecimal("60"), result.getTotalChargeAmount());
        assertEquals(new BigDecimal("100"), result.getPrincipalPaid());
        assertEquals(new BigDecimal("900"), result.getPrincipalOutstanding());
        assertEquals(new BigDecimal("1500"), result.getTotalExpectedRepayment());
        assertEquals(new BigDecimal("600"), result.getTotalRepayment());
        assertEquals(new BigDecimal("900"), result.getTotalOutstanding());
        assertEquals(new BigDecimal("15"), result.getRealizedIncomeFromDiscountFee());
        assertEquals(new BigDecimal("35"), result.getUnrealizedIncomeFromDiscountFee());
        assertEquals(new BigDecimal("50"), result.getTotalDiscountFee());
        assertEquals(new BigDecimal("5"), result.getTotalDiscountFeeAdjustment());
        assertEquals("2024-01-30", result.getOverdueSinceDate());
        assertCurrency(source.getCurrency(), result.getCurrency());
    }

    @Test
    void map_summary_leavesStubAndSerializerFilledFieldsNull() {
        final WorkingCapitalLoanSummaryDataV1 result = mapper.map(fullSummary());

        // not-yet-implemented functionality stubs
        assertNull(result.getPrincipalAdjustments());
        assertNull(result.getPrincipalWrittenOff());
        assertNull(result.getFeeChargesWrittenOff());
        assertNull(result.getPenaltyChargesWrittenOff());
        assertNull(result.getTotalWrittenOff());
        assertNull(result.getTotalRecovered());
        assertNull(result.getWriteoffReasonId());
        assertNull(result.getWriteoffReason());
        assertNull(result.getChargeOffReasonId());
        assertNull(result.getChargeOffReason());

        // per-transaction-type totals are filled by the serializer, not the mapper
        assertNull(result.getTotalPayoutRefund());
        assertNull(result.getTotalPayoutRefundReversed());
        assertNull(result.getTotalGoodwillCredit());
        assertNull(result.getTotalGoodwillCreditReversed());
        assertNull(result.getTotalChargeAdjustment());
        assertNull(result.getTotalChargeAdjustmentReversed());
        assertNull(result.getTotalCreditBalanceRefund());
        assertNull(result.getTotalCreditBalanceRefundReversed());
        assertNull(result.getTotalRepaymentTransaction());
        assertNull(result.getTotalRepaymentTransactionReversed());
        assertNull(result.getTotalPayment());
        assertNull(result.getTotalPaymentReversed());
    }

    @Test
    void map_collection_coversAllFields() {
        final WorkingCapitalLoanCollectionData source = fullCollection();

        final WorkingCapitalLoanCollectionDataV1 result = mapper.map(source);

        assertNotNull(result);
        assertEquals(3, result.getDelinquentDays());
        assertEquals("2024-02-01", result.getDelinquentDate());
        assertEquals(new BigDecimal("100"), result.getDelinquentAmount());

        // delinquentPrincipal is exposed as totalDelinquentAmount
        assertEquals(new BigDecimal("80"), result.getTotalDelinquentAmount());

        assertNotNull(result.getDelinquencyPausePeriods());
        assertEquals(1, result.getDelinquencyPausePeriods().size());
        assertEquals(Boolean.TRUE, result.getDelinquencyPausePeriods().getFirst().getActive());
        assertEquals("2024-01-10", result.getDelinquencyPausePeriods().getFirst().getPausePeriodStart());
        assertEquals("2024-01-20", result.getDelinquencyPausePeriods().getFirst().getPausePeriodEnd());

        assertEquals("2024-01-10", result.getLastPaymentDate());
        assertEquals(new BigDecimal("10.0"), result.getLastPaymentAmount());
        assertEquals("2024-01-09", result.getLastRepaymentDate());
        assertEquals(new BigDecimal("15.0"), result.getLastRepaymentAmount());

        // serializer-only fields stay unmapped
        assertNull(result.getDelinquencySchedule());
    }

    @Test
    void map_delinquencySchedulePeriod_coversAllFields() {
        final WorkingCapitalLoanDelinquencyRangeScheduleData source = fullSchedulePeriod();

        final WorkingCapitalLoanDelinquencySchedulePeriodDataV1 result = mapper.map(source);

        assertNotNull(result);
        assertEquals(2, result.getPeriodNumber());
        assertEquals("2024-01-01", result.getFromDate());
        assertEquals("2024-01-30", result.getToDate());
        assertEquals(new BigDecimal("150.00"), result.getExpectedAmount());
        assertEquals(new BigDecimal("50.00"), result.getPaidAmount());
        assertEquals(new BigDecimal("100.00"), result.getOutstandingAmount());
        assertEquals(Boolean.FALSE, result.getMinPaymentCriteriaMet());
        assertEquals(12, result.getDelinquentDays());
        assertEquals(new BigDecimal("100.00"), result.getDelinquentAmount());
    }

    @Test
    void mapDelinquencySchedule_mapsEveryPeriod() {
        final List<WorkingCapitalLoanDelinquencySchedulePeriodDataV1> result = mapper
                .mapDelinquencySchedule(List.of(fullSchedulePeriod(), fullSchedulePeriod()));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.getFirst().getPeriodNumber());
    }

    @Test
    void map_breachSchedulePeriod_coversAllFields() {
        final WorkingCapitalLoanBreachScheduleData source = fullBreachSchedulePeriod();

        final WorkingCapitalLoanBreachSchedulePeriodDataV1 result = mapper.map(source);

        assertNotNull(result);
        assertEquals(4, result.getPeriodNumber());
        assertEquals("2024-02-01", result.getFromDate());
        assertEquals("2024-03-01", result.getToDate());
        assertEquals(29, result.getNumberOfDays());
        assertEquals(new BigDecimal("120.00"), result.getMinPaymentAmount());
        assertEquals(new BigDecimal("70.00"), result.getOutstandingAmount());
        assertEquals(new BigDecimal("50.00"), result.getPaidAmount());
        assertEquals(Boolean.TRUE, result.getNearBreach());
        assertEquals(Boolean.FALSE, result.getBreach());
        assertEquals(Boolean.FALSE, result.getReset());
    }

    @Test
    void mapBreachSchedule_mapsEveryPeriod() {
        final List<WorkingCapitalLoanBreachSchedulePeriodDataV1> result = mapper
                .mapBreachSchedule(List.of(fullBreachSchedulePeriod(), fullBreachSchedulePeriod()));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(4, result.getFirst().getPeriodNumber());
    }

    @Test
    void map_charge_coversAllFields() {
        final WorkingCapitalLoanChargeData source = fullCharge();

        final WorkingCapitalLoanChargeDataV1 result = mapper.map(source);

        assertNotNull(result);
        assertEquals(7L, result.getId());
        assertEquals(11L, result.getChargeId());
        assertEquals("Processing fee", result.getName());
        assertEquals("2024-03-01", result.getSubmittedOnDate());
        assertEquals("2024-03-02", result.getDueDate());
        assertEquals(new BigDecimal("25.00"), result.getAmount());
        assertEquals(new BigDecimal("10.00"), result.getAmountPaid());
        assertEquals(new BigDecimal("15.00"), result.getAmountOutstanding());
        assertEquals(Boolean.FALSE, result.getPenalty());
        assertEquals(Boolean.FALSE, result.getPaid());
        assertEquals(99L, result.getLoanId());
        assertEquals("charge-ext", result.getExternalId());
        assertEquals("loan-ext", result.getExternalLoanId());
        assertEquals(1, result.getChargeTimeType().getId());
        assertEquals("chargeTimeType.disbursement", result.getChargeTimeType().getCode());
        assertEquals("Disbursement", result.getChargeTimeType().getValue());
        assertEquals(2, result.getChargeCalculationType().getId());
        assertEquals("chargeCalculationType.flat", result.getChargeCalculationType().getCode());
        assertEquals("Flat", result.getChargeCalculationType().getValue());
        assertEquals(3, result.getChargePaymentMode().getId());
        assertEquals("chargePaymentMode.regular", result.getChargePaymentMode().getCode());
        assertEquals("Regular", result.getChargePaymentMode().getValue());

        assertCurrency(source.getCurrency(), result.getCurrency());

        // accrual fields are intentionally not mapped for working capital charges
        assertNull(result.getAmountAccrued());
        assertNull(result.getAmountUnrecognized());

        // write-off is not implemented and custom data is never filled for the loan-details event
        assertNull(result.getAmountWrittenOff());
        assertNull(result.getCustomData());
    }

    @Test
    void map_disbursementDetail_coversAllFields() {
        final WorkingCapitalLoanDisbursementDetailData source = fullDisbursement();

        final WorkingCapitalLoanDisbursementDetailDataV1 result = mapper.map(source);

        assertNotNull(result);
        assertEquals(42, result.getId());
        assertEquals(new BigDecimal("500.00"), result.getPrincipal());
        assertEquals("2024-01-01", result.getExpectedDisbursementDate());
        assertEquals("2024-01-02", result.getActualDisbursementDate());

        // netDisbursalAmount is intentionally not mapped
        assertNull(result.getNetDisbursalAmount());
    }

    @Test
    void toAvroDecimalScale_nullReturnsNull_valueRescaledToEightHalfUp() {
        assertNull(mapper.toAvroDecimalScale(null));

        final BigDecimal scaled = mapper.toAvroDecimalScale(new BigDecimal("0.123456785"));
        assertEquals(8, scaled.scale());
        assertEquals(new BigDecimal("0.12345679"), scaled);
    }

    private static void assertStringEnum(final StringEnumOptionData source, final StringEnumOptionDataV1 result) {
        assertNotNull(result);
        assertEquals(source.getId(), result.getId());
        assertEquals(source.getCode(), result.getCode());
        assertEquals(source.getValue(), result.getValue());
    }

    private static void assertCurrency(final CurrencyData source, final CurrencyDataV1 result) {
        assertNotNull(result);
        assertEquals(source.getCode(), result.getCode());
        assertEquals(source.getName(), result.getName());
        assertEquals(source.getDecimalPlaces(), result.getDecimalPlaces());
        assertEquals(source.getInMultiplesOf(), result.getInMultiplesOf());
        assertEquals(source.getDisplaySymbol(), result.getDisplaySymbol());
        assertEquals(source.getNameCode(), result.getNameCode());
        assertEquals(source.getDisplayLabel(), result.getDisplayLabel());
    }

    private static CurrencyData currency() {
        return new CurrencyData("USD", "US Dollar", 2, 1, "$", "currency.USD");
    }

    private static StringEnumOptionData stringEnum(final String id, final String code, final String value) {
        return new StringEnumOptionData(id, code, value);
    }

    private static WorkingCapitalLoanData fullLoanData() {
        return WorkingCapitalLoanData.builder().id(101L).accountNo("WC-000101").externalId(new ExternalId("ext-101")).clientId(11L)
                .clientAccountNo("CL-11").clientName("John Doe").clientExternalId(new ExternalId("client-ext")).clientOfficeId(22L)
                .fundId(44L).fundName("Main Fund").loanProductId(33L).loanProductName("WC Product")
                .loanProductDescription("WC product description").status(fullStatus()).proposedPrincipal(new BigDecimal("1200.00"))
                .approvedPrincipal(new BigDecimal("1100.00")).principal(new BigDecimal("1000.00"))
                .netDisbursalAmount(new BigDecimal("980.00"))
                .amortizationType(stringEnum("1", "amortizationType.equal.installments", "Equal installments")).npvDayCount(360)
                .loanProductCounter(7).paymentRate(new BigDecimal("2.50")).repaymentEvery(30)
                .repaymentFrequencyType(stringEnum("0", "repaymentFrequency.days", "Days")).discountFee(new BigDecimal("12.00"))
                .proposedDiscountFee(new BigDecimal("13.00")).approvedDiscountFee(new BigDecimal("14.00")).numberOfRepayments(90)
                .periodPaymentAmount(new BigDecimal("120.00")).dailyEir(new BigDecimal("0.05")).calculatedAnnualEir(new BigDecimal("18.25"))
                .totalPaymentVolume(new BigDecimal("5000.00")).breachGraceDays(3).delinquencyGraceDays(7)
                .delinquencyStartType(stringEnum("1", "delinquencyStart.disbursement", "Disbursement"))
                .delinquencyStartDate(LocalDate.of(2024, 1, 5)).breachStartDate(LocalDate.of(2024, 1, 6))
                .lastClosedBusinessDate(LocalDate.of(2024, 2, 1)).chargedOff(Boolean.TRUE).enableInstallmentLevelDelinquency(Boolean.TRUE)
                .currency(currency()).timeline(fullTimeline()).summary(fullSummary()).delinquent(fullCollection()).breach(fullBreach())
                .nearBreach(fullNearBreach()).charges(List.of(fullCharge())).disbursementDetails(List.of(fullDisbursement()))
                .originators(List.of(fullOriginator())).build();
    }

    private static LoanStatusEnumData fullStatus() {
        return new LoanStatusEnumData(300L, "loanStatusType.active", "Active");
    }

    private static LoanApplicationTimelineData fullTimeline() {
        return new LoanApplicationTimelineData().setSubmittedOnDate(LocalDate.of(2024, 1, 1)).setSubmittedByUsername("sub-user")
                .setSubmittedByFirstname("sub-first").setSubmittedByLastname("sub-last").setRejectedOnDate(LocalDate.of(2024, 1, 2))
                .setRejectedByUsername("rej-user").setRejectedByFirstname("rej-first").setRejectedByLastname("rej-last")
                .setWithdrawnOnDate(LocalDate.of(2024, 1, 3)).setWithdrawnByUsername("wd-user").setWithdrawnByFirstname("wd-first")
                .setWithdrawnByLastname("wd-last").setApprovedOnDate(LocalDate.of(2024, 1, 4)).setApprovedByUsername("app-user")
                .setApprovedByFirstname("app-first").setApprovedByLastname("app-last").setExpectedDisbursementDate(LocalDate.of(2024, 1, 5))
                .setActualDisbursementDate(LocalDate.of(2024, 1, 6)).setDisbursedByUsername("dis-user").setDisbursedByFirstname("dis-first")
                .setDisbursedByLastname("dis-last").setClosedOnDate(LocalDate.of(2024, 1, 7)).setClosedByUsername("cl-user")
                .setClosedByFirstname("cl-first").setClosedByLastname("cl-last").setExpectedMaturityDate(LocalDate.of(2024, 1, 8))
                .setWriteOffOnDate(LocalDate.of(2024, 1, 9)).setWriteOffByUsername("wo-user").setWriteOffByFirstname("wo-first")
                .setWriteOffByLastname("wo-last").setChargedOffOnDate(LocalDate.of(2024, 1, 10)).setChargedOffByUsername("co-user")
                .setChargedOffByFirstname("co-first").setChargedOffByLastname("co-last");
    }

    private static WorkingCapitalLoanSummaryData fullSummary() {
        return WorkingCapitalLoanSummaryData.builder().currency(currency()).principal(new BigDecimal("1000"))
                .principalPaid(new BigDecimal("100")).principalOutstanding(new BigDecimal("900")).fee(new BigDecimal("50"))
                .feePaid(new BigDecimal("20")).feeOutstanding(new BigDecimal("30")).penalty(new BigDecimal("10"))
                .penaltyPaid(new BigDecimal("4")).penaltyOutstanding(new BigDecimal("6"))
                .realizedIncomeFromDiscountFee(new BigDecimal("15")).unrealizedIncomeFromDiscountFee(new BigDecimal("35"))
                .overpayment(new BigDecimal("9.99")).totalDisbursement(new BigDecimal("950")).totalDiscountFee(new BigDecimal("50"))
                .totalDiscountFeeAdjustment(new BigDecimal("5")).totalExpectedRepayment(new BigDecimal("1500"))
                .totalRepayment(new BigDecimal("600")).totalOutstanding(new BigDecimal("900")).overdueSinceDate(LocalDate.of(2024, 1, 30))
                .build();
    }

    private static WorkingCapitalLoanCollectionData fullCollection() {
        final DelinquencyPausePeriod pause = new DelinquencyPausePeriod(true, LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 20));
        final WorkingCapitalLoanCollectionData collection = new WorkingCapitalLoanCollectionData();
        collection.setPastDueDays(5L);
        collection.setDelinquentDays(3L);
        collection.setDelinquentDate(LocalDate.of(2024, 2, 1));
        collection.setDelinquentAmount(new BigDecimal("100"));
        collection.setDelinquencyPausePeriods(List.of(pause));
        collection.setInstallmentLevelDelinquency(List.of());
        collection.setDelinquentPrincipal(new BigDecimal("80"));
        // deliberately different dates and amounts, so a payment/repayment mix-up in the mapping cannot pass
        collection.setLastPaymentDate(LocalDate.of(2024, 1, 10));
        collection.setLastPaymentAmount(new BigDecimal("10.0"));
        collection.setLastRepaymentDate(LocalDate.of(2024, 1, 9));
        collection.setLastRepaymentAmount(new BigDecimal("15.0"));
        return collection;
    }

    private static WorkingCapitalLoanDelinquencyRangeScheduleData fullSchedulePeriod() {
        return new WorkingCapitalLoanDelinquencyRangeScheduleData(9L, 101L, 2, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 30),
                new BigDecimal("150.00"), new BigDecimal("50.00"), new BigDecimal("100.00"), Boolean.FALSE, 12L, new BigDecimal("100.00"));
    }

    private static WorkingCapitalLoanBreachScheduleData fullBreachSchedulePeriod() {
        return new WorkingCapitalLoanBreachScheduleData(15L, 101L, 4, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), 29,
                new BigDecimal("120.00"), new BigDecimal("70.00"), new BigDecimal("50.00"), Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
    }

    private static WorkingCapitalLoanChargeData fullCharge() {
        return WorkingCapitalLoanChargeData.builder().id(7L).chargeId(11L).name("Processing fee")
                .chargeTimeType(new EnumOptionData(1L, "chargeTimeType.disbursement", "Disbursement"))
                .submittedOnDate(LocalDate.of(2024, 3, 1)).dueDate(LocalDate.of(2024, 3, 2))
                .chargeCalculationType(new EnumOptionData(2L, "chargeCalculationType.flat", "Flat")).currency(currency())
                .amount(new BigDecimal("25.00")).amountPaid(new BigDecimal("10.00")).amountOutstanding(new BigDecimal("15.00"))
                .penalty(false).chargePaymentMode(new EnumOptionData(3L, "chargePaymentMode.regular", "Regular")).paid(false).loanId(99L)
                .externalId(new ExternalId("charge-ext")).externalLoanId(new ExternalId("loan-ext")).build();
    }

    private static WorkingCapitalLoanDisbursementDetailData fullDisbursement() {
        return WorkingCapitalLoanDisbursementDetailData.builder().id(42L).principal(new BigDecimal("500.00"))
                .expectedDisbursementDate(LocalDate.of(2024, 1, 1)).actualDisbursementDate(LocalDate.of(2024, 1, 2)).build();
    }

    private static WorkingCapitalBreachData fullBreach() {
        return WorkingCapitalBreachData.builder().id(80L).name("Breach rule").breachFrequency(2)
                .breachFrequencyType(stringEnum("1", "breachFrequency.days", "Days"))
                .breachAmountCalculationType(stringEnum("1", "breachAmount.fixed", "Fixed")).breachAmount(new BigDecimal("500.00")).build();
    }

    private static WorkingCapitalNearBreachData fullNearBreach() {
        return WorkingCapitalNearBreachData.builder().id(81L).name("Near breach rule").frequency(1)
                .frequencyType(stringEnum("1", "nearBreachFrequency.days", "Days")).threshold(new BigDecimal("450.00")).build();
    }

    private static LoanOriginatorData fullOriginator() {
        final CodeValueData originatorType = CodeValueData.instance(71L, "Broker", "originator type", 1, true, false);
        final CodeValueData channelType = CodeValueData.instance(72L, "Online", "channel type", 2, true, false);
        return LoanOriginatorData.builder().id(70L).externalId("orig-ext").name("Origin Bank").status("ACTIVE")
                .originatorType(originatorType).channelType(channelType).build();
    }
}
