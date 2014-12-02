/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.accountnumberformat.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class AccountNumberFormatData implements Serializable {

    private final Long id;

    private final EnumOptionData accountType;
    private final EnumOptionData prefixType;

    // template options
    private List<EnumOptionData> accountTypeOptions;
    private Map<String, List<EnumOptionData>> prefixTypeOptions;

    public AccountNumberFormatData(final Long id, final EnumOptionData accountType, final EnumOptionData prefixType) {
        this(id, accountType, prefixType, null, null);
    }

    public AccountNumberFormatData(final List<EnumOptionData> accountTypeOptions, Map<String, List<EnumOptionData>> prefixTypeOptions) {
        this(null, null, null, accountTypeOptions, prefixTypeOptions);
    }

    public void templateOnTop(List<EnumOptionData> accountTypeOptions, Map<String, List<EnumOptionData>> prefixTypeOptions) {
        this.accountTypeOptions = accountTypeOptions;
        this.prefixTypeOptions = prefixTypeOptions;
    }

    private AccountNumberFormatData(final Long id, final EnumOptionData accountType, final EnumOptionData prefixType,
            final List<EnumOptionData> accountTypeOptions, Map<String, List<EnumOptionData>> prefixTypeOptions) {
        this.id = id;
        this.accountType = accountType;
        this.prefixType = prefixType;
        this.accountTypeOptions = accountTypeOptions;
        this.prefixTypeOptions = prefixTypeOptions;
    }

    public Long getId() {
        return this.id;
    }

    public EnumOptionData getAccountType() {
        return this.accountType;
    }

    public EnumOptionData getPrefixType() {
        return this.prefixType;
    }

    public List<EnumOptionData> getAccountTypeOptions() {
        return this.accountTypeOptions;
    }

    public Map<String, List<EnumOptionData>> getPrefixTypeOptions() {
        return this.prefixTypeOptions;
    }

}
