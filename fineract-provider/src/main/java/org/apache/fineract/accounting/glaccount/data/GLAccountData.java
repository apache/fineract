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
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.glaccount.domain.GLAccountUsage;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Immutable object representing a General Ledger Account
 *
 * Note: no getter/setters required as google-gson will produce json from fields of object.
 */

@Data
@RequiredArgsConstructor
@Accessors(chain = true)
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

        //@formatter:off
        return new GLAccountData()
                .setId(id);

        //@formatter:on
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
        return new GLAccountData()
                .setDisabled(disabled)
                .setManualEntriesAllowed(manualEntriesAllowed)
                .setType(type)
                .setUsage(usage);

        //@formatter:on

    }

}
