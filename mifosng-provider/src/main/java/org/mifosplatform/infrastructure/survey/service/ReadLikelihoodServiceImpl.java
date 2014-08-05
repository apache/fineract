/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.service;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.survey.data.LikelihoodData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

@Service
public class ReadLikelihoodServiceImpl implements ReadLikelihoodService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Autowired
    ReadLikelihoodServiceImpl(final RoutingDataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);

    }

    @Override
    public List<LikelihoodData> retrieveAll(final String ppiName) {
        final SqlRowSet likelihood = this._getLikelihood(ppiName);

        List<LikelihoodData> likelihoodDatas = new ArrayList<>();

        while (likelihood.next()) {
            likelihoodDatas.add(new LikelihoodData(likelihood.getLong("id"), likelihood.getString("name"), likelihood.getString("code"),
                    likelihood.getLong("enabled")

            ));

        }

        return likelihoodDatas;
    }

    private SqlRowSet _getLikelihood(final String ppiName) {
        String sql = "SELECT lkp.id, lkh.code , lkh.name, lkp.enabled " + " FROM ppi_poverty_line pl "
                + " JOIN ppi_likelihoods_ppi lkp on lkp.id = pl.likelihood_ppi_id "
                + " JOIN ppi_likelihoods lkh on lkp.likelihood_id = lkh.id " + " WHERE lkp.ppi_name = ? "
                + " GROUP BY pl.likelihood_ppi_id ";

        return this.jdbcTemplate.queryForRowSet(sql, new Object[] { ppiName });

    }

    @Override
    public LikelihoodData retrieve(final Long likelihoodId) {
        final SqlRowSet likelihood = this._getLikelihood(likelihoodId);

        likelihood.first();

        return new LikelihoodData(likelihood.getLong("id"), likelihood.getString("name"), likelihood.getString("code"),
                likelihood.getLong("enabled")

        );

    }

    private SqlRowSet _getLikelihood(final Long likelihoodId) {
        String sql = "SELECT lkp.id, lkh.code , lkh.name, lkp.enabled " + " FROM ppi_likelihoods lkh "
                + " JOIN ppi_likelihoods_ppi lkp on lkp.likelihood_id = lkh.id " + " WHERE lkp.id = ? ";

        return this.jdbcTemplate.queryForRowSet(sql, new Object[] { likelihoodId });

    }

}
