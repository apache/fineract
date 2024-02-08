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

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
@Slf4j
public class LoanLockingServiceImpl implements LoanLockingService {

    private static final String NORMAL_LOAN_INSERT = """
                INSERT INTO m_loan_account_locks (loan_id, version, lock_owner, lock_placed_on, lock_placed_on_cob_business_date)
                SELECT loan.id, ?, ?, ?, ? FROM m_loan loan
                    WHERE loan.id NOT IN (SELECT loan_id FROM m_loan_account_locks)
                    AND loan.id BETWEEN ? AND ?
                    AND loan.loan_status_id IN (100,200,300,303,304)
                    AND (? = loan.last_closed_business_date OR loan.last_closed_business_date IS NULL)
            """;
    private static final String CATCH_UP_LOAN_INSERT = """
                INSERT INTO m_loan_account_locks (loan_id, version, lock_owner, lock_placed_on, lock_placed_on_cob_business_date)
                SELECT loan.id, ?, ?, ?, ? FROM m_loan loan
                    WHERE loan.id NOT IN (SELECT loan_id FROM m_loan_account_locks)
                    AND loan.id BETWEEN ? AND ?
                    AND loan.loan_status_id IN (100,200,300,303,304)
                    AND (? = loan.last_closed_business_date)
            """;

    private static final String BATCH_LOAN_LOCK_INSERT = """
                INSERT INTO m_loan_account_locks (loan_id, version, lock_owner, lock_placed_on, lock_placed_on_cob_business_date) VALUES (?,?,?,?,?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final FineractProperties fineractProperties;
    private final LoanAccountLockRepository loanAccountLockRepository;

    @Override
    public void upgradeLock(List<Long> accountsToLock, LockOwner lockOwner) {
        jdbcTemplate.batchUpdate("""
                    UPDATE m_loan_account_locks SET version= version + 1, lock_owner = ?, lock_placed_on = ? WHERE loan_id = ?
                """, accountsToLock, getInClauseParameterSizeLimit(), (ps, id) -> {
            ps.setString(1, lockOwner.name());
            ps.setObject(2, DateUtils.getAuditOffsetDateTime());
            ps.setLong(3, id);
        });
    }

    @Override
    public List<LoanAccountLock> findAllByLoanIdIn(List<Long> loanIds) {
        return loanAccountLockRepository.findAllByLoanIdIn(loanIds);
    }

    @Override
    public LoanAccountLock findByLoanIdAndLockOwner(Long loanId, LockOwner lockOwner) {
        return loanAccountLockRepository.findByLoanIdAndLockOwner(loanId, lockOwner).orElseGet(() -> {
            log.warn("There is no lock for loan account with id: {}", loanId);
            return null;
        });
    }

    @Override
    public List<LoanAccountLock> findAllByLoanIdInAndLockOwner(List<Long> loanIds, LockOwner lockOwner) {
        return loanAccountLockRepository.findAllByLoanIdInAndLockOwner(loanIds, lockOwner);
    }

    @Override
    public void applyLock(List<Long> loanIds, LockOwner lockOwner) {
        LocalDate cobBusinessDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        jdbcTemplate.batchUpdate(BATCH_LOAN_LOCK_INSERT, loanIds, loanIds.size(), (PreparedStatement ps, Long loanId) -> {
            ps.setLong(1, loanId);
            ps.setLong(2, 1);
            ps.setString(3, lockOwner.name());
            ps.setObject(4, DateUtils.getAuditOffsetDateTime());
            ps.setObject(5, cobBusinessDate);
        });
    }

    @Override
    public void deleteByLoanIdInAndLockOwner(List<Long> loanIds, LockOwner lockOwner) {
        loanAccountLockRepository.deleteByLoanIdInAndLockOwner(loanIds, lockOwner);
    }

    private int getInClauseParameterSizeLimit() {
        return fineractProperties.getQuery().getInClauseParameterSizeLimit();
    }
}
