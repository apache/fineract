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
package org.apache.fineract.accounting.closure.service;

import static org.apache.fineract.query.QAccGlClosure.accGlClosure;
import static org.apache.fineract.query.QMOffice.mOffice;

import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.closure.data.GLClosureData;
import org.apache.fineract.accounting.closure.domain.GLClosureProjection;
import org.apache.fineract.accounting.closure.exception.GLClosureNotFoundException;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.query.QMAppuser;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GLClosureReadPlatformServiceImpl implements GLClosureReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final SQLQueryFactory queryFactory;

    private static final class GLClosureMapper implements RowMapper<GLClosureData> {

        public String schema() {
            return " glClosure.id as id, glClosure.office_id as officeId,office.name as officeName ,glClosure.closing_date as closingDate,"
                    + " glClosure.is_deleted as isDeleted, creatingUser.id as creatingUserId,creatingUser.username as creatingUserName,"
                    + " updatingUser.id as updatingUserId,updatingUser.username as updatingUserName, glClosure.created_date as createdDate,"
                    + " glClosure.lastmodified_date as updatedDate, glClosure.comments as comments "
                    + " from acc_gl_closure as glClosure, m_appuser as creatingUser, m_appuser as updatingUser,m_office as office"
                    + " where glClosure.createdby_id=creatingUser.id and "
                    + " glClosure.lastmodifiedby_id=updatingUser.id and glClosure.office_id=office.id";
        }

        @Override
        public GLClosureData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final LocalDate closingDate = JdbcSupport.getLocalDate(rs, "closingDate");
            final Boolean deleted = rs.getBoolean("isDeleted");
            final LocalDate createdDate = JdbcSupport.getLocalDate(rs, "createdDate");
            final LocalDate lastUpdatedDate = JdbcSupport.getLocalDate(rs, "updatedDate");
            final Long creatingByUserId = rs.getLong("creatingUserId");
            final String createdByUserName = rs.getString("creatingUserName");
            final Long lastUpdatedByUserId = rs.getLong("updatingUserId");
            final String lastUpdatedByUserName = rs.getString("updatingUserName");
            final String comments = rs.getString("comments");

            return new GLClosureData(id, officeId, officeName, closingDate, deleted, createdDate, lastUpdatedDate, creatingByUserId,
                    createdByUserName, lastUpdatedByUserId, lastUpdatedByUserName, comments);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GLClosureData> retrieveAllGLClosures(final Long officeId) {
        final SQLQuery<GLClosureData> query = baseQuery().where(accGlClosure.isDeleted.isFalse());
        if (officeId != null && officeId != 0) {
            query.where(accGlClosure.officeId.eq(officeId));
        }
        return query.orderBy(accGlClosure.closingDate.desc()).fetch();
    }

    @Override
    public GLClosureData retrieveGLClosureById(final long glClosureId) {
        try {

            final GLClosureMapper rm = new GLClosureMapper();
            final String sql = "select " + rm.schema() + " and glClosure.id = ?";

            final GLClosureData glAccountData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { glClosureId }); // NOSONAR

            return glAccountData;
        } catch (final EmptyResultDataAccessException e) {
            throw new GLClosureNotFoundException(glClosureId, e);
        }
    }

    private SQLQuery<GLClosureData> baseQuery() {
        final QMAppuser creatingUser = new QMAppuser("creatingUser");
        final QMAppuser updatingUser = new QMAppuser("updatingUser");
        return queryFactory.select(new GLClosureProjection(creatingUser, updatingUser)).from(accGlClosure).join(mOffice)
                .on(mOffice.id.eq(accGlClosure.officeId)).join(creatingUser).on(creatingUser.id.eq(accGlClosure.createdbyId))
                .join(updatingUser).on(updatingUser.id.eq(accGlClosure.lastmodifiedbyId));
    }

}
