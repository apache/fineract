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
package org.apache.fineract.portfolio.loanaccount.mapper;

import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.organisation.monetary.mapper.CurrencyMapper;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class, uses = { LoanTransactionRelationMapper.class, LoanChargePaidByMapper.class,
        CurrencyMapper.class })
public interface LoanTransactionMapper {

    @Mapping(target = "numberOfRepayments", ignore = true)
    @Mapping(target = "loanRepaymentScheduleInstallments", ignore = true)
    @Mapping(target = "writeOffReasonOptions", ignore = true)
    @Mapping(target = "chargeOffReasonOptions", ignore = true)
    @Mapping(target = "paymentTypeOptions", ignore = true)
    @Mapping(target = "overpaymentPortion", ignore = true)
    @Mapping(target = "transfer", ignore = true)
    @Mapping(target = "fixedEmiAmount", ignore = true)
    @Mapping(target = "date", source = "dateOf")
    @Mapping(target = "loanChargePaidByList", source = "loanChargesPaid")
    @Mapping(target = "manuallyReversed", source = "manuallyAdjustedOrReversed")
    @Mapping(target = "transactionRelations", source = "loanTransactionRelations")
    @Mapping(target = "officeId", source = "office.id")
    @Mapping(target = "officeName", source = "office.name")
    @Mapping(target = "loanId", source = "loan.id")
    @Mapping(target = "externalLoanId", source = "loan.externalId")
    @Mapping(target = "netDisbursalAmount", source = "loan.netDisbursalAmount")
    @Mapping(target = "transactionType", expression = "java(org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.transactionType(loanTransaction.getTypeOf()))")
    @Mapping(target = "paymentDetailData", expression = "java(loanTransaction.getPaymentDetail() != null ? loanTransaction.getPaymentDetail().toData() : null)")
    @Mapping(target = "currency", source = "loan.currency")
    LoanTransactionData mapLoanTransaction(LoanTransaction loanTransaction);
}
