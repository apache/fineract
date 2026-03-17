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
package org.apache.fineract.cob.domain;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractLockingService<T extends AccountLock> implements LockingService<T> {

    private final JdbcTemplate jdbcTemplate;
    private final FineractProperties fineractProperties;
    private final AccountLockRepository<T> loanAccountLockRepository;

    protected abstract String getBatchLoanLockUpgrade();

    protected abstract String getBatchLoanLockInsert();

    @Override
    public void upgradeLock(List<Long> accountsToLock, LockOwner lockOwner) {
        jdbcTemplate.batchUpdate(getBatchLoanLockUpgrade(), accountsToLock, getInClauseParameterSizeLimit(), (ps, id) -> {
            ps.setString(1, lockOwner.name());
            ps.setObject(2, DateUtils.getAuditOffsetDateTime());
            ps.setLong(3, id);
        });
    }

    @Override
    public List<T> findAllByLoanIdIn(List<Long> loanIds) {
        return loanAccountLockRepository.findAllByLoanIdIn(loanIds);
    }

    @Override
    public T findByLoanIdAndLockOwner(Long loanId, LockOwner lockOwner) {
        return loanAccountLockRepository.findByLoanIdAndLockOwner(loanId, lockOwner).orElseGet(() -> {
            log.warn("There is no lock for loan account with id: {}", loanId);
            return null;
        });
    }

    @Override
    public List<T> findAllByLoanIdInAndLockOwner(List<Long> loanIds, LockOwner lockOwner) {
        return loanAccountLockRepository.findAllByLoanIdInAndLockOwner(loanIds, lockOwner);
    }

    @Override
    public void applyLock(List<Long> loanIds, LockOwner lockOwner) {
        LocalDate cobBusinessDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        jdbcTemplate.batchUpdate(getBatchLoanLockInsert(), loanIds, loanIds.size(), (PreparedStatement ps, Long loanId) -> {
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
