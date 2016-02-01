/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.closure.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.accounting.closure.data.GLClosureData;
import org.mifosplatform.accounting.closure.exception.GLClosureNotFoundException;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class GLClosureReadPlatformServiceImpl implements GLClosureReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GLClosureReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

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
    public List<GLClosureData> retrieveAllGLClosures(final Long officeId) {
        final GLClosureMapper rm = new GLClosureMapper();

        String sql = "select " + rm.schema() + " and glClosure.is_deleted = 0";
        final Object[] objectArray = new Object[1];
        int arrayPos = 0;
        if (officeId != null && officeId != 0) {
            sql += " and glClosure.office_id = ?";
            objectArray[arrayPos] = officeId;
            arrayPos = arrayPos + 1;
        }

        sql = sql + " order by glClosure.closing_date desc";

        final Object[] finalObjectArray = Arrays.copyOf(objectArray, arrayPos);
        return this.jdbcTemplate.query(sql, rm, finalObjectArray);
    }

    @Override
    public GLClosureData retrieveGLClosureById(final long glClosureId) {
        try {

            final GLClosureMapper rm = new GLClosureMapper();
            final String sql = "select " + rm.schema() + " and glClosure.id = ?";

            final GLClosureData glAccountData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { glClosureId });

            return glAccountData;
        } catch (final EmptyResultDataAccessException e) {
            throw new GLClosureNotFoundException(glClosureId);
        }
    }

}
