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
package org.apache.fineract.accounting.glaccount.domain;

import static org.apache.fineract.query.QAccGlAccount.accGlAccount;
import static org.apache.fineract.query.QMCodeValue.mCodeValue;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.MappingProjection;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import java.util.Objects;
import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

public final class GLAccountProjection extends MappingProjection<GLAccountData> {

    private static final NumberExpression<Integer> NAME_PREFIX_LENGTH = accGlAccount.hierarchy.length()
            .subtract(Expressions.stringTemplate("replace({0}, '.', '')", accGlAccount.hierarchy).length()).subtract(1).multiply(4);
    private static final StringExpression NAME_PREFIX = Expressions.asString(".".repeat(40)).substring(0, NAME_PREFIX_LENGTH);
    private static final StringExpression NAME_DECORATED = Expressions.stringTemplate("concat({0}, {1})", NAME_PREFIX, accGlAccount.name);
    private static final Expression<?>[] ACCOUNT_COLUMNS = { accGlAccount.id, accGlAccount.name, accGlAccount.parentId, accGlAccount.glCode,
            accGlAccount.disabled, accGlAccount.manualJournalEntriesAllowed, accGlAccount.classificationEnum, accGlAccount.accountUsage,
            accGlAccount.description, NAME_DECORATED, mCodeValue.id, mCodeValue.codeValue };

    private final NumberExpression<Long> runningBalance;

    public GLAccountProjection(final NumberExpression<Long> runningBalance) {
        super(GLAccountData.class, ACCOUNT_COLUMNS, runningBalance == null ? new Expression<?>[0] : new Expression<?>[] { runningBalance });
        this.runningBalance = runningBalance;
    }

    @Override
    protected GLAccountData map(final Tuple row) {
        final int classificationId = Objects.requireNonNull(row.get(accGlAccount.classificationEnum),
                "GL account classification is required");
        final int usageId = Objects.requireNonNull(row.get(accGlAccount.accountUsage), "GL account usage is required");
        final String nameDecorated = row.get(NAME_DECORATED);
        // Preserve the API response: a null nameDecorated produces a null tag ID;
        // otherwise, an account without a tag has tag ID 0.
        final Long tagId = nameDecorated == null ? null : Objects.requireNonNullElse(row.get(mCodeValue.id), 0).longValue();
        // Preserve the API response: read the balance as Long, discarding the fractional part (e.g. 24.75 becomes 24).
        final Long balance = runningBalance == null ? null : Objects.requireNonNullElse(row.get(runningBalance), 0L);

        return new GLAccountData().setId(row.get(accGlAccount.id)).setName(row.get(accGlAccount.name))
                .setParentId(row.get(accGlAccount.parentId)).setGlCode(row.get(accGlAccount.glCode))
                .setDisabled(Boolean.TRUE.equals(row.get(accGlAccount.disabled)))
                .setManualEntriesAllowed(Boolean.TRUE.equals(row.get(accGlAccount.manualJournalEntriesAllowed)))
                .setType(AccountingEnumerations.gLAccountType(classificationId)).setUsage(AccountingEnumerations.gLAccountUsage(usageId))
                .setDescription(row.get(accGlAccount.description)).setNameDecorated(nameDecorated)
                .setTagId(CodeValueData.instance(tagId, row.get(mCodeValue.codeValue))).setOrganizationRunningBalance(balance);
    }
}
