package org.mifosplatform.accounting.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.accounting.domain.GLAccountType;
import org.mifosplatform.accounting.domain.GLAccountUsage;
import org.mifosplatform.accounting.domain.JournalEntryType;
import org.mifosplatform.accounting.domain.PortfolioProductType;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class AccountingEnumerations {

    public static EnumOptionData gLAccountType(final int id) {
        return gLAccountType(GLAccountType.fromInt(id));
    }

    public static EnumOptionData gLAccountType(final GLAccountType accountType) {
        EnumOptionData optionData = new EnumOptionData(accountType.getValue().longValue(), accountType.getCode(), accountType.toString());
        return optionData;
    }

    public static List<EnumOptionData> gLAccountType(GLAccountType[] accountTypes) {
        List<EnumOptionData> optionDatas = new ArrayList<EnumOptionData>();
        for (GLAccountType accountType : accountTypes) {
            optionDatas.add(gLAccountType(accountType));
        }
        return optionDatas;
    }

    public static EnumOptionData gLAccountUsage(final int id) {
        return gLAccountUsage(GLAccountUsage.fromInt(id));
    }

    public static EnumOptionData gLAccountUsage(final GLAccountUsage accountUsage) {
        EnumOptionData optionData = new EnumOptionData(accountUsage.getValue().longValue(), accountUsage.getCode(), accountUsage.toString());
        return optionData;
    }

    public static List<EnumOptionData> gLAccountUsage(GLAccountUsage[] accountUsages) {
        List<EnumOptionData> optionDatas = new ArrayList<EnumOptionData>();
        for (GLAccountUsage accountUsage : accountUsages) {
            optionDatas.add(gLAccountUsage(accountUsage));
        }
        return optionDatas;
    }

    public static EnumOptionData journalEntryType(final int id) {
        return journalEntryType(JournalEntryType.fromInt(id));
    }

    public static EnumOptionData journalEntryType(final JournalEntryType journalEntryType) {
        EnumOptionData optionData = new EnumOptionData(journalEntryType.getValue().longValue(), journalEntryType.getCode(),
                journalEntryType.toString());
        return optionData;
    }

    public static List<EnumOptionData> journalEntryTypes(JournalEntryType[] journalEntryTypes) {
        List<EnumOptionData> optionDatas = new ArrayList<EnumOptionData>();
        for (JournalEntryType journalEntryType : journalEntryTypes) {
            optionDatas.add(journalEntryType(journalEntryType));
        }
        return optionDatas;
    }

    public static EnumOptionData portfolioProductType(final int id) {
        return portfolioProductType(PortfolioProductType.fromInt(id));
    }

    public static EnumOptionData portfolioProductType(final PortfolioProductType portfolioProductType) {
        EnumOptionData optionData = new EnumOptionData(portfolioProductType.getValue().longValue(), portfolioProductType.getCode(),
                portfolioProductType.toString());
        return optionData;
    }

}
