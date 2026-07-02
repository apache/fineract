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

import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanSummaryData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class)
public interface WorkingCapitalLoanSummaryDataMapper {

    @Named("toSummaryData")
    @Mapping(target = "currency", source = ".", qualifiedByName = "toCurrency")
    // Principal
    @Mapping(target = "principal", source = "balance.totalPrincipalDue", qualifiedByName = "nullToZero")
    @Mapping(target = "principalPaid", source = "balance.principalPaid", qualifiedByName = "nullToZero")
    @Mapping(target = "principalAdjustment", source = "balance.principalAdjustment", qualifiedByName = "nullToZero")
    @Mapping(target = "principalOutstanding", source = "balance.principalOutstanding", qualifiedByName = "nullToZero")
    // Fee
    @Mapping(target = "fee", source = "balance.fee", qualifiedByName = "nullToZero")
    @Mapping(target = "feePaid", source = "balance.feePaid", qualifiedByName = "nullToZero")
    @Mapping(target = "feeOutstanding", source = "balance.feeOutstanding", qualifiedByName = "nullToZero")
    // Penalty
    @Mapping(target = "penalty", source = "balance.penalty", qualifiedByName = "nullToZero")
    @Mapping(target = "penaltyPaid", source = "balance.penaltyPaid", qualifiedByName = "nullToZero")
    @Mapping(target = "penaltyOutstanding", source = "balance.penaltyOutstanding", qualifiedByName = "nullToZero")
    // Income recognition
    @Mapping(target = "realizedIncomeFromDiscountFee", source = "balance.realizedIncomeFromDiscountFee", qualifiedByName = "nullToZero")
    @Mapping(target = "unrealizedIncomeFromDiscountFee", source = "balance.unrealizedIncomeFromDiscountFee", qualifiedByName = "nullToZero")
    // Overpayment
    @Mapping(target = "overpayment", source = "balance.overpaymentAmount", qualifiedByName = "nullToZero")
    // Aggregates
    @Mapping(target = "totalExpectedRepayment", source = "balance.totalExpectedRepayment", qualifiedByName = "nullToZero")
    @Mapping(target = "totalRepayment", source = "balance.totalRepayment", qualifiedByName = "nullToZero")
    @Mapping(target = "totalOutstanding", source = "balance.totalOutstanding", qualifiedByName = "nullToZero")
    @Mapping(target = "totalDisbursement", source = "balance.totalDisbursement", qualifiedByName = "nullToZero")
    @Mapping(target = "totalDiscountFee", source = "balance.totalDiscountFee", qualifiedByName = "nullToZero")
    @Mapping(target = "totalDiscountFeeAdjustment", source = "balance.totalDiscountFeeAdjustment", qualifiedByName = "nullToZero")
    WorkingCapitalLoanSummaryData toData(WorkingCapitalLoan loan);

    @Named("toCurrency")
    default CurrencyData toCurrency(final WorkingCapitalLoan loan) {
        return loan.getLoanProduct().getCurrency().toData();
    }

    @Named("nullToZero")
    default BigDecimal nullToZero(final BigDecimal value) {
        return MathUtil.nullToZero(value);
    }
}
