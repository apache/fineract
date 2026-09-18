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
package org.apache.fineract.portfolio.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.portfolio.account.data.StandingInstructionDuesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class StandingInstructionReadPlatformServiceImplTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 9, 18);

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private JdbcTemplate jdbcTemplate;
    private StandingInstructionReadPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
                POSTGRES.getPassword());
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("drop table if exists m_loan_repayment_schedule");
        jdbcTemplate.execute("drop table if exists m_loan");
        jdbcTemplate.execute("create table m_loan (id bigint primary key)");
        jdbcTemplate.execute("""
                create table m_loan_repayment_schedule (
                    loan_id bigint not null references m_loan(id),
                    duedate date not null,
                    completed_derived boolean not null,
                    principal_amount numeric(19, 6),
                    principal_completed_derived numeric(19, 6),
                    principal_writtenoff_derived numeric(19, 6),
                    interest_amount numeric(19, 6),
                    interest_completed_derived numeric(19, 6),
                    interest_writtenoff_derived numeric(19, 6),
                    interest_waived_derived numeric(19, 6),
                    penalty_charges_amount numeric(19, 6),
                    penalty_charges_completed_derived numeric(19, 6),
                    penalty_charges_writtenoff_derived numeric(19, 6),
                    penalty_charges_waived_derived numeric(19, 6),
                    fee_charges_amount numeric(19, 6),
                    fee_charges_completed_derived numeric(19, 6),
                    fee_charges_writtenoff_derived numeric(19, 6),
                    fee_charges_waived_derived numeric(19, 6)
                )
                """);

        DatabaseSpecificSQLGenerator sqlGenerator = mock(DatabaseSpecificSQLGenerator.class);
        when(sqlGenerator.currentBusinessDate()).thenReturn("date '" + BUSINESS_DATE + "'");
        service = new StandingInstructionReadPlatformServiceImpl(jdbcTemplate, null, null, null, null, null, sqlGenerator, null);
    }

    @Test
    void retrievesOutstandingLoanDuesUsingPostgreSQLBooleanPredicate() {
        long loanId = 42L;
        jdbcTemplate.update("insert into m_loan (id) values (?)", loanId);
        insertSchedule(loanId, BUSINESS_DATE.minusDays(1), false, "100.00", "20.00", "30.00", "5.00", "10.00", "2.00");
        insertSchedule(loanId, BUSINESS_DATE.minusDays(2), true, "500.00", "0.00", "0.00", "0.00", "0.00", "0.00");
        insertSchedule(loanId, BUSINESS_DATE.plusDays(1), false, "700.00", "0.00", "0.00", "0.00", "0.00", "0.00");

        StandingInstructionDuesData dues = service.retriveLoanDuesData(loanId);

        assertThat(dues.dueDate()).isEqualTo(BUSINESS_DATE.minusDays(1));
        assertThat(dues.totalDueAmount()).isEqualByComparingTo(new BigDecimal("113.00"));
    }

    private void insertSchedule(long loanId, LocalDate dueDate, boolean completed, String principalAmount, String principalPaid,
            String interestAmount, String interestPaid, String feeAmount, String feePaid) {
        jdbcTemplate.update("""
                insert into m_loan_repayment_schedule (
                    loan_id, duedate, completed_derived,
                    principal_amount, principal_completed_derived, principal_writtenoff_derived,
                    interest_amount, interest_completed_derived, interest_writtenoff_derived, interest_waived_derived,
                    penalty_charges_amount, penalty_charges_completed_derived, penalty_charges_writtenoff_derived,
                    penalty_charges_waived_derived, fee_charges_amount, fee_charges_completed_derived,
                    fee_charges_writtenoff_derived, fee_charges_waived_derived
                ) values (?, ?, ?, ?, ?, 0, ?, ?, 0, 0, 0, 0, 0, 0, ?, ?, 0, 0)
                """, loanId, dueDate, completed, new BigDecimal(principalAmount), new BigDecimal(principalPaid),
                new BigDecimal(interestAmount), new BigDecimal(interestPaid), new BigDecimal(feeAmount), new BigDecimal(feePaid));
    }
}
