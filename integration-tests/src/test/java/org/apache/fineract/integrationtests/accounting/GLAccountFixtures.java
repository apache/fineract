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
package org.apache.fineract.integrationtests.accounting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.client.models.GLAccountData;

record GLAccountFixtures(Map<GLAccountType, List<Long>> byType, Set<Long> fixtureIds) {

    List<Long> expected(final GLAccountType type) {
        return byType.get(type);
    }

    private List<Long> idsIn(final List<GLAccountData> accounts) {
        assertNotNull(accounts, "Expected account options");
        return accounts.stream().map(GLAccountData::getId).filter(fixtureIds::contains).toList();
    }

    void assertIds(final GLAccountType type, final List<GLAccountData> accounts) {
        assertEquals(expected(type), idsIn(accounts), type.name());
    }

    void assertAllIds(final List<GLAccountData> accounts) {
        final List<Long> expected = byType.values().stream().flatMap(List::stream).toList();
        assertEquals(expected, idsIn(accounts));
    }

    void assertDropdown(final Map<String, List<GLAccountData>> accounts) {
        assertDropdown(accounts, GLAccountType.values());
    }

    void assertDropdown(final Map<String, List<GLAccountData>> accounts, final GLAccountType... types) {
        assertNotNull(accounts);
        for (final GLAccountType type : types) {
            final String key = type.name().toLowerCase(Locale.ROOT) + "AccountOptions";
            assertIds(type, accounts.get(key));
        }
    }

    void assertChargeOptions(final List<GLAccountData> assets, final List<GLAccountData> expenses,
            final Map<String, List<GLAccountData>> incomeOrLiability) {
        assertIds(GLAccountType.ASSET, assets);
        assertIds(GLAccountType.EXPENSE, expenses);
        assertDropdown(incomeOrLiability, GLAccountType.INCOME, GLAccountType.LIABILITY);
    }

    <T> void assertUnorderedIds(final GLAccountType type, final Set<T> accounts, final Function<T, Long> id) {
        assertNotNull(accounts, type.name());
        final List<Long> actual = accounts.stream().map(id).filter(fixtureIds::contains).sorted().toList();
        assertEquals(expected(type).stream().sorted().toList(), actual, type.name());
    }
}
