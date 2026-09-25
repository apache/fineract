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
package org.apache.fineract.infrastructure.dataqueries.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

// FINERACT-2622: reproduces the exact steps from the JIRA ticket. A custom report whose SQL ends in ORDER BY used to
// have that ordering silently dropped, because wrapSQL wrapped every report query as "select x.* from (<sql>) x"
// with no guarantee that a derived table's own ORDER BY is honored by the outer query. This test fails on the
// unfixed wrapSQL (ORDER BY left stranded inside the derived table, absent from the returned SQL's outer scope) and
// passes once the top-level trailing ORDER BY is lifted onto the wrapper.
public class GenericDataServiceImplTest {

    private final GenericDataServiceImpl service = new GenericDataServiceImpl(null, null, null, null, null);

    @Test
    public void wrapSqlPreservesOrderByFromJiraRepro() {
        // Steps to reproduce from FINERACT-2622: a report SQL script containing an ORDER BY clause.
        final String reportSql = "SELECT account_no FROM m_loan ORDER BY account_no DESC";

        final String wrapped = service.wrapSQL(reportSql);

        assertThat(wrapped).as("the ORDER BY must survive onto the outer wrapper query, not be left inside the "
                + "derived table where it is not guaranteed to be honored").endsWith("ORDER BY account_no DESC");
    }
}
