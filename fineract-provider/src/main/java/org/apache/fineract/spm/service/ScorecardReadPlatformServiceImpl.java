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
package org.apache.fineract.spm.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.spm.data.ScorecardData;
import org.apache.fineract.spm.data.ScorecardValue;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ScorecardReadPlatformServiceImpl implements ScorecardReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public ScorecardReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class ScorecardMapper implements RowMapper<ScorecardData> {

        public String schema() {
            StringBuilder sb = new StringBuilder(50);
            sb.append(" sc.id as id, sc.survey_id as surveyId, s.a_name as surveyName, ");
            sb.append(" sc.client_id as clientId,");
            sb.append(" sc.user_id as userId, user.username as username ");
            sb.append(" from m_survey_scorecards sc ");
            sb.append(" left join m_surveys s ON s.id = sc.survey_id ");
            sb.append(" left join m_appuser user ON user.id = sc.user_id ");

            return sb.toString();
        }

        @Override
        public ScorecardData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long surveyId = rs.getLong("surveyId");
            final String surveyName = rs.getString("surveyName");
            final Long clientId = rs.getLong("clientId");
            final Long userId = rs.getLong("userId");
            final String username = rs.getString("username");

            return ScorecardData.instance(id, userId, username, surveyId, surveyName, clientId);
        }
    }

    private static final class ScorecardValueMapper implements RowMapper<ScorecardValue> {

        public String schema() {
            StringBuilder sb = new StringBuilder(50);
            sb.append(" sc.question_id as questionId, sc.response_id as responseId, ");
            sb.append(" sc.created_on as createdOn, sc.a_value as value ");
            sb.append(" from m_survey_scorecards sc  ");
            sb.append(" where sc.survey_id = ? and sc.client_id = ?  ");

            return sb.toString();
        }

        @Override
        public ScorecardValue mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long questionId = rs.getLong("questionId");
            final Long responseId = rs.getLong("responseId");
            final LocalDate createdOn = JdbcSupport.getLocalDate(rs, "createdOn");
            final Integer value = rs.getInt("value");

            return ScorecardValue.instance(questionId, responseId, value, createdOn.toDate());
        }
    }

    List<ScorecardValue> getScorecardValueBySurveyAndClient(final Long surveyId, final Long clientId) {
        ScorecardValueMapper scvm = new ScorecardValueMapper();
        String sql = "select " + scvm.schema();
        return this.jdbcTemplate.query(sql, scvm, new Object[] { surveyId, clientId });
    }

    Collection<ScorecardData> updateScorecardValues(Collection<ScorecardData> scorecard) {
        for (ScorecardData scorecardData : scorecard) {
            scorecardData.setScorecardValues(getScorecardValueBySurveyAndClient(scorecardData.getSurveyId(), scorecardData.getClientId()));
        }
        return scorecard;
    }

    @Override
    public Collection<ScorecardData> retrieveScorecardBySurvey(Long surveyId) {
        this.context.authenticatedUser();
        ScorecardMapper scm = new ScorecardMapper();
        String sql = "select " + scm.schema() + " where sc.survey_id = ? " + " group by sc.survey_id, sc.client_id, sc.id ";
        Collection<ScorecardData> scorecardDatas = this.jdbcTemplate.query(sql, scm, new Object[] { surveyId });
        updateScorecardValues(scorecardDatas);
        return scorecardDatas;
    }

    @Override
    public Collection<ScorecardData> retrieveScorecardByClient(Long clientId) {
        this.context.authenticatedUser();
        ScorecardMapper scm = new ScorecardMapper();
        String sql = "select " + scm.schema() + " where sc.client_id = ? " + " group by sc.survey_id, sc.client_id, sc.id ";
        Collection<ScorecardData> scorecardDatas = this.jdbcTemplate.query(sql, scm, new Object[] { clientId });
        updateScorecardValues(scorecardDatas);
        return scorecardDatas;
    }

    @Override
    public Collection<ScorecardData> retrieveScorecardBySurveyAndClient(Long surveyId, Long clientId) {
        this.context.authenticatedUser();
        ScorecardMapper scm = new ScorecardMapper();
        String sql = "select " + scm.schema() + " where sc.survey_id = ? and sc.client_id = ? " + " group by sc.survey_id, sc.client_id, sc.id ";
        Collection<ScorecardData> scorecardDatas = this.jdbcTemplate.query(sql, scm, new Object[] { surveyId, clientId });
        updateScorecardValues(scorecardDatas);
        return scorecardDatas;
    }

}
