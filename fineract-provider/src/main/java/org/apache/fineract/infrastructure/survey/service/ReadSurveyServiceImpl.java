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
package org.apache.fineract.infrastructure.survey.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.infrastructure.survey.data.ClientScoresOverview;
import org.apache.fineract.infrastructure.survey.data.LikelihoodStatus;
import org.apache.fineract.infrastructure.survey.data.SurveyDataTableData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReadSurveyServiceImpl implements ReadSurveyService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final SqlValidator sqlValidator;
    private final GenericDataService genericDataService;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;

    @Override
    public List<SurveyDataTableData> retrieveAllSurveys() {

        String sql = this.retrieveAllSurveySQL("");

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        final List<SurveyDataTableData> surveyDataTables = new ArrayList<>();
        while (rs.next()) {
            final String appTableName = rs.getString("application_table_name");
            final String registeredDatatableName = rs.getString("registered_table_name");
            final String entitySubType = rs.getString("entity_subtype");
            final boolean enabled = rs.getBoolean("enabled");
            final List<ResultsetColumnHeaderData> columnHeaderData = this.genericDataService
                    .fillResultsetColumnHeaders(registeredDatatableName);

            surveyDataTables.add(SurveyDataTableData
                    .create(DatatableData.create(appTableName, registeredDatatableName, entitySubType, columnHeaderData), enabled));
        }

        return surveyDataTables;
    }

    private String retrieveAllSurveySQL(String andClause) {
        // PERMITTED datatables
        return "select application_table_name, cf.enabled, registered_table_name, entity_subtype" + " from x_registered_table "
                + " left join c_configuration cf on x_registered_table.registered_table_name = cf.name " + " where exists" + " (select 'f'"
                + " from m_appuser_role ur " + " join m_role r on r.id = ur.role_id"
                + " left join m_role_permission rp on rp.role_id = r.id" + " left join m_permission p on p.id = rp.permission_id"
                + " where ur.appuser_id = " + this.context.authenticatedUser().getId()
                + " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', registered_table_name))) "
                + " and x_registered_table.category = " + DataTableApiConstant.CATEGORY_PPI + andClause
                + " order by application_table_name, registered_table_name";
    }

    @Override
    public SurveyDataTableData retrieveSurvey(String surveyName) {
        sqlValidator.validate(surveyName);
        final String sql = "select cf.enabled, application_table_name, registered_table_name, entity_subtype" + " from x_registered_table "
                + " left join c_configuration cf on x_registered_table.registered_table_name = cf.name " + " where exists" + " (select 'f'"
                + " from m_appuser_role ur " + " join m_role r on r.id = ur.role_id"
                + " left join m_role_permission rp on rp.role_id = r.id" + " left join m_permission p on p.id = rp.permission_id"
                + " where ur.appuser_id = ? and registered_table_name=?"
                + " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', registered_table_name))) "
                + " order by application_table_name, registered_table_name";

        SurveyDataTableData datatableData = null;

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql, new Object[] { this.context.authenticatedUser().getId(), surveyName }); // NOSONAR
        if (rs.next()) {
            final String appTableName = rs.getString("application_table_name");
            final String registeredDatatableName = rs.getString("registered_table_name");
            final String entitySubType = rs.getString("entity_subtype");
            final boolean enabled = rs.getBoolean("enabled");
            final List<ResultsetColumnHeaderData> columnHeaderData = this.genericDataService
                    .fillResultsetColumnHeaders(registeredDatatableName);
            datatableData = SurveyDataTableData
                    .create(DatatableData.create(appTableName, registeredDatatableName, entitySubType, columnHeaderData), enabled);

        }

        return datatableData;
    }

    @Override
    public List<ClientScoresOverview> retrieveClientSurveyScoreOverview(String surveyName, Long clientId) {

        final String sql = "SELECT  tz.id, lkh.name, lkh.code, poverty_line, tz.date, tz.score FROM ? tz"
                + " JOIN ppi_likelihoods_ppi lkp on lkp.ppi_name = ? AND enabled = ? "
                + " JOIN ppi_scores sc on score_from  <= tz.score AND score_to >=tz.score"
                + " JOIN ppi_poverty_line pvl on pvl.likelihood_ppi_id = lkp.id AND pvl.score_id = sc.id"
                + " JOIN ppi_likelihoods lkh on lkh.id = lkp.likelihood_id " + " WHERE  client_id = ? ";

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql,
                new Object[] { surveyName, surveyName, LikelihoodStatus.ENABLED, clientId });

        List<ClientScoresOverview> scoresOverviews = new ArrayList<>();

        while (rs.next()) {
            scoresOverviews.add(new ClientScoresOverview().setLikelihoodCode(rs.getString("code")).setLikelihoodName(rs.getString("name"))
                    .setScore(rs.getLong("score")).setPovertyLine(rs.getDouble("poverty_line")).setDate(rs.getDate("date").toLocalDate())
                    .setId(rs.getLong("id")).setSurveyName(surveyName));
        }
        return scoresOverviews;
    }

    @Override
    public List<ClientScoresOverview> retrieveClientSurveyScoreOverview(Long clientId) {
        final String surveyNameSql = retrieveAllSurveyNameSQL();
        final SqlRowSet surveyNames = this.jdbcTemplate.queryForRowSet(surveyNameSql);

        ArrayList<String> sqls = new ArrayList<>();

        while (surveyNames.next()) {
            sqls.add("SELECT '" + surveyNames.getString("name")
                    + "' as surveyName, tz.id, lkh.name, lkh.code, poverty_line, tz.date, tz.score FROM " + surveyNames.getString("name")
                    + " tz" + " JOIN ppi_likelihoods_ppi lkp on lkp.ppi_name = '" + surveyNames.getString("name") + "' AND enabled = '"
                    + LikelihoodStatus.ENABLED + "' JOIN ppi_scores sc on score_from  <= tz.score AND score_to >=tz.score"
                    + " JOIN ppi_poverty_line pvl on pvl.likelihood_ppi_id = lkp.id AND pvl.score_id = sc.id"
                    + " JOIN ppi_likelihoods lkh on lkh.id = lkp.likelihood_id " + " WHERE  client_id = ?");
        }

        List<ClientScoresOverview> scoresOverviews = new ArrayList<>();

        for (String sql : sqls) {
            final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql, clientId);

            while (rs.next()) {
                scoresOverviews.add(new ClientScoresOverview().setLikelihoodCode(rs.getString("code"))
                        .setLikelihoodName(rs.getString("name")).setScore(rs.getLong("score")).setPovertyLine(rs.getDouble("poverty_line"))
                        .setDate(rs.getDate("date").toLocalDate()).setId(rs.getLong("id")).setSurveyName(rs.getString("surveyName"))

                );

            }

        }

        return scoresOverviews;
    }

    private String retrieveAllSurveyNameSQL() {
        // PERMITTED datatables
        return "select cf.name from x_registered_table " + " join c_configuration cf on x_registered_table.registered_table_name = cf.name "
                + " where exists" + " (select 'f'" + " from m_appuser_role ur " + " join m_role r on r.id = ur.role_id"
                + " left join m_role_permission rp on rp.role_id = r.id" + " left join m_permission p on p.id = rp.permission_id"
                + " where ur.appuser_id = " + this.context.authenticatedUser().getId()
                + " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', registered_table_name))) "
                + " and x_registered_table.category = " + DataTableApiConstant.CATEGORY_PPI
                + " order by application_table_name, registered_table_name";
    }

    @Override
    public GenericResultsetData retrieveSurveyEntry(String surveyName, Long clientId, Long entryId) {

        return readWriteNonCoreDataService.retrieveDataTableGenericResultSet(surveyName, clientId, null, entryId);

    }
}
