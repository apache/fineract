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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPaymentAllocationRule;
import org.apache.fineract.portfolio.workingcapitalloanproduct.data.WorkingCapitalPaymentAllocationData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAllocationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.mapper.WorkingCapitalLoanProductMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(config = MapstructMapperConfig.class, uses = { DelinquencyBucketMapper.class, WorkingCapitalLoanProductMapper.class,
        WorkingCapitalLoanBalanceMapper.class, WorkingCapitalLoanDisbursementDetailMapper.class })
public interface WorkingCapitalLoanMapper {

    @Mapping(target = "accountNo", source = "accountNumber")
    @Mapping(target = "client", source = "client", qualifiedByName = "clientToData")
    @Mapping(target = "officeId", source = "client.office.id")
    @Mapping(target = "fundId", source = "fund.id")
    @Mapping(target = "fundName", source = "fund.name")
    @Mapping(target = "product", source = "loanProduct")
    @Mapping(target = "status", source = "loanStatus", qualifiedByName = "loanStatusData")
    @Mapping(target = "currency", source = "loanProductRelatedDetails", qualifiedByName = "monetaryCurrencyToCurrencyData")
    @Mapping(target = "periodPaymentRate", source = "loanProductRelatedDetails.periodPaymentRate")
    @Mapping(target = "repaymentEvery", source = "loanProductRelatedDetails.repaymentEvery")
    @Mapping(target = "repaymentFrequencyType", source = "loanProductRelatedDetails", qualifiedByName = "repaymentFrequencyTypeData")
    @Mapping(target = "discount", source = "loanProductRelatedDetails.discount")
    @Mapping(target = "delinquencyBucket", source = "loanProductRelatedDetails.delinquencyBucket")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "paymentAllocation", source = "paymentAllocationRules", qualifiedByName = "paymentAllocationRulesToData")
    @Mapping(target = "timeline", source = "loan", qualifiedByName = "timelineData")
    @Mapping(target = "disbursementDetails", source = "disbursementDetails")
    WorkingCapitalLoanData toData(WorkingCapitalLoan loan);

    List<WorkingCapitalLoanData> toDataList(List<WorkingCapitalLoan> loans);

    @Named("clientToData")
    default ClientData clientToData(final Client client) {
        ClientData clientData = ClientData.instance(client.getId(), client.getDisplayName());
        clientData.setAccountNo(client.getAccountNumber());
        return clientData;
    }

    @Named("loanStatusData")
    default LoanStatusEnumData loanStatusData(final LoanStatus loanStatus) {
        return LoanEnumerations.status(loanStatus);
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

    @Named("paymentAllocationRulesToData")
    default List<WorkingCapitalPaymentAllocationData> paymentAllocationRulesToData(
            final List<WorkingCapitalLoanPaymentAllocationRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        final List<WorkingCapitalPaymentAllocationData> result = new ArrayList<>();
        for (WorkingCapitalLoanPaymentAllocationRule rule : rules) {
            final AtomicInteger counter = new AtomicInteger(1);
            final List<WorkingCapitalPaymentAllocationData.PaymentAllocationOrder> orders = new ArrayList<>();
            if (rule.getAllocationTypes() != null) {
                for (WorkingCapitalPaymentAllocationType type : rule.getAllocationTypes()) {
                    orders.add(new WorkingCapitalPaymentAllocationData.PaymentAllocationOrder(type.name(), counter.getAndIncrement()));
                }
            }
            result.add(new WorkingCapitalPaymentAllocationData(rule.getTransactionType(), orders));
        }
        return result;
    }

    @Named("timelineData")
    default LoanApplicationTimelineData timelineData(final WorkingCapitalLoan loan) {
        final LoanApplicationTimelineData timelineData = new LoanApplicationTimelineData();
        final LocalDate expectedDisbursementDate = loan.getDisbursementDetails().isEmpty() ? null
                : loan.getDisbursementDetails().getFirst().getExpectedDisbursementDate();
        timelineData.setExpectedDisbursementDate(expectedDisbursementDate);
        timelineData.setSubmittedOnDate(loan.getSubmittedOnDate());
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
        if (loan.getDisbursementDetails() != null && !loan.getDisbursementDetails().isEmpty()) {
            timelineData.setDisbursementDetails(
                    Mappers.getMapper(WorkingCapitalLoanDisbursementDetailMapper.class).toDataList(loan.getDisbursementDetails()));
        }
        return timelineData;
    }
}
