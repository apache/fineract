/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.portfolio.client.domain.ClientEnumerations;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.mifosplatform.portfolio.group.data.GroupTimelineData;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 */
public final class AllGroupTypesDataMapper implements RowMapper<GroupGeneralData> {

    private final String schemaSql;

    public AllGroupTypesDataMapper() {
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("g.id as id, g.external_id as externalId, g.display_name as name, ");
        sqlBuilder.append("g.office_id as officeId, o.name as officeName, ");
        sqlBuilder.append("g.parent_id as centerId, pg.display_name as centerName, ");
        sqlBuilder.append("g.staff_id as staffId, s.display_name as staffName, ");
        sqlBuilder.append("g.status_enum as statusEnum, g.activation_date as activationDate, ");
        sqlBuilder.append("g.closedon_date as closedOnDate, ");

        sqlBuilder.append("g.submittedon_date as submittedOnDate, ");
        sqlBuilder.append("sbu.username as submittedByUsername, ");
        sqlBuilder.append("sbu.firstname as submittedByFirstname, ");
        sqlBuilder.append("sbu.lastname as submittedByLastname, ");

        sqlBuilder.append("clu.username as closedByUsername, ");
        sqlBuilder.append("clu.firstname as closedByFirstname, ");
        sqlBuilder.append("clu.lastname as closedByLastname, ");

        sqlBuilder.append("acu.username as activatedByUsername, ");
        sqlBuilder.append("acu.firstname as activatedByFirstname, ");
        sqlBuilder.append("acu.lastname as activatedByLastname, ");

        sqlBuilder.append("g.hierarchy as hierarchy ");
        sqlBuilder.append("from m_group g ");
        sqlBuilder.append("join m_office o on o.id = g.office_id ");
        sqlBuilder.append("left join m_staff s on s.id = g.staff_id ");
        sqlBuilder.append("left join m_group pg on pg.id = g.parent_id ");
        sqlBuilder.append("left join m_appuser sbu on sbu.id = g.submittedon_userid ");
        sqlBuilder.append("left join m_appuser acu on acu.id = g.activatedon_userid ");
        sqlBuilder.append("left join m_appuser clu on clu.id = g.closedon_userid ");

        this.schemaSql = sqlBuilder.toString();
    }

    public String schema() {
        return this.schemaSql;
    }

    @Override
    public GroupGeneralData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

        final Long id = rs.getLong("id");
        final String name = rs.getString("name");
        final String externalId = rs.getString("externalId");

        final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
        final EnumOptionData status = ClientEnumerations.status(statusEnum);
        final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");

        final Long officeId = JdbcSupport.getLong(rs, "officeId");
        final String officeName = rs.getString("officeName");
        final Long centerId = JdbcSupport.getLong(rs, "centerId");
        final String centerName = rs.getString("centerName");
        final Long staffId = JdbcSupport.getLong(rs, "staffId");
        final String staffName = rs.getString("staffName");
        final String hierarchy = rs.getString("hierarchy");

        final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
        final String closedByUsername = rs.getString("closedByUsername");
        final String closedByFirstname = rs.getString("closedByFirstname");
        final String closedByLastname = rs.getString("closedByLastname");

        final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
        final String submittedByUsername = rs.getString("submittedByUsername");
        final String submittedByFirstname = rs.getString("submittedByFirstname");
        final String submittedByLastname = rs.getString("submittedByLastname");

        final String activatedByUsername = rs.getString("activatedByUsername");
        final String activatedByFirstname = rs.getString("activatedByFirstname");
        final String activatedByLastname = rs.getString("activatedByLastname");

        final GroupTimelineData timeline = new GroupTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname,
                submittedByLastname, activationDate, activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate,
                closedByUsername, closedByFirstname, closedByLastname);

        return GroupGeneralData.instance(id, name, externalId, status, activationDate, officeId, officeName, centerId, centerName, staffId,
                staffName, hierarchy, timeline);
    }
}
