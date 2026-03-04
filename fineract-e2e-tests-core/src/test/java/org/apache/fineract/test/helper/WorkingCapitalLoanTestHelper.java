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
package org.apache.fineract.test.helper;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.test.data.LoanStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkingCapitalLoanTestHelper {

    private static final String TABLE_WC_LOAN = "m_wc_loan";
    private static final String TABLE_WC_LOAN_ACCOUNT_LOCKS = "m_wc_loan_account_locks";
    private static final int INITIAL_VERSION = 0;
    private static final long ADMIN_USER_ID = 1L;

    private final JdbcTemplate testJdbcTemplate;
    private final SimpleJdbcInsert wcLoanInsert;

    public WorkingCapitalLoanTestHelper(JdbcTemplate testJdbcTemplate) {
        this.testJdbcTemplate = testJdbcTemplate;
        this.wcLoanInsert = new SimpleJdbcInsert(testJdbcTemplate)//
                .withTableName(TABLE_WC_LOAN)//
                .usingGeneratedKeyColumns("id");
    }

    public Long insertActiveLoan() {
        return insertLoan(LoanStatus.ACTIVE.getValue(), null);
    }

    public String generateUniqueExternalId() {
        return "EXT-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public String generateAccountNumber() {
        return String.valueOf(System.currentTimeMillis());
    }

    public Long insertLoan(int loanStatusId, LocalDate lastClosedBusinessDate) {
        Timestamp now = Timestamp.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant());
        MapSqlParameterSource params = new MapSqlParameterSource()//
                .addValue("account_no", generateAccountNumber()).addValue("external_id", generateUniqueExternalId())
                .addValue("version", INITIAL_VERSION)//
                .addValue("created_by", ADMIN_USER_ID)//
                .addValue("last_modified_by", ADMIN_USER_ID)//
                .addValue("created_on_utc", now)//
                .addValue("last_modified_on_utc", now)//
                .addValue("loan_status_id", loanStatusId)//
                .addValue("last_closed_business_date", lastClosedBusinessDate);
        Number key = wcLoanInsert.executeAndReturnKey(params);
        return Objects.requireNonNull(key, "Generated key must not be null").longValue();
    }

    public LocalDate getLastClosedBusinessDate(Long loanId) {
        return testJdbcTemplate.queryForObject("SELECT last_closed_business_date FROM " + TABLE_WC_LOAN + " WHERE id = ?", LocalDate.class,
                loanId);
    }

    public int getVersion(Long loanId) {
        return testJdbcTemplate.queryForObject("SELECT version FROM " + TABLE_WC_LOAN + " WHERE id = ?", Integer.class, loanId);
    }

    public int countLocksByLoanId(Long loanId) {
        return testJdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + TABLE_WC_LOAN_ACCOUNT_LOCKS + " WHERE loan_id = ?", Integer.class,
                loanId);
    }

    public void deleteById(Long loanId) {
        testJdbcTemplate.update("DELETE FROM " + TABLE_WC_LOAN_ACCOUNT_LOCKS + " WHERE loan_id = ?", loanId);
        testJdbcTemplate.update("DELETE FROM " + TABLE_WC_LOAN + " WHERE id = ?", loanId);
    }
}
