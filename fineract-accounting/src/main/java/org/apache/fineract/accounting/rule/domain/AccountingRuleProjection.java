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

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.accounting.glaccount.data.GLAccountDataForLookup;
import org.apache.fineract.accounting.rule.data.AccountingRuleData;
import org.apache.fineract.query.QAccAccountingRule;
import org.apache.fineract.query.QAccGlAccount;
import org.apache.fineract.query.QMOffice;

public final class AccountingRuleProjection extends MappingProjection<AccountingRuleData> {

    private final QAccAccountingRule rule;
    private final QMOffice office;
    private final QAccGlAccount debitAccount;
    private final QAccGlAccount creditAccount;

    public AccountingRuleProjection(final QAccAccountingRule rule, final QMOffice office, final QAccGlAccount debitAccount,
            final QAccGlAccount creditAccount) {
        super(AccountingRuleData.class, rule.id, rule.name, rule.officeId, office.name, rule.description, rule.systemDefined,
                rule.allowMultipleDebits, rule.allowMultipleCredits, debitAccount.id, debitAccount.name, debitAccount.glCode,
                creditAccount.id, creditAccount.name, creditAccount.glCode);
        this.rule = rule;
        this.office = office;
        this.debitAccount = debitAccount;
        this.creditAccount = creditAccount;
    }

    @Override
    protected AccountingRuleData map(final Tuple row) {
        return new AccountingRuleData().setId(row.get(rule.id)).setName(row.get(rule.name)).setOfficeId(row.get(rule.officeId))
                .setOfficeName(row.get(office.name)).setDescription(row.get(rule.description))
                .setSystemDefined(Boolean.TRUE.equals(row.get(rule.systemDefined)))
                .setAllowMultipleDebitEntries(Boolean.TRUE.equals(row.get(rule.allowMultipleDebits)))
                .setAllowMultipleCreditEntries(Boolean.TRUE.equals(row.get(rule.allowMultipleCredits)))
                .setDebitAccounts(directAccount(row, debitAccount)).setCreditAccounts(directAccount(row, creditAccount));
    }

    private static List<GLAccountDataForLookup> directAccount(final Tuple row, final QAccGlAccount account) {
        final Long id = row.get(account.id);
        if (id == null) {
            return null;
        }
        final GLAccountDataForLookup lookup = new GLAccountDataForLookup().setId(id).setName(row.get(account.name))
                .setGlCode(row.get(account.glCode));
        return new ArrayList<>(List.of(lookup));
    }
}
