package org.mifosplatform.accounting.rule.data;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class AccountingTagRuleData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long tagId;
    @SuppressWarnings("unused")
    private final EnumOptionData transactionType;

    public AccountingTagRuleData(final Long id, final Long tagId, final EnumOptionData transactionType) {
        this.id = id;
        this.tagId = tagId;
        this.transactionType = transactionType;
    }

}
