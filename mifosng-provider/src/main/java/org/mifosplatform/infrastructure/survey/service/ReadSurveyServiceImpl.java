/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.service;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.api.DataTableApiConstant;
import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.mifosplatform.infrastructure.dataqueries.service.GenericDataService;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.survey.data.ClientScoresOverview;
import org.mifosplatform.infrastructure.survey.data.LikelihoodStatus;
import org.mifosplatform.infrastructure.survey.data.SurveyDataTableData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

@Service
public class ReadSurveyServiceImpl implements ReadSurveyService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final GenericDataService genericDataService;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;

    @Autowired
    public ReadSurveyServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final GenericDataService genericDataService, final ReadWriteNonCoreDataService readWriteNonCoreDataService) {

        this.context = context;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.genericDataService = genericDataService;
        this.readWriteNonCoreDataService = readWriteNonCoreDataService;
    }

    @Override
    public List<SurveyDataTableData> retrieveAllSurveys() {

        String sql = this.retrieveAllSurveySQL("");

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        final List<SurveyDataTableData> surveyDataTables = new ArrayList<>();
        while (rs.next()) {
            final String appTableName = rs.getString("application_table_name");
            final String registeredDatatableName = rs.getString("registered_table_name");
            final boolean enabled = rs.getBoolean("enabled");
            final List<ResultsetColumnHeaderData> columnHeaderData = this.genericDataService
                    .fillResultsetColumnHeaders(registeredDatatableName);

            surveyDataTables.add(SurveyDataTableData.create(DatatableData.create(appTableName, registeredDatatableName, columnHeaderData),
                    enabled));
        }

        return surveyDataTables;
    }

    private String retrieveAllSurveySQL(String andClause) {
        // PERMITTED datatables
        return "select application_table_name, cf.enabled, registered_table_name" + " from x_registered_table "
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
        final String sql = "select cf.enabled, application_table_name, registered_table_name" + " from x_registered_table "
                + " left join c_configuration cf on x_registered_table.registered_table_name = cf.name " + " where exists" + " (select 'f'"
                + " from m_appuser_role ur " + " join m_role r on r.id = ur.role_id"
                + " left join m_role_permission rp on rp.role_id = r.id" + " left join m_permission p on p.id = rp.permission_id"
                + " where ur.appuser_id = " + this.context.authenticatedUser().getId() + " and registered_table_name='" + surveyName + "'"
                + " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', registered_table_name))) "
                + " order by application_table_name, registered_table_name";

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        SurveyDataTableData datatableData = null;
        while (rs.next()) {
            final String appTableName = rs.getString("application_table_name");
            final String registeredDatatableName = rs.getString("registered_table_name");
            final boolean enabled = rs.getBoolean("enabled");
            final List<ResultsetColumnHeaderData> columnHeaderData = this.genericDataService
                    .fillResultsetColumnHeaders(registeredDatatableName);

            datatableData = SurveyDataTableData.create(DatatableData.create(appTableName, registeredDatatableName, columnHeaderData),
                    enabled);

        }

        return datatableData;
    }

    @Override
    public List<ClientScoresOverview> retrieveClientSurveyScoreOverview(String surveyName, Long clientId) {

        final String sql = "SELECT  tz.id, lkh.name, lkh.code, poverty_line, tz.date, tz.score FROM " + surveyName + " tz"
                + " JOIN ppi_likelihoods_ppi lkp on lkp.ppi_name = '" + surveyName + "' AND enabled = '" + LikelihoodStatus.ENABLED
                + "' JOIN ppi_scores sc on score_from  <= tz.score AND score_to >=tz.score"
                + " JOIN ppi_poverty_line pvl on pvl.likelihood_ppi_id = lkp.id AND pvl.score_id = sc.id"
                + " JOIN ppi_likelihoods lkh on lkh.id = lkp.likelihood_id " + " WHERE  client_id = " + clientId;

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        List<ClientScoresOverview> scoresOverviews = new ArrayList<>();

        while (rs.next()) {
            scoresOverviews.add(new ClientScoresOverview(rs.getString("code"), rs.getString("name"), rs.getLong("score"), rs
                    .getDouble("poverty_line"), new LocalDate(rs.getTimestamp("date").getTime()), rs.getLong("id"), surveyName));
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
                    + " JOIN ppi_likelihoods lkh on lkh.id = lkp.likelihood_id " + " WHERE  client_id = " + clientId);
        }

        List<ClientScoresOverview> scoresOverviews = new ArrayList<>();

        for (String sql : sqls) {
            final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

            while (rs.next()) {
                scoresOverviews.add(new ClientScoresOverview(rs.getString("code"), rs.getString("name"), rs.getLong("score"), rs
                        .getDouble("poverty_line"), new LocalDate(rs.getTimestamp("date").getTime()), rs.getLong("id"), rs
                        .getString("surveyName")));
            }

        }

        return scoresOverviews;
    }

    private String retrieveAllSurveyNameSQL() {
        // PERMITTED datatables
        return "select cf.name from x_registered_table "
                + " join c_configuration cf on x_registered_table.registered_table_name = cf.name " + " where exists" + " (select 'f'"
                + " from m_appuser_role ur " + " join m_role r on r.id = ur.role_id"
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
