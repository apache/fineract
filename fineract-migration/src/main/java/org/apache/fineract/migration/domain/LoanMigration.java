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
package org.apache.fineract.migration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;

@Entity
@Getter
@Setter
@Table(name = "m_loan_migration_details")
public class LoanMigration extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "migration_start_datetime", nullable = false)
    private OffsetDateTime migrationStartDateTime;

    @Column(name = "migration_end_datetime")
    private OffsetDateTime migrationEndDateTime;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    public static LoanMigration newInstance(Loan loan, OffsetDateTime startDate, String status) {
        LoanMigration migration = new LoanMigration();
        migration.loan = loan;
        migration.migrationStartDateTime = startDate;
        migration.status = status;
        return migration;
    }
}
