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
import org.apache.fineract.infrastructure.survey.data.LikelihoodData;
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
