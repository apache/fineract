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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Oject representing a General Ledger Account
 */
//@formatter:off
@Data
@NoArgsConstructor
// @AllArgsConstructor // NOTE: this would be the "replacement" for all those handwritten constructors; and again: only "public" constructors, and technically we don't need those dreaded all args constructors anymore, fluent setters preferred... at a later stage we'll remove all those static helper functions too, they are not really helping
// @RequiredArgsConstructor // NOTE: it's either @NoArgsConstructor or @RequiredArgsConstructor; it is preferred to not have "final" class attributes
@Accessors(chain = true)
//@formatter:on
public class GLAccountData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Long parentId;
    private String glCode;
    private Boolean disabled;
    private Boolean manualEntriesAllowed;
    private EnumOptionData type;
    private EnumOptionData usage;
    private String description;
    private String nameDecorated;
    private CodeValueData tagId;
    private Long organizationRunningBalance;
    // templates
    private List<EnumOptionData> accountTypeOptions;
    private List<EnumOptionData> usageOptions;
    private List<GLAccountData> assetHeaderAccountOptions;
    private List<GLAccountData> liabilityHeaderAccountOptions;
    private List<GLAccountData> equityHeaderAccountOptions;
    private List<GLAccountData> incomeHeaderAccountOptions;
    private List<GLAccountData> expenseHeaderAccountOptions;
    private Collection<CodeValueData> allowedAssetsTagOptions;
    private Collection<CodeValueData> allowedLiabilitiesTagOptions;
    private Collection<CodeValueData> allowedEquityTagOptions;
    private Collection<CodeValueData> allowedIncomeTagOptions;
    private Collection<CodeValueData> allowedExpensesTagOptions;
    // import fields
    private transient Integer rowIndex;

    public static GLAccountData importInstance(String name, Long parentId, String glCode, Boolean manualEntriesAllowed, EnumOptionData type,
            EnumOptionData usage, String description, CodeValueData tagId, Integer rowIndex) {
        //@formatter:off

        // NOTE: very hard to figure out if you have passed the constructor arguments in the right order
        // return new GLAccountData(name, parentId, glCode, manualEntriesAllowed, type, usage, description, tagId, rowIndex);

        // ... vs. using fluent style setters... immediately clear to everyone what's going on
        return new GLAccountData()
            .setName(name)
            .setParentId(parentId)
            .setGlCode(glCode)
            .setManualEntriesAllowed(manualEntriesAllowed)
            .setType(type)
            .setUsage(usage)
            .setDescription(description)
            .setTagId(tagId)
            .setRowIndex(rowIndex);

        //@formatter:on
    }

    public static GLAccountData createFrom(final Long id) {
        return new GLAccountData().setId(id);
    }

    public static GLAccountData sensibleDefaultsForNewGLAccountCreation(final Integer glAccType) {
        final boolean disabled = false;
        final boolean manualEntriesAllowed = true;
        final EnumOptionData type;
        if (glAccType != null && glAccType >= GLAccountType.getMinValue() && glAccType <= GLAccountType.getMaxValue()) {
            type = AccountingEnumerations.gLAccountType(glAccType);
        } else {
            type = AccountingEnumerations.gLAccountType(GLAccountType.ASSET);
        }
        final EnumOptionData usage = AccountingEnumerations.gLAccountUsage(GLAccountUsage.DETAIL);

        //@formatter:off

        // NOTE: ... again, hard to read if constructor arguments are properly passed
        // return new GLAccountData(id, name, parentId, glCode, disabled, manualEntriesAllowed, type, usage, description, nameDecorated, tagId, organizationRunningBalance);

        // ... vs. easy to read fluent setters... and you can save those unnecessary null values variables... again, empty code that is only distracting
        return new GLAccountData()
            .setDisabled(disabled)
            .setManualEntriesAllowed(manualEntriesAllowed)
            .setType(type)
            .setUsage(usage);

        //@formatter:on
    }
}
