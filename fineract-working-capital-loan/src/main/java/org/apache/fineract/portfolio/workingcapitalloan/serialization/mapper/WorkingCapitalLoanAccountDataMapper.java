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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanAccountDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanBreachSchedulePeriodDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanChargeDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanCollectionDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanDelinquencySchedulePeriodDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanDelinquencyScheduleTagDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanDisbursementDetailDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanSummaryDataV1;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroMapperConfig;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargeData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCollectionData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDisbursementDetailData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanRangeScheduleDelinquencyData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanSummaryData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = AvroMapperConfig.class)
public interface WorkingCapitalLoanAccountDataMapper {

    int AVRO_DECIMAL_SCALE = 8;

    @Mapping(source = "delinquencyGraceDays", target = "graceOnArrearsAgeing")
    @Mapping(source = "summary.overpayment", target = "totalOverpaid")
    @Mapping(source = "dailyEir", target = "dailyEir", qualifiedByName = "toAvroDecimalScale")
    @Mapping(source = "calculatedAnnualEir", target = "calculatedAnnualEir", qualifiedByName = "toAvroDecimalScale")
    @Mapping(source = "numberOfRepayments", target = "actualNoTerm")
    @Mapping(source = "delinquencyStartType", target = "delinquency.delinquencyStartType")
    @Mapping(source = "delinquencyStartDate", target = "delinquency.delinquencyStartDate")
    @Mapping(source = "breach.id", target = "breach.id")
    @Mapping(source = "breach.name", target = "breach.name")
    @Mapping(source = "breach.breachFrequency", target = "breach.breachFrequency")
    @Mapping(source = "breach.breachFrequencyType", target = "breach.breachFrequencyType")
    @Mapping(source = "breach.breachAmountCalculationType", target = "breach.breachAmountCalculationType")
    @Mapping(source = "breach.breachAmount", target = "breach.breachAmount")
    @Mapping(source = "breachGraceDays", target = "breach.breachGraceDays")
    @Mapping(source = "breachStartDate", target = "breach.breachStartDate")
    @Mapping(source = "nearBreach", target = "breach.nearBreach")
    @Mapping(target = "breach.breachSchedule", ignore = true)
    @Mapping(target = "overpaidOnDate", ignore = true)
    @Mapping(target = "customData", ignore = true)
    WorkingCapitalLoanAccountDataV1 map(WorkingCapitalLoanData source);

    @Mapping(source = "principal", target = "totalPrincipal")
    @Mapping(source = "totalDisbursement", target = "principalDisbursed")
    @Mapping(source = "fee", target = "feeChargesCharged")
    @Mapping(source = "feePaid", target = "feeChargesPaid")
    @Mapping(source = "feeOutstanding", target = "feeChargesOutstanding")
    @Mapping(source = "penalty", target = "penaltyChargesCharged")
    @Mapping(source = "penaltyPaid", target = "penaltyChargesPaid")
    @Mapping(source = "penaltyOutstanding", target = "penaltyChargesOutstanding")
    @Mapping(source = "principalAdjustment", target = "principalAdjustments")
    @Mapping(target = "principalWrittenOff", ignore = true)
    @Mapping(target = "feeChargesWrittenOff", ignore = true)
    @Mapping(target = "penaltyChargesWrittenOff", ignore = true)
    @Mapping(target = "totalWrittenOff", ignore = true)
    @Mapping(target = "totalRecovered", ignore = true)
    @Mapping(target = "writeoffReasonId", ignore = true)
    @Mapping(target = "writeoffReason", ignore = true)
    @Mapping(target = "chargeOffReasonId", ignore = true)
    @Mapping(target = "chargeOffReason", ignore = true)
    @Mapping(target = "totalPayoutRefund", ignore = true)
    @Mapping(target = "totalPayoutRefundReversed", ignore = true)
    @Mapping(target = "totalGoodwillCredit", ignore = true)
    @Mapping(target = "totalGoodwillCreditReversed", ignore = true)
    @Mapping(target = "totalChargeAdjustment", ignore = true)
    @Mapping(target = "totalChargeAdjustmentReversed", ignore = true)
    @Mapping(target = "totalCreditBalanceRefund", ignore = true)
    @Mapping(target = "totalCreditBalanceRefundReversed", ignore = true)
    @Mapping(target = "totalRepaymentTransaction", ignore = true)
    @Mapping(target = "totalRepaymentTransactionReversed", ignore = true)
    WorkingCapitalLoanSummaryDataV1 map(WorkingCapitalLoanSummaryData source);

    @Mapping(source = "delinquentPrincipal", target = "totalDelinquentAmount")
    @Mapping(target = "lastPaymentDate", ignore = true)
    @Mapping(target = "lastPaymentAmount", ignore = true)
    @Mapping(target = "lastRepaymentDate", ignore = true)
    @Mapping(target = "lastRepaymentAmount", ignore = true)
    @Mapping(target = "delinquencySchedule", ignore = true)
    WorkingCapitalLoanCollectionDataV1 map(WorkingCapitalLoanCollectionData source);

    @Mapping(target = "amountAccrued", ignore = true)
    @Mapping(target = "amountUnrecognized", ignore = true)
    @Mapping(target = "amountWrittenOff", ignore = true)
    @Mapping(target = "customData", ignore = true)
    WorkingCapitalLoanChargeDataV1 map(WorkingCapitalLoanChargeData source);

    @Mapping(target = "netDisbursalAmount", ignore = true)
    WorkingCapitalLoanDisbursementDetailDataV1 map(WorkingCapitalLoanDisbursementDetailData source);

    @Mapping(target = "tags", ignore = true)
    WorkingCapitalLoanDelinquencySchedulePeriodDataV1 map(WorkingCapitalLoanDelinquencyRangeScheduleData source);

    List<WorkingCapitalLoanDelinquencySchedulePeriodDataV1> mapDelinquencySchedule(
            List<WorkingCapitalLoanDelinquencyRangeScheduleData> source);

    WorkingCapitalLoanDelinquencyScheduleTagDataV1 map(WorkingCapitalLoanRangeScheduleDelinquencyData source);

    WorkingCapitalLoanBreachSchedulePeriodDataV1 map(WorkingCapitalLoanBreachScheduleData source);

    List<WorkingCapitalLoanBreachSchedulePeriodDataV1> mapBreachSchedule(List<WorkingCapitalLoanBreachScheduleData> source);

    @Named("toAvroDecimalScale")
    default BigDecimal toAvroDecimalScale(final BigDecimal value) {
        return value == null ? null : value.setScale(AVRO_DECIMAL_SCALE, RoundingMode.HALF_UP);
    }
}
