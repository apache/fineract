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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientBusinessOwnerData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientBusinessOwnerReadPlatformServiceImpl implements ClientBusinessOwnerReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public ClientBusinessOwnerReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate,
            final CodeValueReadPlatformService codeValueReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.codeValueReadPlatformService = codeValueReadPlatformService;

    }

    private static final class ClientBusinessOwnerMapper implements RowMapper<ClientBusinessOwnerData> {

        public String schema() {
            return "fmb.id AS id, fmb.client_id AS clientId, fmb.firstname AS firstName, fmb.title AS title,"
                    + "fmb.lastname AS lastName, fmb.ownership AS ownership, fmb.email AS email,fmb.mobile_number as mobileNumber,fmb.alter_mobile_number as alterMobileNumber,"
                    + "fmb.is_active as isActive, fmb.lga as lga, fmb.bvn as bvn, fmb.city as city, fmb.street as street, "
                    + "fmb.state_province_id as state_province_id,cv.code_value as state_name, fmb.country_id as country_id,c.code_value as country_name,"
                    + "fmb.created_by as created_by,fmb.created_on as created_on,fmb.updated_by as updated_by,"
                    + "fmb.updated_on as updated_on," + "fmb.date_of_birth AS dateOfBirth, fmb.username as userName, fmb.image_id as imageId "
                    + " FROM m_business_owners fmb" + " left join m_code_value cv on fmb.state_province_id=cv.id"
                    + " left join  m_code_value c on fmb.country_id=c.id";
        }

        @Override
        public ClientBusinessOwnerData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final long id = rs.getLong("id");
            final long clientId = rs.getLong("clientId");
            final String firstName = rs.getString("firstName");
            final String title = rs.getString("title");
            final String lastName = rs.getString("lastName");
            final BigDecimal ownership = rs.getBigDecimal("ownership");
            final String userName = rs.getString("userName");
            final String email = rs.getString("email");
            final String mobileNumber = rs.getString("mobileNumber");
            final String alterMobileNumber = rs.getString("alterMobileNumber");
            final boolean isActive = rs.getBoolean("isActive");
            final String lga = rs.getString("lga");
            final long stateProvinceId = rs.getLong("state_province_id");
            final String stateName = rs.getString("state_name");
            final long countryId = rs.getLong("country_id");
            final String countryName = rs.getString("country_name");
            final LocalDate dateOfBirth = JdbcSupport.getLocalDate(rs, "dateOfBirth");
            final String createdBy = rs.getString("created_by");
            final Date createdOn = rs.getDate("created_on");
            final String updatedBy = rs.getString("updated_by");
            final Date updatedOn = rs.getDate("updated_on");
            final String street = rs.getString("street");
            final String bvn = rs.getString("bvn");
            final String city = rs.getString("city");
            final Long imageId = JdbcSupport.getLong(rs, "imageId");

            return ClientBusinessOwnerData.instance(id, clientId, firstName, title, lastName, ownership, userName, mobileNumber,
                    alterMobileNumber, isActive, city, stateProvinceId, stateName, countryId, countryName, dateOfBirth, createdBy,
                    createdOn, updatedBy, updatedOn, email, street, bvn, lga, null, null, imageId);

        }
    }

    @Override
    public Collection<ClientBusinessOwnerData> getClientBusinessOwners(long clientId) {

        this.context.authenticatedUser();

        final ClientBusinessOwnerMapper rm = new ClientBusinessOwnerMapper();
        final String sql = "select " + rm.schema() + " where fmb.client_id=?";

        return this.jdbcTemplate.query(sql, rm, new Object[] { clientId }); // NOSONAR
    }

    @Override
    public ClientBusinessOwnerData getBusinessOwner(long id) {

        this.context.authenticatedUser();

        final ClientBusinessOwnerMapper rm = new ClientBusinessOwnerMapper();
        final String sql = "select " + rm.schema() + " where fmb.id=? ";

        return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { id }); // NOSONAR
    }

    @Override
    public ClientBusinessOwnerData retrieveTemplate() {

        final List<CodeValueData> countryoptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("COUNTRY"));

        final List<CodeValueData> StateOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("STATE"));

        return ClientBusinessOwnerData.template(countryoptions, StateOptions);
    }

}
