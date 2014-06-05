package org.mifosplatform.accounting.accountmapping.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.organisation.office.data.OfficeData;

public class OfficeToGLAccountMappingData {

    private final Long id;
    private final OfficeData officeData;
    private final GLAccountData glAccountData;
    private final Map<String, List<GLAccountData>> accountingMappingOptions;
    private final Collection<OfficeData> officeOptions;

    public static OfficeToGLAccountMappingData instance(final Long id, final OfficeData officeData, final GLAccountData glAccountData) {
        final Map<String, List<GLAccountData>> accountingMappingOptions = null;
        final Collection<OfficeData> officeOptions = null;
        return new OfficeToGLAccountMappingData(id, officeData, glAccountData, accountingMappingOptions, officeOptions);
    }

    public static OfficeToGLAccountMappingData template(final Map<String, List<GLAccountData>> accountingMappingOptions,
            final Collection<OfficeData> officeOptions) {
        final Long id = null;
        final OfficeData officeData = null;
        final GLAccountData glAccountData = null;
        return new OfficeToGLAccountMappingData(id, officeData, glAccountData, accountingMappingOptions, officeOptions);
    }

    public static OfficeToGLAccountMappingData associateTemplateData(final OfficeToGLAccountMappingData templateData,
            final OfficeToGLAccountMappingData accountMappingData) {
        return new OfficeToGLAccountMappingData(accountMappingData.id, accountMappingData.officeData, accountMappingData.glAccountData,
                templateData.accountingMappingOptions, templateData.officeOptions);
    }

    private OfficeToGLAccountMappingData(final Long id, final OfficeData officeData, final GLAccountData glAccountData,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final Collection<OfficeData> officeOptions) {
        this.id = id;
        this.officeData = officeData;
        this.glAccountData = glAccountData;
        this.accountingMappingOptions = accountingMappingOptions;
        this.officeOptions = officeOptions;
    }

}
