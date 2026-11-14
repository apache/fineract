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

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public abstract class AbstractLockingService implements LockingService {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final FineractProperties fineractProperties;
    private final SQLQueryFactory queryFactory;
    private final RelationalPath<?> lockTable;
    private final NumberPath<Long> loanId;

    protected AbstractLockingService(final JdbcTemplate jdbcTemplate, final DatabaseSpecificSQLGenerator sqlGenerator,
            final FineractProperties fineractProperties, final SQLQueryFactory queryFactory, final RelationalPath<?> lockTable,
            final NumberPath<Long> loanId) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlGenerator = sqlGenerator;
        this.fineractProperties = fineractProperties;
        this.queryFactory = queryFactory;
        this.lockTable = lockTable;
        this.loanId = loanId;
    }

    protected abstract String getTableName();

    protected abstract String getBatchLoanLockInsert();

    protected abstract String getBatchLoanLockUpgrade();

    @Override
    public void upgradeLock(List<Long> accountsToLock, LockOwner lockOwner) {
        jdbcTemplate.batchUpdate(getBatchLoanLockUpgrade(), accountsToLock, getInClauseParameterSizeLimit(), (ps, id) -> {
            ps.setString(1, lockOwner.name());
            ps.setObject(2, DateUtils.getAuditOffsetDateTime());
            ps.setLong(3, id);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findLockIdsByLoanIdIn(final List<Long> loanIds) {
        if (loanIds.isEmpty()) {
            return Collections.emptyList();
        }
        final BooleanExpression predicate = switch (sqlGenerator.getDialect()) {
            case POSTGRESQL -> Expressions.booleanTemplate("{0} = ANY ({1})", loanId, Expressions.constant(loanIds.toArray(Long[]::new)));
            case MYSQL -> Expressions.booleanOperation(Ops.IN, loanId, Expressions.constant(loanIds));
        };
        return queryFactory.select(loanId).from(lockTable).where(predicate).fetch();
    }

    @Override
    public List<Long> findLockIdsByLoanIdInAndLockOwner(List<Long> loanIds, LockOwner lockOwner) {
        if (loanIds.isEmpty()) {
            return Collections.emptyList();
        }
        final String sql = "SELECT loan_id FROM " + getTableName() + " WHERE " + sqlGenerator.in("loan_id", loanIds)
                + " AND lock_owner = ?";
        return jdbcTemplate.queryForList(sql, Long.class, paramsWithLockOwner(loanIds, lockOwner));
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
        if (loanIds.isEmpty()) {
            return;
        }
        final String sql = "DELETE FROM " + getTableName() + " WHERE " + sqlGenerator.in("loan_id", loanIds) + " AND lock_owner = ?";
        jdbcTemplate.update(sql, paramsWithLockOwner(loanIds, lockOwner));
    }

    @Override
    public void updateLockError(Long loanId, LockOwner lockOwner, String error, String stacktrace) {
        String sql = "UPDATE " + getTableName() + " SET error = ?, stacktrace = ? WHERE loan_id = ? AND lock_owner = ?";
        int updated = jdbcTemplate.update(sql, error, stacktrace, loanId, lockOwner.name());
        if (updated == 0) {
            log.warn("No lock found to update error for loan id: {} with owner: {}", loanId, lockOwner);
        }
    }

    private Object[] paramsWithLockOwner(final List<Long> loanIds, final LockOwner lockOwner) {
        final List<Object> params = new ArrayList<>();
        Collections.addAll(params, sqlGenerator.inParametersFor(loanIds));
        params.add(lockOwner.name());
        return params.toArray();
    }

    private int getInClauseParameterSizeLimit() {
        return fineractProperties.getQuery().getInClauseParameterSizeLimit();
    }
}
