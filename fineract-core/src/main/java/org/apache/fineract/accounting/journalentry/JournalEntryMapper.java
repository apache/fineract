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
package org.apache.fineract.accounting.journalentry;

import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.journalentry.data.JournalEntryData;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class)
public interface JournalEntryMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "officeId", source = "office.id")
    @Mapping(target = "officeName", source = "office.name")
    @Mapping(target = "glAccountId", source = "glAccount.id")
    @Mapping(target = "glAccountCode", source = "glAccount.glCode")
    @Mapping(target = "glAccountName", source = "glAccount.name")
    @Mapping(target = "glAccountType", source = "glAccount.type", qualifiedByName = "glAccountType")
    @Mapping(target = "transactionDate", source = "transactionDate")
    @Mapping(target = "entryType", source = "type", qualifiedByName = "journalEntryType")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "entityType", source = "entityType", qualifiedByName = "entityType")
    @Mapping(target = "entityId", source = "entityId")
    @Mapping(target = "submittedOnDate", source = "submittedOnDate")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "currency", source = "currencyCode")
    @Mapping(target = "manualEntry", source = "manualEntry")
    @Mapping(target = "reversed", source = "reversed")
    @Mapping(target = "referenceNumber", source = "referenceNumber")
    @Mapping(target = "paymentTypeId", source = "paymentDetail.paymentType.id")
    @Mapping(target = "accountNumber", source = "paymentDetail.accountNumber")
    @Mapping(target = "checkNumber", source = "paymentDetail.checkNumber")
    @Mapping(target = "routingCode", source = "paymentDetail.routingCode")
    @Mapping(target = "receiptNumber", source = "paymentDetail.receiptNumber")
    @Mapping(target = "bankNumber", source = "paymentDetail.bankNumber")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    @Mapping(target = "createdByUserName", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "officeRunningBalance", ignore = true)
    @Mapping(target = "organizationRunningBalance", ignore = true)
    @Mapping(target = "runningBalanceComputed", ignore = true)
    @Mapping(target = "rowIndex", ignore = true)
    @Mapping(target = "dateFormat", ignore = true)
    @Mapping(target = "locale", ignore = true)
    @Mapping(target = "credits", ignore = true)
    @Mapping(target = "debits", ignore = true)
    @Mapping(target = "transactionDetails", ignore = true)
    @Mapping(target = "savingTransactionId", ignore = true)
    JournalEntryData map(JournalEntry journalEntry);

    @Named("entityType")
    default PortfolioProductType mapEntityType(Integer entityTypeId) {
        return PortfolioProductType.fromInt(entityTypeId);
    }

    @Named("glAccountType")
    default GLAccountType mapGlAccountType(Integer typeId) {
        return GLAccountType.fromInt(typeId);
    }

    @Named("journalEntryType")
    default JournalEntryType mapJournalEntryType(Integer typeId) {
        return JournalEntryType.fromInt(typeId);
    }

    default EnumOptionData mapGlAccountType(GLAccountType accountType) {
        return new EnumOptionData((long) accountType.getValue(), accountType.getCode(), accountType.name());
    }

    default EnumOptionData mapJournalEntryType(JournalEntryType journalEntryType) {
        return new EnumOptionData((long) journalEntryType.getValue(), journalEntryType.getCode(), journalEntryType.name());
    }

    default EnumOptionData mapEntityType(PortfolioProductType entityType) {
        return new EnumOptionData((long) entityType.getValue(), entityType.getCode(), entityType.name());
    }

    default CurrencyData mapCurrency(String currencyCode) {
        return new CurrencyData(currencyCode);
    }
}
