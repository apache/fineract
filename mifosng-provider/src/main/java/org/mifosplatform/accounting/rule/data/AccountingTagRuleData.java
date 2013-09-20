/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.data;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class AccountingTagRuleData {

    @SuppressWarnings("unused")
    private final Long id;
    private final CodeValueData tag;
    @SuppressWarnings("unused")
    private final EnumOptionData transactionType;

    public AccountingTagRuleData(final Long id, final CodeValueData tag, final EnumOptionData transactionType) {
        this.id = id;
        this.tag = tag;
        this.transactionType = transactionType;
    }

    public CodeValueData getTag() {
        return this.tag;
    }

}
