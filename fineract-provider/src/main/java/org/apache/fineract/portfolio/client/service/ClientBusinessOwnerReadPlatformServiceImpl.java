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
            return "fmb.id AS id, fmb.client_id AS clientId, fmb.firstname AS firstName, fmb.title_id AS title_id,fmb.middlename AS middleName, tc.code_value as title_name,"
                    + "fmb.lastname AS lastName, fmb.ownership AS ownership, fmb.email AS email, fmb.mobile_number as mobileNumber,"
                    + "fmb.business_owner_number as businessOwnerNumber, fmb.city_id as city_id, cc.code_value as city_name, "
                    + "fmb.address1 as address1, fmb.address2 as address2, fmb.address3 as address3, fmb.type_id as type_id, type_code.code_value as type_name, "
                    + "fmb.postal_code as postalCode, fmb.landmark as landmark, fmb.bvn as bvn, fmb.nin as nin,  fmb.street as street, "
                    + "fmb.state_province_id as state_province_id, cv.code_value as state_name, fmb.country_id as country_id,c.code_value as country_name,"
                    + "fmb.created_by as created_by,fmb.created_on as created_on,fmb.updated_by as updated_by,"
                    + "fmb.updated_on as updated_on," + " fmb.is_active as isActive, fmb.image_id as imageId "
                    + " FROM m_business_owners fmb" + " left join m_code_value cv on fmb.state_province_id=cv.id"
                    + " left join m_code_value cc on fmb.city_id=cc.id" + " left join  m_code_value type_code on fmb.type_id=type_code.id"
                    + " left join m_code_value tc on fmb.title_id=tc.id" + " left join  m_code_value c on fmb.country_id=c.id";
        }

        @Override
        public ClientBusinessOwnerData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final long id = rs.getLong("id");
            final long clientId = rs.getLong("clientId");
            final String firstName = rs.getString("firstName");
            final long titleId = rs.getLong("title_id");
            final String titleName = rs.getString("title_name");
            final String lastName = rs.getString("lastName");
            final BigDecimal ownership = rs.getBigDecimal("ownership");
            final String email = rs.getString("email");
            final String mobileNumber = rs.getString("mobileNumber");
            final String businessOwnerNumber = rs.getString("businessOwnerNumber");
            final String landmark = rs.getString("landmark");
            final String middleName = rs.getString("middleName");

            final long stateProvinceId = rs.getLong("state_province_id");
            final String stateName = rs.getString("state_name");
            final long countryId = rs.getLong("country_id");
            final String countryName = rs.getString("country_name");
            final long typeId = rs.getLong("type_id");
            final String typeName = rs.getString("type_name");
            final long cityId = rs.getLong("city_id");
            final String cityName = rs.getString("city_name");
            final LocalDate dateOfBirth = JdbcSupport.getLocalDate(rs, "dateOfBirth");
            final String createdBy = rs.getString("created_by");
            final Date createdOn = rs.getDate("created_on");
            final String updatedBy = rs.getString("updated_by");
            final Date updatedOn = rs.getDate("updated_on");
            final String street = rs.getString("street");
            final String bvn = rs.getString("bvn");
            final String nin = rs.getString("nin");
            final String address1 = rs.getString("address1");
            final String address2 = rs.getString("address2");
            final String address3 = rs.getString("address3");
            final String postalCode = rs.getString("postalCode");
            final Long imageId = JdbcSupport.getLong(rs, "imageId");
            final boolean isActive = rs.getBoolean("isActive");

            return ClientBusinessOwnerData.instance(id, clientId, firstName, middleName, titleName, titleId, lastName, ownership, typeId,
                    typeName, cityId, cityName, mobileNumber, businessOwnerNumber, stateProvinceId, stateName, countryId, countryName,
                    dateOfBirth, createdBy, createdOn, updatedBy, updatedOn, email, street, address1, address2, address3, postalCode, bvn,
                    nin, landmark, null, null, null, null, null, imageId, isActive);

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

        final List<CodeValueData> cityOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("CITY"));

        final List<CodeValueData> titleOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("TITLE"));

        final List<CodeValueData> typeOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("TYPE"));

        return ClientBusinessOwnerData.template(countryoptions, StateOptions, cityOptions, titleOptions, typeOptions);
    }

}
