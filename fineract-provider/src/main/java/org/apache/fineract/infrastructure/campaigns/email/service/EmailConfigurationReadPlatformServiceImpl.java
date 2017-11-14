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
package org.apache.fineract.infrastructure.campaigns.email.service;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailConfigurationNotFoundException;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailConfigurationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Service
public class EmailConfigurationReadPlatformServiceImpl implements EmailConfigurationReadPlatformService {
	
	private final JdbcTemplate jdbcTemplate;
    private final EmailConfigurationRowMapper emailConfigurationRowMapper;
    
    @Autowired
    public EmailConfigurationReadPlatformServiceImpl(final RoutingDataSource dataSource) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
    	this.emailConfigurationRowMapper = new EmailConfigurationRowMapper();
    	
    }
	
	private static final class EmailConfigurationRowMapper implements RowMapper<EmailConfigurationData> {
		
		final String schema;
		
		public EmailConfigurationRowMapper() {
			 final StringBuilder sql = new StringBuilder(300);
	            sql.append("cnf.id as id, ");
	            sql.append("cnf.name as name, ");
	            sql.append("cnf.value as value ");
	            sql.append("from scheduled_email_configuration cnf");
	            
	            this.schema = sql.toString();
		}
		
		public String schema() {
            return this.schema;
        }

		@Override
		public EmailConfigurationData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
			
			final Long id = JdbcSupport.getLong(rs, "id");
			final String name = rs.getString("name");
			final String value = rs.getString("value");
			
			return EmailConfigurationData.instance(id, name, value);
		}
		
	}

	@Override
	public Collection<EmailConfigurationData> retrieveAll() {
		final String sql = "select " + this.emailConfigurationRowMapper.schema();

        return this.jdbcTemplate.query(sql, this.emailConfigurationRowMapper, new Object[] {});
	}

	@Override
	public EmailConfigurationData retrieveOne(String name) {
		try {
			final String sql = "select " + this.emailConfigurationRowMapper.schema() + " where cnf.name = ?";

	        return this.jdbcTemplate.queryForObject(sql, this.emailConfigurationRowMapper, name);
		}
		
		catch(final EmptyResultDataAccessException e) {
			
			throw new EmailConfigurationNotFoundException(name);
		}
	}

}
