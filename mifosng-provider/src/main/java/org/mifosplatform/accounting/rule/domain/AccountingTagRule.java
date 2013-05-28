/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "acc_rule_tags")
public class AccountingTagRule extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "acc_rule_id", nullable = false)
    private AccountingRule accountingRule;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private final CodeValue tagId;

    @Column(name = "acc_type_enum", nullable = false)
    private final Integer accountType;

    public static AccountingTagRule create(final CodeValue tagId, final Integer accountType) {
        return new AccountingTagRule(tagId, accountType);
    }

    public AccountingTagRule(final CodeValue tagId, final Integer accountType) {
        this.tagId = tagId;
        this.accountType = accountType;
    }

    public void updateAccountingTagRule(final AccountingRule accountingRule) {
        this.accountingRule = accountingRule;
    }

}
