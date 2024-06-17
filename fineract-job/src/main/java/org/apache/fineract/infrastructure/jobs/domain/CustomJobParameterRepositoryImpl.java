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

import com.google.gson.Gson;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomJobParameterRepositoryImpl implements CustomJobParameterRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DatabaseSpecificSQLGenerator databaseSpecificSQLGenerator;
    private final Gson gson = GoogleGsonSerializerHelper.createSimpleGson();

    @Override
    public Long save(Set<JobParameterDTO> customJobParameters) {
        Objects.requireNonNull(customJobParameters);
        final String insertSQL = "INSERT INTO batch_custom_job_parameters (parameter_json) VALUES (%s)"
                .formatted(databaseSpecificSQLGenerator.castJson(":jsonString"));
        final String jsonString = gson.toJson(customJobParameters);
        SqlParameterSource parameters = new MapSqlParameterSource("jsonString", jsonString);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(insertSQL, parameters, keyHolder);
        return databaseSpecificSQLGenerator.fetchPK(keyHolder);
    }

    @Override
    public Optional<CustomJobParameter> findById(Long id) {
        Objects.requireNonNull(id);
        CustomJobParameterExtractor customJobParameterExtractor = new CustomJobParameterExtractor();
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        return Optional.ofNullable(namedParameterJdbcTemplate.query(
                "SELECT cjp.parameter_json AS parameter_json FROM batch_custom_job_parameters cjp WHERE cjp.id = :id", parameters,
                customJobParameterExtractor));
    }

    private static final class CustomJobParameterExtractor implements ResultSetExtractor<CustomJobParameter> {

        @Override
        public CustomJobParameter extractData(ResultSet rs) throws SQLException, DataAccessException {
            CustomJobParameter jobParameter = null;
            if (rs.next()) {
                jobParameter = new CustomJobParameter();
                jobParameter.setParameterJson(rs.getString("parameter_json"));
            }
            return jobParameter;
        }
    }

}
