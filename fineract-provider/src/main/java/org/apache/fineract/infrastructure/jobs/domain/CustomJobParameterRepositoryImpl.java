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
package org.apache.fineract.infrastructure.jobs.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomJobParameterRepositoryImpl implements CustomJobParameterRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DatabaseSpecificSQLGenerator databaseSpecificSQLGenerator;

    @Override
    public Long save(String jsonString) {
        final StringBuilder insertSqlStatementBuilder = new StringBuilder(500);
        insertSqlStatementBuilder.append("INSERT INTO batch_custom_job_parameters (parameter_json) VALUES (%s)"
                .formatted(databaseSpecificSQLGenerator.castJson(":jsonString")));
        SqlParameterSource parameters = new MapSqlParameterSource("jsonString", jsonString);
        namedParameterJdbcTemplate.update(insertSqlStatementBuilder.toString(), parameters);
        final Long customParameterId = namedParameterJdbcTemplate.getJdbcTemplate().queryForObject(
                DatabaseSpecificSQLGenerator.SELECT_CLAUSE.formatted(databaseSpecificSQLGenerator.lastInsertId()), Long.class);
        return customParameterId;
    }

    @Override
    public Optional<CustomJobParameter> findById(Long id) {
        CustomJobParameterExtractor customJobParameterExtractor = new CustomJobParameterExtractor();
        final StringBuilder sqlStatementBuilder = new StringBuilder(500);
        sqlStatementBuilder.append("SELECT cjp.parameter_json AS parameter_json FROM batch_custom_job_parameters cjp WHERE cjp.id = :id");
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        return namedParameterJdbcTemplate.query(sqlStatementBuilder.toString(), parameters, customJobParameterExtractor);
    }

    private static final class CustomJobParameterExtractor implements ResultSetExtractor<Optional<CustomJobParameter>> {

        @Override
        public Optional<CustomJobParameter> extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                CustomJobParameter jobParameter = new CustomJobParameter();
                jobParameter.setParameterJson(rs.getString("parameter_json"));
                return Optional.of(jobParameter);
            }
            return Optional.empty();
        }
    }

}
