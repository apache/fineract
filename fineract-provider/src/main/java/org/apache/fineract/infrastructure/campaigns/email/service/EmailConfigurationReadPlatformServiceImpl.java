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

import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailConfigurationData;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailConfigurationNotFoundException;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.query.QScheduledEmailConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailConfigurationReadPlatformServiceImpl implements EmailConfigurationReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final SQLQueryFactory queryFactory;
    private final EmailConfigurationRowMapper emailConfigurationRowMapper;
    private final QScheduledEmailConfiguration cnf = new QScheduledEmailConfiguration("cnf");

    @Autowired
    public EmailConfigurationReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate, final SQLQueryFactory queryFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryFactory = queryFactory;
        this.emailConfigurationRowMapper = new EmailConfigurationRowMapper();
    }

    private static final class EmailConfigurationRowMapper implements RowMapper<EmailConfigurationData> {

        private static final String EMAIL_CONFIGURATION_SCHEMA = """
                cnf.id as id,
                cnf.name as name,
                cnf.value as value
                from scheduled_email_configuration cnf\s""";

        EmailConfigurationRowMapper() {}

        private String schema() {
            return EMAIL_CONFIGURATION_SCHEMA;
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
        List<Tuple> rows = queryFactory.select(cnf.id, cnf.name, cnf.value).from(cnf).fetch();
        List<EmailConfigurationData> emailConfigurationData = new ArrayList<>(rows.size());
        for (Tuple row : rows) {
            Integer id = row.get(cnf.id);
            String name = row.get(cnf.name);
            String value = row.get(cnf.value);
            emailConfigurationData.add(EmailConfigurationData.instance(id != null ? id.longValue() : null, name, value));
        }
        return emailConfigurationData;
    }

    @Override
    public EmailConfigurationData retrieveOne(String name) {
        try {
            final String sql = "select " + this.emailConfigurationRowMapper.schema() + " where cnf.name = ?";

            return this.jdbcTemplate.queryForObject(sql, this.emailConfigurationRowMapper, new Object[] { name }); // NOSONAR
        }

        catch (final EmptyResultDataAccessException e) {

            throw new EmailConfigurationNotFoundException(name, e);
        }
    }

}
