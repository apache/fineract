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
package org.apache.fineract.infrastructure.event.external.service.serialization.mapper.workingcapitalloan;

import static org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants.WORKING_CAPITAL_LOAN_TRANSACTION_IDENTIFIER;

import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.avro.generic.v1.EnumOptionDataV1;
import org.apache.fineract.avro.generic.v1.StringEnumOptionDataV1;
import org.apache.fineract.avro.gl.v1.GLAccountDataV1;
import org.apache.fineract.avro.payment.v1.PaymentDetailDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanJournalEntryDataV1;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.AvroMapperConfig;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = AvroMapperConfig.class)
public interface WorkingCapitalLoanJournalEntryDataMapper {

    @Mapping(source = "entityId", target = "loanId")
    @Mapping(source = "transactionId", target = "wcLoanTransactionId", qualifiedByName = "toWorkingCapitalLoanTransactionId")
    @Mapping(source = "type", target = "type", qualifiedByName = "toJournalEntryType")
    @Mapping(source = "office.id", target = "officeId")
    @Mapping(source = "reversalJournalEntry.id", target = "reversalId")
    @Mapping(source = "paymentDetail", target = "paymentDetailData", qualifiedByName = "toPaymentDetailData")
    WorkingCapitalLoanJournalEntryDataV1 map(JournalEntry source);

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "type", target = "type", qualifiedByName = "toGlAccountType")
    @Mapping(source = "usage", target = "usage", qualifiedByName = "toGlAccountUsage")
    @Mapping(target = "nameDecorated", ignore = true)
    @Mapping(target = "tagId", ignore = true)
    @Mapping(target = "organizationRunningBalance", ignore = true)
    GLAccountDataV1 map(GLAccount source);

    EnumOptionDataV1 map(EnumOptionData source);

    PaymentDetailDataV1 map(PaymentDetailData source);

    @Named("toPaymentDetailData")
    default PaymentDetailDataV1 toPaymentDetailData(final PaymentDetail paymentDetail) {
        return paymentDetail == null ? null : map(paymentDetail.toData());
    }

    @Named("toWorkingCapitalLoanTransactionId")
    default Long toWorkingCapitalLoanTransactionId(final String transactionId) {
        if (transactionId == null || !transactionId.startsWith(WORKING_CAPITAL_LOAN_TRANSACTION_IDENTIFIER)) {
            return null;
        }
        final String numericPart = transactionId.substring(WORKING_CAPITAL_LOAN_TRANSACTION_IDENTIFIER.length());
        return numericPart.chars().allMatch(Character::isDigit) && !numericPart.isEmpty() ? Long.valueOf(numericPart) : null;
    }

    @Named("toJournalEntryType")
    default StringEnumOptionDataV1 toJournalEntryType(final Integer type) {
        final JournalEntryType journalEntryType = JournalEntryType.fromInt(type);
        return journalEntryType == null ? null
                : StringEnumOptionDataV1.newBuilder().setId(journalEntryType.name()).setCode(journalEntryType.getCode())
                        .setValue(journalEntryType.name()).build();
    }

    @Named("toGlAccountType")
    default EnumOptionDataV1 toGlAccountType(final Integer type) {
        return type == null ? null : map(AccountingEnumerations.gLAccountType(type));
    }

    @Named("toGlAccountUsage")
    default EnumOptionDataV1 toGlAccountUsage(final Integer usage) {
        return usage == null ? null : map(AccountingEnumerations.gLAccountUsage(usage));
    }
}
