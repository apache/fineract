package org.mifosplatform.accounting.accountmapping.data;

import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;

public class FinancialActivityAccountData {

    private final Long id;
    private final GLAccountData glAccountData;
    private final Map<String, List<GLAccountData>> accountingMappingOptions;

    public static FinancialActivityAccountData instance(final Long id, final GLAccountData glAccountData) {
        final Map<String, List<GLAccountData>> accountingMappingOptions = null;
        return new FinancialActivityAccountData(id, glAccountData, accountingMappingOptions);
    }

    public static FinancialActivityAccountData template(final Map<String, List<GLAccountData>> accountingMappingOptions) {
        final Long id = null;
        final GLAccountData glAccountData = null;
        return new FinancialActivityAccountData(id, glAccountData, accountingMappingOptions);
    }

    public static FinancialActivityAccountData associateTemplateData(final FinancialActivityAccountData templateData,
            final FinancialActivityAccountData accountMappingData) {
        return new FinancialActivityAccountData(accountMappingData.id, accountMappingData.glAccountData,
                templateData.accountingMappingOptions);
    }

    private FinancialActivityAccountData(final Long id, final GLAccountData glAccountData,
            final Map<String, List<GLAccountData>> accountingMappingOptions) {
        this.id = id;
        this.glAccountData = glAccountData;
        this.accountingMappingOptions = accountingMappingOptions;
    }

}
