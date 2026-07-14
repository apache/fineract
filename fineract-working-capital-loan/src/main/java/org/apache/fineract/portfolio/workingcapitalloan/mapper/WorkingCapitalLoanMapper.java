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
package org.apache.fineract.portfolio.workingcapitalloan.mapper;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloanbreach.mapper.WorkingCapitalBreachMapper;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.mapper.WorkingCapitalNearBreachMapper;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.apache.fineract.portfolio.workingcapitalloanproduct.mapper.WorkingCapitalLoanProductMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class, uses = { DelinquencyBucketMapper.class, WorkingCapitalLoanProductMapper.class,
        WorkingCapitalLoanBalanceMapper.class, WorkingCapitalLoanDisbursementDetailMapper.class, WorkingCapitalLoanTransactionMapper.class,
        WorkingCapitalBreachMapper.class, WorkingCapitalNearBreachMapper.class, WorkingCapitalLoanSummaryDataMapper.class,
        WorkingCapitalLoanPaymentAllocationMapper.class })
public interface WorkingCapitalLoanMapper {

    @Mapping(target = "accountNo", source = "accountNumber")
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientAccountNo", source = "client.accountNumber")
    @Mapping(target = "clientName", source = "client.displayName")
    @Mapping(target = "clientExternalId", source = "client.externalId")
    @Mapping(target = "clientOfficeId", source = "client.office.id")
    @Mapping(target = "fundId", source = "fund.id")
    @Mapping(target = "fundName", source = "fund.name")
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "loanProductId", source = "loanProduct.id")
    @Mapping(target = "loanProductName", source = "loanProduct.name")
    @Mapping(target = "loanProductDescription", source = "loanProduct.description")
    @Mapping(target = "status", source = "loanStatus", qualifiedByName = "loanStatusData")
    @Mapping(target = "currency", source = "loanProductRelatedDetails", qualifiedByName = "monetaryCurrencyToCurrencyData")
    @Mapping(target = "paymentRate", source = "loanProductRelatedDetails.periodPaymentRate")
    @Mapping(target = "repaymentEvery", source = "loanProductRelatedDetails.repaymentEvery")
    @Mapping(target = "repaymentFrequencyType", source = "loanProductRelatedDetails", qualifiedByName = "repaymentFrequencyTypeData")
    @Mapping(target = "discountFee", source = "loanProductRelatedDetails.discount")
    @Mapping(target = "proposedDiscountFee", source = "loanProductRelatedDetails.discountProposed")
    @Mapping(target = "approvedDiscountFee", source = "loanProductRelatedDetails.discountApproved")
    @Mapping(target = "breach", source = "loanProductRelatedDetails.breach")
    @Mapping(target = "nearBreach", source = "loanProductRelatedDetails.nearBreach")
    @Mapping(target = "delinquencyBucket", source = "loanProductRelatedDetails.delinquencyBucket")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "paymentAllocation", source = "paymentAllocationRules", qualifiedByName = "paymentAllocationRulesToData")
    @Mapping(target = "timeline", source = "loan", qualifiedByName = "timelineData")
    @Mapping(target = "disbursementDetails", source = "disbursementDetails")
    @Mapping(target = "delinquencyGraceDays", source = "loanProductRelatedDetails.delinquencyGraceDays")
    @Mapping(target = "delinquencyStartType", source = "loanProductRelatedDetails", qualifiedByName = "delinquencyStartTypeData")
    @Mapping(target = "breachGraceDays", source = "loanProductRelatedDetails.breachGraceDays")
    @Mapping(target = "breachStartType", source = "loanProductRelatedDetails", qualifiedByName = "breachStartTypeData")
    @Mapping(target = "breachStartDate", ignore = true)
    @Mapping(target = "delinquencyStartDate", ignore = true)
    @Mapping(target = "delinquent", ignore = true)
    @Mapping(target = "numberOfRepayments", ignore = true)
    @Mapping(target = "periodPaymentAmount", ignore = true)
    @Mapping(target = "dailyEir", ignore = true)
    @Mapping(target = "calculatedAnnualEir", ignore = true)
    @Mapping(target = "summary", source = ".", qualifiedByName = "toSummaryData")
    @Mapping(target = "totalPaymentVolume", source = "totalPaymentVolume")
    @Mapping(target = "principal", source = "loanProductRelatedDetails.principal")
    @Mapping(target = "amortizationType", source = "loanProductRelatedDetails", qualifiedByName = "amortizationTypeData")
    @Mapping(target = "npvDayCount", source = "loanProductRelatedDetails.npvDayCount")
    @Mapping(target = "loanProductCounter", source = "loanProductCounter")
    @Mapping(target = "enableInstallmentLevelDelinquency", source = "loanProductRelatedDetails", qualifiedByName = "installmentLevelDelinquencyEnabled")
    @Mapping(target = "netDisbursalAmount", ignore = true)
    @Mapping(target = "charges", ignore = true)
    @Mapping(target = "originators", ignore = true)
    @Mapping(target = "fraud", source = "fraud")
    @Mapping(target = "chargeOffReason", source = "chargeOffReason", qualifiedByName = "chargeOffReasonData")
    WorkingCapitalLoanData toData(WorkingCapitalLoan loan);

    List<WorkingCapitalLoanData> toDataList(List<WorkingCapitalLoan> loans);

    @Named("loanStatusData")
    default LoanStatusEnumData loanStatusData(final LoanStatus loanStatus) {
        return LoanEnumerations.status(loanStatus);
    }

    @Named("chargeOffReasonData")
    default CodeValueData chargeOffReasonData(final CodeValue chargeOffReason) {
        return chargeOffReason != null ? chargeOffReason.toData() : null;
    }

    @Named("monetaryCurrencyToCurrencyData")
    default CurrencyData monetaryCurrencyToCurrencyData(final WorkingCapitalLoanProductRelatedDetails detail) {
        return (detail != null && detail.getCurrency() != null) ? detail.getCurrency().toData() : null;
    }

    @Named("repaymentFrequencyTypeData")
    default StringEnumOptionData repaymentFrequencyTypeData(final WorkingCapitalLoanProductRelatedDetails detail) {
        return (detail != null && detail.getRepaymentFrequencyType() != null) ? detail.getRepaymentFrequencyType().toStringEnumOptionData()
                : null;
    }

    @Named("delinquencyStartTypeData")
    default StringEnumOptionData delinquencyStartTypeData(final WorkingCapitalLoanProductRelatedDetails detail) {
        return (detail != null && detail.getDelinquencyStartType() != null) ? detail.getDelinquencyStartType().toStringEnumOptionData()
                : null;
    }

    @Named("breachStartTypeData")
    default StringEnumOptionData breachStartTypeData(final WorkingCapitalLoanProductRelatedDetails detail) {
        return (detail != null && detail.getBreachStartType() != null) ? detail.getBreachStartType().toStringEnumOptionData() : null;
    }

    @Named("amortizationTypeData")
    default StringEnumOptionData amortizationTypeData(final WorkingCapitalLoanProductRelatedDetails detail) {
        return (detail != null && detail.getAmortizationType() != null) ? detail.getAmortizationType().getValueAsStringEnumOptionData()
                : null;
    }

    @Named("installmentLevelDelinquencyEnabled")
    default Boolean installmentLevelDelinquencyEnabled(final WorkingCapitalLoanProductRelatedDetails detail) {
        return detail != null && detail.getDelinquencyBucket() != null;
    }

    @Named("timelineData")
    default LoanApplicationTimelineData timelineData(final WorkingCapitalLoan loan) {
        final LoanApplicationTimelineData timelineData = new LoanApplicationTimelineData();
        final LocalDate expectedDisbursementDate = loan.getDisbursementDetails().isEmpty() ? null
                : loan.getDisbursementDetails().getFirst().getExpectedDisbursementDate();
        timelineData.setExpectedDisbursementDate(expectedDisbursementDate);
        timelineData.setSubmittedOnDate(loan.getSubmittedOnDate());
        timelineData.setExpectedMaturityDate(loan.getExpectedMaturityDate());
        timelineData.setActualMaturityDate(loan.getMaturedOnDate());
        if (loan.getApprovedBy() != null) {
            timelineData.setApprovedByUsername(loan.getApprovedBy().getUsername());
            timelineData.setApprovedByFirstname(loan.getApprovedBy().getFirstname());
            timelineData.setApprovedByLastname(loan.getApprovedBy().getLastname());
            timelineData.setApprovedOnDate(loan.getApprovedOnDate());
        }
        final WorkingCapitalLoanDisbursementDetails firstDisbursement = loan.getDisbursementDetails().stream()
                .filter(d -> d.getActualDisbursementDate() != null).findFirst().orElse(null);
        if (firstDisbursement != null && firstDisbursement.getDisbursedBy() != null) {
            timelineData.setDisbursedByUsername(firstDisbursement.getDisbursedBy().getUsername());
            timelineData.setDisbursedByFirstname(firstDisbursement.getDisbursedBy().getFirstname());
            timelineData.setDisbursedByLastname(firstDisbursement.getDisbursedBy().getLastname());
            timelineData.setActualDisbursementDate(firstDisbursement.getActualDisbursementDate());
        }
        if (loan.getClosedBy() != null) {
            timelineData.setClosedByUsername(loan.getClosedBy().getUsername());
            timelineData.setClosedByFirstname(loan.getClosedBy().getFirstname());
            timelineData.setClosedByLastname(loan.getClosedBy().getLastname());
            timelineData.setClosedOnDate(loan.getClosedOnDate());
        }
        if (loan.getRejectedBy() != null) {
            timelineData.setRejectedByUsername(loan.getRejectedBy().getUsername());
            timelineData.setRejectedByFirstname(loan.getRejectedBy().getFirstname());
            timelineData.setRejectedByLastname(loan.getRejectedBy().getLastname());
            timelineData.setRejectedOnDate(loan.getRejectedOnDate());
        }
        return timelineData;
    }
}
