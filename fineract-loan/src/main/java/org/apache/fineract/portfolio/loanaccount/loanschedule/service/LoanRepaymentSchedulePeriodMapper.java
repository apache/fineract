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
package org.apache.fineract.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.PreGeneratedLoanSchedulePeriod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class)
public interface LoanRepaymentSchedulePeriodMapper {

    @Mapping(target = "periodNumber", source = "installmentNumber")
    @Mapping(target = "periodFromDate", source = "fromDate")
    @Mapping(target = "periodDueDate", source = "dueDate")
    @Mapping(target = "principalDue", source = "source", qualifiedByName = "toPrincipalDue")
    @Mapping(target = "interestDue", source = "source", qualifiedByName = "toInterestDue")
    @Mapping(target = "feeChargesDue", source = "source", qualifiedByName = "toFeeChargesDue")
    @Mapping(target = "penaltyChargesDue", source = "source", qualifiedByName = "toPenaltyChargesDue")
    @Mapping(target = "rescheduleInterestPortion", source = "source", qualifiedByName = "toRescheduleInterestPortion")
    @Mapping(target = "isRecalculatedInterestComponent", source = "source", qualifiedByName = "toIsRecalculatedInterestComponent")
    @Mapping(target = "isEMIFixedSpecificToInstallment", source = "source", qualifiedByName = "toIsEMIFixedSpecificToInstallment")
    @Mapping(target = "isRepaymentPeriod", source = "source", qualifiedByName = "toIsRepaymentPeriod")
    @Mapping(target = "isDownPaymentPeriod", source = "source", qualifiedByName = "toIsDownPaymentPeriod")
    @Mapping(target = "loanCompoundingDetails", source = "source", qualifiedByName = "toLoanCompoundingDetails")

    PreGeneratedLoanSchedulePeriod map(LoanRepaymentScheduleInstallment source);

    List<PreGeneratedLoanSchedulePeriod> map(List<LoanRepaymentScheduleInstallment> source);

    @Named("toPrincipalDue")
    default BigDecimal toPrincipalDue(LoanRepaymentScheduleInstallment source) {
        return BigDecimal.ZERO;
    }

    @Named("toInterestDue")
    default BigDecimal toInterestDue(LoanRepaymentScheduleInstallment source) {
        return BigDecimal.ZERO;
    }

    @Named("toFeeChargesDue")
    default BigDecimal toFeeChargesDue(LoanRepaymentScheduleInstallment source) {
        return BigDecimal.ZERO;
    }

    @Named("toPenaltyChargesDue")
    default BigDecimal toPenaltyChargesDue(LoanRepaymentScheduleInstallment source) {
        return BigDecimal.ZERO;
    }

    @Named("toIsRecalculatedInterestComponent")
    default Boolean toIsRecalculatedInterestComponent(LoanRepaymentScheduleInstallment source) {
        return source.isRecalculatedInterestComponent();
    }

    @Named("toRescheduleInterestPortion")
    default BigDecimal toRescheduleInterestPortion(LoanRepaymentScheduleInstallment source) {
        return BigDecimal.ZERO;
    }

    @Named("toIsEMIFixedSpecificToInstallment")
    default Boolean toIsEMIFixedSpecificToInstallment(LoanRepaymentScheduleInstallment source) {
        return Boolean.FALSE;
    }

    @Named("toIsRepaymentPeriod")
    default Boolean toIsRepaymentPeriod(LoanRepaymentScheduleInstallment source) {
        return !source.isDownPayment();
    }

    @Named("toIsDownPaymentPeriod")
    default Boolean toIsDownPaymentPeriod(LoanRepaymentScheduleInstallment source) {
        return source.isDownPayment();
    }

    @Named("toLoanCompoundingDetails")
    default Set toLoanCompoundingDetails(LoanRepaymentScheduleInstallment source) {
        return new HashSet<>();
    }
}
