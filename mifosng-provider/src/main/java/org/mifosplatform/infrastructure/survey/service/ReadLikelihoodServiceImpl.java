package org.mifosplatform.infrastructure.survey.service;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.survey.data.LikelihoodData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cieyou on 3/12/14.
 */
@Service
public class ReadLikelihoodServiceImpl implements ReadLikelihoodService {

    private final static Logger logger = LoggerFactory.getLogger(PovertyLineService.class);
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Autowired
    ReadLikelihoodServiceImpl(final RoutingDataSource dataSource){
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);

    }

    @Override
    public List<LikelihoodData> retrieveAll(final String ppiName)
    {
        final SqlRowSet likelihood =this._getLikelihood(ppiName);

        List<LikelihoodData> likelihoodDatas = new ArrayList<LikelihoodData>();

        while(likelihood.next())
        {
            likelihoodDatas.add(new LikelihoodData(
            likelihood.getLong("id"),
            likelihood.getString("name"),
            likelihood.getString("code"),
            likelihood.getLong("enabled")

            ));

        }


        return likelihoodDatas;
    }

    private SqlRowSet _getLikelihood(final String ppiName)
    {
        String sql = "SELECT lkp.id, lkh.code , lkh.name, lkp.enabled "
                + " FROM ppi_poverty_line pl "
                + " JOIN ppi_likelihoods_ppi lkp on lkp.id = pl.likelihood_ppi_id "
                + " JOIN ppi_likelihoods lkh on lkp.likelihood_id = lkh.id "
                + " WHERE lkp.ppi_name = ? "
                + " GROUP BY pl.likelihood_ppi_id ";

        return  this.jdbcTemplate.queryForRowSet(sql, new Object[] {ppiName});

    }

    @Override
    public LikelihoodData retrieve(final Long likelihoodId)
    {
        final SqlRowSet likelihood =this._getLikelihood(likelihoodId);

        likelihood.first();

        return  new LikelihoodData(
                    likelihood.getLong("id"),
                    likelihood.getString("name"),
                    likelihood.getString("code"),
                    likelihood.getLong("enabled")

            );

    }

    private SqlRowSet _getLikelihood(final Long likelihoodId)
    {
        String sql = "SELECT lkp.id, lkh.code , lkh.name, lkp.enabled "
                + " FROM ppi_likelihoods lkh "
                + " JOIN ppi_likelihoods_ppi lkp on lkp.likelihood_id = lkh.id "
                + " WHERE lkp.id = ? ";

        return  this.jdbcTemplate.queryForRowSet(sql, new Object[] {likelihoodId});

    }


}
