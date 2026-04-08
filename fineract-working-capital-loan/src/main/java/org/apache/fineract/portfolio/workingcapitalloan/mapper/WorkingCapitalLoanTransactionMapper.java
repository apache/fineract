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

import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class)
public interface WorkingCapitalLoanTransactionMapper {

    @Mapping(target = "type", source = "transactionType", qualifiedByName = "loanTransactionTypeToEnumData")
    @Mapping(target = "paymentDetailData", source = "paymentDetail", qualifiedByName = "paymentDetailToData")
    @Mapping(target = "transactionDate", source = "transactionDate")
    @Mapping(target = "principalPortion", source = "allocation.principalPortion")
    @Mapping(target = "feeChargesPortion", source = "allocation.feeChargesPortion")
    @Mapping(target = "penaltyChargesPortion", source = "allocation.penaltyChargesPortion")
    WorkingCapitalLoanTransactionData toData(WorkingCapitalLoanTransaction transaction);

    @Named("loanTransactionTypeToEnumData")
    default LoanTransactionEnumData loanTransactionTypeToEnumData(final LoanTransactionType type) {
        return type == null ? null : LoanEnumerations.transactionType(type);
    }

    @Named("paymentDetailToData")
    default PaymentDetailData paymentDetailToData(final PaymentDetail paymentDetail) {
        if (paymentDetail == null) {
            return null;
        }
        return PaymentDetailData.builder().id(paymentDetail.getId()).accountNumber(paymentDetail.getAccountNumber())
                .checkNumber(paymentDetail.getCheckNumber()).routingCode(paymentDetail.getRoutingCode())
                .receiptNumber(paymentDetail.getReceiptNumber()).bankNumber(paymentDetail.getBankNumber()).build();
    }
}
