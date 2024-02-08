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

    @Autowired
    PovertyLineServiceImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PpiPovertyLineData retrieveAll(final String ppiName) {

        final SqlRowSet povertyLines = this.getPovertyLines(ppiName);

        final SqlRowSet likelihoods = this.getLikelihoods();

        List<LikeliHoodPovertyLineData> listOfLikeliHoodPovertyLineData = new ArrayList<>();

        while (likelihoods.next()) {
            final String codeName = likelihoods.getString("code");

            List<PovertyLineData> povertyLineDatas = new ArrayList<>();

            // create a new povertyLine object when ever it belong to the
            // current likelihood
            while (povertyLines.next()) {
                String likelihoodCode = povertyLines.getString("code");

                if (likelihoodCode.equals(codeName)) {
                    povertyLineDatas.add(
                            new PovertyLineData().setResourceId(povertyLines.getLong("id")).setScoreFrom(povertyLines.getLong("score_from"))
                                    .setScoreTo(povertyLines.getLong("score_to")).setPovertyLine(povertyLines.getDouble("poverty_line")));
                }
            }

            povertyLines.beforeFirst();

            // create the likelihood object with the list of povertyLine object
            // belonging to it

            LikeliHoodPovertyLineData likeliHoodPovertyLineData = new LikeliHoodPovertyLineData().setResourceId(likelihoods.getLong("id"))
                    .setPovertyLineData(povertyLineDatas).setLikeliHoodName(likelihoods.getString("name"))
                    .setLikeliHoodCode(likelihoods.getString("code")).setEnabled(likelihoods.getLong("enabled"));

            listOfLikeliHoodPovertyLineData.add(likeliHoodPovertyLineData);

        }

        PpiPovertyLineData ppiPovertyLineData = new PpiPovertyLineData().setLikeliHoodPovertyLineData(listOfLikeliHoodPovertyLineData)
                .setPpi(ppiName);

        return ppiPovertyLineData;
    }

    @Override
    public LikeliHoodPovertyLineData retrieveForLikelihood(final String ppiName, final Long likelihoodId) {

        final SqlRowSet povertyLines = this.getPovertyLines(likelihoodId);

        List<PovertyLineData> povertyLineDatas = new ArrayList<>();

        while (povertyLines.next()) {

            povertyLineDatas
                    .add(new PovertyLineData().setResourceId(povertyLines.getLong("id")).setScoreFrom(povertyLines.getLong("score_from"))
                            .setScoreTo(povertyLines.getLong("score_to")).setPovertyLine(povertyLines.getDouble("poverty_line")));
        }

        povertyLines.first();

        // create the likelihood object with the list of povertyLine object
        // belonging to it

        return new LikeliHoodPovertyLineData().setResourceId(povertyLines.getLong("id")).setPovertyLineData(povertyLineDatas)
                .setLikeliHoodName(povertyLines.getString("name")).setLikeliHoodCode(povertyLines.getString("code"))
                .setEnabled(povertyLines.getLong("enabled"));

    }

    private SqlRowSet getLikelihoods() {
        String sql = "SELECT lkp.id, lkh.code , lkh.name, lkp.enabled " + " FROM ppi_likelihoods lkh "
                + " JOIN ppi_likelihoods_ppi lkp on lkp.likelihood_id = lkh.id ";
        return this.jdbcTemplate.queryForRowSet(sql);
    }

    private SqlRowSet getPovertyLines(final String ppiName) {
        String sql = "SELECT pl.id, sc.score_from, sc.score_to , pl.poverty_line,lkh.code ,  lkh.name , lkp.ppi_name "
                + " FROM ppi_poverty_line pl " + " JOIN ppi_likelihoods lkh on lkh.id = pl.likelihood_ppi_id "
                + " JOIN ppi_likelihoods_ppi lkp on lkp.id = pl.likelihood_ppi_id " + " JOIN ppi_scores sc on sc.id = pl.score_id "
                + " WHERE lkp.ppi_name = ? ";

        return this.jdbcTemplate.queryForRowSet(sql, new Object[] { ppiName });

    }

    private SqlRowSet getPovertyLines(final Long likelihoodId) {
        String sql = "SELECT pl.id, sc.score_from, sc.score_to , pl.poverty_line,lkh.code , lkp.enabled, lkp.id as likelihood_id , lkh.name , lkp.ppi_name "
                + " FROM ppi_poverty_line pl " + " JOIN ppi_likelihoods_ppi lkp on lkp.id = pl.likelihood_ppi_id "
                + " JOIN ppi_likelihoods lkh on lkh.id = lkp.likelihood_id " + " JOIN ppi_scores sc on sc.id = pl.score_id "
                + " WHERE pl.likelihood_ppi_id = ? ";

        return this.jdbcTemplate.queryForRowSet(sql, new Object[] { likelihoodId });

    }

}
