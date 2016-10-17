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
package org.apache.fineract.accounting.rule.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "acc_rule_tags", uniqueConstraints = { @UniqueConstraint(columnNames = { "acc_rule_id", "tag_id", "acc_type_enum" }, name = "UNIQUE_ACCOUNT_RULE_TAGS") })
public class AccountingTagRule extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "acc_rule_id", nullable = false)
    private AccountingRule accountingRule;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private CodeValue tagId;

    @Column(name = "acc_type_enum", nullable = false)
    private Integer accountType;

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

    public AccountingTagRule() {
        // TODO Auto-generated constructor stub
    }

    public Integer getAccountType() {
        return this.accountType;
    }

    public boolean isDebitAccount() {
        return JournalEntryType.fromInt(this.accountType).isDebitType();
    }

    public boolean isCreditAccount() {
        return JournalEntryType.fromInt(this.accountType).isCreditType();
    }

    public Long getTagId() {
        return this.tagId.getId();
    }

}
