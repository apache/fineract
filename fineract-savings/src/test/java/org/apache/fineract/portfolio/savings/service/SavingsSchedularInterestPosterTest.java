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
package org.apache.fineract.portfolio.savings.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class SavingsSchedularInterestPosterTest {

    @Mock
    private SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    @Mock
    private PlatformSecurityContext platformSecurityContext;

    private SavingsSchedularInterestPoster poster;

    @BeforeEach
    void setUp() {
        poster = new SavingsSchedularInterestPoster(savingsAccountWritePlatformService, jdbcTemplate, savingsAccountReadPlatformService,
                platformSecurityContext);
    }

    @Test
    void testFilterByAccountIdRemovesSkippedEntries() {
        List<Object[]> params = new ArrayList<>(List.of(new Object[] { "a" }, new Object[] { "b" }, new Object[] { "c" }));
        List<Long> accountIds = new ArrayList<>(List.of(1L, 2L, 3L));
        Set<Long> skippedIds = Set.of(2L);

        List<Object[]> result = poster.filterByAccountId(params, accountIds, skippedIds);

        assertEquals(2, result.size());
        assertEquals("a", result.get(0)[0]);
        assertEquals("c", result.get(1)[0]);
    }

    @Test
    void testFilterByAccountIdSkipsAllAccounts() {
        List<Object[]> params = new ArrayList<>(List.of(new Object[] { "a" }, new Object[] { "b" }));
        List<Long> accountIds = new ArrayList<>(List.of(1L, 2L));
        Set<Long> skippedIds = Set.of(1L, 2L);

        List<Object[]> result = poster.filterByAccountId(params, accountIds, skippedIds);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterByAccountIdSkipsNone() {
        List<Object[]> params = new ArrayList<>(List.of(new Object[] { "a" }, new Object[] { "b" }));
        List<Long> accountIds = new ArrayList<>(List.of(1L, 2L));
        Set<Long> skippedIds = new HashSet<>();

        List<Object[]> result = poster.filterByAccountId(params, accountIds, skippedIds);

        assertEquals(2, result.size());
    }

    @Test
    void testFilterByAccountIdHandlesEmptyLists() {
        List<Object[]> params = new ArrayList<>();
        List<Long> accountIds = new ArrayList<>();
        Set<Long> skippedIds = Set.of(1L);

        List<Object[]> result = poster.filterByAccountId(params, accountIds, skippedIds);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterRefNosRemovesSkippedEntries() {
        List<String> refNos = new ArrayList<>(List.of("ref1", "ref2", "ref3"));
        List<Long> accountIds = new ArrayList<>(List.of(1L, 2L, 3L));
        Set<Long> skippedIds = Set.of(2L);

        List<String> result = poster.filterRefNos(refNos, accountIds, skippedIds);

        assertEquals(2, result.size());
        assertEquals("ref1", result.get(0));
        assertEquals("ref3", result.get(1));
    }

    @Test
    void testFilterRefNosSkipsAllAccounts() {
        List<String> refNos = new ArrayList<>(List.of("ref1", "ref2"));
        List<Long> accountIds = new ArrayList<>(List.of(1L, 2L));
        Set<Long> skippedIds = Set.of(1L, 2L);

        List<String> result = poster.filterRefNos(refNos, accountIds, skippedIds);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterMethodsMaintainParallelListAlignment() {
        List<Object[]> params = new ArrayList<>(
                List.of(new Object[] { "p1" }, new Object[] { "p2" }, new Object[] { "p3" }, new Object[] { "p4" }));
        List<String> refNos = new ArrayList<>(List.of("ref1", "ref2", "ref3", "ref4"));
        List<Long> accountIds = new ArrayList<>(List.of(10L, 20L, 10L, 30L));
        Set<Long> skippedIds = Set.of(20L);

        List<Object[]> filteredParams = poster.filterByAccountId(params, accountIds, skippedIds);
        List<String> filteredRefNos = poster.filterRefNos(refNos, accountIds, skippedIds);

        assertEquals(3, filteredParams.size());
        assertEquals(3, filteredRefNos.size());

        assertEquals("p1", filteredParams.get(0)[0]);
        assertEquals("ref1", filteredRefNos.get(0));

        assertEquals("p3", filteredParams.get(1)[0]);
        assertEquals("ref3", filteredRefNos.get(1));

        assertEquals("p4", filteredParams.get(2)[0]);
        assertEquals("ref4", filteredRefNos.get(2));
    }
}
