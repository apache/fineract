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

import static org.apache.fineract.query.QAccRuleTags.accRuleTags;
import static org.apache.fineract.query.QMCodeValue.mCodeValue;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import java.util.Objects;
import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.rule.data.AccountingTagRuleData;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

public final class AccountingTagRuleProjection extends MappingProjection<AccountingTagRuleData> {

    public AccountingTagRuleProjection() {
        super(AccountingTagRuleData.class, accRuleTags.id, accRuleTags.tagId, accRuleTags.accTypeEnum, mCodeValue.codeValue);
    }

    @Override
    protected AccountingTagRuleData map(final Tuple row) {
        final Integer tagId = Objects.requireNonNull(row.get(accRuleTags.tagId));
        final Integer transactionType = Objects.requireNonNull(row.get(accRuleTags.accTypeEnum));
        final CodeValueData tag = CodeValueData.instance(tagId.longValue(), row.get(mCodeValue.codeValue));
        return new AccountingTagRuleData().setId(row.get(accRuleTags.id)).setTag(tag)
                .setTransactionType(AccountingEnumerations.journalEntryType(transactionType));
    }
}
