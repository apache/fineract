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
package org.apache.fineract.accounting.common;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.PortfolioProductType;

public final class AccountingEnumerations {

    private AccountingEnumerations() {

    }

    public static EnumOptionData gLAccountType(final int id) {
        return gLAccountType(GLAccountType.fromInt(id));
    }

    public static EnumOptionData gLAccountType(final GLAccountType accountType) {
        final EnumOptionData optionData = new EnumOptionData(accountType.getValue().longValue(), accountType.getCode(),
                accountType.toString());
        return optionData;
    }

    public static List<EnumOptionData> gLAccountType(final GLAccountType[] accountTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final GLAccountType accountType : accountTypes) {
            optionDatas.add(gLAccountType(accountType));
        }
        return optionDatas;
    }

    public static EnumOptionData gLAccountUsage(final int id) {
        return gLAccountUsage(GLAccountUsage.fromInt(id));
    }

    public static EnumOptionData gLAccountUsage(final GLAccountUsage accountUsage) {
        final EnumOptionData optionData = new EnumOptionData(accountUsage.getValue().longValue(), accountUsage.getCode(),
                accountUsage.toString());
        return optionData;
    }

    public static List<EnumOptionData> gLAccountUsage(final GLAccountUsage[] accountUsages) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final GLAccountUsage accountUsage : accountUsages) {
            optionDatas.add(gLAccountUsage(accountUsage));
        }
        return optionDatas;
    }

    public static EnumOptionData journalEntryType(final int id) {
        return journalEntryType(JournalEntryType.fromInt(id));
    }

    public static EnumOptionData journalEntryType(final JournalEntryType journalEntryType) {
        final EnumOptionData optionData = new EnumOptionData(journalEntryType.getValue().longValue(), journalEntryType.getCode(),
                journalEntryType.toString());
        return optionData;
    }

    public static List<EnumOptionData> journalEntryTypes(final JournalEntryType[] journalEntryTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final JournalEntryType journalEntryType : journalEntryTypes) {
            optionDatas.add(journalEntryType(journalEntryType));
        }
        return optionDatas;
    }

    public static EnumOptionData portfolioProductType(final int id) {
        return portfolioProductType(PortfolioProductType.fromInt(id));
    }

    public static EnumOptionData portfolioProductType(final PortfolioProductType portfolioProductType) {
        final EnumOptionData optionData = new EnumOptionData(portfolioProductType.getValue().longValue(), portfolioProductType.getCode(),
                portfolioProductType.toString());
        return optionData;
    }

    public static EnumOptionData accountingRuleType(final int id) {
        return accountingRuleType(AccountingRuleType.fromInt(id));
    }

    public static EnumOptionData accountingRuleType(final AccountingRuleType type) {
        final EnumOptionData optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), type.toString());
        return optionData;
    }

    public static List<EnumOptionData> accountingRuleTypes(final AccountingRuleType[] accountingRuleTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final AccountingRuleType accountingRuleType : accountingRuleTypes) {
            optionDatas.add(accountingRuleType(accountingRuleType));
        }
        return optionDatas;
    }

}
