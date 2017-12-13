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
package org.apache.fineract.accounting.glaccount.data;

import java.util.Collection;
import java.util.List;

import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

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

    //import fields
    private transient Integer rowIndex;

    public static GLAccountData importInstance(String name, Long parentId, String glCode, Boolean manualEntriesAllowed,
            EnumOptionData type, EnumOptionData usage, String description, CodeValueData tagId,
            Integer rowIndex){
        return new GLAccountData(name,parentId,glCode,manualEntriesAllowed,type,
                usage,description,tagId,rowIndex);
    }

    private GLAccountData(String name, Long parentId, String glCode, Boolean manualEntriesAllowed,
            EnumOptionData type, EnumOptionData usage, String description, CodeValueData tagId,
            Integer rowIndex) {

        this.name = name;
        this.parentId = parentId;
        this.glCode = glCode;
        this.manualEntriesAllowed = manualEntriesAllowed;
        this.type = type;
        this.usage = usage;
        this.description = description;
        this.tagId = tagId;
        this.rowIndex = rowIndex;
        this.id = null;
        this.disabled = null;
        this.nameDecorated = null;
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

    public Integer getRowIndex() {
        return rowIndex;
    }

    public CodeValueData getTagId() {
        return tagId;
    }

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