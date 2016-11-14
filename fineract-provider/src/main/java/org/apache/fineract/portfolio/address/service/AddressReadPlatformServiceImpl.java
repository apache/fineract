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
package org.apache.fineract.portfolio.address.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AddressReadPlatformServiceImpl implements AddressReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final CodeValueReadPlatformService readService;

	@Autowired
	public AddressReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
			final CodeValueReadPlatformService readService) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.readService = readService;
	}

	private static final class AddFieldsMapper implements RowMapper<AddressData> {
		public String schema() {
			return "addr.id as id,client.id as client_id,addr.street as street,addr.address_line_1 as address_line_1,addr.address_line_2 as address_line_2,"
					+ "addr.address_line_3 as address_line_3,addr.town_village as town_village, addr.city as city,addr.county_district as county_district,"
					+ "addr.state_province_id as state_province_id, addr.country_id as country_id,addr.postal_code as postal_code,addr.latitude as latitude,"
					+ "addr.longitude as longitude,addr.created_by as created_by,addr.created_on as created_on,addr.updated_by as updated_by,"
					+ "addr.updated_on as updated_on from m_address as addr,m_client client";
		}

		@Override
		public AddressData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			final long addressId = rs.getLong("id");

			final long clientId = rs.getLong("client_id");

			final String street = rs.getString("street");

			final String address_line_1 = rs.getString("address_line_1");

			final String address_line_2 = rs.getString("address_line_2");

			final String address_line_3 = rs.getString("address_line_3");

			final String town_village = rs.getString("town_village");

			final String city = rs.getString("city");

			final String county_district = rs.getString("county_district");

			final long state_province_id = rs.getLong("state_province_id");

			final long country_id = rs.getLong("country_id");

			final String postal_code = rs.getString("postal_code");

			final BigDecimal latitude = rs.getBigDecimal("latitude");

			final BigDecimal longitude = rs.getBigDecimal("longitude");

			final String created_by = rs.getString("created_by");

			final Date created_on = rs.getDate("created_on");

			final String updated_by = rs.getString("updated_by");

			final Date updated_on = rs.getDate("updated_on");

			return AddressData.instance1(addressId, street, address_line_1, address_line_2, address_line_3,
					town_village, city, county_district, state_province_id, country_id, postal_code, latitude,
					longitude, created_by, created_on, updated_by, updated_on);

		}
	}

	private static final class AddMapper implements RowMapper<AddressData> {
		public String schema() {
			return "cv2.code_value as addressType,ca.client_id as client_id,addr.id as id,ca.address_type_id as addresstyp,ca.is_active as is_active,addr.street as street,addr.address_line_1 as address_line_1,addr.address_line_2 as address_line_2,"
					+ "addr.address_line_3 as address_line_3,addr.town_village as town_village, addr.city as city,addr.county_district as county_district,"
					+ "addr.state_province_id as state_province_id,cv.code_value as state_name, addr.country_id as country_id,c.code_value as country_name,addr.postal_code as postal_code,addr.latitude as latitude,"
					+ "addr.longitude as longitude,addr.created_by as created_by,addr.created_on as created_on,addr.updated_by as updated_by,"
					+ "addr.updated_on as updated_on"
					+ " from m_address addr left join m_code_value cv on addr.state_province_id=cv.id"
					+ " left join  m_code_value c on addr.country_id=c.id"
					+ " join m_client_address ca on addr.id= ca.address_id"
					+ " join m_code_value cv2 on ca.address_type_id=cv2.id";

		}

		@Override
		public AddressData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			final String addressType = rs.getString("addressType");
			final long addressId = rs.getLong("id");

			final long client_id = rs.getLong("client_id");

			final String street = rs.getString("street");

			final long address_type_id = rs.getLong("addresstyp");

			final boolean is_active = rs.getBoolean("is_active");

			final String address_line_1 = rs.getString("address_line_1");

			final String address_line_2 = rs.getString("address_line_2");

			final String address_line_3 = rs.getString("address_line_3");

			final String town_village = rs.getString("town_village");

			final String city = rs.getString("city");

			final String county_district = rs.getString("county_district");

			final long state_province_id = rs.getLong("state_province_id");

			final long country_id = rs.getLong("country_id");

			final String country_name = rs.getString("country_name");

			final String state_name = rs.getString("state_name");

			final String postal_code = rs.getString("postal_code");

			final BigDecimal latitude = rs.getBigDecimal("latitude");

			final BigDecimal longitude = rs.getBigDecimal("longitude");

			final String created_by = rs.getString("created_by");

			final Date created_on = rs.getDate("created_on");

			final String updated_by = rs.getString("updated_by");

			final Date updated_on = rs.getDate("updated_on");

			return AddressData.instance(addressType, client_id, addressId, address_type_id, is_active, street,
					address_line_1, address_line_2, address_line_3, town_village, city, county_district,
					state_province_id, country_id, state_name, country_name, postal_code, latitude, longitude,
					created_by, created_on, updated_by, updated_on);

		}
	}

	@Override
	public Collection<AddressData> retrieveAddressFields(final long clientid) {
		this.context.authenticatedUser();

		final AddFieldsMapper rm = new AddFieldsMapper();
		final String sql = "select " + rm.schema() + " where client.id=?";

		return this.jdbcTemplate.query(sql, rm, new Object[] { clientid });
	}

	@Override
	public Collection<AddressData> retrieveAllClientAddress(final long clientid) {
		this.context.authenticatedUser();
		final AddMapper rm = new AddMapper();
		final String sql = "select " + rm.schema() + " and ca.client_id=?";
		return this.jdbcTemplate.query(sql, rm, new Object[] { clientid });
	}

	@Override
	public Collection<AddressData> retrieveAddressbyType(final long clientid, final long typeid) {
		this.context.authenticatedUser();

		final AddMapper rm = new AddMapper();
		final String sql = "select " + rm.schema() + " and ca.client_id=? and ca.address_type_id=?";

		return this.jdbcTemplate.query(sql, rm, new Object[] { clientid, typeid });
	}

	@Override
	public Collection<AddressData> retrieveAddressbyTypeAndStatus(final long clientid, final long typeid,
			final String status) {
		this.context.authenticatedUser();
		Boolean temp = false;
		temp = Boolean.parseBoolean(status);

		final AddMapper rm = new AddMapper();
		final String sql = "select " + rm.schema() + " and ca.client_id=? and ca.address_type_id=? and ca.is_active=?";

		return this.jdbcTemplate.query(sql, rm, new Object[] { clientid, typeid, temp });
	}

	@Override
	public Collection<AddressData> retrieveAddressbyStatus(final long clientid, final String status) {
		this.context.authenticatedUser();
		Boolean temp = false;
		temp = Boolean.parseBoolean(status);

		final AddMapper rm = new AddMapper();
		final String sql = "select " + rm.schema() + " and ca.client_id=? and ca.is_active=?";

		return this.jdbcTemplate.query(sql, rm, new Object[] { clientid, temp });
	}

	@Override
	public AddressData retrieveTemplate() {
		final List<CodeValueData> countryoptions = new ArrayList<>(
				this.readService.retrieveCodeValuesByCode("COUNTRY"));

		final List<CodeValueData> StateOptions = new ArrayList<>(this.readService.retrieveCodeValuesByCode("STATE"));

		final List<CodeValueData> addressTypeOptions = new ArrayList<>(
				this.readService.retrieveCodeValuesByCode("ADDRESS_TYPE"));

		return AddressData.template(countryoptions, StateOptions, addressTypeOptions);
	}
}
