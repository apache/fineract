/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.data;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.glaccount.domain.GLAccountUsage;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

/**
 * Immutable object representing a General Ledger Account
 * 
 * Note: no getter/setters required as google-gson will produce json from fields
 * of object.
 */
public class GLAccountData {

    private final Long id;
    private final String name;
    private final Long parentId;
    private final String glCode;
    private final Boolean disabled;
    private final Boolean manualEntriesAllowed;
    private final EnumOptionData type;
    private final EnumOptionData usage;
    private final String description;
    private final String nameDecorated;
    private final CodeValueData tagId;
    private final Long organizationRunningBalance;

    // templates
    final List<EnumOptionData> accountTypeOptions;
    final List<EnumOptionData> usageOptions;
    final List<GLAccountData> assetHeaderAccountOptions;
    final List<GLAccountData> liabilityHeaderAccountOptions;
    final List<GLAccountData> equityHeaderAccountOptions;
    final List<GLAccountData> incomeHeaderAccountOptions;
    final List<GLAccountData> expenseHeaderAccountOptions;
    final Collection<CodeValueData> allowedAssetsTagOptions;
    final Collection<CodeValueData> allowedLiabilitiesTagOptions;
    final Collection<CodeValueData> allowedEquityTagOptions;
    final Collection<CodeValueData> allowedIncomeTagOptions;
    final Collection<CodeValueData> allowedExpensesTagOptions;

    public GLAccountData(final Long id, final String name, final Long parentId, final String glCode, final boolean disabled,
            final boolean manualEntriesAllowed, final EnumOptionData type, final EnumOptionData usage, final String description,
            final String nameDecorated, final CodeValueData tagId, final Long organizationRunningBalance) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.glCode = glCode;
        this.disabled = disabled;
        this.manualEntriesAllowed = manualEntriesAllowed;
        this.type = type;
        this.usage = usage;
        this.description = description;
        this.nameDecorated = nameDecorated;
        this.tagId = tagId;
        this.organizationRunningBalance = organizationRunningBalance;
        this.accountTypeOptions = null;
        this.usageOptions = null;
        this.assetHeaderAccountOptions = null;
        this.liabilityHeaderAccountOptions = null;
        this.equityHeaderAccountOptions = null;
        this.incomeHeaderAccountOptions = null;
        this.expenseHeaderAccountOptions = null;
        this.allowedAssetsTagOptions = null;
        this.allowedLiabilitiesTagOptions = null;
        this.allowedEquityTagOptions = null;
        this.allowedIncomeTagOptions = null;
        this.allowedExpensesTagOptions = null;
    }

    public GLAccountData(final GLAccountData accountData, final List<EnumOptionData> accountTypeOptions,
            final List<EnumOptionData> usageOptions, final List<GLAccountData> assetHeaderAccountOptions,
            final List<GLAccountData> liabilityHeaderAccountOptions, final List<GLAccountData> equityHeaderAccountOptions,
            final List<GLAccountData> incomeHeaderAccountOptions, final List<GLAccountData> expenseHeaderAccountOptions,
            final Collection<CodeValueData> allowedAssetsTagOptions, final Collection<CodeValueData> allowedLiabilitiesTagOptions,
            final Collection<CodeValueData> allowedEquityTagOptions, final Collection<CodeValueData> allowedIncomeTagOptions,
            final Collection<CodeValueData> allowedExpensesTagOptions) {
        this.id = accountData.id;
        this.name = accountData.name;
        this.parentId = accountData.parentId;
        this.glCode = accountData.glCode;
        this.disabled = accountData.disabled;
        this.manualEntriesAllowed = accountData.manualEntriesAllowed;
        this.type = accountData.type;
        this.usage = accountData.usage;
        this.description = accountData.description;
        this.nameDecorated = accountData.nameDecorated;
        this.tagId = accountData.tagId;
        this.organizationRunningBalance = accountData.organizationRunningBalance;
        this.accountTypeOptions = accountTypeOptions;
        this.usageOptions = usageOptions;
        this.assetHeaderAccountOptions = assetHeaderAccountOptions;
        this.liabilityHeaderAccountOptions = liabilityHeaderAccountOptions;
        this.equityHeaderAccountOptions = equityHeaderAccountOptions;
        this.incomeHeaderAccountOptions = incomeHeaderAccountOptions;
        this.expenseHeaderAccountOptions = expenseHeaderAccountOptions;
        this.allowedAssetsTagOptions = allowedAssetsTagOptions;
        this.allowedLiabilitiesTagOptions = allowedLiabilitiesTagOptions;
        this.allowedEquityTagOptions = allowedEquityTagOptions;
        this.allowedIncomeTagOptions = allowedIncomeTagOptions;
        this.allowedExpensesTagOptions = allowedExpensesTagOptions;
    }

    public static GLAccountData sensibleDefaultsForNewGLAccountCreation(final Integer glAccType) {
        final Long id = null;
        final String name = null;
        final Long parentId = null;
        final String glCode = null;
        final boolean disabled = false;
        final boolean manualEntriesAllowed = true;
        final EnumOptionData type;
        if (glAccType != null && glAccType >= GLAccountType.getMinValue() && glAccType <= GLAccountType.getMaxValue()) {
            type = AccountingEnumerations.gLAccountType(glAccType);
        } else {
            type = AccountingEnumerations.gLAccountType(GLAccountType.ASSET);
        }
        final EnumOptionData usage = AccountingEnumerations.gLAccountUsage(GLAccountUsage.DETAIL);
        final String description = null;
        final String nameDecorated = null;
        final CodeValueData tagId = null;
        final Long organizationRunningBalance = null;

        return new GLAccountData(id, name, parentId, glCode, disabled, manualEntriesAllowed, type, usage, description, nameDecorated,
                tagId, organizationRunningBalance);
    }

    public GLAccountData(final Long id, final String name, final String glCode) {
        this.id = id;
        this.name = name;
        this.parentId = null;
        this.glCode = glCode;
        this.disabled = null;
        this.manualEntriesAllowed = null;
        this.type = null;
        this.usage = null;
        this.description = null;
        this.nameDecorated = null;
        this.tagId = null;
        this.organizationRunningBalance = null;
        this.accountTypeOptions = null;
        this.usageOptions = null;
        this.assetHeaderAccountOptions = null;
        this.liabilityHeaderAccountOptions = null;
        this.equityHeaderAccountOptions = null;
        this.incomeHeaderAccountOptions = null;
        this.expenseHeaderAccountOptions = null;
        this.allowedAssetsTagOptions = null;
        this.allowedLiabilitiesTagOptions = null;
        this.allowedEquityTagOptions = null;
        this.allowedIncomeTagOptions = null;
        this.allowedExpensesTagOptions = null;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getGlCode() {
        return this.glCode;
    }

    public EnumOptionData getType() {
        return this.type;
    }

    public Integer getTypeId() {
        if (this.type != null) { return this.type.getId().intValue(); }
        return null;
    }

}