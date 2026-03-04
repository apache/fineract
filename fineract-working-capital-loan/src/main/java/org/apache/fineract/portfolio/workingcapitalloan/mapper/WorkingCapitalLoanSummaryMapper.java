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
import java.util.Objects;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.accountdetails.data.WorkingCapitalLoanAccountSummaryData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class, uses = { WorkingCapitalLoanMapper.class })
public interface WorkingCapitalLoanSummaryMapper {

    @Mapping(target = "accountNo", source = "accountNumber")
    @Mapping(target = "externalId", source = "externalId", qualifiedByName = "externalIdToString")
    @Mapping(target = "productId", source = "loan", qualifiedByName = "productIdFromLoan")
    @Mapping(target = "productName", source = "loan", qualifiedByName = "productNameFromLoan")
    @Mapping(target = "shortProductName", source = "loan", qualifiedByName = "shortProductNameFromLoan")
    @Mapping(target = "status", source = "loanStatus", qualifiedByName = "loanStatusToEnumData")
    @Mapping(target = "currency", source = "loanProductRelatedDetails", qualifiedByName = "monetaryCurrencyToCurrencyData")
    @Mapping(target = "loanType", expression = "java((org.apache.fineract.infrastructure.core.data.EnumOptionData) null)")
    @Mapping(target = "loanCycle", source = "loanProductCounter")
    @Mapping(target = "timeline", source = "loan", qualifiedByName = "buildTimeline")
    @Mapping(target = "inArrears", expression = "java(Boolean.FALSE)")
    @Mapping(target = "loanBalance", expression = "java(loan.getBalance() != null ? loan.getBalance().getPrincipalOutstanding() : null)")
    @Mapping(target = "amountPaid", expression = "java(java.math.BigDecimal.ZERO)")
    WorkingCapitalLoanAccountSummaryData toData(WorkingCapitalLoan loan);

    List<WorkingCapitalLoanAccountSummaryData> toDataList(List<WorkingCapitalLoan> loans);

    @Named("externalIdToString")
    default String externalIdToString(final ExternalId externalId) {
        return externalId != null ? externalId.getValue() : null;
    }

    @Named("loanStatusToEnumData")
    default LoanStatusEnumData loanStatusToEnumData(final LoanStatus loanStatus) {
        return loanStatus != null ? LoanEnumerations.status(loanStatus) : null;
    }

    @Named("buildTimeline")
    default LoanApplicationTimelineData buildTimeline(final WorkingCapitalLoan loan) {
        if (loan == null) {
            return null;
        }
        final LoanApplicationTimelineData timeline = new LoanApplicationTimelineData();
        timeline.setSubmittedOnDate(loan.getSubmittedOnDate());
        timeline.setApprovedOnDate(loan.getApprovedOnDate());
        timeline.setRejectedOnDate(loan.getRejectedOnDate());
        final LocalDate expectedDisbursementDate = loan.getDisbursementDetails().isEmpty() ? null
                : loan.getDisbursementDetails().getFirst().getExpectedDisbursementDate();
        timeline.setExpectedDisbursementDate(expectedDisbursementDate);
        final LocalDate actualDisbursementDate = loan.getDisbursementDetails().stream()
                .map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate).filter(Objects::nonNull).findFirst().orElse(null);
        timeline.setActualDisbursementDate(actualDisbursementDate);
        timeline.setClosedOnDate(loan.getClosedOnDate());
        timeline.setExpectedMaturityDate(loan.getExpectedMaturityDate());
        timeline.setActualMaturityDate(loan.getMaturedOnDate());
        return timeline;
    }

    @Named("productIdFromLoan")
    default Long productIdFromLoan(final WorkingCapitalLoan loan) {
        return (loan != null && loan.getLoanProduct() != null) ? loan.getLoanProduct().getId() : null;
    }

    @Named("productNameFromLoan")
    default String productNameFromLoan(final WorkingCapitalLoan loan) {
        return (loan != null && loan.getLoanProduct() != null) ? loan.getLoanProduct().getName() : null;
    }

    @Named("shortProductNameFromLoan")
    default String shortProductNameFromLoan(final WorkingCapitalLoan loan) {
        return (loan != null && loan.getLoanProduct() != null) ? loan.getLoanProduct().getShortName() : null;
    }
}
