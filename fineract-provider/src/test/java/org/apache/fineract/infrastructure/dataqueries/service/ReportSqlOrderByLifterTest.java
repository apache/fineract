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

// FINERACT-2622: a derived table's ORDER BY is not guaranteed to be honored by the outer query that
// GenericDataServiceImpl#wrapSQL builds around every report's SQL, so report results with an ORDER BY clause could
// come back in an arbitrary order. These tests fail on the unfixed wrap ("select x.* from (<sql>) x", ORDER BY left
// stranded inside the derived table) and pass once the top-level trailing ORDER BY is lifted onto the wrapper.
public class ReportSqlOrderByLifterTest {

    @Test
    public void liftsTopLevelTrailingOrderByOntoOuterQuery() {
        assertThat(ReportSqlOrderByLifter.wrap("SELECT account_no FROM m_loan ORDER BY account_no DESC"))
                .isEqualTo("select x.* from (SELECT account_no FROM m_loan ) x ORDER BY account_no DESC");
    }

    @Test
    public void leavesQueryWithoutOrderByUnchanged() {
        assertThat(ReportSqlOrderByLifter.wrap("SELECT account_no FROM m_loan"))
                .isEqualTo("select x.* from (SELECT account_no FROM m_loan) x");
    }

    @Test
    public void carriesTrailingLimitWithOrderBy() {
        assertThat(ReportSqlOrderByLifter.wrap("SELECT a FROM t ORDER BY a DESC LIMIT 10"))
                .isEqualTo("select x.* from (SELECT a FROM t ) x ORDER BY a DESC LIMIT 10");
    }

    @Test
    public void doesNotLiftOrderByInsideSubquery() {
        final String sql = "SELECT a FROM (SELECT a FROM t ORDER BY a) s";
        assertThat(ReportSqlOrderByLifter.wrap(sql)).isEqualTo("select x.* from (" + sql + ") x");
    }

    @Test
    public void doesNotLiftOrderByInsideWindowFunction() {
        final String sql = "SELECT ROW_NUMBER() OVER (ORDER BY a) rn FROM t";
        assertThat(ReportSqlOrderByLifter.wrap(sql)).isEqualTo("select x.* from (" + sql + ") x");
    }

    @Test
    public void doesNotLiftDotQualifiedOrderBy() {
        final String sql = "SELECT l.account_no FROM m_loan l ORDER BY l.account_no DESC";
        assertThat(ReportSqlOrderByLifter.wrap(sql)).isEqualTo("select x.* from (" + sql + ") x");
    }

    @Test
    public void ignoresOrderByInsideStringLiteral() {
        final String sql = "SELECT a FROM t WHERE note = 'order by x'";
        assertThat(ReportSqlOrderByLifter.wrap(sql)).isEqualTo("select x.* from (" + sql + ") x");
    }

    @Test
    public void ignoresOrderByInsideLineComment() {
        final String sql = "SELECT a FROM t -- order by legacy behavior\nWHERE a = 1";
        assertThat(ReportSqlOrderByLifter.wrap(sql)).isEqualTo("select x.* from (" + sql + ") x");
    }

    @Test
    public void ignoresOrderByInsideBlockComment() {
        final String sql = "SELECT a FROM t /* order by not real */ WHERE a = 1";
        assertThat(ReportSqlOrderByLifter.wrap(sql)).isEqualTo("select x.* from (" + sql + ") x");
    }
}
