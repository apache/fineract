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
package org.apache.fineract.organisation.staff.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class StaffReadPlatformServiceImpl implements StaffReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final StaffLookupMapper lookupMapper = new StaffLookupMapper();
    private final StaffInOfficeHierarchyMapper staffInOfficeHierarchyMapper = new StaffInOfficeHierarchyMapper();
    private final ColumnValidator columnValidator;

    @Autowired
    public StaffReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
    		final ColumnValidator columnValidator) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.columnValidator = columnValidator;
    }

    private static final class StaffMapper implements RowMapper<StaffData> {

        public String schema() {
            return " s.id as id,s.office_id as officeId, o.name as officeName, s.firstname as firstname, s.lastname as lastname,"
                    + " s.display_name as displayName, s.is_loan_officer as isLoanOfficer, s.external_id as externalId, s.mobile_no as mobileNo,"
            		+ " s.is_active as isActive, s.joining_date as joiningDate from m_staff s "
                    + " join m_office o on o.id = s.office_id";
        }

        @Override
        public StaffData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String displayName = rs.getString("displayName");
            final Long officeId = rs.getLong("officeId");
            final boolean isLoanOfficer = rs.getBoolean("isLoanOfficer");
            final String officeName = rs.getString("officeName");
            final String externalId = rs.getString("externalId");
            final String mobileNo = rs.getString("mobileNo");
            final boolean isActive = rs.getBoolean("isActive");
            final LocalDate joiningDate = JdbcSupport.getLocalDate(rs, "joiningDate");

            return StaffData.instance(id, firstname, lastname, displayName, officeId, officeName, isLoanOfficer, externalId, mobileNo,
                    isActive, joiningDate);
        }
    }

    private static final class StaffInOfficeHierarchyMapper implements RowMapper<StaffData> {

        public String schema(final boolean loanOfficersOnly) {

            final StringBuilder sqlBuilder = new StringBuilder(200);

            sqlBuilder.append("s.id as id, s.office_id as officeId, ohierarchy.name as officeName,");
            sqlBuilder.append("s.firstname as firstname, s.lastname as lastname,");
            sqlBuilder.append("s.display_name as displayName, s.is_loan_officer as isLoanOfficer, s.external_id as externalId, ");
            sqlBuilder.append("s.mobile_no as mobileNo, s.is_active as isActive, s.joining_date as joiningDate ");
            sqlBuilder.append("from m_office o ");
            sqlBuilder.append("join m_office ohierarchy on o.hierarchy like concat(ohierarchy.hierarchy, '%') ");
            sqlBuilder.append("join m_staff s on s.office_id = ohierarchy.id and s.is_active=1 ");

            if (loanOfficersOnly) {
                sqlBuilder.append("and s.is_loan_officer is true ");
            }

            sqlBuilder.append("where o.id = ? ");

            return sqlBuilder.toString();
        }

        @Override
        public StaffData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String displayName = rs.getString("displayName");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final boolean isLoanOfficer = rs.getBoolean("isLoanOfficer");
            final String externalId = rs.getString("externalId");
            final String mobileNo = rs.getString("mobileNo");
            final boolean isActive = rs.getBoolean("isActive");
            final LocalDate joiningDate = JdbcSupport.getLocalDate(rs, "joiningDate");

            return StaffData.instance(id, firstname, lastname, displayName, officeId, officeName, isLoanOfficer, externalId, mobileNo,
                    isActive, joiningDate);
        }
    }

    private static final class StaffLookupMapper implements RowMapper<StaffData> {

        private final String schemaSql;

        public StaffLookupMapper() {

            final StringBuilder sqlBuilder = new StringBuilder(100);
            sqlBuilder.append("s.id as id, s.display_name as displayName ");
            sqlBuilder.append("from m_staff s ");
            sqlBuilder.append("join m_office o on o.id = s.office_id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public StaffData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String displayName = rs.getString("displayName");
            return StaffData.lookup(id, displayName);
        }
    }

    @Override
    public Collection<StaffData> retrieveAllLoanOfficersInOfficeById(final Long officeId) {
        return retrieveAllStaff(" office_id = ? and is_loan_officer=1 and o.hierarchy like ?", officeId);
    }

    @Override
    public Collection<StaffData> retrieveAllStaffForDropdown(final Long officeId) {
    	
    	//adding the Authorization criteria so that a user cannot see an employee who does not belong to his office or 	a sub office for his office.
        final String hierarchy = this.context.authenticatedUser().getOffice().getHierarchy()+"%";

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);

        final String sql = "select " + this.lookupMapper.schema() + " where s.office_id = ? and s.is_active=1 and o.hierarchy like ? ";

        return this.jdbcTemplate.query(sql, this.lookupMapper, new Object[] { defaultOfficeId, hierarchy });
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    public StaffData retrieveStaff(final Long staffId) {
    	
        //adding the Authorization criteria so that a user cannot see an employee who does not belong to his office or 	a sub office for his office.
        final String hierarchy = this.context.authenticatedUser().getOffice().getHierarchy()+ "%";
         

        try {
            final StaffMapper rm = new StaffMapper();
            final String sql = "select " + rm.schema() + " where s.id = ? and o.hierarchy like ? " ;

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { staffId, hierarchy });
        } catch (final EmptyResultDataAccessException e) {
            throw new StaffNotFoundException(staffId);
        }
    }

    @Override
    public Collection<StaffData> retrieveAllStaff(final String sqlSearch, final Long officeId, final boolean loanOfficersOnly,
            final String status) {
        final String extraCriteria = getStaffCriteria(sqlSearch, officeId, loanOfficersOnly, status);
        return retrieveAllStaff(extraCriteria, officeId);
    }

    private Collection<StaffData> retrieveAllStaff(final String extraCriteria, Long officeId) {
    	
        final StaffMapper rm = new StaffMapper();
        String sql = "select " + rm.schema();
        final String hierarchy = this.context.authenticatedUser().getOffice().getHierarchy()+"%";
        if (StringUtils.isNotBlank(extraCriteria)){
        	sql += " where " + extraCriteria;        	
        }
        sql = sql + " order by s.lastname";
        if(officeId==null){
        	return this.jdbcTemplate.query(sql, rm, new Object[] {hierarchy });
        }        
        return this.jdbcTemplate.query(sql, rm, new Object[] {officeId, hierarchy });
    }

    private String getStaffCriteria(final String sqlSearch, final Long officeId, final boolean loanOfficersOnly, final String status) {

        final StringBuffer extraCriteria = new StringBuffer(200);

        if (sqlSearch != null) {
            extraCriteria.append(" and (").append(sqlSearch).append(")");
            final StaffMapper rm = new StaffMapper();
            this.columnValidator.validateSqlInjection(rm.schema(), sqlSearch);
        }
        if (officeId != null) {
            extraCriteria.append(" and s.office_id = ? ");
        }
        if (loanOfficersOnly) {
            extraCriteria.append(" and s.is_loan_officer is true ");
        }
        // Passing status parameter to get ACTIVE (By Default), INACTIVE or ALL
        // (Both active and Inactive) employees
        if (status!=null) {
            if (status.equalsIgnoreCase("active")) {
                extraCriteria.append(" and s.is_active = 1 ");
            } else if (status.equalsIgnoreCase("inActive")) {
                extraCriteria.append(" and s.is_active = 0 ");
            } else if (status.equalsIgnoreCase("all")) {
            } else {
                throw new UnrecognizedQueryParamException("status", status, new Object[]{"all", "active", "inactive"});
            }
        }
        //adding the Authorization criteria so that a user cannot see an employee who does not belong to his office or 	a sub office for his office.
        
        extraCriteria.append(" and o.hierarchy like ? ");

        if (StringUtils.isNotBlank(extraCriteria.toString())) {
            extraCriteria.delete(0, 4);
        }
        
        // remove begin four letter including a space from the string.
        return extraCriteria.toString();
    }

    @Override
    public Collection<StaffData> retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(final Long officeId, final boolean loanOfficersOnly) {

        String sql = "select " + this.staffInOfficeHierarchyMapper.schema(loanOfficersOnly);
        sql = sql + " order by s.lastname";
        return this.jdbcTemplate.query(sql, this.staffInOfficeHierarchyMapper, new Object[] { officeId });
    }
    
    @Override
	public Object[] hasAssociatedItems(final Long staffId){
        ArrayList<String> params = new ArrayList<String>();        
                
        String sql =  "select c.display_name as client, g.display_name as grp,l.loan_officer_id as loan, s.field_officer_id as sav"+
        			  " from m_staff staff "+
        			  " left outer join m_client c on staff.id = c.staff_id  AND c.status_enum < ? "+
        			  " left outer join m_group g on staff.id = g.staff_id " +
                      " left outer join m_loan l on staff.id = l.loan_officer_id and l.loan_status_id < ? " +
                      " left outer join m_savings_account s on c.staff_id = s.field_officer_id and s.status_enum < ? "+ 
                      " where  staff.id  = ? "+
                      " group by staff.id, client, grp, loan, sav";
        
       
		List<Map<String, Object>> result = this.jdbcTemplate.queryForList(
				sql,
				new Object[] {
						ClientStatus.CLOSED.getValue(),
						LoanStatus.WITHDRAWN_BY_CLIENT.getValue(),
						SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT
								.getValue(), staffId });
        if (result != null) {
       		for (Map<String, Object> map : result) {
       			if (map.get("client") != null) {
       				params.add("client");
       			}
       			if (map.get("grp") != null) {
       				params.add("group");
       			}
       			if (map.get("loan")  != null) {
       				params.add("loan");
       			}
       			if (map.get("sav")  != null) {
       				params.add("savings account");
       			}
       		}		
        }
        return params.toArray();
        
    }
}