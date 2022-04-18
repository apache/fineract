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
package org.apache.fineract.portfolio.client.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientContactInformationData;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierStatus;
import org.apache.fineract.portfolio.client.exception.ClientContactInformationNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientContactInformationReadPlatformServiceImpl implements ClientContactInformationReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public ClientContactInformationReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<ClientContactInformationData> retrieveClientContactInformation(final Long clientId) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final ClientInformationMapper rm = new ClientInformationMapper();

        String sql = "select " + rm.schema();

        sql += " order by ci.id";

        return this.jdbcTemplate.query(sql, rm, new Object[] { clientId, hierarchySearchString }); // NOSONAR
    }

    @Override
    public ClientContactInformationData retrieveClientContactInformation(final Long clientId, final Long clientInformationId) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final String hierarchy = currentUser.getOffice().getHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            final ClientInformationMapper rm = new ClientInformationMapper();

            String sql = "select " + rm.schema();

            sql += " and ci.id = ?";

            final ClientContactInformationData clientIdentifierData = this.jdbcTemplate.queryForObject(sql, rm,
                    new Object[] { clientId, hierarchySearchString, clientInformationId }); // NOSONAR

            return clientIdentifierData;
        } catch (final EmptyResultDataAccessException e) {
            throw new ClientContactInformationNotFoundException(clientInformationId, e);
        }

    }

    private static final class ClientInformationMapper implements RowMapper<ClientContactInformationData> {

        ClientInformationMapper() {}

        public String schema() {
            return "ci.id as id, ci.client_id as clientId, ci.contact_type_id as contactTypeId, ci.status as status, ci.contact_key as contactKey,"
                    + " ci.current as current, cv.code_value as contactType from m_client_contact_information ci, m_client c, m_office o, m_code_value cv"
                    + " where ci.client_id=c.id and c.office_id=o.id" + " and ci.contact_type_id=cv.id"
                    + " and ci.client_id = ? and o.hierarchy like ? ";
        }

        @Override
        public ClientContactInformationData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long contactTypeId = JdbcSupport.getLong(rs, "contactTypeId");
            final String contactKey = rs.getString("contactKey");
            final String contactTypeName = rs.getString("contactType");
            final Boolean current = rs.getBoolean("current");
            final CodeValueData contactType = CodeValueData.instance(contactTypeId, contactTypeName);
            final String status = ClientIdentifierStatus.fromInt(rs.getInt("status")).getCode();
            return ClientContactInformationData.singleItem(id, clientId, contactType, contactKey, status, current);
        }
    }
}
