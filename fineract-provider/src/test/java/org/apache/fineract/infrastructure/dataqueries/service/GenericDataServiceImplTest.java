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

public class GenericDataServiceImplTest {

    private final GenericDataServiceImpl service = new GenericDataServiceImpl(null, null, null, null, null);

    @Test
    public void liftsTopLevelTrailingOrderByOntoOuterQuery() {
        assertThat(service.wrapSQL("SELECT account_no FROM m_loan ORDER BY account_no DESC"))
                .isEqualTo("select x.* from (SELECT account_no FROM m_loan ) x ORDER BY account_no DESC");
    }

    @Test
    public void leavesQueryWithoutOrderByUnchanged() {
        assertThat(service.wrapSQL("SELECT account_no FROM m_loan")).isEqualTo("select x.* from (SELECT account_no FROM m_loan) x");
    }

    @Test
    public void carriesTrailingLimitWithOrderBy() {
        assertThat(service.wrapSQL("SELECT a FROM t ORDER BY a DESC LIMIT 10"))
                .isEqualTo("select x.* from (SELECT a FROM t ) x ORDER BY a DESC LIMIT 10");
    }

    @Test
    public void doesNotLiftOrderByInsideSubquery() {
        final String sql = "SELECT a FROM (SELECT a FROM t ORDER BY a) s";
        assertThat(service.wrapSQL(sql)).isEqualTo("select x.* from (" + sql + ") x");
    }

    @Test
    public void doesNotLiftOrderByInsideWindowFunction() {
        final String sql = "SELECT ROW_NUMBER() OVER (ORDER BY a) rn FROM t";
        assertThat(service.wrapSQL(sql)).isEqualTo("select x.* from (" + sql + ") x");
    }

    @Test
    public void doesNotLiftDotQualifiedOrderBy() {
        final String sql = "SELECT l.account_no FROM m_loan l ORDER BY l.account_no DESC";
        assertThat(service.wrapSQL(sql)).isEqualTo("select x.* from (" + sql + ") x");
    }

    @Test
    public void ignoresOrderByInsideStringLiteral() {
        final String sql = "SELECT a FROM t WHERE note = 'order by x'";
        assertThat(service.wrapSQL(sql)).isEqualTo("select x.* from (" + sql + ") x");
    }
}
