/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.data;

import java.util.List;

import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.glaccount.domain.GLAccountUsage;
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
    private final boolean disabled;
    private final boolean manualEntriesAllowed;
    private final EnumOptionData type;
    private final EnumOptionData usage;
    private final String description;

    // templates
    final List<EnumOptionData> accountTypeOptions;
    final List<EnumOptionData> usageOptions;

    public GLAccountData(final Long id, final String name, final Long parentId, final String glCode, final boolean disabled,
            final boolean manualEntriesAllowed, final EnumOptionData type, final EnumOptionData usage, final String description) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.glCode = glCode;
        this.disabled = disabled;
        this.manualEntriesAllowed = manualEntriesAllowed;
        this.type = type;
        this.usage = usage;
        this.description = description;
        this.accountTypeOptions = null;
        this.usageOptions = null;
    }

    public GLAccountData(final GLAccountData accountData, final List<EnumOptionData> accountTypeOptions,
            final List<EnumOptionData> usageOptions) {
        this.id = accountData.id;
        this.name = accountData.name;
        this.parentId = accountData.parentId;
        this.glCode = accountData.glCode;
        this.disabled = accountData.disabled;
        this.manualEntriesAllowed = accountData.manualEntriesAllowed;
        this.type = accountData.type;
        this.usage = accountData.usage;
        this.description = accountData.description;
        this.accountTypeOptions = accountTypeOptions;
        this.usageOptions = usageOptions;
    }

    public static GLAccountData sensibleDefaultsForNewGLAccountCreation() {
        final Long id = null;
        final String name = null;
        final Long parentId = null;
        final String glCode = null;
        final boolean disabled = false;
        final boolean manualEntriesAllowed = true;
        final EnumOptionData type = AccountingEnumerations.gLAccountType(GLAccountType.ASSET);
        final EnumOptionData usage = AccountingEnumerations.gLAccountUsage(GLAccountUsage.DETAIL);
        final String description = null;
        return new GLAccountData(id, name, parentId, glCode, disabled, manualEntriesAllowed, type, usage, description);
    }
}