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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.client.data.ClientFamilyMembersData;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ClientFamilyMembersReadPlatformServiceImpl implements ClientFamilyMembersReadPlatformService
{
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final CodeValueReadPlatformService codeValueReadPlatformService;
	

	@Autowired
	public ClientFamilyMembersReadPlatformServiceImpl(final PlatformSecurityContext context,
			final RoutingDataSource dataSource,final CodeValueReadPlatformService codeValueReadPlatformService) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.codeValueReadPlatformService=codeValueReadPlatformService;
		
	}

	private static final class ClientFamilyMembersMapper implements RowMapper<ClientFamilyMembersData> {
		public String schema() {
			return "fmb.id AS id, fmb.client_id AS clientId, fmb.firstname AS firstName, fmb.middlename AS middleName,"
					+"fmb.lastname AS lastName,fmb.qualification AS qualification,fmb.mobile_number as mobileNumber,fmb.age as age,fmb.is_dependent as isDependent,cv.code_value AS relationship,fmb.relationship_cv_id AS relationshipId,"
					+"c.code_value AS maritalStatus,fmb.marital_status_cv_id AS maritalStatusId,"
					+"c1.code_value AS gender, fmb.gender_cv_id AS genderId, fmb.date_of_birth AS dateOfBirth, c2.code_value AS profession, fmb.profession_cv_id AS professionId"
					+" FROM m_family_members fmb"
					+" LEFT JOIN m_code_value cv ON fmb.relationship_cv_id=cv.id"
					+" LEFT JOIN m_code_value c ON fmb.marital_status_cv_id=c.id"
					+" LEFT JOIN m_code_value c1 ON fmb.gender_cv_id=c1.id"
					+" LEFT JOIN m_code_value c2 ON fmb.profession_cv_id=c2.id";
		}

		@Override
		public ClientFamilyMembersData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final long id = rs.getLong("id");
			final long clientId = rs.getLong("clientId");
			final String firstName=rs.getString("firstName");
			final String middleName=rs.getString("middleName");
			final String lastName=rs.getString("lastName");
			final String qualification=rs.getString("qualification");
			final String mobileNumber=rs.getString("mobileNumber");
			final long age=rs.getLong("age");
			final boolean isDependent=rs.getBoolean("isDependent");
			final String relationship = rs.getString("relationship");
			final long relationshipId = rs.getLong("relationshipId");
			final String maritalStatus = rs.getString("maritalStatus");
			final long maritalStatusId = rs.getLong("maritalStatusId");
			final String gender = rs.getString("gender");
			final long genderId = rs.getLong("genderId");
			final LocalDate dateOfBirth = JdbcSupport.getLocalDate(rs, "dateOfBirth");
			final String profession = rs.getString("profession");
			final long professionId = rs.getLong("professionId");
					
			return ClientFamilyMembersData.instance(id, clientId, firstName, middleName, lastName,
					qualification,mobileNumber,age,isDependent,relationship,relationshipId,maritalStatus,maritalStatusId,gender,genderId,dateOfBirth,profession,professionId);
			
			

		}
	}
	
	
	@Override
	public Collection<ClientFamilyMembersData> getClientFamilyMembers(long clientId) {

		this.context.authenticatedUser();

		final ClientFamilyMembersMapper rm = new ClientFamilyMembersMapper();
		final String sql = "select " + rm.schema() + " where fmb.client_id=?";

		return this.jdbcTemplate.query(sql, rm, new Object[] { clientId });
	}
	
	
	@Override
	public ClientFamilyMembersData getClientFamilyMember(long id) {

		this.context.authenticatedUser();

		final ClientFamilyMembersMapper rm = new ClientFamilyMembersMapper();
		final String sql = "select " + rm.schema() + " where fmb.id=? ";

		return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { id });
	}
	
	@Override
	public ClientFamilyMembersData retrieveTemplate() {
		
		final List<CodeValueData> maritalStatusOptions = new ArrayList<>(
				this.codeValueReadPlatformService.retrieveCodeValuesByCode("MARITAL STATUS"));

		final List<CodeValueData> genderOptions = new ArrayList<>(this.codeValueReadPlatformService.retrieveCodeValuesByCode("Gender"));

		final List<CodeValueData> relationshipOptions = new ArrayList<>(
				this.codeValueReadPlatformService.retrieveCodeValuesByCode("RELATIONSHIP"));
		
		final List<CodeValueData> professionOptions = new ArrayList<>(
				this.codeValueReadPlatformService.retrieveCodeValuesByCode("PROFESSION"));

		return ClientFamilyMembersData.templateInstance(relationshipOptions,genderOptions, 
				maritalStatusOptions, professionOptions);
	}

}
