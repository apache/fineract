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

import javax.sql.DataSource;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.survey.data.LikeliHoodPovertyLineData;
import org.apache.fineract.infrastructure.survey.data.PovertyLineData;
import org.apache.fineract.infrastructure.survey.data.PpiPovertyLineData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

@Service
public class PovertyLineServiceImpl implements PovertyLineService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Autowired
    PovertyLineServiceImpl(final RoutingDataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);

    }

    @Override
    public PpiPovertyLineData retrieveAll(final String ppiName) {

        final SqlRowSet povertyLines = this._getPovertyLines(ppiName);

        final SqlRowSet likelihoods = this._getLikelihoods();

        List<LikeliHoodPovertyLineData> listOfLikeliHoodPovertyLineData = new ArrayList<>();

        while (likelihoods.next()) {
            final String codeName = likelihoods.getString("code");

            List<PovertyLineData> povertyLineDatas = new ArrayList<>();

            // create a new povertyLine object when ever it belong to the
            // current likelihood
            while (povertyLines.next()) {
                String likelihoodCode = povertyLines.getString("code");

                if (likelihoodCode.equals(codeName)) {
                    povertyLineDatas.add(new PovertyLineData(povertyLines.getLong("id"), povertyLines.getLong("score_from"), povertyLines
                            .getLong("score_to"), povertyLines.getDouble("poverty_line")));
                }
            }

            povertyLines.beforeFirst();

            // create the likelihood object with the list of povertyLine object
            // belonging to it

            LikeliHoodPovertyLineData likeliHoodPovertyLineData = new LikeliHoodPovertyLineData(likelihoods.getLong("id"),
                    povertyLineDatas, likelihoods.getString("name"), likelihoods.getString("code"), likelihoods.getLong("enabled"));

            listOfLikeliHoodPovertyLineData.add(likeliHoodPovertyLineData);

        }

        PpiPovertyLineData ppiPovertyLineData = new PpiPovertyLineData(listOfLikeliHoodPovertyLineData, ppiName);

        return ppiPovertyLineData;
    }

    @Override
    public LikeliHoodPovertyLineData retrieveForLikelihood(final String ppiName, final Long likelihoodId) {

        final SqlRowSet povertyLines = this._getPovertyLines(likelihoodId);

        List<PovertyLineData> povertyLineDatas = new ArrayList<>();

        while (povertyLines.next()) {

            povertyLineDatas.add(new PovertyLineData(povertyLines.getLong("id"), povertyLines.getLong("score_from"), povertyLines
                    .getLong("score_to"), povertyLines.getDouble("poverty_line")));
        }

        povertyLines.first();

        // create the likelihood object with the list of povertyLine object
        // belonging to it

        return new LikeliHoodPovertyLineData(povertyLines.getLong("likelihood_id"), povertyLineDatas, povertyLines.getString("name"),
                povertyLines.getString("code"), povertyLines.getLong("enabled"));

    }

    private SqlRowSet _getLikelihoods() {
        String sql = "SELECT lkp.id, lkh.code , lkh.name, lkp.enabled " + " FROM ppi_likelihoods lkh "
                + " JOIN ppi_likelihoods_ppi lkp on lkp.likelihood_id = lkh.id ";
        return this.jdbcTemplate.queryForRowSet(sql);
    }

    private SqlRowSet _getPovertyLines(final String ppiName) {
        String sql = "SELECT pl.id, sc.score_from, sc.score_to , pl.poverty_line,lkh.code ,  lkh.name , lkp.ppi_name "
                + " FROM ppi_poverty_line pl " + " JOIN ppi_likelihoods lkh on lkh.id = pl.likelihood_ppi_id "
                + " JOIN ppi_likelihoods_ppi lkp on lkp.id = pl.likelihood_ppi_id " + " JOIN ppi_scores sc on sc.id = pl.score_id "
                + " WHERE lkp.ppi_name = ? ";

        return this.jdbcTemplate.queryForRowSet(sql, new Object[] { ppiName });

    }

    private SqlRowSet _getPovertyLines(final Long likelihoodId) {
        String sql = "SELECT pl.id, sc.score_from, sc.score_to , pl.poverty_line,lkh.code , lkp.enabled, lkp.id as likelihood_id , lkh.name , lkp.ppi_name "
                + " FROM ppi_poverty_line pl "
                + " JOIN ppi_likelihoods_ppi lkp on lkp.id = pl.likelihood_ppi_id "
                + " JOIN ppi_likelihoods lkh on lkh.id = lkp.likelihood_id "
                + " JOIN ppi_scores sc on sc.id = pl.score_id "
                + " WHERE pl.likelihood_ppi_id = ? ";

        return this.jdbcTemplate.queryForRowSet(sql, new Object[] { likelihoodId });

    }

}
