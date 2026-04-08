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
package org.apache.fineract.cob.loan;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.AbstractLockingService;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
public class LoanLockingServiceImpl extends AbstractLockingService<LoanAccountLock> {

    private static final String BATCH_LOAN_LOCK_INSERT = """
                INSERT INTO m_loan_account_locks (loan_id, version, lock_owner, lock_placed_on, lock_placed_on_cob_business_date) VALUES (?,?,?,?,?)
            """;

    private static final String BATCH_LOAN_LOCK_UPGRADE = """
                UPDATE m_loan_account_locks SET version= version + 1, lock_owner = ?, lock_placed_on = ? WHERE loan_id = ?
            """;

    public LoanLockingServiceImpl(JdbcTemplate jdbcTemplate, FineractProperties fineractProperties,
            LoanAccountLockRepository loanAccountLockRepository) {
        super(jdbcTemplate, fineractProperties, loanAccountLockRepository);
    }

    @Override
    protected String getBatchLoanLockUpgrade() {
        return BATCH_LOAN_LOCK_UPGRADE;
    }

    @Override
    protected String getBatchLoanLockInsert() {
        return BATCH_LOAN_LOCK_INSERT;
    }
}
